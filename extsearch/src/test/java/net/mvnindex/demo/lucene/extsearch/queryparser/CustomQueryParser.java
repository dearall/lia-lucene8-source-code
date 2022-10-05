package net.mvnindex.demo.lucene.extsearch.queryparser;

/**
 * Copyright Manning Publications Co.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific lan      
*/

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.util.Version;

// From chapter 6
public class CustomQueryParser extends QueryParser {
  public CustomQueryParser(String f, Analyzer a) {
    super(f,a);
  }

  protected final Query getWildcardQuery(String field, String termStr) throws ParseException {
    throw new ParseException("Wildcard not allowed");
  }

  protected Query getFuzzyQuery(String field, String termStr, float minSimilarity) throws ParseException {
    throw new ParseException("Fuzzy queries not allowed");
  }

  /**
   * Replace PhraseQuery with SpanNearQuery to force in-order
   * phrase matching rather than reverse.
   */
  protected Query getFieldQuery(String field, String queryText, int slop) throws ParseException {
    Query orig = super.getFieldQuery(field, queryText, slop);  // ①

    if (!(orig instanceof PhraseQuery)) {         // ②
      return orig;                                // ②
    }                                             // ②

    PhraseQuery pq = (PhraseQuery) orig;
    Term[] terms = pq.getTerms();                 // ③
    SpanTermQuery[] clauses = new SpanTermQuery[terms.length];
    for (int i = 0; i < terms.length; i++) {
      clauses[i] = new SpanTermQuery(terms[i]);
    }

    SpanNearQuery query = new SpanNearQuery(clauses, slop, true);  // ④
    return query;
  }
  /*
    ① 委托给原始 QueryParser 实现来分析，并确定查询的类型
    ② 只重写短语查询 PhraseQuery 的查询类型
    ③ 取出短语查询中全部的词项
    ④ 用从 PhraseQuery 取出的全部词项构建邻近跨度查询 SpanNearQuery
  */

}
