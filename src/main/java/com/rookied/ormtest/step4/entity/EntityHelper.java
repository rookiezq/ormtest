package com.rookied.ormtest.step4.entity;

import java.lang.reflect.Field;
import java.sql.ResultSet;

/**
 * @author rookied
 * @date 2022.03.17
 */
public class EntityHelper {
    /**
     * 将数据集装换为实体对象
     *
     * @param clazz 实体对象类型
     */
    public <TEntity> TEntity create(Class<TEntity> clazz, ResultSet rs) throws Exception {
        if (clazz == null || null == rs) {
            return null;
        }
        // 创建新的实体对象
        TEntity entity = clazz.newInstance();

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
            field.set(entity, colVal);
        }

        return entity;
    }
}
