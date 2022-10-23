package org.apache.lucene.benchmark.byTask.tasks;


import net.mvnindex.demo.lucene.admin.ThreadedIndexWriter;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

import java.io.IOException;

// From chapter 11

/** A task that you can use from a contrib/benchmark
 *  algorithm to create a ThreadedIndexWriter. */

public class CreateThreadedIndexTask extends CreateIndexTask {

  public CreateThreadedIndexTask(PerfRunData runData) {
    super(runData);
  }

  public int doLogic() throws IOException {
    PerfRunData runData = getRunData();
    Config config = runData.getConfig();

    IndexWriterConfig iwconfig = createWriterConfig(config, runData,
            IndexWriterConfig.OpenMode.CREATE, null);
    IndexWriter writer = new ThreadedIndexWriter(
                                runData.getDirectory(), iwconfig,
                                config.get("writer.num.threads", 4),
                                config.get("writer.max.thread.queue.size", 20));

    runData.setIndexWriter(writer);

    return 1;
  }
}
