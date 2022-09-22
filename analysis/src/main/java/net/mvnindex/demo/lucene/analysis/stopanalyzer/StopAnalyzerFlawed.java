package net.mvnindex.demo.lucene.analysis.stopanalyzer;

/**
 * Copyright Manning Publications Co.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific lan      
*/

import net.mvnindex.demo.lucene.analysis.AnalyzerUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.junit.Test;

import java.io.Reader;
import java.util.Arrays;
import java.util.List;

// From chapter 4

/**
 * Stop words actually not necessarily removed due to filtering order
 */
public class StopAnalyzerFlawed extends StopwordAnalyzerBase {
  public StopAnalyzerFlawed() {
    super(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
  }

  public StopAnalyzerFlawed(String[] stopWords) {
    super(StopFilter.makeStopSet(stopWords));
  }

  /**
   * Ordering mistake here
   */
  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    LetterTokenizer src = new LetterTokenizer();

    TokenStream tokenStream = new StopFilter(src, stopwords);
    tokenStream = new LowerCaseFilter(tokenStream);

    return new TokenStreamComponents(r -> {
      src.setReader(r);
    }, tokenStream);
  }

  /**
   * Illustrates that "the" is not removed, although it is lowercased
   */

  public static void main(String[] args) throws Exception {
    AnalyzerUtils.displayTokens(
            new StopAnalyzerFlawed(), "The quick brown...");
  }
}
