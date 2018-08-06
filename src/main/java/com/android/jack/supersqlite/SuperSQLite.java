package com.android.jack.supersqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
/**
 * Created by Administrator on 2018/8/3.
 * The document in project SuperSQLite.
 *
 * @author Jack
 */

public class SuperSQLite<T extends BaseRow> extends SQLiteOpenHelper {

    private static final String TAG = SuperSQLite.class.getSimpleName();

    public static final String ID_FOR_COLUMN_NAME = "_id_";

    public static final String TABLE_NAME = "_data_table_";

    private final Class<T>rowClass;
    private TableInfo tableInfo;

    public SuperSQLite(Context context, String name, Class<T>rowClass) {
        super(context, name, null, 1);
        this.rowClass = rowClass;
        tableInfo = new TableInfo(rowClass);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = generateSQLForCreateTableIfNotExists();
        Log.i(TAG, "onCreate: sql: " + sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private String generateSQLForCreateTableIfNotExists() {
        StringBuilder sql = new StringBuilder()
                .append("CREATE TABLE IF NOT EXISTS ").append(TABLE_NAME)
                .append("(")
                .append(ID_FOR_COLUMN_NAME).append(" INTEGER PRIMARY KEY AUTOINCREMENT");

        for (TableInfo.ColumnInfo columnInfo : tableInfo.columnInfos) {
            String name = columnInfo.name;
            Class<?>type = columnInfo.type;

            if (type == String.class || type == boolean.class || type == char.class ||
                    type == Boolean.class || type == Character.class) {
                sql.append(", ").append(name).append(" TEXT");
            } else if (type == int.class || type == long.class || type == byte.class || type == short.class || type == Date.class ||
                    type == Integer.class || type == Long.class || type == Byte.class || type == Short.class) {
                sql.append(",").append(name).append(" INTEGER");
            } else if (type == double.class || type == float.class ||
                    type == Double.class || type == Float.class) {
                sql.append(",").append(name).append(" REAL");
            }
        }

        sql.append(")");
        return sql.toString();
    }

    private T parseRow(Cursor cursor) {
        T t;
        try {
            t = rowClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        t.id = cursor.getInt(cursor.getColumnIndexOrThrow(ID_FOR_COLUMN_NAME));

        try {
            for (TableInfo.ColumnInfo columnInfo : tableInfo.columnInfos) {
                String name = columnInfo.name;
                Field field = columnInfo.field;
                Class type = columnInfo.type;
                field.setAccessible(true);

                int columnIndex = cursor.getColumnIndexOrThrow(name);

                if (type == String.class) {
                    field.set(t, cursor.getString(columnIndex));
                } else if (type == int.class || type == Integer.class) {
                    field.set(t, cursor.getInt(columnIndex));
                } else if (type == long.class || type == Long.class) {
                    field.set(t, cursor.getLong(columnIndex));
                } else if (type == double.class || type == Double.class) {
                    field.set(t, cursor.getDouble(columnIndex));
                } else if (type == boolean.class || type == Boolean.class) {
                    field.set(t, Boolean.valueOf(cursor.getString(columnIndex)));
                } else if(type == Date.class) {
                    field.set(t, new Date(cursor.getLong(columnIndex)));
                } else if (type == float.class || type == Float.class) {
                    field.set(t, cursor.getFloat(columnIndex));
                } else if (type == char.class || type == Character.class) {
                    field.set(t, cursor.getString(columnIndex).charAt(0));
                } else if (type == byte.class || type == Byte.class) {
                    field.set(t, (byte) cursor.getInt(columnIndex));
                } else if (type == short.class || type == Short.class) {
                    field.set(t, cursor.getShort(columnIndex));
                }
            } // end for
            return t;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 如果使用查询出的对象执行则会更新数据库而不是插入
     * @param t
     * @return
     */
    public synchronized boolean insertOrUpdate(T t) {
        StringBuilder sql = new StringBuilder()
                .append("INSERT OR REPLACE INTO ").append(TABLE_NAME).append(" VALUES")
                .append("(")
                .append(t.id == -1 ? "NULL" : t.id);

        try {
            for (TableInfo.ColumnInfo columnInfo : tableInfo.columnInfos) {
                String name = columnInfo.name;
                Field field = columnInfo.field;
                Class type = columnInfo.type;
                field.setAccessible(true);
                Object oval = field.get(t);

                if (type == String.class || type == boolean.class || type == char.class ||
                        type == Boolean.class || type == Character.class) {//text
                    String val = InternalUtil.toSQLStringValue(oval);
                    sql.append(", ").append(val);
                } else if (type == int.class || type == long.class || type == byte.class || type == short.class ||
                        type == Integer.class || type == Long.class || type == Byte.class || type == Short.class) {//integer
                    String val = InternalUtil.toSQLIntegerValue(oval);
                    sql.append(", ").append(val);
                } else if (type == double.class || type == float.class ||
                        type == Double.class || type == Float.class) {//float
                    String val = InternalUtil.toSQLRealValue(oval);
                    sql.append(", ").append(val);
                } else if (type == Date.class) {
                    String val = InternalUtil.toSQLDateValue(oval);
                    sql.append(", ").append(val);
                } else {
                    Log.w(TAG, "insertOrUpdate: an unsupported field type: name = " + name + ", " + type.getName());
                }
            }
        } catch (IllegalAccessException ignore) {
        }
        sql.append(")");

        try {
            String sSql = sql.toString();
            Log.i(TAG, "insertOrUpdate: " + sSql);
            getWritableDatabase().execSQL(sSql);
            return true;
        } catch (SQLException e) {
            Log.w(TAG, "insertOrUpdate: ", e);
        }
        return false;
    }

    /**
     * 使用 id删除记录
     * @param ids
     * @return
     */
    public synchronized boolean delete(int[]ids) {
        if (ids == null || ids.length == 0) {
            return false;
        }

        StringBuilder sql = new StringBuilder()
                .append("DELETE FROM ").append(TABLE_NAME)
                .append(" WHERE ").append(ID_FOR_COLUMN_NAME).append(" = ").append(ids[0]);

        for (int i = 1; i < ids.length; i++) {
            sql.append(" OR ").append(ID_FOR_COLUMN_NAME).append(" = ").append(ids[i]);
        }

        try {
            String sSql = sql.toString();
            Log.i(TAG, "delete: " + sSql);
            getWritableDatabase().execSQL(sSql);
            return true;
        } catch (SQLException e) {
            Log.w(TAG, "delete: ", e);
            return false;
        }
    }

    public synchronized <E>E executor(Function<E>fun) {
        return fun.function(getWritableDatabase());
    }

    /**
     * 便用 id查询数据库
     * @param ids
     * @return
     */
    public synchronized List<T> query(int[]ids) {
        if (ids == null || ids.length == 0) {
            return new ArrayList<>(0);
        }

        List<T>list = new ArrayList<>(ids.length);

        StringBuilder sql = new StringBuilder()
                .append("SELECT * FROM ").append(TABLE_NAME)
                .append(" WHERE ").append(ID_FOR_COLUMN_NAME).append(" = ").append(ids[0]);

        for (int i = 1; i < ids.length; i++) {
            sql.append(" OR ").append(ID_FOR_COLUMN_NAME).append(" = ").append(ids[i]);
        }

        Cursor cursor = null;
        try {
            String sSql = sql.toString();
            Log.i(TAG, "query: " + sSql);
            cursor = getReadableDatabase().rawQuery(sSql, null);
            boolean hasNext = cursor != null && cursor.moveToFirst();
            while (hasNext) {
                T t = parseRow(cursor);
                list.add(t);
                hasNext = cursor.moveToNext();
            }
        } catch (SQLException e) {
            Log.w(TAG, "query: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * 查询所有符合条件的数据，如："_id_ = 5"
     * @param where
     * @return
     */
    public List<T> query(String where) {
        return query(where, 0, -1);
    }

    /**
     * 分页查询符合条件的数据
     * @param where 如："_id_ = 5 OR name = 'jack'"
     * @param offset
     * @param count
     * @return
     */
    public synchronized List<T> query(String where, int offset, int count) {
        if (offset < 0) {
            offset = 0;
        }

        if (count < 0) {
            count = -1;
        }

        List<T>list = new LinkedList<>();

        if (count == 0) {
            return list;
        }

        StringBuilder sql = new StringBuilder()
                .append("SELECT * FROM ").append(TABLE_NAME);
        if (where != null) {
            sql.append(" WHERE ").append(where);
        }
        sql.append(" ORDER BY ").append(ID_FOR_COLUMN_NAME)
                .append(" LIMIT ").append(offset).append(", ").append(count);

        Cursor cursor = null;
        try {
            String sSql = sql.toString();
            Log.i(TAG, "query: " + sSql);
            cursor = getReadableDatabase().rawQuery(sSql, null);
            boolean hasNext = cursor != null && cursor.moveToFirst();
            while (hasNext) {
                T t = parseRow(cursor);
                list.add(t);
                hasNext = cursor.moveToNext();
            }
        } catch (SQLException e) {
            Log.w(TAG, "query: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * 查询数据总条数量
     * @return
     */
    public int getCount() {
        return getCount(null);
    }

    /**
     * 查询符合条件数据的数量
     * @param where 如："_id_ = 5 AND name = 'jack'"
     * @return
     */
    public synchronized int getCount(String where) {
        String countAlias = "count";
        StringBuilder sql = new StringBuilder()
                .append("SELECT COUNT(").append(ID_FOR_COLUMN_NAME).append(") AS ").append(countAlias).append(" FROM ").append(TABLE_NAME);
        if (where != null) {
            sql.append(" WHERE ").append(where);
        }

        Cursor cursor = null;
        try {
            String sSql = sql.toString();
            Log.i(TAG, "getCount: " + sSql);
            cursor = getReadableDatabase().rawQuery(sSql, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow(countAlias));
            } else {
                return 0;
            }
        } catch (SQLException e) {
            Log.w(TAG, "getCount: ", e);
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
