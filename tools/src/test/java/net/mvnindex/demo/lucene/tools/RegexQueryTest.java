package net.mvnindex.demo.lucene.tools;

import net.mvnindex.demo.lucene.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RegexQueryTest {
    @Test
    public void testRegexQuery() throws Exception {
        Directory directory = TestUtil.getBookIndexDirectory();
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        RegexpQuery q = new RegexpQuery(new Term("title", ".*st.*"));
        TopDocs hits = searcher.search(q, 10);
        assertEquals(2, hits.totalHits.value);
        assertTrue(TestUtil.hitsIncludeTitle(searcher, hits,
                "Tapestry in Action"));
        assertTrue(TestUtil.hitsIncludeTitle(searcher, hits,
                "Mindstorms: Children, Computers, And Powerful Ideas"));
        reader.close();
        directory.close();
    }
}
