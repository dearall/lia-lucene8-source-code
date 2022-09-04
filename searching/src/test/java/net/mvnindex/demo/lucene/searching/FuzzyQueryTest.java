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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FuzzyQueryTest {
    private Directory directory;

    private void indexSingleFieldDocs(Field[] fields) throws Exception {
        directory = new RAMDirectory();
        IndexWriterConfig wconfig = new IndexWriterConfig(new WhitespaceAnalyzer());
        wconfig.setCommitOnClose(true);

        IndexWriter writer = new IndexWriter(directory, wconfig);

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
        DirectoryReader directoryReader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(directoryReader);

        Query query = new FuzzyQuery(new Term("contents", "wuzza"));
        TopDocs matches = searcher.search(query, 10);

        assertEquals("both close enough", 2, matches.totalHits.value);
        assertTrue("wuzzy closer than fuzzy",
                matches.scoreDocs[0].score != matches.scoreDocs[1].score);
        Document doc = searcher.doc(matches.scoreDocs[0].doc);
        assertEquals("wuzza bear", "wuzzy", doc.get("contents"));

        directoryReader.close();
    }
}
