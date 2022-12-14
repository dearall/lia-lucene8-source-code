package net.mvnindex.demo.lucene.extsearch.sorting;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.XYDocValuesField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class XYDocValuesFieldDistanceSortingTest {
    private Directory directory;
    private DirectoryReader reader;
    private IndexSearcher searcher;
    private Query query;

    @Before
    public void setUp() throws Exception {
        directory = new ByteBuffersDirectory();
        IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
        IndexWriter writer = new IndexWriter(directory, config);

        addPoint(writer, "El Charro", "restaurant", 1.f, 2.f);
        addPoint(writer, "Cafe Poca Cosa", "restaurant", 5.f, 9.f);
        addPoint(writer, "Los Betos", "restaurant", 9.f, 6.f);
        addPoint(writer, "Nico's Taco Shop", "restaurant", 3.f, 8.f);

        writer.close();

        reader = DirectoryReader.open(directory);
        searcher = new IndexSearcher(reader);

        query = new TermQuery(new Term("type", "restaurant"));
    }

    private void addPoint(IndexWriter writer, String name, String type, float x, float y)
            throws IOException {
        Document doc = new Document();
        doc.add(new StringField("name", name, Field.Store.YES));
        doc.add(new StringField("type", type, Field.Store.YES));
        doc.add(new XYDocValuesField("location", x, y));
        writer.addDocument(doc);
    }

    @After
    public void tearDown() throws Exception {
        reader.close();
        directory.close();
    }

    @Test
    public void testNearestRestaurantToHome() throws Exception {
        Sort sort = new Sort(XYDocValuesField.newDistanceSort("location", 0.f, 0.f));
        TopFieldDocs hits = searcher.search(query, 10, sort);

        assertEquals("closest",
                "El Charro", searcher.doc(hits.scoreDocs[0].doc).get("name"));
        assertEquals("furthest",
                "Los Betos", searcher.doc(hits.scoreDocs[3].doc).get("name"));
    }


    @Test
    public void testNeareastRestaurantToWork() throws Exception {
        Sort sort = new Sort(XYDocValuesField.newDistanceSort("location",10.f, 10.f));

        TopFieldDocs topDocs = searcher.search(query, 3, sort);     // ???

        assertEquals(4, topDocs.totalHits.value);             // ???
        assertEquals(3, topDocs.scoreDocs.length);            // ???

        FieldDoc fieldDoc = (FieldDoc) topDocs.scoreDocs[0];          // ???

        assertEquals("(10,10) -> (9,6) = sqrt(17)",
                Math.sqrt(17),
                fieldDoc.fields[0]);                                  // ???

        Document document = searcher.doc(fieldDoc.doc);               // ???
        assertEquals("Los Betos", document.get("name"));

        dumpDocs(sort, topDocs);
    }
  /*
    ??? ??????????????????????????????
    ??? ??????????????????
    ??? ???????????????????????????????????????
    ??? ????????????????????????????????? FieldDoc ?????????topDocs.scoreDocs[0] ???????????? ScoreDoc ?????????
    ??????????????????????????? FieldDoc ????????????????????????????????????????????????
    ??? ??????????????????????????????????????????????????? Math.sqrt(17) ??????????????????
    ??? ??????????????????????????? name ?????? "Los Betos"
  */

    private void dumpDocs(Sort sort, TopFieldDocs docs) throws IOException {
        System.out.println("Sorted by: " + sort);
        ScoreDoc[] scoreDocs = docs.scoreDocs;
        for (int i = 0; i < scoreDocs.length; i++) {
            FieldDoc fieldDoc = (FieldDoc) scoreDocs[i];
            Double distance = (Double) fieldDoc.fields[0];
            Document doc = searcher.doc(fieldDoc.doc);
            System.out.println("   " + doc.get("name") + " -> " + distance);
        }
    }
}
