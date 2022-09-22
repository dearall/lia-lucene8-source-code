package net.mvnindex.demo.lucene.common;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.io.IOException;
import java.io.Reader;

/**
 * 由于 StandardAnalyzer 被 final 修饰，不可继承，因此这里按 StandardAnalyzer 实现重新定义了一个完全一样的实现
 * 去除 final 的定义，以使其可以被子类化，以自定义方式重写某些方法实现，如 CreateTestIndex 的内部类
 * MyStandardAnalyzer 重写了 getPositionIncrementGap(String field) 实现，以适应 catchall 域索引
 */
public class ExtendableStandardAnalyzer extends StopwordAnalyzerBase {

    /** Default maximum allowed token length */
    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

    private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

    /** Builds an analyzer with the given stop words.
     * @param stopWords stop words */
    public ExtendableStandardAnalyzer(CharArraySet stopWords) {
        super(stopWords);
    }

    /** Builds an analyzer with no stop words.
     */
    public ExtendableStandardAnalyzer() {
        this(CharArraySet.EMPTY_SET);
    }

    /** Builds an analyzer with the stop words from the given reader.
     * @see WordlistLoader#getWordSet(Reader)
     * @param stopwords Reader to read stop words from */
    public ExtendableStandardAnalyzer(Reader stopwords) throws IOException {
        this(loadStopwordSet(stopwords));
    }

    /**
     * Set the max allowed token length.  Tokens larger than this will be chopped
     * up at this token length and emitted as multiple tokens.  If you need to
     * skip such large tokens, you could increase this max length, and then
     * use {@code LengthFilter} to remove long tokens.  The default is
     * {@link StandardAnalyzer#DEFAULT_MAX_TOKEN_LENGTH}.
     */
    public void setMaxTokenLength(int length) {
        maxTokenLength = length;
    }

    /** Returns the current maximum token length
     *
     *  @see #setMaxTokenLength */
    public int getMaxTokenLength() {
        return maxTokenLength;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {
        final StandardTokenizer src = new StandardTokenizer();
        src.setMaxTokenLength(maxTokenLength);
        TokenStream tok = new LowerCaseFilter(src);
        tok = new StopFilter(tok, stopwords);
        return new TokenStreamComponents(r -> {
            src.setMaxTokenLength(ExtendableStandardAnalyzer.this.maxTokenLength);
            src.setReader(r);
        }, tok);
    }

    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        return new LowerCaseFilter(in);
    }
}
