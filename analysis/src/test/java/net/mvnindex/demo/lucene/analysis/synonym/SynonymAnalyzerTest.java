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
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.queryparser.classic.QueryParser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

// From chapter 4
public class SynonymAnalyzerTest {
  private Directory directory;
  private DirectoryReader reader;
  private IndexSearcher searcher;

  private static SynonymAnalyzer synonymAnalyzer = new SynonymAnalyzer(new TestSynonymEngine());

  @Before
  public void setUp() throws IOException {
    directory = new ByteBuffersDirectory();
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
  }

  @Test
  public void testJumps() throws Exception {
    TokenStream stream = synonymAnalyzer.tokenStream("contents", new StringReader("jumps")); // ???

    CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
    PositionIncrementAttribute posIncr = stream.addAttribute(PositionIncrementAttribute.class);

    int i = 0;
    String[] expected = new String[]{"jumps",              // ???
                                     "hops",               // ???
                                     "leaps"};             // ???

    stream.reset();
    while(stream.incrementToken()) {
      assertEquals(expected[i], term.toString());

      int expectedPos;      // ???
      if (i == 0) {         // ???
        expectedPos = 1;    // ???
      } else {              // ???
        expectedPos = 0;    // ???
      }                     // ???
      assertEquals(expectedPos,                      // ???
                   posIncr.getPositionIncrement());  // ???
      i++;
    }
    assertEquals(3, i);
    stream.end();
    stream.close();
  }

  /*
    ??? ?????? SynonymAnalyzer ????????????
    ??? ????????????????????????
    ??? ????????????????????? position
  */

  @Test
  public void testSearchByAPI() throws Exception {

    TermQuery tq = new TermQuery(new Term("content", "hops"));  // ???
    assertEquals(1, TestUtil.hitCount(searcher, tq));

    TopDocs docs = searcher.search(tq, 1);
    System.out.println("content: "+ searcher.doc(docs.scoreDocs[0].doc).get("content"));

    PhraseQuery.Builder builder = new PhraseQuery.Builder();

    builder.add(new Term("content", "fox"));    // ???
    builder.add(new Term("content", "hops"));   // ???
    PhraseQuery pq = builder.build();

    assertEquals(1, TestUtil.hitCount(searcher, pq));

    docs = searcher.search(pq, 1);
    System.out.println("content: "+ searcher.doc(docs.scoreDocs[0].doc).get("content"));
  }

  /*
    ??? ?????? "hops"
    ??? ?????? "fox hops"
  */

  @Test
  public void testWithQueryParser() throws Exception {
    Query query = new QueryParser("content", synonymAnalyzer).parse("\"fox jumps\"");  // ???
    assertEquals(1, TestUtil.hitCount(searcher, query));                   // ???
    System.out.println("With SynonymAnalyzer, \"fox jumps\" parses to " +
                                         query.toString("content"));

    query = new QueryParser("content",                                          // ???
                            new StandardAnalyzer()).parse("\"fox jumps\"");
    assertEquals(1, TestUtil.hitCount(searcher, query));                  // ???
    System.out.println("With StandardAnalyzer, \"fox jumps\" parses to " +
                                         query.toString("content"));
  }

  /*
    ??? SynonymAnalyzer ?????????????????????
    ??? StandardAnalyzer ????????????????????????
  */
}
