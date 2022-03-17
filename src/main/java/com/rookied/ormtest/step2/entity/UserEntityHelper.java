package com.rookied.ormtest.step2.entity;

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

        ue._userId = rs.getInt("id");
        ue._userName = rs.getString("name");
        ue._password = rs.getString("pwd");
        //
        // 都是硬编码会不会很累?
        // 而且要是 UserEntity 和 t_user 加字段,
        // 还得改代码...
        // 为何不尝试一下反射?
        // 跳到 step020 看看吧!
        //
        return ue;
    }
}
