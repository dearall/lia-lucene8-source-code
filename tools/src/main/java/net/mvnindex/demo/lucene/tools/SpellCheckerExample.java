
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

import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.search.spell.LevenshteinDistance;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
import java.nio.file.Paths;

// From chapter 8
public class SpellCheckerExample {

  public static void main(String[] args) throws IOException {
    String spellCheckDir = "indexes/spellchecker";
    String wordToRespell = "letuce";
    if (args.length != 2) {
      System.out.println("Usage: java net.mvnindex.demo.lucene.tools.SpellCheckerExample SpellCheckerIndexDir wordToRespell");
    }
    else {
      spellCheckDir = args[0];
      wordToRespell = args[1];
    }

    Directory dir = FSDirectory.open(Paths.get(spellCheckDir));

    SpellChecker spell = new SpellChecker(dir);           // ①
    spell.setStringDistance(new LevenshteinDistance());   // ②

    //spell.setStringDistance(new JaroWinklerDistance());

    String[] suggestions = spell.suggestSimilar(wordToRespell, 5); // ③
    System.out.println(suggestions.length + " suggestions for '" + wordToRespell + "':");
    for (String suggestion : suggestions)
      System.out.println("  " + suggestion);
  }
}
/*
  ① 从已存在的拼写检查索引创建 SpellCheck
  ② 使用莱文斯坦距离 LevenshteinDistance 设置字符串编辑距离，以用户对建议 suggestion 进行排名
  ③ 生成重新拼写的建议候选项
*/

