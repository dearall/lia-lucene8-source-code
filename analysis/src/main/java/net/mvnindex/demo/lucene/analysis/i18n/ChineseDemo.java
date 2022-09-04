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
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;


import java.awt.*;
import java.io.IOException;
import java.io.StringReader;

// From chapter 4
public class ChineseDemo {
  private static String[] strings = {"道德經"};  //A

  private static Analyzer[] analyzers = {
    new SimpleAnalyzer(),
    new StandardAnalyzer(),
    new CJKAnalyzer (),
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
    while(stream.incrementToken()) {   //C
      buffer.append("[");
      buffer.append(term.toString());
      buffer.append("] ");
    }
    stream.end();
    stream.close();

    String output = buffer.toString();

    System.out.println(analyzer.getClass().getSimpleName() + " : " + string);
    System.out.println(output);
    System.out.println("-------------");

/*
    Frame f = new Frame();
    f.setTitle(analyzer.getClass().getSimpleName() + " : " + string);
    f.setResizable(true);

    Font font = new Font(null, Font.PLAIN, 36);
    int width = getWidth(f.getFontMetrics(font), output);

    f.setSize((width < 250) ? 350 : width + 100, 100);

    // NOTE: if Label doesn't render the Chinese characters
    // properly, try using javax.swing.JLabel instead
    Label label = new Label(output);   //D
 //   label.setSize(width, 75);
    label.setAlignment(Label.CENTER);
    label.setFont(font);
    f.add(label);

    f.setVisible(true);*/
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
#A Analyze this text
#B Test these analyzers
#C Retrieve tokens
#D Display analysis
*/
