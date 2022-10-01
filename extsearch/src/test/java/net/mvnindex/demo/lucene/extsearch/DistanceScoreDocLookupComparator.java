package net.mvnindex.demo.lucene.extsearch;

import org.apache.lucene.geo.XYEncodingUtils;
import org.apache.lucene.geo.XYRectangle;
import org.apache.lucene.index.*;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.LeafFieldComparator;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.util.ArrayUtil;

import java.io.IOException;

public class DistanceScoreDocLookupComparator extends FieldComparator<Double>
        implements LeafFieldComparator {        // ①
    final String field;
    final int x;
    final int y;

    SortedNumericDocValues currentDocs;         // ②

    // distances needs to be calculated with square root to
    // avoid numerical issues (square distances are different but
    // actual distances are equal)
    final double[] values;                      // ③

    double bottom;                              // ④
    double topValue;

    // current bounding box(es) for the bottom distance on the PQ.
    // these are pre-encoded with XYPoint's encoding and
    // used to exclude uncompetitive hits faster.
    int minX = Integer.MIN_VALUE;
    int maxX = Integer.MAX_VALUE;
    int minY = Integer.MIN_VALUE;
    int maxY = Integer.MAX_VALUE;

    // the number of times setBottom has been called (adversary protection)
    int setBottomCounter = 0;

    private long[] currentValues = new long[4];
    private int valuesDocID = -1;

    public DistanceScoreDocLookupComparator(String fieldName, int x, int y, int numHits) {
        this.field = fieldName;
        this.x = x;
        this.y = y;

        this.values = new double[numHits];
    }

    @Override
    public LeafFieldComparator getLeafComparator(LeafReaderContext context) throws IOException {
        LeafReader reader = context.reader();
        FieldInfo info = reader.getFieldInfos().fieldInfo(field);
        if (info != null) {
            IntPointDocValuesField.checkCompatible(info);
        }
        currentDocs = DocValues.getSortedNumeric(reader, field); // ⑤
        valuesDocID = -1;
        return this;
    }

    double sortKey(int doc) throws IOException {                 // ⑥
        if (doc > currentDocs.docID()) {
            currentDocs.advance(doc);
        }
        double minValue = Double.POSITIVE_INFINITY;
        if (doc == currentDocs.docID()) {
            setValues();
            int numValues = currentDocs.docValueCount();
            for (int i = 0; i < numValues; i++) {
                long encoded = currentValues[i];
                double docX = (int)(encoded >> 32);
                double docY = (int)(encoded & 0xFFFFFFFF);
                final double diffX = x - docX;
                final double diffY = y - docY;
                double distance =  Math.sqrt(diffX * diffX + diffY * diffY);
                minValue = Math.min(minValue, distance);
            }
        }
        return minValue;
    }

    @Override
    public int compare(int slot1, int slot2) {                   // ⑦
        return Double.compare(values[slot1], values[slot2]);
    }

    @Override
    public void setTopValue(Double value) {
        topValue = value.doubleValue();
    }

    @Override
    public void setBottom(int slot) throws IOException {         // ⑧
        bottom = values[slot];
        // make bounding box(es) to exclude non-competitive hits, but start
        // sampling if we get called way too much: don't make gobs of bounding
        // boxes if comparator hits a worst case order (e.g. backwards distance order)
        if (bottom < Float.MAX_VALUE && (setBottomCounter < 1024 || (setBottomCounter & 0x3F) == 0x3F)) {

            XYRectangle rectangle = XYRectangle.fromPointDistance((float) x, (float) y, (float) bottom);
            // pre-encode our box to our integer encoding, so we don't have to decode
            // to double values for uncompetitive hits. This has some cost!
            this.minX = XYEncodingUtils.encode(rectangle.minX);
            this.maxX = XYEncodingUtils.encode(rectangle.maxX);
            this.minY = XYEncodingUtils.encode(rectangle.minY);
            this.maxY = XYEncodingUtils.encode(rectangle.maxY);
        }
        setBottomCounter++;
    }

    private void setValues() throws IOException {
        if (valuesDocID != currentDocs.docID()) {
            assert valuesDocID < currentDocs.docID(): " valuesDocID=" + valuesDocID + " vs " + currentDocs.docID();
            valuesDocID = currentDocs.docID();
            int count = currentDocs.docValueCount();
            if (count > currentValues.length) {
                currentValues = new long[ArrayUtil.oversize(count, Long.BYTES)];
            }
            for(int i=0;i<count;i++) {
                currentValues[i] = currentDocs.nextValue();
            }
        }
    }

    @Override
    public int compareBottom(int doc) throws IOException {       // ⑨
        if (doc > currentDocs.docID()) {
            currentDocs.advance(doc);
        }
        if (doc < currentDocs.docID()) {
            return Double.compare(bottom, Double.POSITIVE_INFINITY);
        }

        setValues();

        int numValues = currentDocs.docValueCount();

        int cmp = -1;
        for (int i = 0; i < numValues; i++) {
            long encoded = currentValues[i];

            // test bounding box
            int xBits = (int)(encoded >> 32);
            if (xBits < minX || xBits > maxX) {
                continue;
            }
            int yBits = (int)(encoded & 0xFFFFFFFF);
            if (yBits < minY || yBits > maxY) {
                continue;
            }

            // only compute actual distance if its inside "competitive bounding box"
            double docX = xBits;
            double docY = xBits;
            final double diffX = x - docX;
            final double diffY = y - docY;
            double distance =  Math.sqrt(diffX * diffX + diffY * diffY);
            cmp = Math.max(cmp, Double.compare(bottom, distance));
            // once we compete in the PQ, no need to continue.
            if (cmp > 0) {
                return cmp;
            }
        }
        return cmp;
    }

    @Override
    public void setScorer(Scorable scorer) throws IOException {}

    @Override
    public void copy(int slot, int doc) throws IOException {        // ⑩
        values[slot] = sortKey(doc);
    }

    @Override
    public int compareTop(int doc) throws IOException {
        return Double.compare(topValue, sortKey(doc));
    }

    @Override
    public Double value(int slot) {                                 // ⑪
        return values[slot];
    }
}

/*
    ① FieldComparator 实现，同时实现 LeafFieldComparator 接口
    ② 临时保存当前段内域的 SortedNumericDocValues 对象
    ③ 队列中每个文档表示距离数组
    ④ 表示队列中最低距离的值
    ⑤ 从索引中获取位置的 DocValues 对象
    ⑥ 为一个文档计算距离
    ⑦ 在 top N 列表中比较两个距离
    ⑧ 在 top N 列表中记录最低评分文档
    ⑨ 将新文档与最低评分文档比较
    ⑩ 将新文档评分插入到 top N 列表指定位置
    ⑪ 从 top N 列表中取出指定位置的值
 */