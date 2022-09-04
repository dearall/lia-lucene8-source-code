package net.mvnindex.demo.lucene.analysis.positional;

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
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;

import java.io.Reader;
import java.util.Set;

// From chapter 4
public class PositionalPorterStopAnalyzer extends Analyzer {
  private CharArraySet stopWords;

  public PositionalPorterStopAnalyzer() {
    this(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
  }

  public PositionalPorterStopAnalyzer(CharArraySet stopWords) {
    this.stopWords = stopWords;
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    LetterTokenizer source = new LetterTokenizer();
    TokenStream tokenStream = new LowerCaseFilter(source);
    tokenStream = new StopFilter(tokenStream, stopWords);
    tokenStream = new PorterStemFilter(tokenStream);

    return new TokenStreamComponents(
            r -> {source.setReader(r);},
            tokenStream);
  }
}
