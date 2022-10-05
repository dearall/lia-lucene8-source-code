package net.mvnindex.demo.lucene.extsearch.filters;

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

import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

// From chapter 6
public class SpecialsFilter {
  private SpecialsAccessor accessor;

  public SpecialsFilter(SpecialsAccessor accessor) {
    this.accessor = accessor;
  }

  public ConstantScoreQuery getFilter() {
    String[] isbns = accessor.isbns();

    // 过滤多个 Term
    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    for (String isbn : isbns) {
      TermQuery tq = new TermQuery(new Term("isbn", isbn));
      builder.add(tq, BooleanClause.Occur.SHOULD);
    }

    return new ConstantScoreQuery(builder.build());
  }

  public String toString() {
    return "SpecialsFilter";
  }
}
