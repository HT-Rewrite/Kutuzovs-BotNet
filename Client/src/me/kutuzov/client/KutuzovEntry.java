package me.kutuzov.client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class KutuzovEntry {
    private static ArrayList<Socket> sockets = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        for(int i = 0; i < 10; i++)
            sockets.add(new Socket("localhost", 33901));

        Thread.sleep(8000);
        for(Socket socket : sockets)
            socket.close();
    }
}