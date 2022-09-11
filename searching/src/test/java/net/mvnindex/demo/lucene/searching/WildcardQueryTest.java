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
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class WildcardQueryTest {
    private final String indexPath = "indexes";
    private Directory directory;
    private DirectoryReader reader;
    private IndexWriter writer;
    private IndexSearcher searcher;

    @Before
    public void setUp() throws IOException {
        directory = FSDirectory.open(Paths.get(indexPath));
        IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
        writer = new IndexWriter(directory, config);
    }

    @After
    public void tearDown() throws IOException {
        reader.close();
        directory.close();
        deleteDir(new File(indexPath));
    }
    public static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
        dir.delete();
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
    public void testWildcard() throws Exception {
        indexSingleFieldDocs(new Field[]
                {
                    new TextField("contents", "wild", Field.Store.YES),
                    new TextField("contents", "child", Field.Store.YES),
                    new TextField("contents", "mild", Field.Store.YES),
                    new TextField("contents", "mildew", Field.Store.YES)
                });
        reader = DirectoryReader.open(directory);
        searcher = new IndexSearcher(reader);
        Query query = new WildcardQuery(new Term("contents", "?ild*")); //①

        TopDocs matches = searcher.search(query, 10);
        assertEquals("child no match", 3, matches.totalHits.value);

        assertEquals("score the same", matches.scoreDocs[0].score,
                matches.scoreDocs[1].score, 0.0);
        assertEquals("score the same", matches.scoreDocs[1].score,
                matches.scoreDocs[2].score, 0.0);

        for(int i=0; i<matches.scoreDocs.length; i++) {
            System.out.print("match " + i + "  [contents]: " + searcher.doc(matches.scoreDocs[i].doc).get("contents"));
            System.out.println(" [score]: " + matches.scoreDocs[i].score);
        }
    }
}

/*
* ① 创建 WildcardQuery 实例
*
* */
