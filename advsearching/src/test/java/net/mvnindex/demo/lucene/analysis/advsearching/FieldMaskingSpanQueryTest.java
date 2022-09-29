package net.mvnindex.demo.lucene.analysis.advsearching;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.FieldMaskingSpanQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class FieldMaskingSpanQueryTest {
    private ByteBuffersDirectory directory;
    private DirectoryReader reader;
    private IndexSearcher searcher;
    private Analyzer analyzer;

    @Before
    public void setUp() throws Exception {
        directory = new ByteBuffersDirectory();

        analyzer = new WhitespaceAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(directory, config);

        Document doc = new Document();
        doc.add(new StringField("teacherid", "1", Field.Store.YES));
        doc.add(new TextField("studentfirstname", "james", Field.Store.YES));
        doc.add(new TextField("studentsurname", "jones", Field.Store.YES));
        writer.addDocument(doc);

        doc = new Document();
        doc.add(new StringField("teacherid", "2", Field.Store.YES));
        doc.add(new TextField("studentfirstname", "james", Field.Store.YES));
        doc.add(new TextField("studentsurname", "smith", Field.Store.YES));
        doc.add(new TextField("studentfirstname", "sally", Field.Store.YES));
        doc.add(new TextField("studentsurname", "jones", Field.Store.YES));
        writer.addDocument(doc);

        writer.close();

        reader = DirectoryReader.open(directory);
        searcher = new IndexSearcher(reader);
    }

    @After
    public void tearDown() throws Exception {
        reader.close();
        directory.close();
    }

    @Test
    public void testFieldMaskingSpanQuery() throws IOException {
        SpanQuery q1  = new SpanTermQuery(new Term("studentfirstname", "james"));
        SpanQuery q2  = new SpanTermQuery(new Term("studentsurname", "jones"));
        SpanQuery q2m = new FieldMaskingSpanQuery(q2, "studentfirstname");
        Query query = new SpanNearQuery(new SpanQuery[]{q1, q2m}, -1, false);

        TopDocs hits = searcher.search(query, 10);
        assertEquals(1, hits.totalHits.value);

        for (ScoreDoc sd : hits.scoreDocs) {
            Document doc = searcher.doc(sd.doc);
            System.out.println("[teacherid]: " + doc.get("teacherid"));
            System.out.println("[studentfirstname]: " + doc.get("studentfirstname"));
            System.out.println("[studentsurname]: " + doc.get("studentsurname"));
            System.out.println();
        }
    }
}
