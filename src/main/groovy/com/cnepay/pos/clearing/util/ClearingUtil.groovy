package com.cnepay.pos.clearing.util

import java.lang.reflect.Field

/**
 * User: andongxu
 * Time: 15-9-1 下午6:33 
 */
class ClearingUtil {

    /**
     * 对象转为字符串
     * @param obj
     * @return
     */
    static String obj2string(Object obj) {
        String res = "";
        try {
            Class parentClass = obj.getClass().getSuperclass();
            Field[] parentField = parentClass.getDeclaredFields();
            for (Field field : parentField) {
                field.setAccessible(true);
                Object value = field.get(obj);
                value = value == null ? null : value.toString();
                res += field.getName() + "=" + value + ",";
            }
            Class sonClass = obj.getClass();
            Field [] sonField = sonClass.getDeclaredFields();
            for (Field field : sonField) {
                field.setAccessible(true);
                Object value = field.get(obj);
                value = value == null ? null : value.toString();
                res += field.getName() + "=" + value + ",";
            }
        } catch (Exception e) {
            return  null;
        }
        return res;
    }

//    static def get
}
