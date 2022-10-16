package net.mvnindex.demo.lucene.benchmark;

import org.apache.lucene.benchmark.byTask.Benchmark;

public class Benchmarker {
    public static void main(String[] args) {
        // verify command line args
        if (args.length < 1) {
            System.err.println("Usage: java -jar benchmark-1.0-SNAPSHOT-shaded.jar <algorithm file>");
            System.exit(1);
        }
        Benchmark.exec(args);
    }
}
