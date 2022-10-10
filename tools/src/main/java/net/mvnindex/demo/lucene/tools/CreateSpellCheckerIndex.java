
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

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
import java.nio.file.Paths;

// From chapter 8
public class CreateSpellCheckerIndex {

  public static void main(String[] args) throws IOException {

    String spellCheckDir = "indexes/spellchecker";
    String indexDir = "indexes/wordnet";
    String indexField = "word";

    if (args.length != 3) {
      System.out.println("Usage: java -jar target/lucene-tools-1.0-SNAPSHOT-shaded.jar SpellCheckerIndexDir IndexDir IndexField");
    }
    else {
      spellCheckDir = args[0];
      indexDir = args[1];
      indexField = args[2];
    }


    System.out.println("Now build SpellChecker index...");
    Directory dir = FSDirectory.open(Paths.get(spellCheckDir));
    SpellChecker spell = new SpellChecker(dir);         // ①
    long startTime = System.currentTimeMillis();
    
    Directory dir2 = FSDirectory.open(Paths.get(indexDir));
    DirectoryReader reader = DirectoryReader.open(dir2); // ②

    try {
      spell.indexDictionary(new LuceneDictionary(reader, indexField),
              new IndexWriterConfig(), true);   //③
    } finally {
      reader.close();
    }
    dir.close();
    dir2.close();
    long endTime = System.currentTimeMillis();
    System.out.println("  took " + (endTime-startTime) + " milliseconds");
  }
}
/*
  ① 在拼写检测器索引目录上创建 SpellChecker
  ② 打开包含要加入到拼写词典单词的 IndexReader
  ③ 将特定域内全部的单词加入到拼写检查索引
*/
