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

import net.mvnindex.demo.lucene.common.TestUtil;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

// From chapter 5
public class FilterTest {
  private Directory directory;
  private DirectoryReader directoryReader;
  private IndexSearcher searcher;
  private Query allBooks;

  @Before
  public void setUp() throws Exception {    // #1
    allBooks = new MatchAllDocsQuery();
    //dir = TestUtil.getBookIndexDirectory();
    directory = FSDirectory.open(new File("../index").toPath());
    directoryReader = DirectoryReader.open(directory);
    searcher = new IndexSearcher(directoryReader);
  }

  @After
  public void tearDown() throws Exception {
    directoryReader.close();
    directory.close();
  }

  @Test
  public void testTermRangeFilter() throws Exception {
    //Filter filter = new TermRangeFilter("title2", "d", "j", true, true);
    TermRangeQuery termRangeQuery = TermRangeQuery.newStringRange(
            "title2", "d", "j", true, true);
    ConstantScoreQuery constantScoreQuery = new ConstantScoreQuery(termRangeQuery);
    long hitcount = TestUtil.hitCount(searcher, allBooks, constantScoreQuery);

    assertEquals(3, hitcount);
    //System.out.println("hit count: " + hitcount);
  }

  /*
    #1 setUp() establishes baseline book count
  */

  @Test
  public void testNumericDateFilter() throws Exception {
    // pub date of Lucene in Action, Second Edition and
    // JUnit in Action, Second Edition is May 2010

    Query query = IntPoint.newRangeQuery("pubmonth", 201001, 201006);
    ConstantScoreQuery constantScoreQuery = new ConstantScoreQuery(query);
    assertEquals(2, TestUtil.hitCount(searcher, allBooks, constantScoreQuery));
  }

  @Test
  public void testDocValuesRangeFilter() throws Exception {
//    Filter filter = FieldCacheRangeFilter.newStringRange("title2", "d", "j", true, true);

    Query query = SortedDocValuesField.newSlowRangeQuery("title2",
            new BytesRef("d"),
            new BytesRef("j"),
            true,
            true);
    ConstantScoreQuery filter = new ConstantScoreQuery(query);

    assertEquals(3, TestUtil.hitCount(searcher, allBooks, filter));

    Query pubmonth = IntPoint.newRangeQuery("pubmonth", 201001, 201006);
    Query dvQuery = NumericDocValuesField.newSlowRangeQuery("pubmonth", 201001, 201006);
    Query iodquery = new IndexOrDocValuesQuery(pubmonth, dvQuery);
    filter = new ConstantScoreQuery(iodquery);

    assertEquals(2, TestUtil.hitCount(searcher, allBooks, filter));
  }

  @Test
  public void testTemFilter() throws Exception {
    TermQuery categoryQuery =
       new TermQuery(new Term("category", "/technology/computers/programming"));

    ConstantScoreQuery filter = new ConstantScoreQuery(categoryQuery);

    assertEquals("expected 5 hits",
                 5,
                 TestUtil.hitCount(searcher, allBooks, filter));
  }

  @Test
  public void testDocValuesTermsFilter() throws Exception {

    Query dvQuery = SortedDocValuesField.newSlowExactQuery("category",
            new BytesRef("/technology/computers/programming"));

    ConstantScoreQuery filter = new ConstantScoreQuery(dvQuery);

    assertEquals("expected 5 hits",
            5, TestUtil.hitCount(searcher, allBooks, filter));
  }

  @Test
  public void testConstantScoreQueryFilter() throws Exception {
    TermQuery categoryQuery =
            new TermQuery(new Term("category", "/technology/computers/programming"));

    ConstantScoreQuery filter = new ConstantScoreQuery(categoryQuery);

    TopDocs topDocs = searcher.search(filter,10);

    System.out.println("hit count: " + topDocs.totalHits.value);
    System.out.println("topDocs.scoreDocs[] length: " + topDocs.scoreDocs.length);

    Document doc;
    for (ScoreDoc sd : topDocs.scoreDocs){
      doc =	searcher.doc(sd.doc);
      String title2 = doc.get("title2");
      System.out.println("hit doc title2: " + title2);
    }

    assertEquals("expected 5 hits",
            5,
            topDocs.totalHits.value);
  }

  @Test
  public void testConstantScoreQueryFilterAndQuery() throws Exception {
    TermQuery categoryQuery =
            new TermQuery(new Term("category", "/technology/computers/programming"));

    ConstantScoreQuery filter = new ConstantScoreQuery(categoryQuery);

    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    builder.add(allBooks, BooleanClause.Occur.MUST);
    builder.add(filter, BooleanClause.Occur.MUST);
    BooleanQuery booleanQuery = builder.build();

    TopDocs topDocs = searcher.search(booleanQuery,10);

    System.out.println("hit count: " + topDocs.totalHits.value);
    System.out.println("topDocs.scoreDocs[] length: " + topDocs.scoreDocs.length);

    Document doc;
    for (ScoreDoc sd : topDocs.scoreDocs){
      doc =	searcher.doc(sd.doc);
      String title2 = doc.get("title2");
      System.out.println("hit doc title2: " + title2);
    }

    assertEquals("expected 5 hits",
            5,
            topDocs.totalHits.value);
  }

  @Test
  public void testSpanQueryFilter() throws Exception {
    SpanQuery subjectQuery = new SpanTermQuery(new Term("subject", "lucene"));
    ConstantScoreQuery filter = new ConstantScoreQuery(subjectQuery);

    assertEquals("only lucene in action",
                 1,
                 TestUtil.hitCount(searcher, allBooks, filter));
  }

  @Test
  public void testFilterAlternative() throws Exception {
    TermQuery categoryQuery =
       new TermQuery(new Term("category", "/philosophy/eastern"));

    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    builder.add(allBooks, BooleanClause.Occur.MUST);
    builder.add(categoryQuery, BooleanClause.Occur.MUST);
    BooleanQuery constrainedQuery = builder.build();

    assertEquals("only tao te ching",
                 1,
                 TestUtil.hitCount(searcher, constrainedQuery));
  }

  @Test
  public void testPrefixQueryFilter() throws Exception {
    PrefixQuery prefixFilter = new PrefixQuery(
                            new Term("category",
                                     "/technology/computers"));
    assertEquals("only /technology/computers/* books",
                 8,
                 TestUtil.hitCount(searcher,
                                   allBooks,
                                   prefixFilter));
  }
/*
  public void testCachingWrapper() throws Exception {
    Filter filter = new TermRangeFilter("title2",
                                        "d", "j",
                                        true, true);

    CachingWrapperFilter cachingFilter;
    cachingFilter = new CachingWrapperFilter(filter);
    assertEquals(3,
                 TestUtil.hitCount(searcher,
                                   allBooks,
                                   cachingFilter));
  }*/
}
