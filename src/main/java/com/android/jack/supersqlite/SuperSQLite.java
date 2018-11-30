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

    /**
     *
     * @param context
     * @param dbName 数据库名
     */
    public SuperSQLite(Context context, String dbName) {
        super(context, dbName, null, 1);
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

    /**
     * 为 POJO 类创建对应的数据库表
     * @param clsTable POJO 类
     * @throws SQLException
     */
    public final void createTable(Class<?> clsTable) throws SQLException {
        TableInfo tableInfo = getTableInfo(clsTable, false);
        String sql = tableInfo.getSQLDescription();
        Log.i(TAG, "createTable: " + sql);
        getWritableDatabase().execSQL(sql);
    }

    /**
     * 将数据插入到 POJO 类对应的数据库表中，如果数据表不存在将自动创建
     * @param clsTable 数据表对应的 POJO 类
     * @param items 数据对象
     * @param <T>
     * @throws SQLException
     */
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
     *
     * 将数据插入或更新到 POJO 类对应的数据库表中，如果数据表不存在将自动创建
     * 如果数据表中已存在对应主键的数据则会更新旧的数据，否则插入一条新的数据
     * @param clsTable 数据表对应的 POJO 类
     * @param items 数据对象
     * @throws PrimaryKeyNotFoundException 未声明主键时抛出{@link com.android.jack.supersqlite.constraint.PrimaryKey}
     * @throws SQLException
     */
    @SafeVarargs
    public final <T>void insertOrUpdate(Class<T>clsTable, T...items) throws PrimaryKeyNotFoundException,  SQLException {
        if (items == null || items.length == 0) {
            return;
        }

        TableInfo tableInfo = getTableInfo(clsTable, true);
        if (tableInfo.primaryKey == null) {
            throw new PrimaryKeyNotFoundException("the primary key cannot be found in table definition of " + clsTable.getSimpleName());
        }

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
     * 从 POJO 类对应的数据库表中删除与数据对象主键相等的数据
     * @param clsTable 数据表对应的 POJO 类
     * @param items 数据对象
     * @param <T>
     * @throws PrimaryKeyNotFoundException 未声明主键时抛出{@link com.android.jack.supersqlite.constraint.PrimaryKey}
     * @throws SQLException
     */
    @SafeVarargs
    public final <T>void delete(Class<T>clsTable, T...items) throws PrimaryKeyNotFoundException, SQLException {
        if (items == null || items.length == 0) {
            return;
        }

        TableInfo tableInfo = getTableInfo(clsTable, false);
        if (tableInfo.primaryKey == null) {
            throw new PrimaryKeyNotFoundException("the primary key cannot be found in table definition of " + clsTable.getSimpleName());
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
     * 从数所库删除数据
     * @param clsTable 数据表对应的 POJO 类
     * @param where 条件表达式，不能为空，否则将抛出SQLException
     * @throws SQLException
     */
    public final void delete(Class<?>clsTable, String where) throws SQLException {
        if (where == null || where.trim().isEmpty()) {
            throw new SQLException("'where' can not be empty.");
        }

        TableInfo tableInfo = getTableInfo(clsTable, false);

        String sql = "DELETE FROM " + tableInfo.name + " WHERE " + where;

        Log.i(TAG, "delete: " + sql);
        getWritableDatabase().execSQL(sql);
    }

    /**
     * 从数所库查询数据，描述SQL：SELECT * FROM table_name WHERE id > '10'
     * @param clsTable 数据表对应的 POJO 类
     * @param where 条件表达式，为空则返回全表
     * @param <T>
     * @return
     * @throws SQLException
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
     * 从数所库分页查询数据，描述SQL：SELECT * FROM table_name where id > '10' LIMIT 10, 5
     * @param clsTable 数据表对应的 POJO 类
     * @param where 条件表达式，为空则返回全表
     * @param offset
     * @param count
     * @param <T>
     * @return
     * @throws SQLException
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

    /**
     * 执行 sql 查询语句，并将查询结果以 model 指定的数据模型填入到 outResult 结果接收列表中
     * @param sql SQL 查询语句
     * @param model POJO 类数据模型定义
     * @param outResult 接收查询结果列表
     * @param <T>
     * @return outResult 查询结果接收列表
     * @throws SQLException
     */
    public final <T>List<T> rawQuery(String sql, Class<T>model, List<T>outResult) throws SQLException {
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery(sql, null);
            boolean hasNext = cursor != null && cursor.moveToFirst();
            while (hasNext) {
                T t = readRow(cursor, model);
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
     * 查询数据表总的数据数量
     * @return
     */
    public final <T>int getCount(Class<T>clsTable) {
        return getCount(clsTable, null);
    }

    /**
     * 查询数据表符合条件的数据数量
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
