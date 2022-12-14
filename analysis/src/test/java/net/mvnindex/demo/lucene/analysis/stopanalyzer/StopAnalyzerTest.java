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
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

// From chapter 4
public class StopAnalyzerTest extends TestCase {
  private StopAnalyzer stopAnalyzer = new StopAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);

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
