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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.queryparser.classic.QueryParser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;

// From chapter 4
public class SynonymAnalyzerTest {
  Directory directory;
  DirectoryReader directoryReader;
  private IndexSearcher searcher;

  private static SynonymAnalyzer synonymAnalyzer = new SynonymAnalyzer(new TestSynonymEngine());

  @Before
  public void setUp() throws Exception {
    directory = new RAMDirectory();
    IndexWriterConfig config = new IndexWriterConfig(synonymAnalyzer);
    IndexWriter writer = new IndexWriter(directory, config);

    Document doc = new Document();
    doc.add(new TextField("content",
                      "The quick brown fox jumps over the lazy dog",
                      Field.Store.YES));  //#2
    writer.addDocument(doc);
                                  
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
  public void testJumps() throws Exception {
    TokenStream stream = synonymAnalyzer.tokenStream("contents",                   // #A
                                  new StringReader("jumps"));   // #A
    CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
    PositionIncrementAttribute posIncr = stream.addAttribute(PositionIncrementAttribute.class);

    int i = 0;
    String[] expected = new String[]{"jumps",              // #B
                                     "hops",               // #B
                                     "leaps"};             // #B

    stream.reset();
    while(stream.incrementToken()) {
      assertEquals(expected[i], term.toString());

      int expectedPos;      // #C
      if (i == 0) {         // #C
        expectedPos = 1;    // #C
      } else {              // #C
        expectedPos = 0;    // #C
      }                     // #C
      assertEquals(expectedPos,                      // #C
                   posIncr.getPositionIncrement());  // #C
      i++;
    }
    assertEquals(3, i);
    stream.end();
    stream.close();
  }

  /*
    #A Analyze with SynonymAnalyzer
    #B Check for correct synonyms
    #C Verify synonyms positions
  */

  @Test
  public void testSearchByAPI() throws Exception {

    TermQuery tq = new TermQuery(new Term("content", "hops"));  //#1
    assertEquals(1, TestUtil.hitCount(searcher, tq));

    PhraseQuery.Builder builder = new PhraseQuery.Builder();

    builder.add(new Term("content", "fox"));    //#2
    builder.add(new Term("content", "hops"));   //#2
    PhraseQuery pq = builder.build();

    assertEquals(1, TestUtil.hitCount(searcher, pq));
  }

  /*
    #1 Search for "hops"
    #2 Search for "fox hops"
  */

  @Test
  public void testWithQueryParser() throws Exception {
    Query query = new QueryParser("content", synonymAnalyzer).parse("\"fox jumps\"");  // 1
    assertEquals(1, TestUtil.hitCount(searcher, query));                   // 1
    System.out.println("With SynonymAnalyzer, \"fox jumps\" parses to " +
                                         query.toString("content"));

    query = new QueryParser("content",                                      // 2
                            new StandardAnalyzer()).parse("\"fox jumps\""); // B
    assertEquals(1, TestUtil.hitCount(searcher, query));                   // 2
    System.out.println("With StandardAnalyzer, \"fox jumps\" parses to " +
                                         query.toString("content"));
  }

  /*
    #1 SynonymAnalyzer finds the document
    #2 StandardAnalyzer also finds document
  */
}
