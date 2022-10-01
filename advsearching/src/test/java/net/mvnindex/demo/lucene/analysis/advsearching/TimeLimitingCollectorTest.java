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

import junit.framework.TestCase;
import net.mvnindex.demo.lucene.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.*;
import org.apache.lucene.search.TimeLimitingCollector.TimeExceededException;
import org.apache.lucene.store.Directory;

// From chapter 5
public class TimeLimitingCollectorTest extends TestCase {
  public void testTimeLimitingCollector() throws Exception {
    Directory directory = TestUtil.getBookIndexDirectory();
    DirectoryReader reader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(reader);

    Query q = new MatchAllDocsQuery();
    long numAllBooks = TestUtil.hitCount(searcher, q);

    TopScoreDocCollector topDocs = TopScoreDocCollector.create(10, Integer.MAX_VALUE);
    TimeLimitingCollector collector = new TimeLimitingCollector(topDocs,            // ①
            TimeLimitingCollector.getGlobalCounter(), 2);
    try {
      searcher.search(q, collector);
      assertEquals(numAllBooks, topDocs.getTotalHits());  // ②
    } catch (TimeExceededException tee) {                 // ③
      System.out.println("Too much time taken.");         // ③
    }

    TimeLimitingCollector.getGlobalTimerThread().stopTimer();
    reader.close();
    directory.close();
  }
}

/*
  ① 封装已存在的 Collector 实例
  ② 如果没有超时，得到所有的命中
  ③ 超时，输出信息提示
*/
