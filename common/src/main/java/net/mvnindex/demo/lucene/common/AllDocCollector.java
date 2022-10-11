package net.mvnindex.demo.lucene.common;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// From chapter 6

/**
 * Gathers all documents from a search.
 */

public class AllDocCollector implements Collector, LeafCollector {
  List<ScoreDoc> docs = new ArrayList<>();
  private Scorable scorer;
  private int docBase;

  @Override
  public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
    this.docBase = context.docBase;
    return this;
  }

  @Override
  public ScoreMode scoreMode() {
    return ScoreMode.COMPLETE;
  }

  @Override
  public void setScorer(Scorable scorer) throws IOException {
    this.scorer = scorer;
  }

  public void collect(int doc) throws IOException {
    docs.add(
            new ScoreDoc(doc + docBase,       // ①
                     scorer.score()));            // ②
  }

  public void reset() {
    docs.clear();
  }

  public List<ScoreDoc> getHits() {
    return docs;
  }
}

/*
  ① 变基为全局（绝对）docID
  ② 记录评分值
*/
