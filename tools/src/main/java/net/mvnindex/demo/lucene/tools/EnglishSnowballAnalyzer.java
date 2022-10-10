package net.mvnindex.demo.lucene.tools;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class EnglishSnowballAnalyzer extends StopwordAnalyzerBase {
    public EnglishSnowballAnalyzer() {
        this(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
    }
    public EnglishSnowballAnalyzer(CharArraySet stopwords) {
        super(stopwords);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new StandardTokenizer();
        TokenStream result = new EnglishPossessiveFilter(source);
        result = new LowerCaseFilter(result);
        result = new StopFilter(result, stopwords);

        result = new SnowballFilter(result, "English");

        return new TokenStreamComponents(source, result);
    }
}
