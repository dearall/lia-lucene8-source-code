package net.mvnindex.demo.lucene.tools;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;

import java.io.FileWriter;
import java.io.StringReader;

public class HighlightIt {
    private static final String text =
            "In this section we'll show you how to make the simplest " +
                    "programmatic query, searching for a single term, and then " +
                    "we'll see how to use QueryParser to accept textual queries. " +
                    "In the sections that follow, we’ll take this simple example " +
                    "further by detailing all the query types built into Lucene. " +
                    "We begin with the simplest search of all: searching for all " +
                    "documents that contain a single term.";

    public static void main(String[] args) throws Exception {

        String filename = "index.html";

        if (args.length != 1) {
            System.out.println("Usage: HighlightIt <filename-out>");
        }
        else{
            filename = args[0];
        }

        String searchText = "term";
        QueryParser parser = new QueryParser("f", new StandardAnalyzer());
        Query query = parser.parse(searchText);       // ①

        SimpleHTMLFormatter formatter =               // ②
                new SimpleHTMLFormatter("<span class=\"highlight\">", "</span>");

        StandardAnalyzer analyzer = new StandardAnalyzer();
        TokenStream tokens = analyzer.tokenStream("f", new StringReader(text));  // ③

        QueryScorer scorer = new QueryScorer(query, "f");                            // ④

        Highlighter highlighter = new Highlighter(formatter, scorer);                     // ⑤
        highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer));                  // ⑥

        String result = highlighter.getBestFragments(tokens, text, 3, "..."); // ⑦

        FileWriter writer = new FileWriter(filename);    // ⑧
        writer.write("<html>");
        writer.write("<style>\n" +
                ".highlight {\n" +
                " background: yellow;\n" +
                "}\n" +
                "</style>");
        writer.write("<body>");
        writer.write(result);
        writer.write("</body></html>");
        writer.close();
    }
}

/*
① 创建查询
② 自定义高亮显示的包围标签
③ 分析原始文本
④ 创建 QueryScorer
⑤ 创建 highlighter
⑥ 使用 SimpleSpanFragmenter 进行分片
⑦ 高亮显示评分最高的最多三个片段
⑧ 写入高亮显示的 HTML 文件
*/
