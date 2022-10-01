package net.mvnindex.demo.lucene.extsearch;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.FieldInfo;

public class IntPointDocValuesField extends Field {
    public static final FieldType TYPE = new FieldType();
    static {
        TYPE.setDocValuesType(DocValuesType.SORTED_NUMERIC);
        TYPE.freeze();
    }

    public IntPointDocValuesField(String name, int x, int y) {
        super(name, TYPE);
        setLocationValue(x, y);
    }

    public void setLocationValue(int x, int y) {
        fieldsData = Long.valueOf((((long) x) << 32) | (y & 0xFFFFFFFFL));
    }
    /** helper: checks a fieldinfo and throws exception if its definitely not a IntPointDocValuesField */
    static void checkCompatible(FieldInfo fieldInfo) {
        // dv properties could be "unset", if you e.g. used only StoredField with this same name in the segment.
        if (fieldInfo.getDocValuesType() != DocValuesType.NONE && fieldInfo.getDocValuesType() != TYPE.docValuesType()) {
            throw new IllegalArgumentException("field=\"" + fieldInfo.name + "\" was indexed with docValuesType=" + fieldInfo.getDocValuesType() +
                    " but this type has docValuesType=" + TYPE.docValuesType() +
                    ", is the field really a IntPointDocValuesField?");
        }
    }
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getClass().getSimpleName());
        result.append(" <");
        result.append(name);
        result.append(':');

        long currentValue = (Long)fieldsData;
        result.append((int)(currentValue >> 32));
        result.append(',');
        result.append((int)(currentValue & 0xFFFFFFFF));

        result.append('>');
        return result.toString();
    }

}
