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

import net.mvnindex.demo.lucene.common.TestUtil;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

// From chapter 3
public class BooleanQueryTest {
  @Test
  public void testAnd() throws Exception {
    TermQuery searchingBooks = new TermQuery(new Term("subject","search"));  //#1

    Query books2010 = IntPoint.newRangeQuery("pubmonth",
            201001,  201012);   //#2

    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    builder.add(searchingBooks, BooleanClause.Occur.MUST);  //#3
    builder.add(books2010, BooleanClause.Occur.MUST);       //#3
    BooleanQuery searchingBooks2010 = builder.build();

//    Directory dir = TestUtil.getBookIndexDirectory();
    Directory directory = FSDirectory.open(new File( "../index").toPath());
    DirectoryReader dirReader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(dirReader);
    TopDocs matches = searcher.search(searchingBooks2010, 10);

    assertTrue(TestUtil.hitsIncludeTitle(searcher, matches,
                                 "Lucene in Action, Second Edition"));

    dirReader.close();
    directory.close();
  }

/*
#1 Match books with subject “search”
#2 Match books in 2004
#3 Combines two queries
*/

  @Test
  public void testOr() throws Exception {
    TermQuery methodologyBooks = new TermQuery(                       // #1
               new Term("category",                                // #1
                 "/technology/computers/programming/methodology"));   // #1

    TermQuery easternPhilosophyBooks = new TermQuery(                 // #2
        new Term("category",                                       // #2
            "/philosophy/eastern"));                                  // #2

    BooleanQuery.Builder builder = new BooleanQuery.Builder();        // #3
    builder.add(methodologyBooks, BooleanClause.Occur.SHOULD);        // #3
    builder.add(easternPhilosophyBooks, BooleanClause.Occur.SHOULD);  // #3
    BooleanQuery enlightenmentBooks = builder.build();                // #3

    //Directory dir = TestUtil.getBookIndexDirectory();
    Directory directory = FSDirectory.open(new File( "../index").toPath());
    DirectoryReader dirReader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(dirReader);
    TopDocs matches = searcher.search(enlightenmentBooks, 10);
    System.out.println("or = " + enlightenmentBooks);

    assertTrue(TestUtil.hitsIncludeTitle(searcher, matches,
                                         "Extreme Programming Explained"));
    assertTrue(TestUtil.hitsIncludeTitle(searcher, matches,
                                         "Tao Te Ching \u9053\u5FB7\u7D93"));
    dirReader.close();
    directory.close();
  }

  /*
#1 Match 1st category
#2 Match 2nd category
#3 Combine
   */
}
