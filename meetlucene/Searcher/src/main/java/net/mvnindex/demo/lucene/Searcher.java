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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

// From chapter 1

/**
 * This code was originally written for
 * Erik's Lucene intro java.net article
 */
public class Searcher {

  public static void main(String[] args)
          throws IllegalArgumentException, ParseException, IOException {
    if(args.length == 1) {
      if (args[0].equals("-h") || args[0].equals("--help")) {
        System.out.println("Usage: java -jar target/Searcher-1.0-SNAPSHOT-shaded.jar <query> <index dir>");
        return;
      }
    }

    String q = "patent";
    String indexDir = "../indexes/MeetLucene";

    if(args.length >= 1) //①
      q = args[0];
    if(args.length >= 2) //②
      indexDir = args[1];

    search(indexDir, q);
  }

  public static void search(String indexDir, String q) throws IOException, ParseException {

    Directory dir = FSDirectory.open(Paths.get(indexDir));  //③
    DirectoryReader reader = DirectoryReader.open(dir);
    IndexSearcher is = new IndexSearcher(reader);           //④

    QueryParser parser = new QueryParser("contents", new StandardAnalyzer());
    Query query = parser.parse(q);                  //⑤

    long start = System.currentTimeMillis();
    TopDocs hits = is.search(query, 10);          //⑥
    long end = System.currentTimeMillis();

    System.err.println("Found " + hits.totalHits +   //⑦
      " document(s) (in " + (end - start) +
      " milliseconds) that matched query '" +
      q + "':");

    for(ScoreDoc scoreDoc : hits.scoreDocs) {
      Document doc = is.doc(scoreDoc.doc);           //⑧
      System.out.println(doc.get("fullpath"));       //⑨
    }

    reader.close();     //⑩
    dir.close();        //⑪
  }
}

/*
① 索引目录
② 查询字符串
③ 打开索引库
④ 创建 IndexSearcher 实例
⑤ 解析查询字符串为 Query 查询实例
⑥ 执行搜索
⑦ 输出搜索结果
⑧ 获取匹配文档
⑨ 显示完整路径文件名
⑩ 关闭索引库 reader
⑪ 关闭索引库
*/
