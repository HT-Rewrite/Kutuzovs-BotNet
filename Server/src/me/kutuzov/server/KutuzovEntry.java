package me.kutuzov.server;

import static me.kutuzov.server.KutuzovEnvironment.*;
import static me.kutuzov.server.util.ConsoleUtils.*;

public class KutuzovEntry {
    public static void main(String[] args) {
        pnl(String.format("Welcome to %s's v%s server panel!", NAME, VERSION));
        trySleep(5000);
        KutuzovServer server = new KutuzovServer();
        server.boot();
    }
}