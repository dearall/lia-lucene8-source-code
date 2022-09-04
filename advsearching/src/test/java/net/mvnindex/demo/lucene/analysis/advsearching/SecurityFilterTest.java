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
    directory = new RAMDirectory();
    IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
    IndexWriter writer = new IndexWriter(directory, config);

    Document document = new Document();
    document.add(new StringField("owner",
                           "elwood",
                           Field.Store.YES));
    document.add(new TextField("keywords",
                           "elwood's sensitive info",
                           Field.Store.YES));

    writer.addDocument(document);

    document = new Document();
    document.add(new StringField("owner",
                           "jake",
                           Field.Store.YES ));

    document.add(new TextField("keywords",
                           "jake's sensitive info",
                           Field.Store.YES));

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

  /*
#1 Elwood
#2 Jake
  */

  @Test
  public void testSecurityFilter() throws Exception {
    TermQuery query = new TermQuery(new Term("keywords", "info"));

    assertEquals("Both documents match",
                 2,
                 TestUtil.hitCount(searcher, query));

    ConstantScoreQuery jakeFilter = new ConstantScoreQuery(new TermQuery(new Term("owner", "jake")));

    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    builder.add(query, BooleanClause.Occur.MUST);
    builder.add(jakeFilter, BooleanClause.Occur.FILTER);
    BooleanQuery booleanQuery = builder.build();

    TopDocs hits = searcher.search(booleanQuery, 10);

    assertEquals(1, hits.totalHits.value);
    assertEquals("elwood is safe",
                 "jake's sensitive info",
                  searcher.doc(hits.scoreDocs[0].doc).get("keywords"));
  }
  /*
    #1 TermQuery for "info"
    #2 Returns documents containing "info"
    #3 Filter
    #4 Same TermQuery, constrained results
  */
}
