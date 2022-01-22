package me.kutuzov.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleUtils {
    public static void clearConsole() {

        try {
            final String os = System.getProperty("os.name");
            if (os.contains("Windows"))
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        }
        catch (final Exception e) {e.printStackTrace();}
    }
    public static String readLine() { try { return new BufferedReader(new InputStreamReader(System.in)).readLine(); } catch (IOException exception) {} return ""; }
    public static void trySleep(long millis) { try { Thread.sleep(millis); } catch (InterruptedException ignored) {}}
    public static void pnl(String s) { System.out.println(s); }
    public static void pwl(String s) { System.out.print(s); }

    public static void asyncRun(Runnable runnable) { new Thread(runnable).start(); }
}