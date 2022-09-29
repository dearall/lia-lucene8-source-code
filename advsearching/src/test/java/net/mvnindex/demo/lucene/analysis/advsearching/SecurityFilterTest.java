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
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

// From chapter 5
public class SecurityFilterTest {
  private Directory directory;
  private DirectoryReader reader;
  private IndexSearcher searcher;

  @Before
  public void setUp() throws Exception {
    directory = new ByteBuffersDirectory();
    IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
    IndexWriter writer = new IndexWriter(directory, config);

    Document document = new Document();
    document.add(new StringField("owner", "elwood", Field.Store.YES));
    document.add(new TextField("keywords", "elwood's sensitive info", Field.Store.YES));
    writer.addDocument(document);

    document = new Document();
    document.add(new StringField("owner", "jake", Field.Store.YES ));
    document.add(new TextField("keywords", "jake's sensitive info", Field.Store.YES));
    writer.addDocument(document);

    writer.close();

    reader = DirectoryReader.open(directory);
    searcher = new IndexSearcher(reader);
  }

  @After
  public void tearDown() throws Exception {
    reader.close();
    directory.close();
  }


  @Test
  public void testSecurityFilter() throws Exception {
    TermQuery query = new TermQuery(new Term("keywords", "info"));                        // ①
    assertEquals("Both documents match", 2, TestUtil.hitCount(searcher, query));  // ②

    ConstantScoreQuery jakeFilter = new ConstantScoreQuery(new TermQuery(new Term("owner", "jake"))); // ③

    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    builder.add(query, BooleanClause.Occur.MUST);
    builder.add(jakeFilter, BooleanClause.Occur.MUST);                  // ④
    BooleanQuery booleanQuery = builder.build();

    TopDocs hits = searcher.search(booleanQuery, 10);

    assertEquals(1, hits.totalHits.value);
    assertEquals("elwood is safe",
                 "jake's sensitive info",
                  searcher.doc(hits.scoreDocs[0].doc).get("keywords")); // ⑤
  }
  /*
  ① 为 "keywords":"info" 词项创建 TermQuery 查询
  ② 验证搜索包含 "keywords":"info" 的 2 个文档
  ③ 构造过滤器，把文档的搜索范围限制在 jake 所有者的文档范围内
  ④ 将查询子句和过滤子句连接起来构建布尔查询
  ⑤ 执行过滤查询，并验证搜索结果，结果中只包含 "owner" 为 "jake" 的文档
  */
}
