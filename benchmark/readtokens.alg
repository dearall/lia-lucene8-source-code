analyzer=org.apache.lucene.analysis.standard.StandardAnalyzer
content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource
docs.file=K:\\wikimedia.org\\enwiki-latest-pages-articles.lines.txt

{ "Rounds"
    ResetSystemErase
    { ReadTokens > : 200000
    NewRound
} : 3

RepSumByPrefRound ReadTokens