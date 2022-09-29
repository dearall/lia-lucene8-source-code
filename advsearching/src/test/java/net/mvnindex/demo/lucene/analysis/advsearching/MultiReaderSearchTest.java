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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

// From chapter 5
public class MultiReaderSearchTest {
  private Analyzer analyzer = new WhitespaceAnalyzer();
  private IndexWriterConfig aIndexWriterConfig = new IndexWriterConfig(analyzer);
  private IndexWriterConfig bIndexWriterConfig = new IndexWriterConfig(analyzer);
  private Directory aTOmDirectory;
  private Directory nTOzDirectory;

  private IndexSearcher[] searchers;

  @Before
  public void setUp() throws Exception {

    String[] animals = { "aardvark", "beaver", "coati",
                       "dog", "elephant", "frog", "gila monster",
                       "horse", "iguana", "javelina", "kangaroo",
                       "lemur", "moose", "nematode", "orca",
                       "python", "quokka", "rat", "scorpion",
                       "tarantula", "uromastyx", "vicuna",
                       "walrus", "xiphias", "yak", "zebra"};


    aTOmDirectory = new ByteBuffersDirectory();
    nTOzDirectory = new ByteBuffersDirectory();

    IndexWriter aTOmWriter = new IndexWriter(aTOmDirectory, aIndexWriterConfig);
    IndexWriter nTOzWriter = new IndexWriter(nTOzDirectory, bIndexWriterConfig);

    for (int i=animals.length - 1; i >= 0; i--) {
      Document doc = new Document();
      String animal = animals[i];

      doc.add(new StringField("animal", animal, Field.Store.YES));
      if (animal.charAt(0) < 'n') {
        aTOmWriter.addDocument(doc);
      } else {                                       
        nTOzWriter.addDocument(doc);
      }
    }

    aTOmWriter.close();
    nTOzWriter.close();
  }

  @After
  public void tearDown() throws Exception {
    aTOmDirectory.close();
    nTOzDirectory.close();
  }

  @Test
  public void testMultiReaderSearch() throws Exception {
    IndexReader aIndexReader = DirectoryReader.open(aTOmDirectory);
    IndexReader nIndexReader = DirectoryReader.open(nTOzDirectory);
    MultiReader multiReader = new MultiReader(aIndexReader, nIndexReader);

    IndexSearcher indexSearcher = new IndexSearcher(multiReader);
    TermRangeQuery query = new TermRangeQuery("animal",
            new BytesRef("h"),
            new BytesRef("t"),
            true,
            true);
    TopDocs animal = indexSearcher.search(query,20);

    assertEquals(12, animal.totalHits.value);
    System.out.println("totalHits: " + animal.totalHits.value);

    ScoreDoc[] scoreDocs = animal.scoreDocs;
    System.out.println("scoreDocs length: " + scoreDocs.length);

    for (ScoreDoc sd : scoreDocs) {
      System.out.println("[docId]: "+sd.doc + ",  [animal]: "
              + indexSearcher.doc(sd.doc).get("animal") + ", [score]: " + sd.score);
    }

    multiReader.close();
  }
}
