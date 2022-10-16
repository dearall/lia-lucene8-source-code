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

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

// From chapter 1

/**
 * This code was originally written for
 * Erik's Lucene intro java.net article
 */
public class Indexer {

  public static void main(String[] args) throws Exception {
    if(args.length == 1) {
      if (args[0].equals("-h") || args[0].equals("--help")) {
        System.out.println("Usage: java -jar target/Indexer-1.0-SNAPSHOT-shaded.jar <index dir> <data dir>");
        return;
      }
    }

    String indexDir = "../indexes/MeetLucene";  //①
    String dataDir = "data";                    //②

    if(args.length >= 1)
      indexDir = args[0];

    if(args.length >= 2)
      dataDir = args[1];

    long start = System.currentTimeMillis();
    Indexer indexer = new Indexer(indexDir);
    int numIndexed;
    try {
      numIndexed = indexer.index(dataDir, new TextFilesFilter());
    } finally {
      indexer.close();
    }
    long end = System.currentTimeMillis();

    System.out.println("Indexing " + numIndexed + " files took "
      + (end - start) + " milliseconds");
  }

  private IndexWriter writer;

  public Indexer(String indexDir) throws IOException {
    Directory dir = FSDirectory.open(Paths.get(indexDir));
    IndexWriterConfig wconfig = new IndexWriterConfig(
            new StandardAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET));
    wconfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE)
            .setUseCompoundFile(false);

    writer = new IndexWriter(dir,  wconfig);  //③
  }

  public void close() throws IOException {
    writer.close();   //④
  }

  public int index(String dataDir, FileFilter filter)
    throws Exception {

    File[] files = new File(dataDir).listFiles();

    for (File f: files) {
      if (!f.isDirectory() &&
          !f.isHidden() &&
          f.exists() &&
          f.canRead() &&
          (filter == null || filter.accept(f))) {
        indexFile(f);
      }
    }

    return writer.getDocStats().numDocs;                          //⑤
  }

  private static class TextFilesFilter implements FileFilter {
    public boolean accept(File path) {
      return path.getName().toLowerCase().endsWith(".txt");        //⑥
    }
  }

  protected Document getDocument(File f) throws Exception {
    Document doc = new Document();

    TextField contentsField = new TextField("contents", new FileReader(f));
    doc.add(contentsField);      //⑦

    StringField filenameField = new StringField("filename", f.getName(), Field.Store.YES);
    doc.add(filenameField);//⑧

    StringField fullpathField = new StringField("fullpath", f.getCanonicalPath(), Field.Store.YES);
    doc.add(fullpathField);//⑨

    return doc;
  }

  private void indexFile(File f) throws Exception {
    System.out.println("Indexing " + f.getCanonicalPath());
    Document doc = getDocument(f);
    writer.addDocument(doc);                              //⑩
  }
}

/*
① Create index in this directory
② Index *.txt files from this directory
③ Create Lucene IndexWriter
④ Close IndexWriter
⑤ Return number of documents indexed
⑥ Index .txt files only, using FileFilter
⑦ Index file content
⑧ Index file name
⑨ Index file full path
⑩ Add document to Lucene index
*/
