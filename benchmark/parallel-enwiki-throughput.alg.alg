analyzer=org.apache.lucene.analysis.standard.StandardAnalyzer
content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource
directory=FSDirectory

docs.file=K:\\wikimedia.org\\enwiki-latest-pages-articles.lines.txt

doc.stored = true
doc.term.vector = true
ram.flush.mb = 128
compound = false
merge.factor = 30
log.step=1000

{ "Rounds"
    ResetSystemErase
    { "BuildIndex"
        -CreateIndex
        [ { "AddDocs" AddDoc > : 40000] : 5
        -CloseIndex
    }
    NewRound
} : 3

RepSumByPrefRound BuildIndex
