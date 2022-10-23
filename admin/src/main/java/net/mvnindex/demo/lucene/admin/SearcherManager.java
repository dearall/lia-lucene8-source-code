package net.mvnindex.demo.lucene.admin;


import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

import java.io.IOException;

// From chapter 11

/** Utility class to get/refresh searchers when you are
 *  using multiple threads. */

public class SearcherManager {

  private IndexSearcher currentSearcher;                            // ①
  private IndexWriter writer;

  public SearcherManager(Directory dir) throws IOException {
    currentSearcher = new IndexSearcher(DirectoryReader.open(dir));  //②
    warm(currentSearcher);
  }

  public SearcherManager(IndexWriter writer) throws IOException {
    this.writer = writer;
    currentSearcher = new IndexSearcher(DirectoryReader.open(writer));//③
    warm(currentSearcher);

    writer.getConfig().setMergedSegmentWarmer(                        // ③
        new IndexWriter.IndexReaderWarmer() {
          public void warm(LeafReader reader) throws IOException {
            SearcherManager.this.warm(new IndexSearcher(reader));
          }
        });
  }

  public void warm(IndexSearcher searcher)    // ④
    throws IOException                        // ④
  {}                                          // ④

  private boolean reopening;

  private synchronized void startReopen()
    throws InterruptedException {
    while (reopening) {
      wait();
    }
    reopening = true;
  }

  private synchronized void doneReopen() {
    reopening = false;
    notifyAll();
  }

  public void maybeReopen()                      //⑤
    throws InterruptedException,                 //⑤
           IOException {                         //⑤

    startReopen();

    try {
      final IndexSearcher searcher = get();
      try {
        IndexReader newReader = DirectoryReader.openIfChanged(
                (DirectoryReader) currentSearcher.getIndexReader());

        if (newReader != null && newReader != currentSearcher.getIndexReader()) {
          IndexSearcher newSearcher = new IndexSearcher(newReader);
          if (writer == null) {
            warm(newSearcher);
          }
          swapSearcher(newSearcher);
        }
      } finally {
        release(searcher);
      }
    } finally {
      doneReopen();
    }
  }

  public synchronized IndexSearcher get() {                      //⑥
    currentSearcher.getIndexReader().incRef();
    return currentSearcher;
  }    

  public synchronized void release(IndexSearcher searcher)       //⑦
    throws IOException {
    searcher.getIndexReader().decRef();
  }

  private synchronized void swapSearcher(IndexSearcher newSearcher)
    throws IOException {
    release(currentSearcher);
    currentSearcher = newSearcher;
  }

  public void close() throws IOException {
    swapSearcher(null);
  }
}

/*
① 当前 IndexSearcher
② 从 Directory 创建 searcher 实例
③ 从近实时的 reader 创建 searcher 实例
④ 在子类中实现
⑤ 重新打开 searcher
⑥ 获取当前的 searcher
⑦ 释放 searcher
*/
