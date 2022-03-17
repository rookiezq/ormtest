package com.rookied.ormtest.step5.entity;

import java.sql.ResultSet;

/**
 * 抽象的实体助手
 * @author rookied
 * @date 2022.03.17
 */
public abstract class AbstractEntityHelper {
    /**
     * 将数据集转换为实体对象
     *
     * @param rs 数据集
     *
     */
    public abstract Object create(ResultSet rs);
}

