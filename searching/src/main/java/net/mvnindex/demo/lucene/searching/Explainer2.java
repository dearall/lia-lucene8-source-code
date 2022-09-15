package net.mvnindex.demo.lucene.searching;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;

// From chapter 3
public class Explainer2 {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: Explainer <index dir> <query>");
            System.exit(1);
        }

        String indexDir = args[0];
        String queryExpression = args[1];

        Directory directory = FSDirectory.open(Paths.get(indexDir));
        QueryParser parser = new QueryParser("contents", new SimpleAnalyzer());
        Query query = parser.parse(queryExpression);

        System.out.println("Query: " + queryExpression);

        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new ClassicSimilarity());              // ③
        TopDocs topDocs = searcher.search(query, 10);

        for (ScoreDoc match : topDocs.scoreDocs) {
            Explanation explanation = searcher.explain(query, match.doc);     // ①

            System.out.println("----------");
            Document doc = searcher.doc(match.doc);
            System.out.println(doc.get("title"));
            System.out.println(explanation.toString());  //②
        }
        reader.close();
        directory.close();
    }
}

/*
  ① 返回 Explanation 对象
  ② 打印输出 Explanation 对象
  ③ 使用经典的 TF-IDF 相似度算法作为搜索结果的评分算法。注意，这样切换算法后，Lucene 会提出如下警告：
  Expert: Historical scoring implementation. You might want to consider using BM25Similarity instead,
  which is generally considered superior to TF-IDF.

*/
