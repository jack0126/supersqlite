package com.android.jack.supersqlite;

/**
 * Created by hesanxing on 2018/11/29.
 *
 * @author jack
 */

interface ISQLTypeConverter {
    ISQLTypeConverter TEXT_CONVERTER = new ISQLTypeConverter() {
        @Override
        public String convert(Object value) {
            return value == null ? "NULL" : "'" + value + "'";
        }
    };
    ISQLTypeConverter INTEGER_CONVERTER = new ISQLTypeConverter() {
        @Override
        public String convert(Object value) {
            return value == null ? "0" : value.toString();
        }
    };
    ISQLTypeConverter REAL_CONVERTER = new ISQLTypeConverter() {
        @Override
        public String convert(Object value) {
            return value == null ? "0.0" : value.toString();
        }
    };
    String convert(Object value);
}
