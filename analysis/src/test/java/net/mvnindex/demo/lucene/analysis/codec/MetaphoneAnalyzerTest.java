package net.mvnindex.demo.lucene.analysis.codec;

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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;


// From chapter 4
public class MetaphoneAnalyzerTest extends TestCase {
  private Directory directory;

  @Before
  public void setUp() throws IOException {
    directory = new ByteBuffersDirectory();
  }

  @After
  public void tearDown() throws IOException {
    directory.close();
  }

  @Test
  public void testKoolKat() throws Exception {
    Analyzer analyzer = new MetaphoneReplacementAnalyzer();
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    IndexWriter writer = new IndexWriter(directory, config);

    Document doc = new Document();
    doc.add(new TextField("contents",  "cool cat", Field.Store.YES));     // ①
    writer.addDocument(doc);
    writer.close();

    DirectoryReader reader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(reader);

    Query query = new QueryParser("contents", analyzer).parse("kool kat");   // ②

    TopDocs hits = searcher.search(query, 1);
    assertEquals(1, hits.totalHits.value);            //③

    int docID = hits.scoreDocs[0].doc;
    doc = searcher.doc(docID);
    System.out.println("contents: "+ doc.get("contents"));    // ④

    reader.close();
  }

  /*
    ① 索引文档
    ② 解析查询文本
    ③ 验证匹配
    ④ 检索原始值
  */
}
