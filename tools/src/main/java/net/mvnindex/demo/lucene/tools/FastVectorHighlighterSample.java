package net.mvnindex.demo.lucene.tools;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.vectorhighlight.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import java.io.FileWriter;
import java.io.IOException;

// From chapter 8
public class FastVectorHighlighterSample {

  static final String[] DOCS = {                                      // ①
    "the quick brown fox jumps over the lazy dog",
    "the quick gold fox jumped over the lazy black dog",
    "the quick fox jumps over the black dog",
    "the red fox jumped over the lazy dark gray dog"
  };
  static final String QUERY = "quick OR fox OR \"lazy dog\"~1";       // ②
  static final String F = "f";
  static Directory dir = new ByteBuffersDirectory();
  static Analyzer analyzer = new StandardAnalyzer();

  public static void main(String[] args) throws Exception {
    String filename = "fast-highter.html";
    if (args.length != 1) {
      System.out.println("Usage: FastVectorHighlighterSample <filename>");
    }
    else{
      filename = args[0];
    }
    makeIndex();
    searchIndex(filename);
  }

  static void makeIndex() throws IOException {
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    IndexWriter writer = new IndexWriter(dir, config);

    FieldType tvTextStoredType = new FieldType(TextField.TYPE_STORED);
    tvTextStoredType.setStoreTermVectors(true);
    tvTextStoredType.setStoreTermVectorPositions(true);
    tvTextStoredType.setStoreTermVectorOffsets(true);

    for(String d : DOCS){
      Document doc = new Document();
      doc.add(new Field(F, d, tvTextStoredType));
      writer.addDocument(doc);
    }
    writer.close();
  }
  
  static void searchIndex(String filename) throws Exception {
    QueryParser parser = new QueryParser(F, analyzer);
    Query query = parser.parse(QUERY);

    FastVectorHighlighter highlighter = getHighlighter();             // ③
    FieldQuery fieldQuery = highlighter.getFieldQuery(query);         // ④

    DirectoryReader reader = DirectoryReader.open(dir);
    IndexSearcher searcher = new IndexSearcher(reader);
    TopDocs docs = searcher.search(query, 10);                       

    FileWriter writer = new FileWriter(filename);
    writer.write("<html>");
    writer.write("<body>");
    writer.write("<p>QUERY : " + QUERY + "</p>");
    for(ScoreDoc scoreDoc : docs.scoreDocs) {
      String snippet = highlighter.getBestFragment(                   // ⑤
          fieldQuery, searcher.getIndexReader(),                      // ⑤
          scoreDoc.doc, F, 100 );                          // ⑤
      if (snippet != null) {
        writer.write(scoreDoc.doc + " : " + snippet + "<br/>");
      }
    }
    writer.write("</body></html>");
    writer.close();
    reader.close();
  }
  
  static FastVectorHighlighter getHighlighter() {
    FragListBuilder fragListBuilder = new SimpleFragListBuilder();
    FragmentsBuilder fragmentBuilder =
      new ScoreOrderFragmentsBuilder(
        BaseFragmentsBuilder.COLORED_PRE_TAGS,
        BaseFragmentsBuilder.COLORED_POST_TAGS);

    return new FastVectorHighlighter(true, true,  // ⑥
        fragListBuilder, fragmentBuilder);                              // ⑥
  }
}

/*
① 对这些文档进行索引
② 查询表达式
③ 获取 FastVectorHighlighter 对象
④ 创建 FieldQuery 对象
⑤ 高亮显示评分最高的片段
⑥ 创建 FastVectorHighlighter 实例对象
*/
