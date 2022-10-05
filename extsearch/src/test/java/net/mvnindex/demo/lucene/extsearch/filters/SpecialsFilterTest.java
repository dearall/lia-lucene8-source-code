package net.mvnindex.demo.lucene.extsearch.filters;

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
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

// From chapter 6
public class SpecialsFilterTest {
  Directory directory;
  DirectoryReader reader;

  private Query allBooks;
  private IndexSearcher searcher;

  @Before
  public void setUp() throws Exception {
    directory = TestUtil.getBookIndexDirectory();
    reader = DirectoryReader.open(directory);
    searcher = new IndexSearcher(reader);

    allBooks = new MatchAllDocsQuery();
  }

  @After
  public void tearDown() throws Exception {
    reader.close();
    directory.close();
  }

  @Test
  public void testCustomFilter() throws Exception {
    String[] isbns = new String[] {"9780061142666", "9780394756820"};

    SpecialsAccessor accessor = new TestSpecialsAccessor(isbns);
    SpecialsFilter filter = new SpecialsFilter(accessor);

    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    builder.add(allBooks, BooleanClause.Occur.MUST);
    builder.add(filter.getFilter(), BooleanClause.Occur.FILTER);

    Query query = builder.build();

    TopDocs hits = searcher.search(query, 10);
    assertEquals("the specials", isbns.length, hits.totalHits.value);

    for (ScoreDoc scoreDoc: hits.scoreDocs) {
      Document doc = searcher.doc(scoreDoc.doc);
      System.out.println("[title]: "+ doc.get("title") + ", [isbn]: " + doc.get("isbn") + ", [score]: "
              + scoreDoc.score );
    }
  }

  @Test
  public void testFilteredQuery() throws Exception {
    String[] isbns = new String[] {"9780880105118"};

    SpecialsAccessor accessor = new TestSpecialsAccessor(isbns);
    SpecialsFilter filter = new SpecialsFilter(accessor);

    WildcardQuery educationBooks = new WildcardQuery(
            new Term("category", "*education*"));

    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    builder.add(educationBooks, BooleanClause.Occur.SHOULD);
    builder.add(filter.getFilter(), BooleanClause.Occur.FILTER);

    TermQuery logoBooks = new TermQuery(new Term("subject", "logo"));

    BooleanQuery.Builder builderLogoOrEdBooks = new BooleanQuery.Builder();
    builderLogoOrEdBooks.add(logoBooks, BooleanClause.Occur.SHOULD);
    builderLogoOrEdBooks.add(builder.build(), BooleanClause.Occur.SHOULD);

    BooleanQuery logoOrEdBooks = builderLogoOrEdBooks.build();

    TopDocs hits = searcher.search(logoOrEdBooks, 10);
    System.out.println(logoOrEdBooks.toString());
    assertEquals("Papert and Steiner", 2, hits.totalHits.value);

    for (ScoreDoc scoreDoc: hits.scoreDocs) {
      Document doc = searcher.doc(scoreDoc.doc);
      System.out.println("[title]: "+ doc.get("title") + ", [isbn]: "
              + doc.get("isbn") + ", [score]: " + scoreDoc.score );
    }
  }
}
