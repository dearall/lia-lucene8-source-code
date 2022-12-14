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

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

// From chapter 4
public class StopAnalyzer2 extends StopwordAnalyzerBase {

  public StopAnalyzer2() {
    super(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
  }

  public StopAnalyzer2(String[] stopWords) {
    super(StopFilter.makeStopSet(stopWords));
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    LetterTokenizer src = new LetterTokenizer();
    TokenStream tokenStream = new LowerCaseFilter(src);
    tokenStream = new StopFilter(tokenStream, stopwords);

    return new TokenStreamComponents(r -> {
      src.setReader(r);
    }, tokenStream);
  }
}
