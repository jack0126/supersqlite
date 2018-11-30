package com.android.jack.supersqlite;

import android.database.Cursor;
import android.database.SQLException;

/**
 * Created by hesanxing on 2018/11/29.
 *
 * @author jack
 */

interface IJavaTypeConverter<T> {
    IJavaTypeConverter<String> STRING_CONVERTER = new IJavaTypeConverter<String>() {
        @Override
        public String convert(Cursor cursor, String name) throws SQLException {
            return cursor.getString(cursor.getColumnIndexOrThrow(name));
        }
    };
    IJavaTypeConverter<Boolean> BOOLEAN_CONVERTER = new IJavaTypeConverter<Boolean>() {
        @Override
        public Boolean convert(Cursor cursor, String name) throws SQLException {
            return Boolean.parseBoolean(cursor.getString(cursor.getColumnIndexOrThrow(name)));
        }
    };
    IJavaTypeConverter<Character> CHAR_CONVERTER = new IJavaTypeConverter<Character>() {
        @Override
        public Character convert(Cursor cursor, String name) throws SQLException {
            return cursor.getString(cursor.getColumnIndexOrThrow(name)).charAt(0);
        }
    };
    IJavaTypeConverter<Byte> BYTE_CONVERTER = new IJavaTypeConverter<Byte>() {
        @Override
        public Byte convert(Cursor cursor, String name) throws SQLException {
            return (byte) cursor.getInt(cursor.getColumnIndexOrThrow(name));
        }
    };
    IJavaTypeConverter<Short> SHORT_CONVERTER = new IJavaTypeConverter<Short>() {
        @Override
        public Short convert(Cursor cursor, String name) throws SQLException {
            return cursor.getShort(cursor.getColumnIndexOrThrow(name));
        }
    };
    IJavaTypeConverter<Integer> INT_CONVERTER = new IJavaTypeConverter<Integer>() {
        @Override
        public Integer convert(Cursor cursor, String name) throws SQLException {
            return cursor.getInt(cursor.getColumnIndexOrThrow(name));
        }
    };
    IJavaTypeConverter<Long> LONG_CONVERTER = new IJavaTypeConverter<Long>() {
        @Override
        public Long convert(Cursor cursor, String name) throws SQLException {
            return cursor.getLong(cursor.getColumnIndexOrThrow(name));
        }
    };
    IJavaTypeConverter<Float> FLOAT_CONVERTER = new IJavaTypeConverter<Float>() {
        @Override
        public Float convert(Cursor cursor, String name) throws SQLException {
            return cursor.getFloat(cursor.getColumnIndexOrThrow(name));
        }
    };
    IJavaTypeConverter<Double> DOUBLE_CONVERTER = new IJavaTypeConverter<Double>() {
        @Override
        public Double convert(Cursor cursor, String name) throws SQLException {
            return cursor.getDouble(cursor.getColumnIndexOrThrow(name));
        }
    };
    T convert(Cursor cursor, String name) throws SQLException;
}
