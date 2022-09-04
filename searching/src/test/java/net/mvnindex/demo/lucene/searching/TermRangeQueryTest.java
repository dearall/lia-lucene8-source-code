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
import net.mvnindex.demo.lucene.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

// From chapter 3
public class TermRangeQueryTest {

  @Test
  public void testTermRangeQuery() throws Exception {
//    Directory dir = TestUtil.getBookIndexDirectory();
    Directory directory = FSDirectory.open(new File( "../index").toPath());
    DirectoryReader dirReader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(dirReader);
    TermRangeQuery query = TermRangeQuery.newStringRange("title2", "d", "j", true, true);

    TopDocs matches = searcher.search(query, 100);
    assertEquals(3, matches.totalHits.value);

    for(int i=0;i<matches.totalHits.value;i++) {
      System.out.println("match " + i + ": " + searcher.doc(matches.scoreDocs[i].doc).get("title2"));
    }

    dirReader.close();
    directory.close();
  }
}
