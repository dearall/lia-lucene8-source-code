package net.mvnindex.demo.lucene.extsearch.queryparser;

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

import junit.framework.TestCase;
import net.mvnindex.demo.lucene.common.TestUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertTrue;

// From chapter 6
public class NumericQueryParserTest {
  private Analyzer analyzer;
  private IndexSearcher searcher;
  private Directory directory;
  private DirectoryReader reader;

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

  static class PointRangeQueryParser extends QueryParser {
    public PointRangeQueryParser(String field, Analyzer a) {
      super(field, a);
    }

    protected Query getRangeQuery(String field, String part1, String part2,
                               boolean startInclusive, boolean endInclusive)
            throws ParseException {
      Query q = super.getRangeQuery(field, part1, part2, startInclusive, endInclusive);  // ①
      TermRangeQuery query = (TermRangeQuery)q;

      if ("price".equals(field)) {
        return DoublePoint.newRangeQuery(                // ②
                      "price",                           // ②
                      Double.parseDouble(query.getLowerTerm().utf8ToString()),
                      Double.parseDouble(query.getUpperTerm().utf8ToString()));
      } else {
        return query;                                    // ③
      }
    }
  }

  /*
    ① 通过父类的解析出默认的 TermRangeQuery 实例
    ② 创建匹配的 PointRangeQuery 查询并返回
    ③ 返回默认的 TermRangeQuery
  */

  @Test
  public void testNumericRangeQuery() throws Exception {
    String expression = "price:[10 TO 20]";

    QueryParser parser = new PointRangeQueryParser("subject", analyzer);

    Query query = parser.parse(expression);
    System.out.println(expression + " parsed to " + query);
  }

  public static class PointDateRangeQueryParser extends QueryParser {
    public PointDateRangeQueryParser(String field, Analyzer a) {
      super(field, a);
    }

    protected Query getRangeQuery(String field, String part1, String part2,
                                  boolean startInclusive, boolean endInclusive)
            throws ParseException {
      Query q = super.getRangeQuery(field, part1, part2, startInclusive, endInclusive);
      TermRangeQuery query = (TermRangeQuery)q;

      if ("pubmonth".equals(field)) {
        return IntPoint.newRangeQuery(
                    "pubmonth",
                    Integer.parseInt(query.getLowerTerm().utf8ToString()),
                    Integer.parseInt(query.getUpperTerm().utf8ToString()));
      } else {
        return query;
      }
    }
  }

  @Test
  public void testDefaultDateRangeQuery() throws Exception {
    QueryParser parser = new QueryParser("subject", analyzer);
    Query query = parser.parse("pubmonth:[1/1/04 TO 12/31/04]");
    System.out.println("default date parsing: " + query);
  }

  @Test
  public void testDateRangeQuery() throws Exception {
    String expression = "pubmonth:[2010/01/01 TO 2010/06/01]";

    QueryParser parser = new PointDateRangeQueryParser("subject", analyzer);
    
    parser.setDateResolution("pubmonth", DateTools.Resolution.MONTH);    // ①
    parser.setLocale(Locale.CHINA);

    Query query = parser.parse(expression);
    System.out.println(expression + " parsed to " + query);

    TopDocs matches = searcher.search(query, 10);
    assertTrue("expecting at least one result !", matches.totalHits.value > 0);
  }
  /*
    ① 告知 QueryParser 所用的日期解析粒度
  */
}
