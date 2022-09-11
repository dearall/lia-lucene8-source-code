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
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

// From chapter 3
public class PrefixQueryTest {
  @Test
  public void testPrefix() throws Exception {
    Directory directory = TestUtil.getBookIndexDirectory();
    DirectoryReader reader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(reader);

    Term term = new Term("category", "/technology/computers/programming");

    PrefixQuery query = new PrefixQuery(term);                      //①
    TopDocs matches = searcher.search(query, 10);
    long programmingAndBelow = matches.totalHits.value;
    System.out.println("PrefixQuery matches totalHits: "+ programmingAndBelow);

    for(int i=0; i<matches.scoreDocs.length; i++) {
      System.out.print("match " + i + "  [title]: " + searcher.doc(matches.scoreDocs[i].doc).get("title"));
      System.out.println(" [category]: " + searcher.doc(matches.scoreDocs[i].doc).get("category"));
    }

    matches = searcher.search(new TermQuery(term), 10);           //②
    long justProgramming = matches.totalHits.value;
    System.out.println("-------------------");
    System.out.println("TermQuery matches totalHits: "+ justProgramming);
    for(int i=0; i<matches.totalHits.value; i++) {
      System.out.print("match " + i + "  [title]: " + searcher.doc(matches.scoreDocs[i].doc).get("title"));
      System.out.println(" [category]: " + searcher.doc(matches.scoreDocs[i].doc).get("category"));
    }

    assertTrue(programmingAndBelow > justProgramming);

    reader.close();
    directory.close();
  }
}

/*
  #① PrefixQuery 查询, 搜索结果包括其本身，以及以其为前缀的子分类
  #② TermQuery 查询, 只搜索结果只包括其本身，不包括以其为前缀的子分类
*/

