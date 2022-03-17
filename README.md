# ormtest
通过不断优化ORM来学习反射

## step1 基本的jdbc
通过最基本的jdbc连接数据库，映射到实体类

```java
while(rs.next()){
    // 创建新的实体对象
    UserEntity ue=new UserEntity();

    ue._userId=rs.getInt("id");
    ue._userName=rs.getString("name");
    ue._password=rs.getString("pwd");

    System.out.println(ue);
    //
    // 关于上面这段代码,
    // 我们是否可以将其封装到一个助手类里??
    // 这样做的好处是:
    // 当实体类发生修改时, 只需要改助手类就可以了...
    //
}
```
