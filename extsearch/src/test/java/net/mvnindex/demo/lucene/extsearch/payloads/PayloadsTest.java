package net.mvnindex.demo.lucene.extsearch.payloads;

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
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.payloads.AveragePayloadFunction;
import org.apache.lucene.queries.payloads.PayloadDecoder;
import org.apache.lucene.queries.payloads.PayloadScoreQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import static org.junit.Assert.assertEquals;

// From chapter 6
public class PayloadsTest{
  Directory directory;
  IndexWriter writer;
  BulletinPayloadsAnalyzer analyzer;

  @Before
  public void setUp() throws Exception {
    directory = new ByteBuffersDirectory();
    analyzer = new BulletinPayloadsAnalyzer(5.0F);                  // ①
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    writer = new IndexWriter(directory, config);
  }

  void addDoc(String title, String contents) throws IOException {
    Document doc = new Document();
    doc.add(new StringField("title",
                      title,
                      Field.Store.YES));
    doc.add(new TextField("contents",
                      contents,
                      Field.Store.NO));
    analyzer.setIsBulletin(contents.startsWith("Bulletin:"));
    writer.addDocument(doc);
  }

  @Test
  public void testPayloadTermQuery() throws Throwable {
    addDoc("Hurricane warning",
           "Bulletin: A hurricane warning was issued at " +
           "6 AM for the outer great banks");
    addDoc("Warning label maker",
           "The warning label maker is a delightful toy for " +
           "your precocious seven year old's warning needs");
    addDoc("Tornado warning",
           "Bulletin: There is a tornado warning for " +
           "Worcester county until 6 PM today");

    writer.close();

    IndexReader reader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(reader);

    Term warning = new Term("contents", "warning");
    
    Query query1 = new TermQuery(warning);
    System.out.println("\nTermQuery results:");
    TopDocs hits = searcher.search(query1, 10);
    TestUtil.dumpHits(searcher, hits);

    assertEquals("Warning label maker",                                // ②
                 searcher.doc(hits.scoreDocs[0].doc).get("title"));

    Query query2 = new PayloadScoreQuery(
            new SpanTermQuery(warning),
            new AveragePayloadFunction(),
            PayloadDecoder.FLOAT_DECODER);

    System.out.println("\nPayloadTermQuery results:");
    hits = searcher.search(query2, 10);
    TestUtil.dumpHits(searcher, hits);

    assertEquals("Warning label maker",                        // ③
                 searcher.doc(hits.scoreDocs[2].doc).get("title"));    // ③
    reader.close();
  }
}

/*
  ① 加权 5.0
  ② 普通 term 查询评级是第一位
  ③ 加权后被评级到最后一位
*/
