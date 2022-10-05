package net.mvnindex.demo.lucene.extsearch.collector;

import net.mvnindex.demo.lucene.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

// From chapter 6
public class CollectorTest {

  @Test
  public void testCollecting() throws Exception {
    Directory directory = TestUtil.getBookIndexDirectory();
    TermQuery query = new TermQuery(new Term("contents", "junit"));
    DirectoryReader reader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(reader);

    BookLinkCollector collector = new BookLinkCollector();
    searcher.search(query, collector);

    Map<String,String> linkMap = collector.getLinks();
    assertEquals("ant in action",
                 linkMap.get("http://www.manning.com/loughran"));

    TopDocs hits = searcher.search(query, 10);
    System.out.println("---------------------------");
    TestUtil.dumpHits(searcher, hits);

    reader.close();
    directory.close();
  }
}
