package net.mvnindex.demo.lucene.searching;

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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

// From chapter 3
public class QueryParserTest {

  private Analyzer analyzer;
  private Directory directory;
  private DirectoryReader reader;
  private IndexSearcher searcher;

  @Before
  public void setUp() throws Exception {
    analyzer = new WhitespaceAnalyzer();
    directory = TestUtil.getBookIndexDirectory();
    reader = DirectoryReader.open(directory);
    searcher = new IndexSearcher(reader);
  }

  @After
  public void tearDown() throws Exception {
    reader.close();
    directory.close();
  }

  @Test
  public void testToString() throws Exception {
    BooleanQuery.Builder builder = new BooleanQuery.Builder();

    builder.add(new FuzzyQuery(new Term("field", "kountry")),
            BooleanClause.Occur.MUST);
    builder.add(new TermQuery(new Term("title", "western")),
            BooleanClause.Occur.SHOULD);

    BooleanQuery query = builder.build();

    System.out.println("query: " + query.toString());
    System.out.println("query: " + query.toString("field"));

    assertEquals("both kinds", "+kountry~2 title:western",
            query.toString("field"));
  }

  @Test
  public void testPrefixQuery() throws Exception {
    QueryParser parser = new QueryParser("category", new StandardAnalyzer());

    Query q = parser.parse("/Computers/technology*");
    System.out.println("q type: " + q.getClass().getTypeName());
    System.out.println(q.toString("category"));
  }

  @Test
  public void testFuzzyQuery() throws Exception {
    QueryParser parser = new QueryParser("subject", analyzer);
    Query query = parser.parse("kountry~");
    System.out.println("fuzzy: " + query);
    System.out.println("query type: " + query.getClass().getName());

    query = parser.parse("kountry~1");
    System.out.println("fuzzy 2: " + query);
    System.out.println("query type: " + query.getClass().getName());
  }

  @Test
  public void testGrouping() throws Exception {
    Query query = new QueryParser("subject", analyzer)
            .parse("(agile OR extreme) AND methodology");
    TopDocs matches = searcher.search(query, 10);

    assertTrue(TestUtil.hitsIncludeTitle(searcher, matches,
                                         "Extreme Programming Explained"));
    assertTrue(TestUtil.hitsIncludeTitle(searcher,
                                         matches,
                                         "The Pragmatic Programmer"));

    for(int i=0; i<matches.scoreDocs.length; i++) {
      System.out.println("match " + i + "  [subject]: " + searcher.doc(matches.scoreDocs[i].doc).get("subject"));
    }
  }


  @Test
  public void testTermQuery() throws Exception {
    QueryParser parser = new QueryParser("subject", analyzer);
    Query query = parser.parse("computers");
    System.out.println("term: " + query);
  }

  @Test
  public void testTermRangeQuery() throws Exception {
    Query query = new QueryParser("subject", analyzer).parse("title2:[q TO v]"); //①
    assertTrue(query instanceof TermRangeQuery);
    System.out.println("query type: " + query.getClass().getName());

    TopDocs matches = searcher.search(query, 10);
    System.out.println("matches count: "+ matches.totalHits.value);

    assertTrue(TestUtil.hitsIncludeTitle(searcher, matches, "Tapestry in Action"));

    for (ScoreDoc match : matches.scoreDocs){
      Document doc = searcher.doc(match.doc);
      System.out.println("title: " + doc.get("title"));
    }

    System.out.println("-------------");

    query = new QueryParser("subject", analyzer)
                            .parse("title2:{q TO \"tapestry in action\"}");       //②
    matches = searcher.search(query, 10);
    System.out.println("matches count: "+ matches.totalHits.value);

    assertFalse(TestUtil.hitsIncludeTitle(searcher, matches,  "Tapestry in Action"));//③

    for (ScoreDoc match : matches.scoreDocs){
      Document doc = searcher.doc(match.doc);
      System.out.println("title: " + doc.get("title"));
    }
  }
  /*
    ① 验证包含边界的范围 range
    ② 验证排除边界的范围 range
    ③ 验证排除 "Tapestry in Action"
  */

/*
  @Test
  public void testPointRangeQuery() throws ParseException{
    Query query = new QueryParser("subject", analyzer).parse("pubmonth:[200605 TO 200609]"); //①
    System.out.println("query type: " + query.getClass().getName());


  }
*/

  @Test
  public void testPhraseQuery() throws Exception {
    Query q = new QueryParser("field", new StandardAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET))
                .parse("\"This is Some Phrase*\"");

    assertEquals("analyzed",
        "\"? ? some phrase\"", q.toString("field"));

    System.out.println("q.toString(): " + q.toString());
    System.out.println("q.type: " + q.getClass().getName());


    q = new QueryParser("field", analyzer).parse("\"term\"");
    assertTrue("reduced to TermQuery", q instanceof TermQuery);

    System.out.println("q type: " + q.getClass().getName());
  }

  @Test
  public void testSlop() throws Exception {
    Query q = new QueryParser("field", analyzer)
            .parse("\"exact phrase\"");
    assertEquals("zero slop",
        "\"exact phrase\"", q.toString("field"));

    QueryParser qp = new QueryParser("field", analyzer);
    qp.setPhraseSlop(5);
    q = qp.parse("\"sloppy phrase\"");

    System.out.println("q.toString(): " + q.toString());
    assertEquals("sloppy, implicitly",
        "\"sloppy phrase\"~5", q.toString("field"));
  }

  @Test
  public void testLowercasing() throws Exception {
    Query q = new QueryParser("field", analyzer).parse("PrefixQuery*");
    assertEquals("not lowercased", "PrefixQuery*", q.toString("field"));

/*  QueryParser qp = new QueryParser("field", analyzer);
    qp.setLowercaseExpandedTerms(false);
    q = qp.parse("PrefixQuery*");
    assertEquals("not lowercased", "PrefixQuery*", q.toString("field"));*/
  }

  @Test
  public void testWildcard() {
    try {
      new QueryParser("field", analyzer).parse("*xyz");
      fail("Leading wildcard character should not be allowed");
    } catch (ParseException expected) {
      assertTrue(true);
    }
  }
/*
  public void testBoost() throws Exception {
    Query q = new QueryParser(Version.LUCENE_30,
                              "field", analyzer).parse("term^2");
    assertEquals("term^2.0", q.toString("field"));
  }

  public void testParseException() {
    try {
      new QueryParser(Version.LUCENE_30,
                      "contents", analyzer).parse("^&#");
    } catch (ParseException expected) {
      // expression is invalid, as expected
      assertTrue(true);
      return;
    }

    fail("ParseException expected, but not thrown");
  }*/
}
