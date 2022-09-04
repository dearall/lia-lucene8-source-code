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

import junit.framework.TestCase;
import net.mvnindex.demo.lucene.analysis.AnalyzerUtils;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

// From chapter 4
public class StopAnalyzerTest extends TestCase {
  public static final CharArraySet ENGLISH_STOP_WORDS_SET;
  static {
    List<String> stopWords = Arrays.asList("a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there", "these", "they", "this", "to", "was", "will", "with");
    CharArraySet stopSet = new CharArraySet(stopWords.size(), false);
    stopSet.addAll(stopWords);
    ENGLISH_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
  }

  private StopAnalyzer stopAnalyzer = new StopAnalyzer(ENGLISH_STOP_WORDS_SET);

  @Test
  public void testHoles() throws Exception {
    String[] expected = { "one", "enough"};

    AnalyzerUtils.assertAnalyzesTo(stopAnalyzer,
                                   "one is not enough",
                                   expected);
    AnalyzerUtils.assertAnalyzesTo(stopAnalyzer,
                                   "one is enough",
                                   expected);
    AnalyzerUtils.assertAnalyzesTo(stopAnalyzer,
                                   "one enough",
                                   expected);
    AnalyzerUtils.assertAnalyzesTo(stopAnalyzer,
                                   "one but not enough",
                                   expected);
  }
}
