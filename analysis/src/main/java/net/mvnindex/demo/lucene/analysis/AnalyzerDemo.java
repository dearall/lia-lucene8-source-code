package net.mvnindex.demo.lucene.analysis;

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

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// From chapter 4

/**
 * Adapted from code which first appeared in a java.net article
 * written by Erik
 */
public class AnalyzerDemo {
  public static final CharArraySet ENGLISH_STOP_WORDS_SET;
  private static final String[] examples = {
    "The quick brown fox jumped over the lazy dog",
    "XY&Z Corporation - xyz@example.com"
  };

  static {
    List<String> stopWords = Arrays.asList("a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there", "these", "they", "this", "to", "was", "will", "with");
    CharArraySet stopSet = new CharArraySet(stopWords.size(), false);
    stopSet.addAll(stopWords);
    ENGLISH_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
  }

  private static final Analyzer[] analyzers = new Analyzer[] { 
    new WhitespaceAnalyzer(),
    new SimpleAnalyzer(),
    new StopAnalyzer(ENGLISH_STOP_WORDS_SET),
    new StandardAnalyzer()
  };

  public static void main(String[] args) throws IOException {

    String[] strings = examples;
    if (args.length > 0) {    // A
      strings = args;
    }

    for (String text : strings) {
      analyze(text);
    }
  }

  private static void analyze(String text) throws IOException {
    System.out.println("Analyzing \"" + text + "\"");
    for (Analyzer analyzer : analyzers) {
      String name = analyzer.getClass().getSimpleName();
      System.out.println("  " + name + ":");
      System.out.print("    ");
      AnalyzerUtils.displayTokens(analyzer, text); // B
      System.out.println("\n");
    }
  }
}

// #A Analyze command-line strings, if specified
// #B Real work done in here
