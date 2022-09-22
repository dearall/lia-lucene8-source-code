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


import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class CreateTestIndex {
  
  public static Document getDocument(String rootDir, File file) throws IOException {
    Properties props = new Properties();
    props.load(new FileInputStream(file));

    Document doc = new Document();

    // category comes from relative path below the base directory
    String category = file.getParent().substring(rootDir.length()); // ①
    category = category.replace(File.separatorChar, '/');   // ①

    String isbn = props.getProperty("isbn");          //②
    String title = props.getProperty("title");        //②
    String author = props.getProperty("author");      //②
    String url = props.getProperty("url");            //②
    String subject = props.getProperty("subject");    //②

    String pubmonth = props.getProperty("pubmonth");   //②

    System.out.println(title + "\n" + author + "\n" + subject + "\n" + pubmonth + "\n" + category + "\n---------");

    doc.add(new StringField("isbn",               //③
                      isbn,
                      Field.Store.YES));
    doc.add(new StringField("category",           //③
                      category,
                      Field.Store.YES));
    doc.add(new SortedDocValuesField("category",  //③
                      new BytesRef(category)));

    doc.add(new TextField("title",                //③
                      title,
                      Field.Store.YES));

    doc.add(new StringField("title2",             //③
                      title.toLowerCase(),
                      Field.Store.YES));

    doc.add(new SortedDocValuesField("title2",    //③
                      new BytesRef(title.toLowerCase())));

    // split multiple authors into unique field instances
    String[] authors = author.split(",");
    for (String a : authors) {                         //③
      doc.add(new TextField("author",
                        a,
                        Field.Store.YES));
    }

    doc.add(new StringField("url",                //③
                      url,
                      Field.Store.YES));

    doc.add(new TextField("subject",              //③
                      subject,
                      Field.Store.YES));

    doc.add(new IntPoint("pubmonth", Integer.parseInt(pubmonth)));                //③
    doc.add(new NumericDocValuesField("pubmonth", Integer.parseInt(pubmonth)));   //③
    doc.add(new StoredField("pubmonth", Integer.parseInt(pubmonth)));             //③

    Date d;
    try {
      d = DateTools.stringToDate(pubmonth);
    } catch (ParseException pe) {
      throw new RuntimeException(pe);
    }
    doc.add(new IntPoint("pubmonthAsDay", (int) (d.getTime()/(1000*3600*24)))) ;              //③
    doc.add(new NumericDocValuesField("pubmonthAsDay", (int) (d.getTime()/(1000*3600*24)))) ; //③
    doc.add(new StoredField("pubmonthAsDay", (int) (d.getTime()/(1000*3600*24)))) ;

    for(String text : new String[] {title, subject, author, category}) {     //③④
      doc.add(new TextField("contents", text, Field.Store.NO));
    }

    return doc;
  }

  private static String aggregate(String[] strings) {
    StringBuilder buffer = new StringBuilder();

    for (int i = 0; i < strings.length; i++) {
      buffer.append(strings[i]);
      buffer.append(" ");
    }

    return buffer.toString();
  }

  private static void findFiles(List<File> result, File dir) {
    for(File file : dir.listFiles()) {
      if (file.getName().endsWith(".properties")) {
        result.add(file);
      } else if (file.isDirectory()) {
        findFiles(result, file);
      }
    }
  }

  private static class MyStandardAnalyzer extends ExtendableStandardAnalyzer {
    public MyStandardAnalyzer(CharArraySet stopWords) {
      super(stopWords);
    }
    public int getPositionIncrementGap(String field) {                // ⑤
      if (field.equals("contents")) {
        return 100;
      } else {
        return 0;
      }
    }
  }

  public static void main(String[] args) throws IOException {
    System.out.println("Use: java -jar target/common-1.0-SNAPSHOT-shaded.jar");

    String dataDir = "../data";

    List<File> results = new ArrayList<File>();
    findFiles(results, new File(dataDir));

    System.out.println(results.size() + " books to index");

    Directory dir = TestUtil.getBookIndexDirectory();
    IndexWriterConfig wconfig = new IndexWriterConfig(new MyStandardAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET));
    wconfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

    IndexWriter w = new IndexWriter(dir,  wconfig);

    for(File file : results) {
      Document doc = getDocument(dataDir, file);
      w.addDocument(doc);
    }
    w.close();
    dir.close();
  }
}

/*
  ① 获取 category
  ② 从属性文件中获取各个域值
  ③ 向 Document 实例添加域 field
  ④ 向 Document 实例添加一个 catchall 域
  ⑤ 自定义分析器，重写多值域位置增量间隙
*/
