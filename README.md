# ormtest
通过不断优化ORM来学习反射

> 测试使用了5条数据

## step1 基本的jdbc

**耗时1ms**

通过最基本的jdbc连接数据库，映射到实体类

```java
while(rs.next()){
     // 创建新的实体对象
     UserEntity ue=new UserEntity();

     ue._userId=rs.getInt("id");
     ue._userName=rs.getString("name");
     ue._password=rs.getString("pwd");

     System.out.println(ue);
}

关于上面这段代码,
我们是否可以将其封装到一个助手类里
这样做的好处是:
当实体类发生修改时, 只需要改助手类就可以了...
```

## step2 封装映射代码

**耗时1ms**

将step1中的rs赋值到实体类的代码封装到一个工具类中，当实体类发生修改时, 只需要改助手类就可以了

```java
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
        return ue;
    }
}
这段代码的劣势在于如果需要修改、添加字段，那么就需要在这个类中修改对应的代码
可以使用反射，动态添加字段
```

## step3 使用反射实现映射

**耗时7ms**

自定义一个注解

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * 列名称
     */
    String name();
}
```

将UserEntity中需要的字段使用注解

```java
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
}
```

使用反射扫描UserEntity中使用Column注解的字段

```java
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
```

从这里就能看出反射相比前面的方式拓展性更强，但是还不够通用，只能用来创建UserEntity

## step4 改成通用entity生成器

**耗时7ms**

将UserEntity换成更通用的，通过传入需要的Entity类型

```java
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
```

但是使用反射的方式，性能太差了，有没有一种办法可以同时兼顾step2的速度和step4的拓展性

## step5 javassist生成代码

javassist是一个可以动态生成、创建字节码的工具，这样的话我们就可以动态生成类似step2的代码。

使用工厂模式，抽象出一个AbstractEntityHelper抽象类，拥有一个create抽象方法，来创造具体的Entity；

EntityHelperFactory则获取AbstractEntityHelper的实现类。

AbstractEntityHelper

```java
/**
 * 抽象的实体助手
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
```

EntityHelperFactory

```java
/**
 * 工厂类
 */
public class EntityHelperFactory {
    private static final Map<Class<?>, AbstractEntityHelper> entityHelperMap = new HashMap<>();

    private EntityHelperFactory() {
    }

    public static AbstractEntityHelper getEntityHelper(Class<?> entityClass) throws Exception {
        if (entityClass == null) {
            return null;
        }
        //从缓存map中获取
        AbstractEntityHelper helper = entityHelperMap.get(entityClass);
        if (helper != null) {
            return helper;
        }
        //javassist生成

        //获取类池
        ClassPool pool = ClassPool.getDefault();
        pool.appendSystemPath();

        // 导入相关类, 生成以下代码:
        // import java.sql.ResultSet
        // import org.ormtest.entity.UserEntity
        // import ...
        pool.importPackage(ResultSet.class.getName());
        pool.importPackage(entityClass.getName());

        //助手抽象类
        CtClass ctClass = pool.getCtClass(AbstractEntityHelper.class.getName());
        //助手实现类的名字
        final String helperName = entityClass.getName() + "Helper";

        //创建助手类
        //public class UserEntityHelper extends AbstractEntityHelper { ...
        CtClass helperClass = pool.makeClass(helperName, ctClass);

        //创建默认构造器
        //public UserEntityHelper() {}
        CtConstructor constructor = new CtConstructor(new CtClass[0], helperClass);
        //空函数体
        constructor.setBody("{}");
        //添加默认构造器
        helperClass.addConstructor(constructor);

        //函数代码字符串
        final StringBuffer sb = new StringBuffer();
        //添加create函数
        sb.append("public Object create(java.sql.ResultSet rs) throws Exception {\n");
        //UserEntity obj = new UserEntity();
        sb.append(entityClass.getName())
                .append(" obj = new ")
                .append(entityClass.getName())
                .append("();\n");

        Field[] declaredFields = entityClass.getDeclaredFields();
        for (Field field : declaredFields) {
            Column column = field.getAnnotation(Column.class);
            if (column == null) {
                continue;
            }

            String colName = column.name();
            if (Integer.TYPE.equals(field.getType())) {
                //obj._userId = rs.getInt("id");
                sb.append("obj.")
                        .append(field.getName())
                        .append(" = rs.getInt(\"")
                        .append(colName)
                        .append("\");\n");
            } else if (String.class.equals(field.getType())) {
                //obj._userName = rs.getString("name");
                sb.append("obj.")
                        .append(field.getName())
                        .append(" = rs.getString(\"")
                        .append(colName)
                        .append("\");\n");
            } else {

            }
        }
        sb.append("return obj;\n");
        sb.append("}");

        //创建方法
        CtMethod cm = CtNewMethod.make(sb.toString(), helperClass);
        helperClass.addMethod(cm);
        //输出该类
        helperClass.writeFile("./src/main/java");
        Class<?> javaClazz = helperClass.toClass();
        helper = (AbstractEntityHelper) javaClazz.newInstance();

        entityHelperMap.put(entityClass, helper);
        return helper;
    }
}
```



