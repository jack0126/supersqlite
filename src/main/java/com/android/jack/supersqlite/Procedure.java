package com.android.jack.supersqlite;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Administrator on 2018/8/8.
 * The document in project GameRoom.
 *
 * @author Jack
 */
public interface Procedure {
    void procedure(SQLiteDatabase db);
}
