package net.mvnindex.demo.lucene;

/**
 * Copyright Manning Publications Co.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific lan      
*/

import net.mvnindex.demo.lucene.common.TestUtil;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.*;

// From chapter 2
public class IndexingTest {
  protected String[] ids = {"1", "2"};
  protected String[] unindexed = {"Netherlands", "Italy"};
  protected String[] unstored = {"Amsterdam has lots of bridges",
                                 "Venice has lots of canals"};
  protected String[] text = {"Amsterdam", "Venice"};

  private final String indexPath = "indexes";
  private Directory directory;
  private IndexWriter indexWriter;

  static final FieldType idType = new FieldType();        //④
  static final FieldType countryType = new FieldType();
  static final FieldType contentsType = new FieldType();
  static final FieldType cityType = new FieldType();

  static {
    idType.setOmitNorms(true);
    idType.setIndexOptions(IndexOptions.DOCS);
    idType.setStored(true);
    idType.setTokenized(false);
    idType.freeze();

    countryType.setStored(true);
    countryType.setIndexOptions(IndexOptions.NONE);
    countryType.freeze();

    contentsType.setTokenized(true);
    contentsType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
    contentsType.setStored(false);
    contentsType.freeze();

    cityType.setStored(true);
    cityType.setTokenized(false);
    cityType.setIndexOptions(IndexOptions.DOCS);
    cityType.freeze();
  }

  @Before
  public void setUp() throws Exception {        //①
    directory = FSDirectory.open(Paths.get(indexPath));
    IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
    config.setMergeScheduler(new SerialMergeScheduler()); //②
    indexWriter = new IndexWriter(directory, config); //③

    for (int i = 0; i < ids.length; i++) {
      Document doc = new Document();                 //④

      doc.add(new Field("id", ids[i], idType)); //⑤
      doc.add(new Field("country", unindexed[i],countryType)); //⑤
      doc.add(new Field("contents", unstored[i], contentsType)); //⑤
      doc.add(new Field("city", text[i], cityType)); //⑤

      indexWriter.addDocument(doc); //⑥
    }
    indexWriter.commit();
  }

  @After
  public void tearDown() throws IOException {
    indexWriter.close();
    directory.close();
    deleteDir(new File(indexPath));
  }

  public static void deleteDir(File dir) {
    if (dir.isDirectory()) {
      String[] children = dir.list();
      for (int i = 0; i < children.length; i++) {
        new File(dir, children[i]).delete();
      }
    }
    dir.delete();
  }

  protected long getHitCount(String fieldName, String searchString) throws IOException { // ⑧
    DirectoryReader reader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(reader);

    Term t = new Term(fieldName, searchString);
    Query query = new TermQuery(t);
    long hitCount = TestUtil.hitCount(searcher, query);
    reader.close();

    return hitCount;
  }

  @Test
  public void testIndexWriter() throws IOException {  // ⑨
    assertEquals(ids.length, indexWriter.getDocStats().numDocs);
  }

  @Test
  public void testIndexReader() throws IOException {  // ⑩
    IndexReader reader = DirectoryReader.open(directory);
    assertEquals(ids.length, reader.maxDoc());
    assertEquals(ids.length, reader.numDocs());
    reader.close();
  }


  @Test
  public void testDeleteBeforeFlush() throws IOException {
    assertEquals(2, indexWriter.getDocStats().numDocs);  //①

    indexWriter.deleteDocuments(new Term("id", "1"));     //②
    assertTrue(indexWriter.hasDeletions());                       //③

    assertEquals(2, indexWriter.getDocStats().maxDoc);    //④
    assertEquals(2, indexWriter.getDocStats().numDocs);   //⑤

    DirectoryReader reader = DirectoryReader.open(directory);     //⑥
    System.out.println("total docs: " + reader.maxDoc());
    System.out.println("live docs: " + reader.numDocs());
    System.out.println("deleted docs: " + reader.numDeletedDocs());
    reader.close();

    System.out.println("--------------");

    reader = DirectoryReader.open(indexWriter);                   //⑦
    System.out.println("total docs: " + reader.maxDoc());
    System.out.println("live docs: " + reader.numDocs());
    System.out.println("deleted docs: " + reader.numDeletedDocs());
    reader.close();
  }

  @Test
  public void testDeleteAfterFlush() throws IOException {
    assertEquals(2, indexWriter.getDocStats().numDocs); //①

    indexWriter.deleteDocuments(new Term("id", "1"));   //②
    indexWriter.flush();                                        //③
    assertFalse(indexWriter.hasDeletions());                    //④

    assertEquals(1, indexWriter.getDocStats().maxDoc);  //⑤
    assertEquals(1, indexWriter.getDocStats().numDocs); //⑥

    DirectoryReader reader = DirectoryReader.open(directory);   //⑦
    System.out.println("total docs: " + reader.maxDoc());
    System.out.println("live docs: " + reader.numDocs());
    System.out.println("deleted docs: " + reader.numDeletedDocs());
    reader.close();

    System.out.println("--------------");

    reader = DirectoryReader.open(indexWriter);                  //⑧
    System.out.println("total docs: " + reader.maxDoc());
    System.out.println("live docs: " + reader.numDocs());
    System.out.println("deleted docs: " + reader.numDeletedDocs());
    reader.close();
  }


  @Test
  public void testUpdate() throws IOException {
    assertEquals(1, getHitCount("city", "Amsterdam"));

    Document doc = new Document(); // ①
    doc.add(new Field("id", "1", idType));
    doc.add(new Field("country", "Netherlands", countryType));
    doc.add(new Field("contents", "Den Haag has a lot of museums", contentsType));
    doc.add(new Field("city", "Den Haag", cityType));

    indexWriter.updateDocument(new Term("id", "1"), doc); // ②

    indexWriter.commit(); //③

    assertEquals(0, getHitCount("city", "Amsterdam")); //④
    assertEquals(1, getHitCount("city", "Den Haag"));  //⑤
  }


  @Test
  public void testMaxFieldLength() throws IOException {
    assertEquals(1, getHitCount("contents", "bridges"));  //1

    Document doc = new Document();
    doc.add(new TextField("contents", "these bridges can't be found", Field.Store.NO));
    indexWriter.addDocument(doc);
    indexWriter.commit();

    assertEquals(2, getHitCount("contents", "bridges"));   //4
  }
}
