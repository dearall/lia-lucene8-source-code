package net.mvnindex.demo.lucene.analysis.synonym;

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
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.queryparser.classic.QueryParser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

// From chapter 4
public class SynonymAnalyzerTest {
  private final String indexPath = "indexes";
  private Directory directory;
  private DirectoryReader reader;
  private IndexSearcher searcher;

  private static SynonymAnalyzer synonymAnalyzer = new SynonymAnalyzer(new TestSynonymEngine());

  @Before
  public void setUp() throws IOException {
    directory = FSDirectory.open(Paths.get(indexPath));
    IndexWriterConfig config = new IndexWriterConfig(synonymAnalyzer);
    IndexWriter writer = new IndexWriter(directory, config);

    Document doc = new Document();
    doc.add(new TextField("content",
            "The quick brown fox jumps over the lazy dog",
            Field.Store.YES));
    writer.addDocument(doc);

    writer.close();

    reader = DirectoryReader.open(directory);
    searcher = new IndexSearcher(reader);
  }

  @After
  public void tearDown() throws IOException {
    reader.close();
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


  @Test
  public void testJumps() throws Exception {
    TokenStream stream = synonymAnalyzer.tokenStream("contents", new StringReader("jumps")); // ①

    CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
    PositionIncrementAttribute posIncr = stream.addAttribute(PositionIncrementAttribute.class);

    int i = 0;
    String[] expected = new String[]{"jumps",              // ②
                                     "hops",               // ②
                                     "leaps"};             // ②

    stream.reset();
    while(stream.incrementToken()) {
      assertEquals(expected[i], term.toString());

      int expectedPos;      // ③
      if (i == 0) {         // ③
        expectedPos = 1;    // ③
      } else {              // ③
        expectedPos = 0;    // ③
      }                     // ③
      assertEquals(expectedPos,                      // ③
                   posIncr.getPositionIncrement());  // ③
      i++;
    }
    assertEquals(3, i);
    stream.end();
    stream.close();
  }

  /*
    ① 使用 SynonymAnalyzer 进行分析
    ② 检查正确的同义词
    ③ 验证同义词位置 position
  */

  @Test
  public void testSearchByAPI() throws Exception {

    TermQuery tq = new TermQuery(new Term("content", "hops"));  // ①
    assertEquals(1, TestUtil.hitCount(searcher, tq));

    TopDocs docs = searcher.search(tq, 1);
    System.out.println("content: "+ searcher.doc(docs.scoreDocs[0].doc).get("content"));

    PhraseQuery.Builder builder = new PhraseQuery.Builder();

    builder.add(new Term("content", "fox"));    // ②
    builder.add(new Term("content", "hops"));   // ②
    PhraseQuery pq = builder.build();

    assertEquals(1, TestUtil.hitCount(searcher, pq));

    docs = searcher.search(pq, 1);
    System.out.println("content: "+ searcher.doc(docs.scoreDocs[0].doc).get("content"));
  }

  /*
    ① 搜索 "hops"
    ② 搜索 "fox hops"
  */

  @Test
  public void testWithQueryParser() throws Exception {
    Query query = new QueryParser("content", synonymAnalyzer).parse("\"fox jumps\"");  // ①
    assertEquals(1, TestUtil.hitCount(searcher, query));                   // ①
    System.out.println("With SynonymAnalyzer, \"fox jumps\" parses to " +
                                         query.toString("content"));

    query = new QueryParser("content",                                          // ②
                            new StandardAnalyzer()).parse("\"fox jumps\"");
    assertEquals(1, TestUtil.hitCount(searcher, query));                  // ②
    System.out.println("With StandardAnalyzer, \"fox jumps\" parses to " +
                                         query.toString("content"));
  }

  /*
    ① SynonymAnalyzer 搜索到索引文档
    ② StandardAnalyzer 也搜索到索引文档
  */
}
