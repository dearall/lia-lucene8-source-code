package net.mvnindex.demo.lucene.tools;

import net.mvnindex.demo.lucene.common.TestUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;

public class HighlightTest {
    @Test
    public void testHighlighting() throws Exception {
        String text = "The quick brown fox jumps over the lazy dog";
        TermQuery query = new TermQuery(new Term("field", "fox"));

        SimpleAnalyzer analyzer = new SimpleAnalyzer();
        TokenStream tokenStream = analyzer.tokenStream("field", new StringReader(text));

        QueryScorer scorer = new QueryScorer(query, "field");
        scorer.setWrapIfNotCachingTokenFilter(true);
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
        Highlighter highlighter = new Highlighter(scorer);
        highlighter.setTextFragmenter(fragmenter);
//        assertEquals("The quick brown <B>fox</B> jumps over the lazy dog",
//                highlighter.getBestFragment(tokenStream, text));

        System.out.println("BestFragment: " + highlighter.getBestFragment(tokenStream, text));
    }

    @Test
    public void testHits() throws Exception {
        Directory directory = TestUtil.getBookIndexDirectory();
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        TermQuery query = new TermQuery(new Term("title", "action"));
        TopDocs hits = searcher.search(query, 10);

        QueryScorer scorer = new QueryScorer(query, "title");
        Highlighter highlighter = new Highlighter(scorer);
        highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer));

        for (ScoreDoc sd : hits.scoreDocs) {
            Document doc = searcher.doc(sd.doc);
            String title = doc.get("title");

            TokenStream stream = TokenSources.getTermVectorTokenStreamOrNull(
                    "title",
                    reader.getTermVectors(sd.doc),
                    -1);
            String fragment = highlighter.getBestFragment(stream, title);

            System.out.println(fragment);
        }

        reader.close();
        directory.close();
    }
}
