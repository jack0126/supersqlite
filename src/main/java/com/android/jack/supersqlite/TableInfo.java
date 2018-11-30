package com.android.jack.supersqlite;

import android.support.annotation.NonNull;

import com.android.jack.supersqlite.constraint.AutoIncrement;
import com.android.jack.supersqlite.constraint.Check;
import com.android.jack.supersqlite.constraint.DeclareType;
import com.android.jack.supersqlite.constraint.Default;
import com.android.jack.supersqlite.constraint.NotNull;
import com.android.jack.supersqlite.constraint.PrimaryKey;
import com.android.jack.supersqlite.constraint.Unique;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/8/4.
 * The document in project GameRoom.
 *
 * @author Jack
 */

class TableInfo {

    String name;
    List<ColumnInfo>columnInfos;
    ColumnInfo primaryKey;

    private Map<String, ColumnInfo>columnInfoMap;

    static class ColumnInfo implements Comparable<ColumnInfo> {
        String name;
        Field field;
        Class<?> fieldType;
        boolean hasDeclareType;
        String sqlType;
        boolean isPrimaryKey;
        boolean hasAutoIncrement;
        boolean hasUnique;
        boolean isNotNull;
        boolean hasDefault;
        boolean hasCheck;
        String defaultValue;
        String checkExpression;
        ISQLTypeConverter sqlTypeConverter;
        IJavaTypeConverter<?> javaTypeConverter;

        private ColumnInfo(Field field) {
            name = parseColumnName(field);
            fieldType = field.getType();
            this.field = field;
            hasDeclareType = field.isAnnotationPresent(DeclareType.class);
            isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
            hasAutoIncrement = field.isAnnotationPresent(AutoIncrement.class);
            hasUnique = field.isAnnotationPresent(Unique.class);
            isNotNull = field.isAnnotationPresent(NotNull.class);
            hasDefault = field.isAnnotationPresent(Default.class);
            if (hasDefault) {
                defaultValue = field.getAnnotation(Default.class).value();
            }
            if (hasCheck) {
                checkExpression = field.getAnnotation(Check.class).value();
            }
            if (hasDeclareType) {
                sqlType = field.getAnnotation(DeclareType.class).value();
            }

            if (fieldType == String.class) {
                sqlType = hasDeclareType ? sqlType : "TEXT";
                sqlTypeConverter = ISQLTypeConverter.TEXT_CONVERTER;
                javaTypeConverter = IJavaTypeConverter.STRING_CONVERTER;
            } else if (fieldType == int.class || fieldType == Integer.class) {
                sqlType = hasDeclareType ? sqlType : "INTEGER";
                sqlTypeConverter = ISQLTypeConverter.INTEGER_CONVERTER;
                javaTypeConverter = IJavaTypeConverter.INT_CONVERTER;
            } else if (fieldType == long.class || fieldType == Long.class) {
                sqlType = hasDeclareType ? sqlType : "INTEGER";
                sqlTypeConverter = ISQLTypeConverter.INTEGER_CONVERTER;
                javaTypeConverter = IJavaTypeConverter.LONG_CONVERTER;
            } else if (fieldType == double.class || fieldType == Double.class) {
                sqlType = hasDeclareType ? sqlType : "REAL";
                sqlTypeConverter = ISQLTypeConverter.REAL_CONVERTER;
                javaTypeConverter = IJavaTypeConverter.DOUBLE_CONVERTER;
            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                sqlType = hasDeclareType ? sqlType : "TEXT";
                sqlTypeConverter = ISQLTypeConverter.TEXT_CONVERTER;
                javaTypeConverter = IJavaTypeConverter.BOOLEAN_CONVERTER;
            } else if (fieldType == float.class || fieldType == Float.class) {
                sqlType = hasDeclareType ? sqlType : "REAL";
                sqlTypeConverter = ISQLTypeConverter.REAL_CONVERTER;
                javaTypeConverter = IJavaTypeConverter.FLOAT_CONVERTER;
            } else if (fieldType == char.class || fieldType == Character.class) {
                sqlType = hasDeclareType ? sqlType : "TEXT";
                sqlTypeConverter = ISQLTypeConverter.TEXT_CONVERTER;
                javaTypeConverter = IJavaTypeConverter.CHAR_CONVERTER;
            } else if (fieldType == byte.class || fieldType == Byte.class) {
                sqlType = hasDeclareType ? sqlType : "INTEGER";
                sqlTypeConverter = ISQLTypeConverter.INTEGER_CONVERTER;
                javaTypeConverter = IJavaTypeConverter.BYTE_CONVERTER;
            } else if (fieldType == short.class || fieldType == Short.class) {
                sqlType = hasDeclareType ? sqlType : "INTEGER";
                sqlTypeConverter = ISQLTypeConverter.INTEGER_CONVERTER;
                javaTypeConverter = IJavaTypeConverter.SHORT_CONVERTER;
            } else {
                throw new NotSupportedFieldTypeException("field: an unsupported field type: " + fieldType.getName() + " to field " + field.getName());
            }
        }

        private String parseColumnName(Field field) {
            if (field.isAnnotationPresent(Binding.class)) {
                String name = field.getAnnotation(Binding.class).value();
                if (name.trim().isEmpty()) {
                    throw new BindingException("field: " + field.getName() + " to binding \"" + name + "\" of column");
                }
                return name;
            } else {
                return field.getName();
            }
        }

        @Override
        public int compareTo(@NonNull ColumnInfo columnInfo) {
            final String s1 = name, s2 = columnInfo.name;
            final int l1 = s1.length(), l2 = s2.length();
            final int size = Math.min(l1, l2);

            for (int i = 0; i < size; i++) {
                char c1 = s1.charAt(i), c2 = s2.charAt(i);
                if (c1 > c2) {
                    return 1;
                } else if (c1 < c2) {
                    return -1;
                }
            }

            return l1 > l2 ? 1 : -1;
        }

    }

    TableInfo(Class<?>cls) {
        name = parseTableName(cls);
        columnInfos = parseColumnInfos(cls);
        columnInfoMap = generateColumnInfoMap(columnInfos);
    }

    ColumnInfo getColumnInfo(String columnName) {
        return columnInfoMap.get(columnName);
    }

    private Map<String, ColumnInfo> generateColumnInfoMap(List<ColumnInfo>columnInfos) {
        HashMap<String, ColumnInfo>map = new HashMap<>();
        for (ColumnInfo columnInfo : columnInfos) {
            map.put(columnInfo.name, columnInfo);
            if (columnInfo.isPrimaryKey) {
                primaryKey = columnInfo;
            }
        }
        return map;
    }

    private String parseTableName(Class<?>cls) {
        if (cls.isAnnotationPresent(Binding.class)) {
            String name = cls.getAnnotation(Binding.class).value();
            if (name.trim().isEmpty()) {
                throw new BindingException("class: " + cls.getName() + " to binding \"" + name + "\" of table");
            }
            return name;
        } else {
            return cls.getSimpleName();
        }
    }

    private List<ColumnInfo> parseColumnInfos(Class<?>cls) {
        Field[]fields = cls.getDeclaredFields();
        List<ColumnInfo>result = new ArrayList<>(fields.length);

        for (Field field : fields) {
            //跳过静态字段、常量和临时变量
            int modifiers = field.getModifiers();
            if ((modifiers & Modifier.STATIC) == Modifier.STATIC ||
                    (modifiers & Modifier.FINAL) == Modifier.FINAL ||
                    (modifiers & Modifier.TRANSIENT) == Modifier.TRANSIENT) {
                continue;
            }

            result.add(new ColumnInfo(field));
        }// end for
        Collections.sort(result);
        return result;
    }

    private String sqlDescriptionCache;
    String getSQLDescription() {
        if (sqlDescriptionCache != null) {
            return sqlDescriptionCache;
        }

        StringBuilder sql = new StringBuilder(512)
                .append("CREATE TABLE IF NOT EXISTS ").append(name)
                .append("(");

        for (int i = 0; i < columnInfos.size(); i++) {
            TableInfo.ColumnInfo columnInfo = columnInfos.get(i);
            String name = columnInfo.name;

            if (i != 0) {
                sql.append(", ");
            }
            sql.append(name).append(' ').append(columnInfo.sqlType);

            // append constraint
            if (columnInfo.isNotNull) {
                sql.append(" NOT NULL");
            }
            if (columnInfo.hasDefault) {
                sql.append(" DEFAULT ").append(columnInfo.defaultValue);
            }
            if (columnInfo.isPrimaryKey) {
                sql.append(" PRIMARY KEY");
            }
            if (columnInfo.hasAutoIncrement) {
                sql.append(" AUTOINCREMENT");
            }
            if (columnInfo.hasUnique) {
                sql.append(" UNIQUE");
            }
            if (columnInfo.hasCheck) {
                sql.append(" CHECK(").append(columnInfo.checkExpression).append(")");
            }
        }

        sql.append(")");
        sqlDescriptionCache = sql.toString();
        return sqlDescriptionCache;
    }

}
