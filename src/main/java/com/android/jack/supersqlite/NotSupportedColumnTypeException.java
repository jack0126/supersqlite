package com.android.jack.supersqlite;

/**
 * Created by Administrator on 2018/8/4.
 * The document in project GameRoom.
 *
 * @author Jack
 */

class NotSupportedColumnTypeException extends RuntimeException {
    public NotSupportedColumnTypeException(String message) {
        super(message);
    }

    public NotSupportedColumnTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotSupportedColumnTypeException(Throwable cause) {
        super(cause);
    }
}
