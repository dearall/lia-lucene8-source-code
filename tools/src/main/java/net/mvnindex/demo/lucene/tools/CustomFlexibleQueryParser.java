package net.mvnindex.demo.lucene.tools;

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
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.*;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorPipeline;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.builders.StandardQueryBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.StandardQueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.standard.nodes.WildcardQueryNode;
import org.apache.lucene.queryparser.flexible.standard.processors.StandardQueryNodeProcessorPipeline;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

import java.util.List;

// From chapter 9
public class CustomFlexibleQueryParser extends StandardQueryParser {

  public CustomFlexibleQueryParser(Analyzer analyzer) {
    super(analyzer);

    StandardQueryNodeProcessorPipeline processors = (StandardQueryNodeProcessorPipeline) getQueryNodeProcessor();
    processors.add(new NoFuzzyOrWildcardQueryProcessor());               // ①

    StandardQueryTreeBuilder builders = (StandardQueryTreeBuilder) getQueryBuilder();    // ②
    builders.setBuilder(TokenizedPhraseQueryNode.class, new SpanNearPhraseQueryBuilder());//②
    builders.setBuilder(SlopQueryNode.class, new SlopQueryNodeBuilder());//②
  }

  private final class NoFuzzyOrWildcardQueryProcessor extends QueryNodeProcessorImpl {
    protected QueryNode preProcessNode(QueryNode node) throws QueryNodeException {
      if (node instanceof FuzzyQueryNode || node instanceof WildcardQueryNode) {   //③
        throw new QueryNodeException(new MessageImpl("no"));
      }
      return node;
    }
    protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
      return node;
    }
    protected List<QueryNode> setChildrenOrder(List<QueryNode> children) {
      return children;
    }
  }

  private class SpanNearPhraseQueryBuilder implements StandardQueryBuilder {
    public Query build(QueryNode queryNode) throws QueryNodeException {
      TokenizedPhraseQueryNode phraseNode = (TokenizedPhraseQueryNode) queryNode;

      List<QueryNode> children = phraseNode.getChildren();   // ④

      SpanTermQuery[] clauses;
      if (children != null) {
        int numTerms = children.size();
        clauses = new SpanTermQuery[numTerms];
        for (int i=0;i<numTerms;i++) {
          FieldQueryNode termNode = (FieldQueryNode) children.get(i);
          TermQuery termQuery = (TermQuery) termNode
            .getTag(QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);
          clauses[i] = new SpanTermQuery(termQuery.getTerm());
        }
      } else {
        clauses = new SpanTermQuery[0];
      }

      return new SpanNearQuery(clauses, 0, true); // ⑤
    }
  }

  public class SlopQueryNodeBuilder implements StandardQueryBuilder {  // ⑥

    public Query build(QueryNode queryNode) throws QueryNodeException {
      SlopQueryNode phraseSlopNode = (SlopQueryNode) queryNode;
      
      Query query = (Query) phraseSlopNode.getChild().getTag(
                           QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);

      int slop = phraseSlopNode.getValue();

      if (query instanceof PhraseQuery) {
        Term[] terms = ((PhraseQuery) query).getTerms();
        PhraseQuery.Builder builder = new PhraseQuery.Builder();
        builder.setSlop(slop);
        for (Term term: terms){
          builder.add(term);
        }
        return builder.build();
      } else if (query instanceof MultiPhraseQuery) {
        MultiPhraseQuery.Builder builder = new MultiPhraseQuery.Builder((MultiPhraseQuery) query);
        builder.setSlop(slop);
        return builder.build();
      }

      return query;
    }
  }
}

/*
  ① 安装自定义节点处理器
  ② 安装两个自定义查询构建器
  ③ 防止模糊和通配符查询
  ④ 拿取短语中全部的词项
  ⑤ 创建 SpanNearQuery
  ⑥ 重写 slop 查询节点
*/
