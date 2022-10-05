package net.mvnindex.demo.lucene.extsearch.collector;


import org.apache.lucene.index.*;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// From chapter 6
public class BookLinkCollector implements Collector, LeafCollector {
  private int docBase;
  private Scorable scorer;

  private Map<String,String> documents = new HashMap<String,String>();
  private BinaryDocValues urls;
  private SortedDocValues titles;

  public BookLinkCollector(){}


  @Override
  public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
    doSetNextReader(context);

    return this;
  }

  /**
   * This method is called before collecting <code>context</code>.
   */
  protected void doSetNextReader(LeafReaderContext context) throws IOException {
    this.docBase = context.docBase;
    LeafReader reader = context.reader();

    urls = DocValues.getBinary(reader, "url");            // ①
    titles =  DocValues.getSorted(reader, "title2");      // ①
  }

  @Override
  public ScoreMode scoreMode() {
    return ScoreMode.COMPLETE_NO_SCORES;
  }

  @Override
  public void setScorer(Scorable scorer) throws IOException {
    this.scorer = scorer;
  }

  public void collect(int docID) throws IOException {
    String url = "";
    String title = "";
    try {
      if(urls.advanceExact(docID)) {
        url = urls.binaryValue().utf8ToString();
      }
      if(titles.advanceExact(docID)) {
        title = titles.binaryValue().utf8ToString();
      }

    }catch (IOException e){
      // ignore
    }
    documents.put(url, title);                          // ②
    System.out.println(title + ":" + scorer.score());
  }

  public Map<String,String> getLinks() {
    return Collections.unmodifiableMap(documents);
  }
}

/*
  ① 载入 DocValues
  ② 将匹配的 ur, title 存入 map
*/
