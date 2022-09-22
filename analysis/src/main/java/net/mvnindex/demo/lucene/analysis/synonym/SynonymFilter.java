package net.mvnindex.demo.lucene.analysis.synonym;

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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.Stack;

// From chapter 4
public class SynonymFilter extends TokenFilter {
  public static final String TOKEN_TYPE_SYNONYM = "SYNONYM";

  private Stack<String> synonymStack;
  private SynonymEngine engine;
  private State current;

  private final CharTermAttribute termAtt;
  private final PositionIncrementAttribute posIncrAtt;

  public SynonymFilter(TokenStream in, SynonymEngine engine) {
    super(in);
    synonymStack = new Stack<String>();                     // ①
    this.engine = engine;

    this.termAtt = addAttribute(CharTermAttribute.class);
    this.posIncrAtt = addAttribute(PositionIncrementAttribute.class);
  }

  public final boolean incrementToken() throws IOException {
    if (synonymStack.size() > 0) {                          // ②
      String syn = synonymStack.pop();                      // ②
      restoreState(current);                                // ②
      termAtt.setEmpty();
      termAtt.append(syn);
      posIncrAtt.setPositionIncrement(0);                   // ③
      return true;
    }

    if (!input.incrementToken())                            // ④
      return false;

    if (addAliasesToStack()) {                              // ⑤
      current = captureState();                             // ⑥
    }

    return true;                                            // ⑦
  }

  private boolean addAliasesToStack() throws IOException {
    String[] synonyms = engine.getSynonyms(termAtt.toString()); // ⑧
    if (synonyms == null) {
      return false;
    }
    for (String synonym : synonyms) {                           // ⑨
      synonymStack.push(synonym);
    }
    return true;
  }
}

/*
① 定义同义词缓存
② 弹出缓存的同义词
③ 设置位置增量为 0
④ 读取下一个词元
⑤ 将当期词元的所有同义词压入缓存栈
⑥ 保存当前词元状态
⑦ 返回当前词元
⑧ 检索当前词元的所有同义词
⑨ 将检索到的每一个同义词压入到缓存栈
*/
