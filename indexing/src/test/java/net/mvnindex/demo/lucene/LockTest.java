package net.mvnindex.demo.lucene;

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

import junit.framework.TestCase;
import net.mvnindex.demo.lucene.common.TestUtil;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

// From chapter 2
public class LockTest {

  private Directory dir;
  private File indexDir;

  @Before
  public void setUp() throws IOException {
    indexDir = new File(
      System.getProperty("java.io.tmpdir", "tmp") +
      System.getProperty("file.separator") + "index");
    dir = FSDirectory.open(indexDir.toPath());
  }

  @Test
  public void testWriteLock() throws IOException {

    IndexWriterConfig writerConfig = new IndexWriterConfig(new SimpleAnalyzer());
    IndexWriterConfig writerConfig2 = new IndexWriterConfig(new SimpleAnalyzer());
    IndexWriter writer1 = new IndexWriter(dir, writerConfig);
    IndexWriter writer2 = null;
    try {
      writer2 = new IndexWriter(dir, writerConfig2);
      fail("We should never reach this point");
    }
    catch (LockObtainFailedException e) {
      // e.printStackTrace();  // #A
    }
    finally {
      writer1.close();
      assertNull(writer2);
      TestUtil.rmDir(indexDir);
    }
  }
}

/*
#A Expected exception: only one IndexWriter allowed at once
*/
