package net.mvnindex.demo.lucene.searching;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WildcardQueryTest {
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
    public void testWildcard() throws Exception {
        indexSingleFieldDocs(new Field[]
                {
                    new TextField("contents", "wild", Field.Store.YES),
                    new TextField("contents", "child", Field.Store.YES),
                    new TextField("contents", "mild", Field.Store.YES),
                    new TextField("contents", "mildew", Field.Store.YES)
                });
        DirectoryReader directoryReader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(directoryReader);
        Query query = new WildcardQuery(new Term("contents", "?ild*"));

        TopDocs matches = searcher.search(query, 10);
        assertEquals("child no match", 3, matches.totalHits.value);

        assertEquals("score the same", matches.scoreDocs[0].score,
                matches.scoreDocs[1].score, 0.0);
        assertEquals("score the same", matches.scoreDocs[1].score,
                matches.scoreDocs[2].score, 0.0);

        directoryReader.close();
    }
}
