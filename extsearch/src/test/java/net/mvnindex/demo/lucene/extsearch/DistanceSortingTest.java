package net.mvnindex.demo.lucene.extsearch;

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

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

// From chapter 6
public class DistanceSortingTest{
  private Directory directory;
  private DirectoryReader reader;
  private IndexSearcher searcher;
  private Query query;

  @Before
  public void setUp() throws Exception {
    directory = new ByteBuffersDirectory();
    IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
    IndexWriter writer = new IndexWriter(directory, config);

    addPoint(writer, "El Charro", "restaurant", 1, 2);
    addPoint(writer, "Cafe Poca Cosa", "restaurant", 5, 9);
    addPoint(writer, "Los Betos", "restaurant", 9, 6);
    addPoint(writer, "Nico's Taco Shop", "restaurant", 3, 8);

    writer.close();

    reader = DirectoryReader.open(directory);
    searcher = new IndexSearcher(reader);

    query = new TermQuery(new Term("type", "restaurant"));
  }

  private void addPoint(IndexWriter writer, String name, String type, int x, int y)
      throws IOException {
    Document doc = new Document();
    doc.add(new StringField("name", name, Field.Store.YES));
    doc.add(new StringField("type", type, Field.Store.YES));
    doc.add(new IntPointDocValuesField("location", x, y));
    writer.addDocument(doc);
  }

  @After
  public void tearDown() throws Exception {
    reader.close();
    directory.close();
  }

  @Test
  public void testNearestRestaurantToHome() throws Exception {
    Sort sort = new Sort(new SortField("location", new DistanceComparatorSource(0, 0)));

    TopFieldDocs hits = searcher.search(query, 10, sort);

    assertEquals("closest",
                 "El Charro", searcher.doc(hits.scoreDocs[0].doc).get("name"));
    assertEquals("furthest",
                 "Los Betos", searcher.doc(hits.scoreDocs[3].doc).get("name"));
  }

  @Test
  public void testNeareastRestaurantToWork() throws Exception {
    Sort sort = new Sort(new SortField("location", new DistanceComparatorSource(10, 10)));

    TopFieldDocs topDocs = searcher.search(query, 3, sort);     // ①

    assertEquals(4, topDocs.totalHits.value);             // ②
    assertEquals(3, topDocs.scoreDocs.length);            // ③

    FieldDoc fieldDoc = (FieldDoc) topDocs.scoreDocs[0];          // ④

    assertEquals("(10,10) -> (9,6) = sqrt(17)",
            Math.sqrt(17),
            fieldDoc.fields[0]);                                  // ⑤

    Document document = searcher.doc(fieldDoc.doc);               // ⑥
    assertEquals("Los Betos", document.get("name"));

    dumpDocs(sort, topDocs);
  }

  /*
    ① 指定最大返回命中数量
    ② 验证命中总数
    ③ 验证实际返回的匹配文档总数
    ④ 返回最高分的匹配文档的 FieldDoc 对象。topDocs.scoreDocs[0] 返回一个 ScoreDoc 对象，
    必须将它强制转换为 FieldDoc 类型来获取排序时计算出的结果值。
    ⑤ 取回第一个计算的值，并验证其结果与 Math.sqrt(17) 计算的值相同
    ⑥ 获取文档，并验证其 name 值为 "Los Betos"
  */

  private void dumpDocs(Sort sort, TopFieldDocs docs) throws IOException {
    System.out.println("Sorted by: " + sort);
    ScoreDoc[] scoreDocs = docs.scoreDocs;
    for (int i = 0; i < scoreDocs.length; i++) {
      FieldDoc fieldDoc = (FieldDoc) scoreDocs[i];
      Double distance = (Double) fieldDoc.fields[0];
      Document doc = searcher.doc(fieldDoc.doc);
      System.out.println("   " + doc.get("name") + " -> " + distance);
    }
  }
}
