package com.geektime.week1;

import java.io.*;

public class MyClassLoader extends ClassLoader{

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = load(name);
        return defineClass(name, bytes, 0, bytes.length);
    }

    private byte[] load(String name){
        InputStream in = null;
        byte[] bytes = null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()){
            File file = new File("src/main/java/"+ name.replace(".", "/") +".xlass");
            in = new FileInputStream(file);
            int len = 0;
            int count = 0;
            while (-1 != (len = in.read())) {
                len = 255 - len;
                out.write(len);
                count++;
            }
            bytes = out.toByteArray();

            for(int i = 0; i < count; ++i) {
                bytes[i] = (byte)(~bytes[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }
}
