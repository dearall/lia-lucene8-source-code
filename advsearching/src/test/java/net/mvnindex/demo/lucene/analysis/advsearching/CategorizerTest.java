package net.mvnindex.demo.lucene.analysis.advsearching;

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

import net.mvnindex.demo.lucene.common.TestUtil;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.store.Directory;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

// From chapter 5
public class CategorizerTest {
  Map<String, Map<String,Integer>> categoryMap;

  @Before
  public void setUp() throws Exception {
    categoryMap = new TreeMap<>();
    buildCategoryVectors();
    dumpCategoryVectors();
  }

  @Test
  public void testCategorization() throws Exception {
    assertEquals("/technology/computers/programming/methodology",
        getCategory("extreme agile methodology"));
    assertEquals("/education/pedagogy",
        getCategory("montessori education philosophy"));
  }

  private void dumpCategoryVectors() {
    for(Map.Entry<String, Map<String, Integer>> entry : categoryMap.entrySet()) {
      String category = entry.getKey();
      System.out.println("Category: " + category);

      Map<String, Integer> vectorMap = categoryMap.get(category);
      for(Map.Entry<String,Integer> termfreqentry : vectorMap.entrySet()){
        System.out.println("    " + termfreqentry.getKey() + " = " + termfreqentry.getValue());
      }
    }
  }

  private void buildCategoryVectors() throws IOException {
    Directory directory = TestUtil.getBookIndexDirectory();
    IndexReader reader = DirectoryReader.open(directory);

    int maxDoc = reader.maxDoc();

    for (int i = 0; i < maxDoc; i++) {
      Document doc = reader.document(i);
      String category = doc.get("category");

      Map<String, Integer> vectorMap = categoryMap.get(category);
      if (vectorMap == null) {
        vectorMap = new TreeMap<>();
        categoryMap.put(category, vectorMap);
      }

      Terms terms = reader.getTermVector(i, "subject");
      addTermFreqToMap(vectorMap, terms);
    }

    reader.close();
    directory.close();
  }

  private void addTermFreqToMap(Map<String, Integer> vectorMap, Terms terms) throws IOException {
    TermsEnum termsEnum = terms.iterator();
    PostingsEnum postingsEnum = null;
    while (termsEnum.next() != null) {
      String term = termsEnum.term().utf8ToString();
      postingsEnum = termsEnum.postings(postingsEnum);

      int doc = 0;
      int freq = 0;
      while ((doc = postingsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS){
        freq += postingsEnum.freq();
        System.out.println("doc: " + doc +", freq: "+ freq);
      }

      System.out.println("term: "+ term+ ", freq: "+ freq);

      if (vectorMap.containsKey(term)) {
        Integer value = vectorMap.get(term);
        vectorMap.put(term, (value + freq));
      } else {
        vectorMap.put(term, freq);
      }
    }
    System.out.println();
  }


  private String getCategory(String subject) {
    String[] words = subject.split(" ");
    double bestAngle = Double.MAX_VALUE;
    String bestCategory = null;

    for(Map.Entry<String, Map<String, Integer>> entry : categoryMap.entrySet()){
      String category = entry.getKey();
      System.out.println(category);

      double angle = computeAngle(words, category);
      System.out.println(" -> angle = " + angle + " (" + Math.toDegrees(angle) + ")");

      if (angle < bestAngle) {
        bestAngle = angle;
        bestCategory = category;
      }
    }

    return bestCategory;
  }

  private double computeAngle(String[] words, String category) {
    Map<String, Integer> vectorMap = categoryMap.get(category);

    int dotProduct = 0;
    int sumOfSquares = 0;
    for (String word : words) {
      int categoryWordFreq = 0;

      if (vectorMap.containsKey(word)) {
        categoryWordFreq = vectorMap.get(word);
      }

      dotProduct += categoryWordFreq;  // ①
      sumOfSquares += categoryWordFreq * categoryWordFreq;
    }


    double denominator;
    if (sumOfSquares == words.length) {
      denominator = sumOfSquares;     // ②
    } else {
      denominator = Math.sqrt(sumOfSquares) *
                    Math.sqrt(words.length);
    }

    double ratio = dotProduct / denominator;

    return Math.acos(ratio);
  }
  /*
    ① 计算是经过简化的，假设 words 数组里每个单词的频率为 1
    ② 域数值 N 的平方根乘以 N 的平方根得到 N。程序的这种简便方法避免了比值大于 1 的情况（这在余弦函数中是非法值），从而解决精度问题。
  */
}


