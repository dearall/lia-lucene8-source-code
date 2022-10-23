package net.mvnindex.demo.lucene.searching;

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
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

// From chapter 3
public class NearRealTimeTest {
  @Test
  public void testNearRealTime() throws Exception {
    Directory dir = new ByteBuffersDirectory();
    IndexWriterConfig config = new IndexWriterConfig();
    IndexWriter writer = new IndexWriter(dir, config);

    for(int i=0;i<10;i++) {
      Document doc = new Document();

      doc.add(new StringField("id", ""+i, Field.Store.NO));
      doc.add(new TextField("text", "aaa", Field.Store.NO));

      writer.addDocument(doc);
    }

    DirectoryReader reader = DirectoryReader.open(writer);    // ①
    IndexSearcher searcher = new IndexSearcher(reader);       // ②

    Query query = new TermQuery(new Term("text", "aaa"));
    TopDocs docs = searcher.search(query, 1);
    assertEquals(10, docs.totalHits.value);           // ③

    writer.deleteDocuments(new Term("id", "7"));       // ④

    Document doc = new Document();
    doc.add(new StringField("id", "11", Field.Store.NO ));
    doc.add(new TextField("text", "bbb", Field.Store.NO));
    writer.addDocument(doc);                                           // ⑤

    DirectoryReader newReader = DirectoryReader.openIfChanged(reader); // ⑥
    assertFalse(reader == newReader);                          // ⑦
    if(newReader != null) {
      reader.close();                                                    // ⑧
      searcher = new IndexSearcher(newReader);

      TopDocs hits = searcher.search(query, 10);
      assertEquals(9, hits.totalHits.value);                      // ⑨

      query = new TermQuery(new Term("text", "bbb"));
      hits = searcher.search(query, 1);
      assertEquals(1, hits.totalHits.value);                      // ⑩

      newReader.close();
    }
    writer.close();
    dir.close();
  }
}

/*
  ① 创建近实时 reader。调用 DirectoryReader.open(writer) 打开一个 reader，能够搜索所有之前提交的索引，加上当前 writer 未提交的索引
  ② 将 reader 封装到 IndexSearcher 实例
  ③ 确认搜索返回 10 个命中结果
  ④ 删除 id 为 7 的 1 个文档
  ⑤ 向索引库中添加 1 个文档
  ④⑤ 对索引进行更改，但没有提交它们
  ⑥ 重新打开一个 reader
  ⑦ 确认新打开的 reader 与之前的 reader 不是同一个
  ⑧ 关闭旧的 reader
  ⑥⑦⑧ 调用 DirectoryReader.openIfChanged(reader) 重新打开一个新的 reader，因为我们对索引进行了更改，
newReader 与前一个 reader 不同，因此必须将旧 reader 关闭。
  ⑨ 确认现在有 9 个命中文档
  ⑩ 确认搜索匹配新加入的文档
  ⑨⑩ 由 writer 对索引所做更改反映在新的搜索操作上
*/
