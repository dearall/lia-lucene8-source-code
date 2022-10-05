package net.mvnindex.demo.lucene.analysis.keyword;

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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

// From chapter 4
public class KeywordAnalyzerTest {
  private final String indexPath = "indexes";
  private Directory directory;
  private DirectoryReader reader;
  private IndexSearcher searcher;

  @Before
  public void setUp() throws Exception {
    directory = new ByteBuffersDirectory();
    IndexWriterConfig config = new IndexWriterConfig(new SimpleAnalyzer());
    IndexWriter writer = new IndexWriter(directory, config);

    Document doc = new Document();
    doc.add(new StringField("partnum",  // ①
                      "Q36",
                      Field.Store.NO));

    doc.add(new TextField("description",
                      "Illidium Space Modulator",
                      Field.Store.YES));

    writer.addDocument(doc);
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
  public void testTermQuery() throws Exception {
    Query query = new TermQuery(new Term("partnum", "Q36"));    // ②
    assertEquals(1, TestUtil.hitCount(searcher, query));        // ③
  }

  @Test
  public void testBasicQueryParser() throws Exception {
    Query query = new QueryParser("description", new SimpleAnalyzer())             // ④
                      .parse("partnum:Q36 AND SPACE");                          // ④
    assertEquals("note Q36 -> q",
                 "+partnum:q +space", query.toString("description"));    // ⑤
    assertEquals("doc not found :(", 0, TestUtil.hitCount(searcher, query));

    System.out.println("query 表示： " + query.toString("description"));
  }

/*
① StringField 不对域进行分析
② 通过 API 直接创建 TermQuery，不对词项进行分析
③ 验证文档匹配
④ QueryParser 对每个词项和短语进行分析
⑤ toString() 方法，输出 "+partnum:q +space"
*/

  @Test
  public void testPerFieldAnalyzer() throws Exception {
    Map<String, Analyzer> analyzerPerField = new HashMap<>();
    analyzerPerField.put("partnum", new KeywordAnalyzer());

    PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new SimpleAnalyzer(),
            analyzerPerField);

    Query query = new QueryParser("description", analyzer).parse(
                "partnum:Q36 AND SPACE");

    assertEquals("Q36 kept as-is",
              "+partnum:Q36 +space", query.toString("description"));  
    assertEquals("doc found!", 1, TestUtil.hitCount(searcher, query));
  }
}
