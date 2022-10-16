package net.mvnindex.demo.lucene.benchmark;

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

import org.apache.lucene.benchmark.quality.*;
import org.apache.lucene.benchmark.quality.trec.TrecJudge;
import org.apache.lucene.benchmark.quality.trec.TrecTopicsReader;
import org.apache.lucene.benchmark.quality.utils.SimpleQQParser;
import org.apache.lucene.benchmark.quality.utils.SubmissionReport;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Paths;

// From chapter 9

/* This code was extracted from the Lucene
   contrib/benchmarker sources */

public class PrecisionRecall {

  public static void main(String[] args) throws Throwable {

    File topicsFile = new File("benchmark/topics.txt");
    File qrelsFile = new File("benchmark/qrels.txt");

    Directory dir = FSDirectory.open(Paths.get("meetlucene/indexes/MeetLucene"));
    DirectoryReader reader = DirectoryReader.open(dir);
    IndexSearcher searcher = new IndexSearcher(reader);

    String docNameField = "filename"; 
    
    PrintWriter logger = new PrintWriter(System.out, true); 

    TrecTopicsReader qReader = new TrecTopicsReader();   //①
    QualityQuery qqs[] = qReader.readQueries(            //①
        new BufferedReader(new FileReader(topicsFile))); //①
    
    Judge judge = new TrecJudge(new BufferedReader(      //②
        new FileReader(qrelsFile)));                     //②
    
    judge.validateData(qqs, logger);                     //③
    
    QualityQueryParser qqParser = new SimpleQQParser("title", "contents");  //④
    
    QualityBenchmark qrun = new QualityBenchmark(qqs, qqParser, searcher, docNameField);
    SubmissionReport submitLog = null;
    QualityStats stats[] = qrun.execute(judge,           //⑤
            submitLog, logger);
    QualityStats avg = QualityStats.average(stats);      //⑥
    avg.log("SUMMARY",2,logger, "  ");//⑥

    reader.close();
    dir.close();
  }
}

/*
① 读取 TREC topics 文件创建 QualityQuery[] 数组
② 使用 TrecJudge 加载 TREC 的 Qrel 格式文件，创建 Judge 实例
③ 验证查询与 Judge 匹配
④ 创建 QualityQueryParser 用于将质量查询解析为真正的 Lucene 查询
⑤ 执行基线测试 benchmarker
⑥ 打印精确度（precision）和召回（recall）值指标
*/
