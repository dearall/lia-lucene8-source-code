package net.mvnindex.demo.lucene.extsearch;

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

import org.apache.lucene.document.XYDocValuesField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;

import java.io.IOException;

// From chapter 6
public class DistanceComparatorSource extends FieldComparatorSource { // ①
  private int x;
  private int y;

  public DistanceComparatorSource(int x, int y) {                     // ②
    this.x = x;
    this.y = y;
  }

  @Override
  public FieldComparator<?> newComparator(String fieldname,           // ③
                                          int numHits,
                                          int sortPos,
                                          boolean reversed) {
    System.out.println("newComparator(): fieldname = "+ fieldname + ", numHits = "+ numHits +
            ", sortPos = " + sortPos + ", reversed = "+ reversed);
    return new DistanceScoreDocLookupComparator(fieldname, x, y, numHits);
  }

  public String toString() {
    return "Distance from ("+x+","+y+")";
  }
}

/*
① 继承自 FieldComparatorSource 类
② 通过构造器传递基位置
③ 实现 FieldComparatorSource 中唯一的虚方法 newComparator()，创建比较器返回。其中 fieldname 参数是从哪个域获取相关数据，numHits 参数表明存储在
最高评分值列表中的命中数量，sortPos 参数表明这个 SortField 在 Sort 中位置，如果是主比较器，sortPos==0，第二比较器
sortPos==1，以此类推。有些比较器在作为主比较器时能够对其自身进行优化。reversed 参数表明是否对其自然顺序进行倒转
*/

