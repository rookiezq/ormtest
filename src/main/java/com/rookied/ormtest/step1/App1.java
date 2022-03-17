package com.rookied.ormtest.step1;


import com.rookied.ormtest.step1.entity.UserEntity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author rookied
 * @date 2022.03.17
 */
public class App1 {
    public static void main(String[] args) throws Exception {
        new App1().start();
    }

    private void start() throws Exception {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        String url = "jdbc:mysql://localhost:3306/rookied?useSSL=false";
        String user = "root";
        String password = "root";
        Connection conn = DriverManager.getConnection(url, user, password);
        // 简历陈述对象
        Statement stmt = conn.createStatement();
        // 创建 SQL 查询
        // ormtest 数据库中有个 t_user 数据表,
        // t_user 数据表包括三个字段: user_id、user_name、password,
        // t_user 数据表有 20 万条数据
        String sql = "select * from user limit 20000";

        // 执行查询
        ResultSet rs = stmt.executeQuery(sql);

        // 获取开始时间
        long t0 = System.currentTimeMillis();

        while (rs.next()) {
            // 创建新的实体对象
            UserEntity ue = new UserEntity();

            ue._userId = rs.getInt("id");
            ue._userName = rs.getString("name");
            ue._password = rs.getString("pwd");

            System.out.println(ue);
            //
            // 关于上面这段代码,
            // 我们是否可以将其封装到一个助手类里??
            // 这样做的好处是:
            // 当实体类发生修改时, 只需要改助手类就可以了...
            //
        }

        // 获取结束时间
        long t1 = System.currentTimeMillis();

        // 关闭数据库连接
        stmt.close();
        conn.close();

        // 打印实例化花费时间
        System.out.println("实例化花费时间 = " + (t1 - t0) + "ms");
    }
}
