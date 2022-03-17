package com.rookied.ormtest.step4.entity;

/**
 * @author rookied
 * @date 2022.03.17
 */
public class UserEntity {
    /**
     * 用户 Id
     */
    @Column(name = "id")
    public int _userId;

    /**
     * 用户名
     */
    @Column(name = "name")
    public String _userName;

    /**
     * 密码
     */
    @Column(name = "pwd")
    public String _password;

    @Override
    public String toString() {
        return "UserEntity{" +
                "_userId=" + _userId +
                ", _userName='" + _userName + '\'' +
                ", _password='" + _password + '\'' +
                '}';
    }
}
