package net.mvnindex.demo.lucene.searching;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FuzzyQueryTest {
    private Directory directory;
    private DirectoryReader reader;
    private IndexWriter writer;
    private IndexSearcher searcher;

    @Before
    public void setUp() throws IOException {
        directory = new ByteBuffersDirectory();
        IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
        writer = new IndexWriter(directory, config);
    }

    @After
    public void tearDown() throws IOException {
        reader.close();
        directory.close();
    }

    private void indexSingleFieldDocs(Field[] fields) throws Exception {
        for (Field f : fields) {
            Document doc = new Document();
            doc.add(f);
            writer.addDocument(doc);
        }
        writer.close();
    }

    @Test
    public void testFuzzy() throws Exception {
        indexSingleFieldDocs(new Field[] {
                new TextField("contents", "fuzzy", Field.Store.YES),
                new TextField("contents", "wuzzy", Field.Store.YES)
        });
        reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        Query query = new FuzzyQuery(new Term("contents", "wuzza")); // ①
        TopDocs matches = searcher.search(query, 10);

        assertEquals("both close enough", 2, matches.totalHits.value);
        assertTrue("wuzzy closer than fuzzy",
                matches.scoreDocs[0].score != matches.scoreDocs[1].score);
        Document doc = searcher.doc(matches.scoreDocs[0].doc);
        assertEquals("wuzza bear", "wuzzy", doc.get("contents"));

        for(int i=0; i<matches.scoreDocs.length; i++) {
            System.out.print("match " + i + "  [contents]: " + searcher.doc(matches.scoreDocs[i].doc).get("contents"));
            System.out.println(" [score]: " + matches.scoreDocs[i].score);
        }
    }
}

/*
* ① 创建 FuzzyQuery 实例
* */