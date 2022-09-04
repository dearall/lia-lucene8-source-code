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
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// From chapter 5
public class MultiFieldQueryParserTest {
  @Test
  public void testDefaultOperator() throws Exception {
    Query query = new MultiFieldQueryParser(new String[]{"title", "subject"}, new SimpleAnalyzer())
            .parse("development");

    //Directory dir = TestUtil.getBookIndexDirectory();
    Directory directory = FSDirectory.open(new File("../index").toPath());
    DirectoryReader directoryReader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(directoryReader);

    TopDocs hits = searcher.search(query, 10);

    assertTrue(TestUtil.hitsIncludeTitle(
           searcher,
           hits,
           "Ant in Action"));

    assertTrue(TestUtil.hitsIncludeTitle(     //A
           searcher,                          //A
           hits,                              //A
           "Extreme Programming Explained")); //A
    directoryReader.close();
    directory.close();
  }

  @Test
  public void testSpecifiedOperator() throws Exception {

    BooleanClause.Occur[] flags = {BooleanClause.Occur.MUST, BooleanClause.Occur.MUST};
    Query query = MultiFieldQueryParser.parse(
        "lucene",
            new String[]{"title", "subject"},
            flags,
            new SimpleAnalyzer());

    Directory directory = FSDirectory.open(new File("../index").toPath());
    DirectoryReader directoryReader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(directoryReader);

    TopDocs hits = searcher.search(query, 10);

    assertTrue(TestUtil.hitsIncludeTitle(
            searcher,
            hits,
            "Lucene in Action, Second Edition"));
    assertEquals("one and only one", 1, hits.scoreDocs.length);

    directoryReader.close();
    directory.close();
  }

  /*
    #A Has development in the subject field
   */

}
