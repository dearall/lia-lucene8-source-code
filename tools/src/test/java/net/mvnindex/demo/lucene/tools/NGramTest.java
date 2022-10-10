package net.mvnindex.demo.lucene.tools;

import net.mvnindex.demo.lucene.analysis.AnalyzerUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.junit.Test;

import java.io.IOException;


public class NGramTest {
    private static class NGramAnalyzer extends Analyzer {
        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            final Tokenizer source = new NGramTokenizer(2, 4);
            TokenStream result = new LowerCaseFilter(source);

            return new TokenStreamComponents(source, result);
        }
    }

    private static class NGramAnalyzer2 extends Analyzer {

        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            final Tokenizer source = new StandardTokenizer();
            TokenStream result = new LowerCaseFilter(source);
            result = new NGramTokenFilter(source, 2, 4, true);

            return new TokenStreamComponents(source, result);
        }
    }

    @Test
    public void testNGramTokenizer() throws IOException {
        AnalyzerUtils.displayTokensWithFullDetails(new NGramAnalyzer(), "lettuce");
    }

    @Test
    public void testNGramTokenFilter() throws IOException {
        AnalyzerUtils.displayTokensWithFullDetails(new NGramAnalyzer2(), "lettuce");
    }
}
