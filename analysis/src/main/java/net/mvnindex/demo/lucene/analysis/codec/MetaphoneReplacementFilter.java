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

import org.apache.commons.codec.language.Metaphone;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;

// From chapter 4
public class MetaphoneReplacementFilter extends TokenFilter {
  public static final String METAPHONE = "metaphone";

  private Metaphone metaphoner = new Metaphone();
  private CharTermAttribute termAttr;
  private TypeAttribute typeAttr;

  public MetaphoneReplacementFilter(TokenStream input) {
    super(input);
    termAttr = addAttribute(CharTermAttribute.class);
    typeAttr = addAttribute(TypeAttribute.class);
  }

  public final boolean incrementToken() throws IOException {
    if (!input.incrementToken())                             // ①
      return false;

    String encoded = metaphoner.encode(termAttr.toString());  //②
    termAttr.setEmpty();
    termAttr.append(encoded);                                 //③
    typeAttr.setType(METAPHONE);                              //④

    return true;
  }
}

/*
① 转入下一个词元
② 转换到 Metaphone 编码
③ 使用编码后的文本覆盖原始的 CharTermAttribute 词元文本
④ 设置词元类型为 METAPHONE，即 "metaphone"
*/
