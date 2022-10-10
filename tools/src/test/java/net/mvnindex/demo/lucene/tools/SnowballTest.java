package net.mvnindex.demo.lucene.tools;

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
import org.junit.Test;

// From chapter 8
public class SnowballTest{

  @Test
  public void testEnglish() throws Exception {
    Analyzer analyzer = new EnglishSnowballAnalyzer();
    AnalyzerUtils.assertAnalyzesTo(analyzer, "stemming algorithms", new String[] {"stem", "algorithm"});
  }
}
