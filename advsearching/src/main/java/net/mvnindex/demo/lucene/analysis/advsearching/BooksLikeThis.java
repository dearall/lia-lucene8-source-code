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
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

// From chapter 5
public class BooksLikeThis {
  private IndexReader reader;
  private IndexSearcher searcher;

  public static void main(String[] args) throws IOException {
//    Directory directory = TestUtil.getBookIndexDirectory();
    Directory directory = FSDirectory.open(Paths.get("index"));
    IndexReader reader = DirectoryReader.open(directory);

    int numDocs = reader.maxDoc();

    BooksLikeThis blt = new BooksLikeThis(reader);
    for (int i = 0; i < numDocs; i++) {                         // ①
      System.out.println();
      Document doc = reader.document(i);
      System.out.println(doc.get("title"));

      Document[] docs = blt.docsLike(i, 10);               // ②
      if (docs.length == 0) {
        System.out.println("  None like this");
      }
      for (Document likeThisDoc : docs) {
        System.out.println("  -> " + likeThisDoc.get("title"));
      }
    }
    reader.close();
    directory.close();
  }

  public BooksLikeThis(IndexReader reader) {
    this.reader = reader;
    searcher = new IndexSearcher(reader);
  }

  public Document[] docsLike(int id, int max) throws IOException {
    Document doc = reader.document(id);

    String[] authors = doc.getValues("author");                     // ③
    BooleanQuery.Builder builder = new BooleanQuery.Builder();            // ③
    for (String author : authors) {
      builder.add(new TermQuery(new Term("author", author)), BooleanClause.Occur.SHOULD);
    }
    BoostQuery authorQuery = new BoostQuery(builder.build(), 2.0f);  // ③

    Terms vector = reader.getTermVector(id, "subject");               // ④
    TermsEnum termsEnum = vector.iterator();

    BooleanQuery.Builder subjectBuilder = new BooleanQuery.Builder();

    while (termsEnum.next() != null) {
      TermQuery tq = new TermQuery(new Term("subject", termsEnum.term().utf8ToString())); // ④
      subjectBuilder.add(tq, BooleanClause.Occur.SHOULD);                  // ④
    }
    BooleanQuery subjectQuery = subjectBuilder.build();

    BooleanQuery.Builder likeThisBuilder = new BooleanQuery.Builder();
    likeThisBuilder.add(authorQuery, BooleanClause.Occur.SHOULD);             // ⑤
    likeThisBuilder.add(subjectQuery, BooleanClause.Occur.SHOULD);            // ⑤
    likeThisBuilder.add(new TermQuery(                                        // ⑥
        new Term("isbn", doc.get("isbn"))), BooleanClause.Occur.MUST_NOT); // ⑥

    BooleanQuery likeThisQuery = likeThisBuilder.build();

//     System.out.println("  Query: " +
//        likeThisQuery.toString("contents"));

    TopDocs hits = searcher.search(likeThisQuery, 10);
    int size = max;
    if (max > hits.scoreDocs.length) size = hits.scoreDocs.length;

    Document[] docs = new Document[size];
    for (int i = 0; i < size; i++) {
      docs[i] = reader.document(hits.scoreDocs[i].doc);
    }

    return docs;
  }
}
/*
① 迭代索引中所有的书籍，并找到与每一本书类似的书籍
② 查找类似这本书的其它书籍
③ 为具有相同作者的查询加权，考虑到具有相同作者的书籍更加类似，因此对相同作者的查询提升权限，
这样，这类书籍就很可能排列在其他作者类似书籍的前面
④ 利用从当前文档的 "subject" 域词向量获取的所有词项，把它们加入到另一个布尔查询
⑤ 联合作者 author 和主题内容 subject 创建最终布尔查询
⑥ 排除当前书籍，这样更加明确地最优匹配其他判断条件
*/
