package com.geektime.week1;

import java.lang.reflect.Method;
import java.util.Base64;

public class ClassLoaderTest{

    public static void main(String[] args) throws Exception {
        MyClassLoader myClassLoader = new MyClassLoader();
        Object object = myClassLoader.loadClass("com.geektime.week1.Hello").newInstance();    // 加载并初始化Hello类
        Method method = object.getClass().getMethod("hello");
        method.invoke(object);
    }
}
