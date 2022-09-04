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
//    Directory dir = TestUtil.getBookIndexDirectory();
    Directory directory = FSDirectory.open(new File( "../index").toPath());
    DirectoryReader dirReader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(dirReader);

    Term term = new Term("category",                           //#A
                         "/technology/computers/programming");    //#A
    PrefixQuery query = new PrefixQuery(term);                    //#A

    TopDocs matches = searcher.search(query, 10);                 //#A
    long programmingAndBelow = matches.totalHits.value;

    matches = searcher.search(new TermQuery(term), 10);           //#B
    long justProgramming = matches.totalHits.value;

    assertTrue(programmingAndBelow > justProgramming);

    dirReader.close();
    directory.close();
  }
}

/*
  #A Search, including subcategories
  #B Search, without subcategories
*/

