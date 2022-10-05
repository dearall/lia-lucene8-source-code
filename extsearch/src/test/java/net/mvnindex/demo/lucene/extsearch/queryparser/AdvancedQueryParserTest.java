package net.mvnindex.demo.lucene.extsearch.queryparser;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// From chapter 6
public class AdvancedQueryParserTest {
  private Analyzer analyzer = new WhitespaceAnalyzer();

  @Test
  public void testCustomQueryParser() {
    CustomQueryParser parser = new CustomQueryParser("field", analyzer);
    try {
      parser.parse("a?t");
      fail("Wildcard queries should not be allowed");
    } catch (ParseException expected) {

    }

    try {
      parser.parse("xunit~");
      fail("Fuzzy queries should not be allowed");
    } catch (ParseException expected) {

    }
  }

  @Test
  public void testPhraseQuery() throws Exception {
    CustomQueryParser parser = new CustomQueryParser("field", analyzer);

    Query query = parser.parse("singleTerm");
    assertTrue("TermQuery", query instanceof TermQuery);

    query = parser.parse("\"a phrase\"");
    assertTrue("SpanNearQuery", query instanceof SpanNearQuery);
  }
}
