analyzer=org.apache.lucene.analysis.standard.StandardAnalyzer
content.source=org.apache.lucene.benchmark.byTask.feeds.LineDocSource

directory=FSDirectory

doc.stored = true
doc.term.vectors = true
merge.factor = 2000
content.source.forever = false
docs.file=K:\\wikimedia.org\\enwiki-latest-pages-articles.lines.txt

doc.reuse.fields = false
writer.num.threads = 4
writer.max.thread.queue.size = 20

{ "Rounds"
    ResetSystemErase

    { "BuildIndex"
        -CreateThreadedIndex
        { "AddDocs" AddDoc > : 200000
            -CloseIndex
    }
    NewRound

} : 3

RepSumByPrefRound BuildIndex