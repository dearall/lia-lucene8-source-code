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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
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
    String category = file.getParent().substring(rootDir.length());
    category = category.replace(File.separatorChar, '/');

    String isbn = props.getProperty("isbn");
    String title = props.getProperty("title");
    String author = props.getProperty("author");
    String url = props.getProperty("url");
    String subject = props.getProperty("subject");

    String pubmonth = props.getProperty("pubmonth");

    System.out.println(title + "\n" + author + "\n" + subject + "\n" + pubmonth + "\n" + category + "\n---------");

    doc.add(new StringField("isbn",
                      isbn,
                      Field.Store.YES));
    doc.add(new StringField("category",
                      category,
                      Field.Store.YES));
    doc.add(new SortedDocValuesField("category",
                      new BytesRef(category)));

    doc.add(new TextField("title",
                      title,
                      Field.Store.YES));

    doc.add(new StringField("title2",
                      title.toLowerCase(),
                      Field.Store.YES));

    doc.add(new SortedDocValuesField("title2",
                      new BytesRef(title.toLowerCase())));

    // split multiple authors into unique field instances
    String[] authors = author.split(",");
    for (String a : authors) {
      doc.add(new TextField("author",
                        a,
                        Field.Store.YES));
    }

    doc.add(new StringField("url",
                      url,
                      Field.Store.YES));

    doc.add(new TextField("subject",
                      subject,
                      Field.Store.YES));

    doc.add(new IntPoint("pubmonth", Integer.parseInt(pubmonth)));
    doc.add(new NumericDocValuesField("pubmonth", Integer.parseInt(pubmonth)));
    doc.add(new StoredField("pubmonth", Integer.parseInt(pubmonth)));

    Date d;
    try {
      d = DateTools.stringToDate(pubmonth);
    } catch (ParseException pe) {
      throw new RuntimeException(pe);
    }
    doc.add(new IntPoint("pubmonthAsDay", (int) (d.getTime()/(1000*3600*24)))) ;
    doc.add(new NumericDocValuesField("pubmonthAsDay", (int) (d.getTime()/(1000*3600*24)))) ;
    doc.add(new StoredField("pubmonthAsDay", (int) (d.getTime()/(1000*3600*24)))) ;

    for(String text : new String[] {title, subject, author, category}) {
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

  public static void main(String[] args) throws IOException {
    System.out.println("Use: java -jar target/common-1.0-SNAPSHOT-shaded.jar");

    String dataDir = "../data";

    List<File> results = new ArrayList<File>();
    findFiles(results, new File(dataDir));

    System.out.println(results.size() + " books to index");

    //Directory dir = FSDirectory.open(Paths.get(indexDir));
    Directory dir = TestUtil.getBookIndexDirectory();
    IndexWriterConfig wconfig = new IndexWriterConfig(new StandardAnalyzer());
    wconfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE)
            .setUseCompoundFile(false);

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
  #1 Get category
  #2 Pull fields
  #3 Add fields to Document instance
  #4 Flag subject field
  #5 Add catch-all contents field
  #6 Custom analyzer to override multi-valued position increment
*/
