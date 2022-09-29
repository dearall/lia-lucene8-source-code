package net.mvnindex.demo.lucene.analysis.advsearching;

import net.mvnindex.demo.lucene.common.TestUtil;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queries.function.valuesource.IntFieldSource;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class FunctionQueryTest {
    private Directory directory;
    private IndexWriter writer;
    private IndexReader reader;
    private IndexSearcher searcher;

    @Before
    public void setUp() throws Exception {
        directory = new ByteBuffersDirectory();
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET));
        writer = new IndexWriter(directory, config);

        addDoc(7, "this hat is green");
        addDoc(42, "this hat is blue");
        writer.close();

        reader = DirectoryReader.open(directory);
        searcher = new IndexSearcher(reader);
    }

    @After
    public void tearDown() throws Exception {
        reader.close();
        directory.close();
    }

    private void addDoc(int score, String content) throws Exception {
        Document doc = new Document();
        doc.add(new NumericDocValuesField("score", score));
        doc.add(new TextField("content", content, Field.Store.NO));
        doc.add(new StoredField("content", content));
        writer.addDocument(doc);
    }

    @Test
    public void testFunctionQuery() throws Throwable {
        Query q = new FunctionQuery(new IntFieldSource("score"));
        TopDocs hits = searcher.search(q, 10);
        assertEquals(2, hits.scoreDocs.length);       // ①
        assertEquals(1, hits.scoreDocs[0].doc);       // ②
        assertEquals(42, (int) hits.scoreDocs[0].score);
        assertEquals(0, hits.scoreDocs[1].doc);
        assertEquals(7, (int) hits.scoreDocs[1].score);

        for (ScoreDoc sd : hits.scoreDocs){
            Document doc = searcher.doc(sd.doc);
            System.out.println("[doc]: " + sd.doc + " [score]: "
                    + sd.score + ", [content]: "+ doc.get("content"));
        }
    }

  /*
    ① 匹配所有文档
    ② doc 1 在第一位，因为静态分数（42）比 doc 0 的分数（7）高
  */

    static class ScoreValuesSource extends DoubleValuesSource {
        final DoubleValuesSource boost;

        private ScoreValuesSource(DoubleValuesSource boost) {
            this.boost = boost;
        }

        @Override
        public DoubleValues getValues(LeafReaderContext ctx, DoubleValues scores) throws IOException {
            DoubleValues boosts = DoubleValues.withDefault(boost.getValues(ctx, scores), 1);
            return new DoubleValues() {
                @Override
                public double doubleValue() throws IOException {
                    System.out.println("[score]: " + scores.doubleValue() + ", [boost]: "+ boosts.doubleValue());
                    return Math.sqrt(scores.doubleValue()) * boosts.doubleValue();
                }

                @Override
                public boolean advanceExact(int doc) throws IOException {
                    return boosts.advanceExact(doc);
                }
            };
        }

        @Override
        public boolean needsScores() {
            return true;
        }

        @Override
        public Explanation explain(LeafReaderContext ctx, int docId, Explanation scoreExplanation) throws IOException {
            if (scoreExplanation.isMatch() == false) {
                return scoreExplanation;
            }
            Explanation boostExpl = boost.explain(ctx, docId, scoreExplanation);
            if (boostExpl.isMatch() == false) {
                return scoreExplanation;
            }
            return Explanation.match(scoreExplanation.getValue().doubleValue() * boostExpl.getValue().doubleValue(),
                    "product of:", scoreExplanation, boostExpl);
        }

        @Override
        public DoubleValuesSource rewrite(IndexSearcher reader) throws IOException {
            return new ScoreValuesSource(boost.rewrite(reader));
        }

        @Override
        public int hashCode() {
            return Objects.hash(boost);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScoreValuesSource that = (ScoreValuesSource) o;
            return Objects.equals(boost, that.boost);
        }

        @Override
        public String toString() {
            return "boost(" + boost.toString() + ")";
        }

        @Override
        public boolean isCacheable(LeafReaderContext ctx) {
            return boost.isCacheable(ctx);
        }
    }

    @Test
    public void testFunctionScoreQuery() throws ParseException, IOException {
        Query q = new QueryParser("content", new StandardAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET))
                .parse("the green hat");

        ScoreValuesSource source = new ScoreValuesSource(DoubleValuesSource.fromIntField("score"));
        FunctionScoreQuery fsquery = new FunctionScoreQuery(q, source);
        TopDocs topDocs = searcher.search(fsquery, 10);

        assertEquals(2, topDocs.scoreDocs.length);
        assertEquals(1, topDocs.scoreDocs[0].doc);

        for (ScoreDoc sd : topDocs.scoreDocs){
            Document doc = searcher.doc(sd.doc);
            System.out.println("[doc]: " + sd.doc + ", [score]: "
                    + sd.score + ", [content]: "+ doc.get("content"));
        }
    }

    // 评分算法
    static public double customScore(double score, double boost) {
        System.out.println("score: "+ score + ", boost: "+ boost);
        return Math.sqrt(score) * boost;
    }

    @Test
    public void testFunctionScoreQueryWithExpression() throws NoSuchMethodException,
            ParseException, IOException, java.text.ParseException {
        SimpleBindings bindings = new SimpleBindings();
        bindings.add("score", DoubleValuesSource.SCORES);
        bindings.add("boost", DoubleValuesSource.fromIntField("score"));

        Map<String, Method> scoreMethods = new HashMap<>();
        // add all the default functions
        scoreMethods.putAll(JavascriptCompiler.DEFAULT_FUNCTIONS);

        String sort_method= "customScore(score,boost)";
        scoreMethods.put("customScore", getClass().getDeclaredMethod("customScore", double.class, double.class));

        Expression expr = JavascriptCompiler.compile(sort_method, scoreMethods, getClass().getClassLoader());

        Query q = new QueryParser("content", new StandardAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET))
                .parse("the green hat");
        FunctionScoreQuery fsquery = new FunctionScoreQuery(q, expr.getDoubleValuesSource(bindings));

        TopDocs topDocs = searcher.search(fsquery, 10);

        assertEquals(2, topDocs.scoreDocs.length);
        assertEquals(1, topDocs.scoreDocs[0].doc);

        for (ScoreDoc sd : topDocs.scoreDocs){
            Document doc = searcher.doc(sd.doc);
            System.out.println("[doc]: " + sd.doc + " [score]: "
                    + sd.score + ", [content]: "+ doc.get("content"));
        }
    }


    static class RecencyBoostingValuesSource extends DoubleValuesSource {
        final DoubleValuesSource publishDay;

        double multiplier;
        long today;
        int maxDaysAgo;
        static long MSEC_PER_DAY = 1000 * 3600 * 24;

        RecencyBoostingValuesSource(DoubleValuesSource publishDay, double multiplier, int maxDaysAgo) {
            this.publishDay = publishDay;

            today = new Date().getTime() / MSEC_PER_DAY;
            this.multiplier = multiplier;
            this.maxDaysAgo = maxDaysAgo;
        }

        @Override
        public DoubleValues getValues(LeafReaderContext ctx, DoubleValues scores) throws IOException {
            DoubleValues boosts = DoubleValues.withDefault(publishDay.getValues(ctx, scores), 1); // ①
            return new DoubleValues() {
                @Override
                public double doubleValue() throws IOException {
                    System.out.println("[score]: " + scores.doubleValue() + ", [pubmonthAsDay]: "+ boosts.doubleValue());

                    double daysAgo = today - boosts.doubleValue();                       // ②
                    System.out.println("daysAgo: " + daysAgo + ", maxDaysAgo: "+ maxDaysAgo);

                    if (daysAgo < maxDaysAgo) {                                           // ③
                        double boost = multiplier *  (maxDaysAgo-daysAgo) / maxDaysAgo;   // ④
                        System.out.println("加权计算：" + scores.doubleValue() * (1.0+boost));
                        return scores.doubleValue() * (1.0+boost);                        // ④
                    } else {
                        System.out.println("未加权计算："+ scores.doubleValue());
                        return scores.doubleValue();                                      // ⑤
                    }
                }

                @Override
                public boolean advanceExact(int doc) throws IOException {
                    return boosts.advanceExact(doc);
                }
            };
        }

        @Override
        public boolean needsScores() {
            return true;
        }

        @Override
        public DoubleValuesSource rewrite(IndexSearcher reader) throws IOException {
            return new RecencyBoostingValuesSource(publishDay.rewrite(reader), multiplier, maxDaysAgo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(publishDay);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            RecencyBoostingValuesSource that = (RecencyBoostingValuesSource) obj;
            return Objects.equals(this.publishDay, that.publishDay);
        }

        @Override
        public String toString() {
            return "boost(" + publishDay.toString() + ")";
        }

        @Override
        public boolean isCacheable(LeafReaderContext ctx) {
            return publishDay.isCacheable(ctx);
        }
    }

    /*
      ① 从 DocValues 获取索引的出版天数
      ② 计算过去的天数
      ③ 跳过比较旧的书籍
      ④ 计算加权因子，并计算新的评分值
      ⑤ 对不符合最近条件的文档，原值返回评分
    */

    @Test
    public void testRecency() throws Throwable {
        Directory directory = TestUtil.getBookIndexDirectory();
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        QueryParser parser = new QueryParser("contents",
                new StandardAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET));
        Query q = parser.parse("java in action");       // ①

        TopDocs hits = searcher.search(q, 10);

        for (int i = 0; i < hits.scoreDocs.length; i++) {
            Document doc = reader.document(hits.scoreDocs[i].doc);
            System.out.println((1 + i) + ": " +
                    doc.get("title") +
                    ": pubmonth=" +
                    doc.get("pubmonth") +
                    " score=" + hits.scoreDocs[i].score);
        }
        System.out.println();

        RecencyBoostingValuesSource source = new RecencyBoostingValuesSource(         // ②
                DoubleValuesSource.fromIntField("pubmonthAsDay"), 2.0, 14 * 365);

        FunctionScoreQuery query = new FunctionScoreQuery(q, source);
        hits = searcher.search(query, 10);

        for (int i = 0; i < hits.scoreDocs.length; i++) {
            Document doc = reader.document(hits.scoreDocs[i].doc);
            System.out.println((1 + i) + ": " +
                    doc.get("title") +
                    ": pubmonth=" +
                    doc.get("pubmonth") +
                    " score=" + hits.scoreDocs[i].score);
        }

        reader.close();
        directory.close();
    }

    /**
     * ① 解析简单的查询
     * ② 创建最近加权查询
     */
}
