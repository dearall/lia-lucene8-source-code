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

import junit.framework.TestCase;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

// From chapter 3
public class NumericRangeQueryTest {

  @Test
  public void testInclusive() throws Exception {
    //Directory dir = TestUtil.getBookIndexDirectory();
    Directory directory = FSDirectory.open(new File( "../index").toPath());
    DirectoryReader dirReader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(dirReader);
    // pub date of TTC was September 2006
    Query query = IntPoint.newRangeQuery("pubmonth", 200605, 200609);

    TopDocs matches = searcher.search(query, 10);

    assertEquals(1, matches.totalHits.value);

    for(int i=0; i<matches.totalHits.value; i++) {
      System.out.println("match " + i + ": " + searcher.doc(matches.scoreDocs[i].doc).get("author"));
    }

    dirReader.close();
    directory.close();
  }

  @Test
  public void testExclusive() throws Exception {
    //Directory dir = TestUtil.getBookIndexDirectory();
    Directory directory = FSDirectory.open(new File( "../index").toPath());
    DirectoryReader dirReader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(dirReader);

    // pub date of TTC was September 2006
    Query query = IntPoint.newRangeQuery("pubmonth", Math.addExact(200605, 1),
            Math.addExact(200609, -1));

    TopDocs matches = searcher.search(query, 10);
    assertEquals(0, matches.totalHits.value);

    dirReader.close();
    directory.close();
  }
}
