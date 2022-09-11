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
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

// From chapter 3
public class BasicSearchingTest {

  @Test
  public void testTerm() throws Exception {
    Directory dir = TestUtil.getBookIndexDirectory(); //①
    DirectoryReader reader = DirectoryReader.open(dir);
    IndexSearcher searcher = new IndexSearcher(reader);//②

    Term t = new Term("subject", "ant");
    Query query = new TermQuery(t);
    TopDocs docs = searcher.search(query, 10);
    assertEquals("Ant in Action",              //③
                 1, docs.totalHits.value);

    t = new Term("subject", "junit");
    docs = searcher.search(new TermQuery(t), 10);
    assertEquals("Ant in Action, " +           //④
                 "JUnit in Action, Second Edition",
                 2, docs.totalHits.value);

    reader.close(); //⑤
    dir.close();    //⑥
  }

  /*
    ① 使用 TestUtil 获取索引库目录
    ② 创建 IndexSearcher 实例
    ③ 确认搜索结果有一个文档匹配词项查询："subject":"ant"
    ④ 确认搜索结果有两个文档匹配词项查询："subject":"junit"
    ⑤ 关闭 reader
    ⑥ 关闭 dir
  */

  @Test
  public void testKeyword() throws Exception {
    Directory directory = TestUtil.getBookIndexDirectory();
    DirectoryReader reader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(reader);

    Term t = new Term("isbn", "9781935182023");
    Query query = new TermQuery(t);
    TopDocs docs = searcher.search(query, 10);
    assertEquals("JUnit in Action, Second Edition", 1, docs.totalHits.value);

    reader.close();
    directory.close();
  }

  @Test
  public void testQueryParser() throws Exception {
    Directory dir = TestUtil.getBookIndexDirectory();
    DirectoryReader reader = DirectoryReader.open(dir);
    IndexSearcher searcher = new IndexSearcher(reader);

    QueryParser parser = new QueryParser("contents", new SimpleAnalyzer());  //①

    Query query = parser.parse("+JUNIT +ANT -MOCK");                     //②
    TopDocs docs = searcher.search(query, 10);
    assertEquals(1, docs.totalHits.value);

    Document d = searcher.doc(docs.scoreDocs[0].doc);
    assertEquals("Ant in Action", d.get("title"));

    query = parser.parse("mock OR junit");                                //②
    docs = searcher.search(query, 10);
    assertEquals("Ant in Action, " + 
                 "JUnit in Action, Second Edition",
                 2, docs.totalHits.value);

    reader.close();
    dir.close();
  }
  /*
  ① 创建 QueryParser
  ② 解析用户文本
  */
}
