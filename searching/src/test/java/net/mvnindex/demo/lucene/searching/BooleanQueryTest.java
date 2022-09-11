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
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

// From chapter 3
public class BooleanQueryTest {
  @Test
  public void testAnd() throws Exception {
    TermQuery searchingBooks = new TermQuery(new Term("subject","search"));                 //①

    Query books2010 = IntPoint.newRangeQuery("pubmonth", 201001,  201012);  //②

    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    builder.add(searchingBooks, BooleanClause.Occur.MUST);  //③
    builder.add(books2010, BooleanClause.Occur.MUST);       //③
    BooleanQuery searchingBooks2010 = builder.build();      //④

    Directory directory = TestUtil.getBookIndexDirectory();
    DirectoryReader reader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(reader);
    TopDocs matches = searcher.search(searchingBooks2010, 10);

    assertTrue(TestUtil.hitsIncludeTitle(searcher, matches,
                                 "Lucene in Action, Second Edition"));

    for(int i=0; i<matches.totalHits.value; i++) {
      System.out.print("match " + i + "  [subject]: " + searcher.doc(matches.scoreDocs[i].doc).get("subject"));
      System.out.println(" [pubmonth]: " + searcher.doc(matches.scoreDocs[i].doc).get("pubmonth"));
    }

    reader.close();
    directory.close();
  }

/*
① 匹配 "subject" 域包含 "search"
② 匹配出版日期 "pubmonth" 域在 [201001, 201012] 区间
③ 使用 MUST 操作符联合两个子查询
④ 构建 BooleanQuery 实例对象
*/

  @Test
  public void testOr() throws Exception {
    TermQuery methodologyBooks = new TermQuery(                       // ①
               new Term("category",                                // ①
                 "/technology/computers/programming/methodology"));   // ①

    TermQuery easternPhilosophyBooks = new TermQuery(                 // ②
        new Term("category",                                       // ②
            "/philosophy/eastern"));                                  // ②

    BooleanQuery.Builder builder = new BooleanQuery.Builder();        // ③
    builder.add(methodologyBooks, BooleanClause.Occur.SHOULD);        // ③
    builder.add(easternPhilosophyBooks, BooleanClause.Occur.SHOULD);  // ③
    BooleanQuery enlightenmentBooks = builder.build();                // ④

    Directory directory = TestUtil.getBookIndexDirectory();
    DirectoryReader reader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(reader);

    TopDocs matches = searcher.search(enlightenmentBooks, 10);
    System.out.println("or = " + enlightenmentBooks);

    assertTrue(TestUtil.hitsIncludeTitle(searcher, matches,
                                         "Extreme Programming Explained"));
    assertTrue(TestUtil.hitsIncludeTitle(searcher, matches,
                                         "Tao Te Ching \u9053\u5FB7\u7D93"));

    for(int i=0; i<matches.totalHits.value; i++) {
      System.out.print("match " + i + "  [title]: " + searcher.doc(matches.scoreDocs[i].doc).get("title"));
      System.out.println(" [category]: " + searcher.doc(matches.scoreDocs[i].doc).get("category"));
    }
    reader.close();
    directory.close();
  }

  /*
  ① 匹配第一个 "category" 的 TermQuery
  ② 匹配第二个 "category" 的 TermQuery
  ③ 使用 SHOULD 操作符联合两个 TermQuery
  ④ 构建 BooleanQuery 对象
   */
}
