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
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.text.DecimalFormat;

// From chapter 5
public class SortingExample {
  private Directory directory;

  public SortingExample(Directory directory) {
    this.directory = directory;
  }

  public void displayResults(Query query, Sort sort) throws IOException {     // ①
    DirectoryReader reader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(reader);

    TopDocs results = searcher.search(query, 20, sort, true);   // ②

    System.out.println("\nResults for: " +                                    // ③
        query.toString() + " sorted by " + sort.toString());

    System.out.println("----------------------------------------------------------------------------------------");

    System.out.println(StringUtils.rightPad("Title", 30) +
      StringUtils.rightPad("pubmonth", 10) +
      StringUtils.center("id", 4) +
      StringUtils.center("score", 15));
    System.out.println();

    PrintStream out = new PrintStream(System.out, true, "UTF-8"); // ④

    DecimalFormat scoreFormatter = new DecimalFormat("0.######");
    for (ScoreDoc sd : results.scoreDocs) {
      int docID = sd.doc;
      float score = sd.score;
      //System.out.println("score: " + score);
      Document doc = searcher.doc(docID);
      out.println(
          StringUtils.rightPad(                                                    // ⑤
              StringUtils.abbreviate(doc.get("title"), 29), 30) +
          StringUtils.rightPad(doc.get("pubmonth"), 10) +
          StringUtils.center("" + docID, 4) +
          StringUtils.leftPad(
             scoreFormatter.format(score), 12));
      out.println("   " + doc.get("category"));
      //out.println(searcher.explain(query, docID));   // ⑥
    }

    System.out.println("*****");
    reader.close();
  }

/*
  ① Sort 对象封装了一个有序的用于排序信息域的集合
  ② 调用重载的 search(Query query, int n, Sort sort, boolean doDocScores) 方法，接收自定义的 Sort 实现，并为 doDocScores 参数传递 true
  值，以计算匹配结果评分值。
  ③ 使用 Sort 类的 toString() 方法打印自己
  ④ 通过 System.out 对象创建接受 UTF-8 编码的 PrintStream
  ⑤ 通过 Apache Commons Lang 的 StringUtils 格式化输出
  ⑥ 输出评分解释，现在把这行代码注释掉
*/

  public static void main(String[] args) throws Exception {
    Query allBooks = new MatchAllDocsQuery();

    QueryParser parser = new QueryParser("contents", new StandardAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET));  // ①
    BooleanQuery.Builder builder = new BooleanQuery.Builder();

    builder.add(allBooks, BooleanClause.Occur.SHOULD);
    builder.add(parser.parse("java OR action"), BooleanClause.Occur.SHOULD);
    BooleanQuery query = builder.build();

    Directory directory = FSDirectory.open(Paths.get("index"));             // ②

    SortingExample example = new SortingExample(directory);                     // ②

    example.displayResults(query, Sort.RELEVANCE);                              // ②

    example.displayResults(query, Sort.INDEXORDER);                             // ②

    example.displayResults(query, new Sort(new SortField("category", SortField.Type.STRING)));           // ②

    example.displayResults(query, new Sort(new SortField("pubmonth", SortField.Type.INT, true))); // ②

    example.displayResults(query,                                               // ②
        new Sort(new SortField("category", SortField.Type.STRING),
                 SortField.FIELD_SCORE,
                 new SortField("pubmonth", SortField.Type.INT, true)
                 ));


    example.displayResults(query, new Sort(new SortField[] {SortField.FIELD_SCORE, new SortField("category",
            SortField.FIELD_DOC.getType().STRING)}));                           // ②


    directory.close();
  }
}

/*
① 创建测试查询
② 创建执行实例
*/