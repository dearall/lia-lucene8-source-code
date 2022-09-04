package net.mvnindex.demo.lucene.analysis.codec;

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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;

import java.io.Reader;

// From chapter 4
public class MetaphoneReplacementAnalyzer extends Analyzer {
  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    LetterTokenizer src = new LetterTokenizer();
    TokenStream tokenStream = new MetaphoneReplacementFilter(src);

    return new TokenStreamComponents(r -> {
      src.setReader(r);
    }, tokenStream);
  }
}
