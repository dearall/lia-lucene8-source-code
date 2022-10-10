
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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.nio.file.Paths;

// From chapter 8
public class BooksMoreLikeThis {
  public static void main(String[] args) throws Throwable {

    String indexDir = "index";
    FSDirectory directory = FSDirectory.open(Paths.get(indexDir));
    IndexReader reader = DirectoryReader.open(directory);

    IndexSearcher searcher = new IndexSearcher(reader);

    int numDocs = reader.maxDoc();

    MoreLikeThis mlt = new MoreLikeThis(reader);               // ①
    mlt.setFieldNames(new String[] {"title", "author"});
    mlt.setMinTermFreq(1);                                     // ②
    mlt.setMinDocFreq(1);

    for (int docID = 0; docID < numDocs; docID++) {            // ③
      System.out.println();
      Document doc = reader.document(docID);
      System.out.println(doc.get("title"));

      Query query = mlt.like(docID);                           // ④
      System.out.println("  query=" + query);

      TopDocs similarDocs = searcher.search(query, 10);        
      if (similarDocs.totalHits.value == 0)
        System.out.println("  None like this");
      for(int i=0;i<similarDocs.scoreDocs.length;i++) {
        if (similarDocs.scoreDocs[i].doc != docID) {           // ⑤
          doc = reader.document(similarDocs.scoreDocs[i].doc);
          System.out.println("  -> " + doc.getField("title").stringValue());
        }
      }
    }

    reader.close();
    directory.close();
  }
}

/*
  ① 实例化 MoreLikeThis
  ② 设置默认最小值
  ③ 迭代文档中全部文档
  ④ 构建查找类似文档的查询
  ⑤ 不显示同一文档
*/
