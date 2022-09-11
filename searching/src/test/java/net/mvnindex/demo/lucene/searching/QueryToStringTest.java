package net.mvnindex.demo.lucene.searching;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.TermQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QueryToStringTest {
    @Test
    public void testToString() throws Exception {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        builder.add(new FuzzyQuery(new Term("field", "kountry")),
                BooleanClause.Occur.MUST);
        builder.add(new TermQuery(new Term("title", "western")),
                BooleanClause.Occur.SHOULD);

        BooleanQuery query = builder.build();

        assertEquals("both kinds", "+kountry~2 title:western",
                query.toString("field"));

        System.out.println("query: " + query.toString());
        System.out.println("query: " + query.toString("field"));
    }
}
