package net.mvnindex.demo.lucene.tools;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.util.Version;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FlexibleQueryParserTest {

    @Test
    public void testSimple() throws Exception {
        Analyzer analyzer = new StandardAnalyzer();
        StandardQueryParser parser = new StandardQueryParser(analyzer);
        Query q = null;
        try {
            q = parser.parse("(agile OR extreme) AND methodology", "subject");
        } catch (QueryNodeException exc) {
            // TODO: handle exc
        }
        System.out.println("parsed: " + q);
    }

    @Test
    public void testPhraseQuery() throws Exception {
        Analyzer analyzer = new StandardAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
        StandardQueryParser parser = new CustomFlexibleQueryParser(analyzer);

        Query query = parser.parse("singleTerm", "subject");
        assertTrue("TermQuery", query instanceof TermQuery);

        query = parser.parse("\"a phrase test\"", "subject");
        System.out.println("got query=" + query);
        assertTrue("SpanNearQuery", query instanceof SpanNearQuery);
    }

    @Test
    public void testNoFuzzyOrWildcard() throws Exception {
        Analyzer analyzer = new StandardAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
        StandardQueryParser parser = new CustomFlexibleQueryParser(analyzer);
        try {
            parser.parse("agil*", "subject");
            fail("didn't hit expected exception");
        } catch (QueryNodeException exc) {
            // expected
        }

        try {
            parser.parse("agil~0.8", "subject");
            fail("didn't hit expected exception");
        } catch (QueryNodeException exc) {
            // expected
        }
    }
}
