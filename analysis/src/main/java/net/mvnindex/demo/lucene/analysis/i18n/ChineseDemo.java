package net.mvnindex.demo.lucene.analysis.i18n;

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
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;


import java.awt.*;
import java.io.IOException;
import java.io.StringReader;

// From chapter 4
public class ChineseDemo {
  private static String[] strings = {"道德經"};  // ①

  private static Analyzer[] analyzers = {       // ②
    new SimpleAnalyzer(),
    new StandardAnalyzer(),
    new CJKAnalyzer (),
    new SmartChineseAnalyzer()
  };

  public static void main(String args[]) throws Exception {

    for (String string : strings) {
      for (Analyzer analyzer : analyzers) {
        analyze(string, analyzer);
      }
    }
  }

  private static void analyze(String string, Analyzer analyzer)
         throws IOException {
    StringBuffer buffer = new StringBuffer();

    TokenStream stream = analyzer.tokenStream("contents", new StringReader(string));
    CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);

    stream.reset();
    while(stream.incrementToken()) {   // ③
      buffer.append("[");
      buffer.append(term.toString());
      buffer.append("] ");
    }
    stream.end();
    stream.close();

    String output = buffer.toString();

    System.out.println(analyzer.getClass().getSimpleName() + " : " + string);
    System.out.println(output);         // ④
    System.out.println("-------------");

  }

  private static int getWidth(FontMetrics metrics, String s) {
    int size = 0;
    int length = s.length();
		for (int i = 0; i < length; i++) {
      size += metrics.charWidth(s.charAt(i));
    }
    size = size + 50;

    return size;
  }
}

/* 	
① 分析这段中文文本
② 测试这 4 个分析器
③ 检索分析出来的词元
④ 显示分析结果
*/
