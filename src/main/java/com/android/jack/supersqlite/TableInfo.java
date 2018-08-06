package com.android.jack.supersqlite;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2018/8/4.
 * The document in project GameRoom.
 *
 * @author Jack
 */

class TableInfo {

    final List<ColumnInfo>columnInfos;

    static class ColumnInfo {
        String name;
        Field field;
        Class<?> type;
    }

    TableInfo(Class<? extends BaseRow>rowClass) {
        columnInfos = new ArrayList<>(32);
        init(rowClass);
    }

    private void init(Class<? extends BaseRow>rowClass) {
        String[]names = null;
        try {
            //检查是否定义表节构字段常量
            final String columnsNameOfField = "COLUMNS_NAME";
            Field field = rowClass.getField(columnsNameOfField);
            int modifiers = field.getModifiers();
            Class<?> type = field.getType();

            if ((modifiers & Modifier.STATIC) == Modifier.STATIC && type == String[].class) {
                field.setAccessible(true);
                Object oval = field.get(rowClass);
                String[]tableStruct = (String[]) oval;
                if (tableStruct != null) {
                    names = Arrays.copyOf(tableStruct, tableStruct.length);
                }
            }
        } catch (NoSuchFieldException ignore) {
        } catch (IllegalAccessException ignore) {
        }

        if (names != null) {//已定义表结构信息
            try {
                for (String name : names) {
                    Field field = rowClass.getField(name);
                    Class<?>type = field.getType();

                    //移除静态字段和常量
                    int modifiers = field.getModifiers();
                    if ((modifiers & Modifier.STATIC) == Modifier.STATIC ||
                            (modifiers & Modifier.FINAL) == Modifier.FINAL) {
                        throw new NotSupportedColumnTypeException("Field: " + name);
                    }

                    if (InternalUtil.isSupportedType(type)) {
                        ColumnInfo columnInfo = new ColumnInfo();
                        columnInfo.name = name;
                        columnInfo.type = type;
                        columnInfo.field = field;
                        columnInfos.add(columnInfo);
                    } else {
                        throw new NotSupportedColumnTypeException("Field: " + name);
                    }
                }// end for
            } catch (NoSuchFieldException e) {
                throw new ColumnNameException(e);
            }
        } else {//未定义表结构信息
            for (Field field : rowClass.getDeclaredFields()) {
                String name = field.getName();
                Class type = field.getType();

                //移除静态字段和常量
                int modifiers = field.getModifiers();
                if ((modifiers & Modifier.STATIC) == Modifier.STATIC ||
                        (modifiers & Modifier.FINAL) == Modifier.FINAL) {
                    continue;
                }

                if (InternalUtil.isSupportedType(type)) {
                    ColumnInfo columnInfo = new ColumnInfo();
                    columnInfo.name = name;
                    columnInfo.type = type;
                    columnInfo.field = field;
                    columnInfos.add(columnInfo);
                } else {
                    throw new NotSupportedColumnTypeException("Type: " + type.getName());
                }
            }// end for
        }
    }//end method init
}
