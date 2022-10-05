package net.mvnindex.demo.lucene.extsearch.queryparser;


import net.mvnindex.demo.lucene.extsearch.queryparser.NumericQueryParserTest.PointDateRangeQueryParser;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;

//import javax.servlet.*;
//import javax.servlet.http.*;
import java.io.IOException;

// From chapter 6
public class SearchServletFragment /* extends HttpServlet*/ {

  private IndexSearcher searcher;
/*
  protected void doGet(HttpServletRequest request,
                       HttpServletResponse response) 
      throws ServletException, IOException {
    
    QueryParser parser = new PointDateRangeQueryParser("contents", new StandardAnalyzer());
    
    parser.setLocale(request.getLocale());
    parser.setDateResolution(DateTools.Resolution.DAY);

    Query query = null;
    try {
      query = parser.parse(request.getParameter("q"));
    } catch (ParseException e) {
      e.printStackTrace(System.err);
    }

    TopDocs docs = searcher.search(query, 10);
  }*/
}
