package com.android.jack.supersqlite;

/**
 * Created by Administrator on 2018/8/4.
 * The document in project GameRoom.
 *
 * @author Jack
 */

class ColumnNameException extends RuntimeException {
    public ColumnNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public ColumnNameException(Throwable cause) {
        super(cause);
    }
}
