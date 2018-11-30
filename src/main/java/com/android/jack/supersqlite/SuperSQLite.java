package com.android.jack.supersqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/8/3.
 * The document in project SuperSQLite.
 *
 * @author Jack
 */

public class SuperSQLite extends SQLiteOpenHelper {

    private static final String TAG = SuperSQLite.class.getSimpleName();
    private static final Map<Class<?>, TableInfo>sTableInfoCache = new HashMap<>(32);

    public SuperSQLite(Context context, String name) {
        super(context, name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private TableInfo getTableInfo(Class<?>cls, boolean autoCreation) throws SQLException {
        TableInfo tableInfo = sTableInfoCache.get(cls);
        if (tableInfo != null) {
            return tableInfo;
        }

        tableInfo = new TableInfo(cls);

        if (autoCreation) {
            String sql = tableInfo.getSQLDescription();
            Log.i(TAG, "SQL description: " + sql);
            getWritableDatabase().execSQL(sql);
        }

        sTableInfoCache.put(cls, tableInfo);
        return tableInfo;
    }

    public final void createTable(Class<?> clsTable) throws SQLException {
        TableInfo tableInfo = getTableInfo(clsTable, false);
        String sql = tableInfo.getSQLDescription();
        Log.i(TAG, "createTable: " + sql);
        getWritableDatabase().execSQL(sql);
    }

    @SafeVarargs
    public final <T>void insert(Class<T>clsTable, T...items) throws SQLException {
        if (items == null || items.length == 0) {
            return;
        }

        TableInfo tableInfo = getTableInfo(clsTable, true);

        StringBuilder sqlBuilder = new StringBuilder(512)
                .append("INSERT INTO ").append(tableInfo.name)
                .append(" VALUES");
        for (int i = 0; i < items.length; i++) {
            if (i != 0) {
                sqlBuilder.append(", ");
            }
            sqlBuilder.append('(');
            appendContentForValues(sqlBuilder, tableInfo, items[i]);
            sqlBuilder.append(')');
        }

        String sql = sqlBuilder.toString();
        Log.i(TAG, "insert: " + sql);
        getWritableDatabase().execSQL(sql);
    }

    /**
     * 插入数据或更新数据，依懒主键
     * @param items
     * @throws SQLException
     */
    @SafeVarargs
    public final <T>void insertOrUpdate(Class<T>clsTable, T...items) throws SQLException {
        if (items == null || items.length == 0) {
            return;
        }

        TableInfo tableInfo = getTableInfo(clsTable, true);

        StringBuilder sqlBuilder = new StringBuilder(512)
                .append("INSERT OR REPLACE INTO ").append(tableInfo.name)
                .append(" VALUES");
        for (int i = 0; i < items.length; i++) {
            if (i != 0) {
                sqlBuilder.append(", ");
            }
            sqlBuilder.append('(');
            appendContentForValues(sqlBuilder, tableInfo, items[i]);
            sqlBuilder.append(')');
        }

        String sql = sqlBuilder.toString();
        Log.i(TAG, "insertOrUpdate: " + sql);
        getWritableDatabase().execSQL(sql);
    }

    private void appendContentForValues(StringBuilder sqlBuilder, TableInfo tableInfo, Object value) {
        try {
            for (int i = 0; i < tableInfo.columnInfos.size(); i++) {
                TableInfo.ColumnInfo columnInfo = tableInfo.columnInfos.get(i);
                ISQLTypeConverter sqlTypeConverter = columnInfo.sqlTypeConverter;
                Field field = columnInfo.field;
                field.setAccessible(true);
                Object oval = field.get(value);

                if (i != 0) {
                    sqlBuilder.append(", ");
                }

                if (columnInfo.hasAutoIncrement && (oval == null || ((int) oval) == 0)) {
                    sqlBuilder.append("NULL");
                } else {
                    sqlBuilder.append(sqlTypeConverter.convert(oval));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除数据，依懒主键
     * @param items
     * @throws SQLException
     */
    @SafeVarargs
    public final <T>void delete(Class<T>clsTable, T...items) throws SQLException {
        if (items == null || items.length == 0) {
            return;
        }

        TableInfo tableInfo = getTableInfo(clsTable, false);

        if (tableInfo.primaryKey == null) {
            throw new SQLException("the primary key cannot be found in table definition of " + clsTable.getSimpleName());
        }

        StringBuilder whereBuilder = new StringBuilder(128);

        try {
            for (int i = 0; i < items.length; i++) {
                tableInfo.primaryKey.field.setAccessible(true);
                Object primaryKeyValue = tableInfo.primaryKey.field.get(items[i]);

                if (i != 0) {
                    whereBuilder.append(" OR ");
                }
                whereBuilder.append(tableInfo.primaryKey.name).append(" = ");
                if (primaryKeyValue == null) {
                    whereBuilder.append("NULL");
                } else {
                    whereBuilder.append(',').append(primaryKeyValue).append(',');
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        delete(clsTable, whereBuilder.toString());
    }

    /**
     *
     * @param where 不可为空
     */
    public final void delete(Class<?>clsTable, String where) throws SQLException {
        if (where == null || where.trim().isEmpty()) {
            throw new SQLException("'where' can not be empty.");
        }

        TableInfo tableInfo = getTableInfo(clsTable, false);

        StringBuilder sqlBuilder = new StringBuilder(128)
                .append("DELETE FROM ").append(tableInfo.name)
                .append(" WHERE ").append(where);

        String sql = sqlBuilder.toString();
        Log.i(TAG, "delete: " + sql);
        getWritableDatabase().execSQL(sql);
    }

    /**
     * 查询所有符合条件的数据，如："id = 5"
     * @param where
     * @return
     */
    public final <T>List<T> query(Class<T>clsTable, String where) throws SQLException {
        TableInfo tableInfo = getTableInfo(clsTable, false);

        StringBuilder sqlBuilder = new StringBuilder(128)
                .append("SELECT * FROM ").append(tableInfo.name);
        if (where != null) {
            sqlBuilder.append(" WHERE ").append(where);
        }

        String sql = sqlBuilder.toString();
        Log.i(TAG, "query: " + sql);
        return rawQuery(sql, clsTable, new LinkedList<T>());
    }

    /**
     * 分页查询符合条件的数据
     * @param where 如："id = 5 OR name = 'jack'"
     * @param offset
     * @param count
     * @return
     */
    public final <T>List<T> query(Class<T>clsTable, String where, int offset, int count) throws SQLException {
        if (offset < 0) {
            offset = 0;
        }

        if (count < 0) {
            count = -1;
        }

        List<T> list = new ArrayList<>(count);
        if (count == 0) {
            return list;
        }

        TableInfo tableInfo = getTableInfo(clsTable, false);

        StringBuilder sqlBuilder = new StringBuilder(128)
                .append("SELECT * FROM ").append(tableInfo.name);
        if (where != null) {
            sqlBuilder.append(" WHERE ").append(where);
        }
        sqlBuilder.append(" LIMIT ").append(offset).append(", ").append(count);

        String sql = sqlBuilder.toString();
        Log.i(TAG, "query: " + sql);
        return rawQuery(sql, clsTable, list);
    }

    public final <T>List<T> rawQuery(String sql, Class<T>cls, List<T>outResult) throws SQLException {
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery(sql, null);
            boolean hasNext = cursor != null && cursor.moveToFirst();
            while (hasNext) {
                T t = readRow(cursor, cls);
                outResult.add(t);
                hasNext = cursor.moveToNext();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return outResult;
    }

    protected <T>T readRow(Cursor cursor, Class<T>cls) throws SQLException {
        T t;
        try {
            t = cls.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        TableInfo tableInfo = getTableInfo(cls, false);
        String[]columnNames = cursor.getColumnNames();
        try {
            for (String columnName : columnNames) {
                TableInfo.ColumnInfo columnInfo = tableInfo.getColumnInfo(columnName);
                if (columnInfo == null) {
                    Log.w(TAG, "readRow: column info not found in table " + tableInfo.name + ": " + columnName);
                    continue;
                }

                IJavaTypeConverter<?> javaTypeConverter = columnInfo.javaTypeConverter;
                String name = columnInfo.name;
                Field field = columnInfo.field;
                field.setAccessible(true);
                field.set(t, javaTypeConverter.convert(cursor, name));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return t;
    }

    /**
     * 查询数据总条数量
     * @return
     */
    public final <T>int getCount(Class<T>clsTable) {
        return getCount(clsTable, null);
    }

    /**
     * 查询符合条件数据的数量
     * @param where 如："id = 5 AND name = 'jack'"
     * @return
     */
    public final int getCount(Class<?>clsTable, String where) throws SQLException {
        TableInfo tableInfo = getTableInfo(clsTable, false);
        String countAlias = "count";
        StringBuilder sqlBuilder = new StringBuilder(128)
                .append("SELECT COUNT(*) AS ").append(countAlias).append(" FROM ").append(tableInfo.name);

        if (where != null) {
            sqlBuilder.append(" WHERE ").append(where);
        }

        Cursor cursor = null;
        try {
            String sql = sqlBuilder.toString();
            Log.i(TAG, "getCount: " + sql);
            cursor = getReadableDatabase().rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex(countAlias));
            } else {
                return 0;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public final <T>T executor(Function<T>fun) {
        return fun.function(getWritableDatabase());
    }

    public final void executor(Procedure proc) {
        proc.procedure(getWritableDatabase());
    }

}
