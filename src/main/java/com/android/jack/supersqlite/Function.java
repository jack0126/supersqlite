package com.android.jack.supersqlite;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Administrator on 2018/8/5.
 * The document in project CustomStartup.
 *
 * @author Jack
 */

public interface Function<T> {
    T function(SQLiteDatabase db);
}
