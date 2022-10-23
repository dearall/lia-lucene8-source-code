package net.mvnindex.demo.lucene.admin;


import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// From chapter 11

/** Drop-in replacement for IndexWriter that uses multiple
 *  threads, under the hood, to index added documents. */

public class ThreadedIndexWriter extends IndexWriter {

  private ExecutorService threadPool;

  private class Job implements Runnable {                       //①
    Document doc;
    Term delTerm;
    public Job(Document doc, Term delTerm) {
      this.doc = doc;
      this.delTerm = delTerm;
    }
    public void run() {                                         //②
      try {
        if (delTerm != null) {
          ThreadedIndexWriter.super.updateDocument(delTerm, doc);
        } else {
          ThreadedIndexWriter.super.addDocument(doc);
        }
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
    }
  }

  public ThreadedIndexWriter(Directory dir, IndexWriterConfig config,
                             int numThreads, int maxQueueSize)
       throws CorruptIndexException, IOException {
    super(dir, config);

    threadPool = new ThreadPoolExecutor(                        //③
          numThreads, numThreads,
          0, TimeUnit.SECONDS,
          new ArrayBlockingQueue<Runnable>(maxQueueSize, false),
          new ThreadPoolExecutor.CallerRunsPolicy());
  }

  public void addDocument(Document doc) {                       //④
    threadPool.execute(new Job(doc, null));              //④
  }

  public void updateDocument(Term term, Document doc) {         //④
    threadPool.execute(new Job(doc, term));                     //④
  }

  public void close()
      throws CorruptIndexException, IOException {
    finish();
    super.close();
  }


  public void rollback()
      throws CorruptIndexException, IOException {
    finish();
    super.rollback();
  }

  private void finish() {                                       // ⑤
    threadPool.shutdown();
    while(true) {
      try {
        if (threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)) {
          break;
        }
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(ie);
      }
    }
  }
}

/*
① 持有一个被加入索引的文档
② 执行真正的添加和更新文档操作
③ 创建线程池
④ 让线程池执行工作
⑤ 结束线程池
*/
