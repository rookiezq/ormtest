package com.rookied.ormtest.step5.entity;

import javassist.*;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * 工厂类
 * @author rookied
 * @date 2022.03.17
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
