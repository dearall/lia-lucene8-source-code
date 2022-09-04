package net.mvnindex.demo.lucene.analysis.synonym;

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
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.util.Arrays;
import java.util.List;

// From chapter 4
public class SynonymAnalyzer extends Analyzer {
  private SynonymEngine engine;

  public static final CharArraySet ENGLISH_STOP_WORDS_SET;
  static {
    List<String> stopWords = Arrays.asList("a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there", "these", "they", "this", "to", "was", "will", "with");
    CharArraySet stopSet = new CharArraySet(stopWords.size(), false);
    stopSet.addAll(stopWords);
    ENGLISH_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
  }

  public SynonymAnalyzer(SynonymEngine engine) {
    this.engine = engine;
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    StandardTokenizer source = new StandardTokenizer();

    TokenStream tokenStream = new LowerCaseFilter(source);
    tokenStream = new StopFilter(tokenStream, ENGLISH_STOP_WORDS_SET);
    tokenStream = new SynonymFilter(tokenStream, engine);

    return new TokenStreamComponents(r -> {
      source.setReader(r);
    }, tokenStream);
  }
}
