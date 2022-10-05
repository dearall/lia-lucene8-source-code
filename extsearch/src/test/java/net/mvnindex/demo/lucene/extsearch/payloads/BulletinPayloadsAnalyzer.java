package net.mvnindex.demo.lucene.extsearch.payloads;

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
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

import java.io.Reader;

// From chapter 6
public class BulletinPayloadsAnalyzer extends StopwordAnalyzerBase {
  private boolean isBulletin;
  private float boost;
  BulletinPayloadsFilter payloadTokenStream;

  public BulletinPayloadsAnalyzer(float boost) {
    this.boost = boost;
  }

  void setIsBulletin(boolean v) {
    isBulletin = v;
    if(payloadTokenStream != null) {
      payloadTokenStream.setIsBulletin(isBulletin);
    }
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    LetterTokenizer src = new LetterTokenizer();

    TokenStream tokenStream = new LowerCaseFilter(src);
    tokenStream = new StopFilter(tokenStream, stopwords);
    payloadTokenStream = new BulletinPayloadsFilter(tokenStream, boost);
    payloadTokenStream.setIsBulletin(isBulletin);

    return new TokenStreamComponents(r -> {
      src.setReader(r);
    }, payloadTokenStream);
  }
}
