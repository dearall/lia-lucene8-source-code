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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

// From chapter 5
public class SpanQueryTest {
  private ByteBuffersDirectory directory;
  private DirectoryReader reader;
  private IndexSearcher searcher;

  private SpanTermQuery the;
  private SpanTermQuery quick;
  private SpanTermQuery brown;
  private SpanTermQuery red;
  private SpanTermQuery fox;
  private SpanTermQuery lazy;
  private SpanTermQuery sleepy;
  private SpanTermQuery dog;
  private SpanTermQuery cat;
  private Analyzer analyzer;

  @Before
  public void setUp() throws Exception {
    directory = new ByteBuffersDirectory();

    analyzer = new WhitespaceAnalyzer();
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    IndexWriter writer = new IndexWriter(directory, config);

    Document doc = new Document();
    doc.add(new TextField("f",
        "the quick brown fox jumps over the lazy dog",
        Field.Store.YES));
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new TextField("f",
        "the quick red fox jumps over the sleepy cat",
        Field.Store.YES));
    writer.addDocument(doc);

    writer.close();

    reader = DirectoryReader.open(directory);
    searcher = new IndexSearcher(reader);

    the = new SpanTermQuery(new Term("f", "the"));
    quick = new SpanTermQuery(new Term("f", "quick"));
    brown = new SpanTermQuery(new Term("f", "brown"));
    red = new SpanTermQuery(new Term("f", "red"));
    fox = new SpanTermQuery(new Term("f", "fox"));
    lazy = new SpanTermQuery(new Term("f", "lazy"));
    sleepy = new SpanTermQuery(new Term("f", "sleepy"));
    dog = new SpanTermQuery(new Term("f", "dog"));
    cat = new SpanTermQuery(new Term("f", "cat"));
  }

  @After
  public void tearDown() throws Exception {
    reader.close();
    directory.close();
  }

  private void assertOnlyBrownFox(Query query) throws Exception {
    TopDocs hits = searcher.search(query, 10);
    assertEquals(1, hits.totalHits.value);
    assertEquals("wrong doc", 0, hits.scoreDocs[0].doc);
  }

  private void assertBothFoxes(Query query) throws Exception {
    TopDocs hits = searcher.search(query, 10);
    assertEquals(2, hits.totalHits.value);
  }

  private void assertNoMatches(Query query) throws Exception {
    TopDocs hits = searcher.search(query, 10);
    assertEquals(0, hits.totalHits.value);
  }

  @Test
  public void testSpanTermQuery() throws Exception {
    assertOnlyBrownFox(brown);
    dumpSpans(brown);
  }

  @Test
  public void testTheTermQuery() throws Exception {
    dumpSpans(the);
  }

  @Test
  public void testSpanFirstQuery() throws Exception {
    SpanFirstQuery sfq = new SpanFirstQuery(brown, 2);
    assertNoMatches(sfq);

    dumpSpans(sfq);

    sfq = new SpanFirstQuery(brown, 3);
    dumpSpans(sfq);
    assertOnlyBrownFox(sfq);

    sfq = new SpanFirstQuery(brown, 4);
    dumpSpans(sfq);
    assertOnlyBrownFox(sfq);
  }

  @Test
  public void testSpanNearQuery() throws Exception {
    SpanQuery[] quick_brown_dog = new SpanQuery[]{quick, brown, dog};
    SpanNearQuery snq = new SpanNearQuery(quick_brown_dog, 0, true);// ???
    assertNoMatches(snq);
    dumpSpans(snq);

    snq = new SpanNearQuery(quick_brown_dog, 4, true);              // ???
    assertNoMatches(snq);
    dumpSpans(snq);

    snq = new SpanNearQuery(quick_brown_dog, 5, true);              // ???
    assertOnlyBrownFox(snq);
    dumpSpans(snq);

    // interesting - even a sloppy phrase query would require
    // more slop to match
    snq = new SpanNearQuery(new SpanQuery[]{lazy, fox}, 3, false); // ???
    assertOnlyBrownFox(snq);
    dumpSpans(snq);

    PhraseQuery.Builder builder = new PhraseQuery.Builder();
    builder.setSlop(4);

    builder.add(new Term("f", "lazy"));                                // ???
    builder.add(new Term("f", "fox"));                                 // ???
    PhraseQuery pq = builder.build();                                          // ???
    assertNoMatches(pq);

    builder.setSlop(5);                                                // ???
    pq = builder.build();
    assertOnlyBrownFox(pq);                                            // ???
  }

/*
    ??? ????????????????????? SpanNearQuery ?????????slop ??? 0?????????????????????????????????????????????????????????????????????????????????????????????
    ??? ????????????????????????slop ??? 4????????? 4 ???????????????????????????????????????????????????
    ??? ?????????????????????????????????slop ??? 5????????? 5 ????????????????????????SpanNearQuery ?????????????????????
    ??? ????????? SpanTermQuery ????????????????????????????????????inOrder ???????????? false???????????? slop ??????????????? 3
    ??? ??????????????? PhraseQuery ?????????slop ?????? 4??????????????????
    ??? ???????????? PhraseQuery, slop ?????? 5?????????????????????
  */

  @Test
  public void testSpanNotQuery() throws Exception {
    SpanNearQuery quick_fox = new SpanNearQuery(new SpanQuery[]{quick, fox}, 1, true);
    assertBothFoxes(quick_fox);
    dumpSpans(quick_fox);

    SpanNotQuery quick_fox_dog = new SpanNotQuery(quick_fox, dog);
    assertBothFoxes(quick_fox_dog);
    dumpSpans(quick_fox_dog);

    SpanNotQuery no_quick_red_fox = new SpanNotQuery(quick_fox, red);
    assertOnlyBrownFox(no_quick_red_fox);
    dumpSpans(no_quick_red_fox);
  }

  @Test
  public void testSpanNotQueryAdv() throws Exception {
    SpanNearQuery quick_fox = new SpanNearQuery(new SpanQuery[]{quick, fox}, 1, true);
    SpanNotQuery no_quick_red_fox = new SpanNotQuery(quick_fox, red, 1);
    assertOnlyBrownFox(no_quick_red_fox);
    dumpSpans(no_quick_red_fox);
  }

  @Test
  public void testSpanQueryFilter() throws Exception {
    SpanQuery[] quick_brown_dog = new SpanQuery[]{quick, brown, dog};
    SpanQuery snq = new SpanNearQuery(quick_brown_dog, 5, true);
    ConstantScoreQuery filter = new ConstantScoreQuery(snq);

    Query query = new MatchAllDocsQuery();

    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    builder.add(query, BooleanClause.Occur.MUST);
    builder.add(filter, BooleanClause.Occur.MUST);
    BooleanQuery booleanQuery = builder.build();

    TopDocs topDocs = searcher.search(booleanQuery,10);

    assertEquals(1, topDocs.totalHits.value);
    assertEquals("wrong doc", 0, topDocs.scoreDocs[0].doc);
  }

  @Test
  public void testSpanOrQuery() throws Exception {
    SpanNearQuery quick_fox =
        new SpanNearQuery(new SpanQuery[]{quick, fox}, 1, true);

    SpanNearQuery lazy_dog =
        new SpanNearQuery(new SpanQuery[]{lazy, dog}, 0, true);

    SpanNearQuery sleepy_cat =
        new SpanNearQuery(new SpanQuery[]{sleepy, cat}, 0, true);

    SpanNearQuery qf_near_ld =
        new SpanNearQuery(
            new SpanQuery[]{quick_fox, lazy_dog}, 3, true);
    assertOnlyBrownFox(qf_near_ld);
    dumpSpans(qf_near_ld);

    SpanNearQuery qf_near_sc =
        new SpanNearQuery(
            new SpanQuery[]{quick_fox, sleepy_cat}, 3, true);
    dumpSpans(qf_near_sc);

    SpanOrQuery or = new SpanOrQuery(
        new SpanQuery[]{qf_near_ld, qf_near_sc});
    assertBothFoxes(or);
    dumpSpans(or);
  }

  @Test
  public void testPlay() throws Exception {
    SpanOrQuery or = new SpanOrQuery(new SpanQuery[]{quick, fox});
    dumpSpans(or);

    SpanNearQuery quick_fox =
        new SpanNearQuery(new SpanQuery[]{quick, fox}, 1, true);
    SpanFirstQuery sfq = new SpanFirstQuery(quick_fox, 4);
    dumpSpans(sfq);

    dumpSpans(new SpanTermQuery(new Term("f", "the")));

    SpanNearQuery quick_brown =
        new SpanNearQuery(new SpanQuery[]{quick, brown}, 0, false);
    dumpSpans(quick_brown);
  }

  private void dumpSpans(SpanQuery query) throws IOException {
    System.out.println(query + ":");

    SpanWeight spanWeight = query.createWeight(searcher, ScoreMode.COMPLETE, 0.0f);
    Spans spans = spanWeight.getSpans(reader.getContext().leaves().get(0),
            SpanWeight.Postings.POSITIONS);

    int numSpans = 0;

    TopDocs hits = searcher.search(query, 10);
    float[] scores = new float[2];
    for (ScoreDoc sd : hits.scoreDocs) {
      scores[sd.doc] = sd.score;
    }

    int id;
    while ((id = spans.nextDoc()) != Spans.NO_MORE_DOCS) {                 // ???

      Document doc = reader.document(id);                                  // ???
      numSpans++;

      while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS){

        TokenStream stream = analyzer.tokenStream("contents",     // ???
                new StringReader(doc.get("f")));

        CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
        stream.reset();

        int startPos = spans.startPosition();
        int endPos = spans.endPosition();
        System.out.println("[start position, end position]: [" + startPos + ", " + endPos + "]");

        StringBuilder buffer = new StringBuilder();
        buffer.append("   ");

        int i = 0;
        while (stream.incrementToken()) {     // ???
          if (i == startPos) {                // ???
            buffer.append("<");               // ???
          }                                   // ???
          buffer.append(term.toString());     // ???
          if (i + 1 == endPos) {              // ???
            buffer.append(">");               // ???
          }

          buffer.append(" ");
          i++;
        }
        stream.end();
        stream.close();

        buffer.append("(").append(scores[id]).append(") ");
        System.out.println(buffer);
      }
    }
    if (numSpans == 0) {
      System.out.println("   No spans");
    }
    else {
      System.out.println("numSpans: " + numSpans);
    }
    System.out.println();
  }
/*
   ??? ??????????????? span
   ??? ???????????????????????????
   ??? ??????????????????
   ??? ??????????????????
   ??? ??? span ???????????? < ??? >
*/
}

