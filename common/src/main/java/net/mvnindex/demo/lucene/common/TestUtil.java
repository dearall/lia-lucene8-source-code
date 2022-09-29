package net.mvnindex.demo.lucene.common;

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

import org.apache.lucene.document.Document;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class TestUtil {
  public static boolean hitsIncludeTitle(IndexSearcher searcher, TopDocs hits, String title)
    throws IOException {
    for (ScoreDoc match : hits.scoreDocs) {
      Document doc = searcher.doc(match.doc);
      if (title.equals(doc.get("title"))) {
        return true;
      }
    }
    System.out.println("title '" + title + "' not found");
    return false;
  }

  public static long hitCount(IndexSearcher searcher, Query query) throws IOException {
    TopDocs docs = searcher.search(query, 1);
    return docs.totalHits.value;
  }

  public static long hitCount(IndexSearcher searcher, Query query, Query filter)
          throws IOException {
    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    builder.add(query, BooleanClause.Occur.MUST);         //①
    builder.add(filter, BooleanClause.Occur.FILTER);      //②
    BooleanQuery booleanQuery = builder.build();

    TopDocs topDocs = searcher.search(booleanQuery,10);// ③

    System.out.println("hit count: " + topDocs.totalHits.value);
    System.out.println("topDocs.scoreDocs[] length: " + topDocs.scoreDocs.length);

    Document doc;
    for (ScoreDoc sd : topDocs.scoreDocs){
      doc =	searcher.doc(sd.doc);
      System.out.println("hit doc title: " + doc.get("title"));
      System.out.println("hit doc score: " + sd.score);
    }
    System.out.println();

    return topDocs.totalHits.value;
  }
/*
  ① 通过 BooleanClause.Occur.MUST 操作符为 BooleanQuery 添加必须的查询子句
  ② 通过 BooleanClause.Occur.FILTER 操作符为 BooleanQuery 添加过滤器
  ③ 由 IndexSearcher 执行过滤查询
  */

  public static void dumpHits(IndexSearcher searcher, TopDocs hits)
    throws IOException {
    if (hits.totalHits.value == 0) {
      System.out.println("No hits");
    }

    for (ScoreDoc match : hits.scoreDocs) {
      Document doc = searcher.doc(match.doc);
      System.out.println(match.score + ":" + doc.get("title"));
    }
  }
  
  public static Directory getBookIndexDirectory() throws IOException {
    return FSDirectory.open(Paths.get("../index"));
  }

  public static void rmDir(File dir) throws IOException {
    if (dir.exists()) {
      File[] files = dir.listFiles();
      for (int i = 0; i < files.length; i++) {
        if (!files[i].delete()) {
          throw new IOException("could not delete " + files[i]);
        }
      }
      dir.delete();
    }
  }
}
