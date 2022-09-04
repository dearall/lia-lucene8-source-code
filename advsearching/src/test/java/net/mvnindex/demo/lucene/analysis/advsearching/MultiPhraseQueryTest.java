package net.mvnindex.demo.lucene.analysis.advsearching;

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

import net.mvnindex.demo.lucene.analysis.synonym.SynonymAnalyzer;
import net.mvnindex.demo.lucene.analysis.synonym.SynonymEngine;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// From chapter 5
public class MultiPhraseQueryTest {
  private Directory directory;
  private DirectoryReader directoryReader;
  private IndexSearcher searcher;

  @Before
  public void setUp() throws Exception {
    directory = new RAMDirectory();
    IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
    IndexWriter writer = new IndexWriter(directory, config);

    Document doc1 = new Document();
    doc1.add(new TextField("field",
              "the quick brown fox jumped over the lazy dog",
              Field.Store.YES));
    writer.addDocument(doc1);

    Document doc2 = new Document();
    doc2.add(new TextField("field",
              "the fast fox hopped over the hound",
              Field.Store.YES));
    writer.addDocument(doc2);
    writer.close();

    directoryReader = DirectoryReader.open(directory);
    searcher = new IndexSearcher(directoryReader);
  }

  @After
  public void tearDown() throws Exception {
    directoryReader.close();
    directory.close();
  }

  @Test
  public void testBasic() throws Exception {
    MultiPhraseQuery.Builder builder = new MultiPhraseQuery.Builder();
    builder.add(new Term[] {                             // #A
        new Term("field", "quick"),              // #A
        new Term("field", "fast")                // #A
    });
    builder.add(new Term("field", "fox"));         // #B

    MultiPhraseQuery query = builder.build();

    System.out.println(query);

    TopDocs hits = searcher.search(query, 10);
    assertEquals("fast fox match", 1, hits.totalHits.value);

    builder.setSlop(1);
    query = builder.build();

    System.out.println(query);

    hits = searcher.search(query, 10);
    assertEquals("both match", 2, hits.totalHits.value);
  }

  /*
#A Any of these terms may be in first position to match
#B Only one in second position
  */

  @Test
  public void testAgainstOR() throws Exception {
    PhraseQuery.Builder builder = new PhraseQuery.Builder();
    builder.setSlop(1);

    builder.add(new Term("field", "quick"));
    builder.add(new Term("field", "fox"));
    PhraseQuery quickFox = builder.build();

    builder = new PhraseQuery.Builder();
    builder.add(new Term("field", "fast"));
    builder.add(new Term("field", "fox"));
    PhraseQuery fastFox = builder.build();

    BooleanQuery.Builder booleanBuilder = new BooleanQuery.Builder();
    booleanBuilder.add(quickFox, BooleanClause.Occur.SHOULD);
    booleanBuilder.add(fastFox, BooleanClause.Occur.SHOULD);
    BooleanQuery query = booleanBuilder.build();

    TopDocs hits = searcher.search(query, 10);
    assertEquals(2, hits.totalHits.value);
  }

  @Test
  public void testQueryParser() throws Exception {
    SynonymEngine engine = new SynonymEngine() {
        public String[] getSynonyms(String s) {
          if (s.equals("quick"))
            return new String[] {"fast"};
          else
            return null;
        }
      };

    Query q = new QueryParser("field", new SynonymAnalyzer(engine))
            .parse("\"quick fox\"");

    assertEquals("analyzed",
        "field:\"(quick fast) fox\"", q.toString());
    assertTrue("parsed as MultiPhraseQuery", q instanceof MultiPhraseQuery);
  }

  private void debug(TopDocs hits) throws IOException {
    for (ScoreDoc sd : hits.scoreDocs) {
      Document doc = searcher.doc(sd.doc);
      System.out.println(sd.score + ": " + doc.get("field"));
    }

  }
}
