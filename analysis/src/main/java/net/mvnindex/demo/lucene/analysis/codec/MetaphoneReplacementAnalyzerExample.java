package net.mvnindex.demo.lucene.analysis.codec;

import net.mvnindex.demo.lucene.analysis.AnalyzerUtils;

import java.io.IOException;

public class MetaphoneReplacementAnalyzerExample {
    public static void main(String[] args) throws IOException {
        MetaphoneReplacementAnalyzer analyzer =
                new MetaphoneReplacementAnalyzer();
        AnalyzerUtils.displayTokens(analyzer,
                "The quick brown fox jumped over the lazy dog");

        System.out.println("");
        AnalyzerUtils.displayTokens(analyzer,
                "Tha quik brown phox jumpd ovvar tha lazi dag");
    }
}
