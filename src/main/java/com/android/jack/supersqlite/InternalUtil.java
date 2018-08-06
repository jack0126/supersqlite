package com.android.jack.supersqlite;

import java.util.Date;
/**
 * Created by Administrator on 2018/8/4.
 * The document in project GameRoom.
 *
 * @author Jack
 */


class InternalUtil {

    static boolean isSupportedType(Class<?>type) {
        return (type == String.class ||
                type == int.class || type == long.class || type == double.class || type == boolean.class ||
                type == Date.class ||
                type == float.class || type == char.class || type == byte.class || type == short.class ||
                type == Integer.class || type == Long.class || type == Double.class || type == Boolean.class ||
                type == Character.class || type == Float.class || type == Byte.class || type == Short.class);
    }

    static String toSQLStringValue(Object val) {
        if (val == null) {
            return "''";
        } else {
            return "'" + val + "'";
        }
    }

    static String toSQLIntegerValue(Object val) {
        if (val == null) {
            return "0";
        } else {
            return val.toString();
        }
    }

    static String toSQLRealValue(Object val) {
        if (val == null) {
            return "0.0";
        } else {
            return val.toString();
        }
    }

    static String toSQLDateValue(Object val) {
        if (val == null) {
            return "0";
        } else {
            return String.valueOf(((Date) val).getTime());
        }
    }
}
