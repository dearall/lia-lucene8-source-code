package net.mvnindex.demo.lucene.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/**
 * Debug Analysis Process.
 *
 * @author NiYanchun
 **/
public class AnalysisDebug {

    public static void main(String[] args) throws Exception {
        Analyzer analyzer = new SimpleAnalyzer();
//        Analyzer analyzer = new StandardAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
        String contents = "The quick brown fox jumped over the lazy dog";
//        String contents = "I'll email you at xyz@example.com";
        try (TokenStream tokenStream = analyzer.tokenStream("contents", contents)) {
            tokenStream.reset();

            while (tokenStream.incrementToken()) {
                System.out.println("token: " + tokenStream.reflectAsString(false));
            }
            tokenStream.end();
        }
    }
}