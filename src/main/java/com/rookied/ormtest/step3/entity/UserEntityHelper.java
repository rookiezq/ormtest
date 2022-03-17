package com.rookied.ormtest.step3.entity;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;

/**
 * @author rookied
 * @date 2022.03.17
 */
public class UserEntityHelper {
    /**
     * 将数据集装换为实体对象
     *
     * @param rs 数据集
     * @return
     * @throws Exception
     */
    public UserEntity create(ResultSet rs) throws Exception {
        if (null == rs) {
            return null;
        }
        // 创建新的实体对象
        UserEntity ue = new UserEntity();

        Class<?> clazz = UserEntity.class;
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            //获取Column注释
            Column column = field.getAnnotation(Column.class);
            if (column == null) continue;

            //取出注释值
            String colName = column.name();
            Object colVal = rs.getObject(colName);
            if (colVal == null) continue;

            //ue设置字段值
            field.set(ue,colVal);
        }

        return ue;
    }
}
