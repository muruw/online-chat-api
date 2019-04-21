import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client {

    public static void main(String[] args) throws Exception {
        // Clientis tuleb muuta saatmist natuke
        try (Socket socket = new Socket("51.15.118.3", 1337);
             DataOutputStream outData = new DataOutputStream(socket.getOutputStream());
             DataInputStream inData = new DataInputStream(socket.getInputStream())) {
            ArrayList<String> sõnumisisu = new ArrayList<>(Arrays.asList(args));
            sõnumisisu.remove(0);
            sõnumisisu.remove(0);
            if (args.length == 2) {
                writeMessage(outData, Long.parseLong(args[0]), Long.parseLong(args[1]), "");
                readMessage(inData);
            } else if (args.length >= 3) {
                String sõnum = String.join(" ", sõnumisisu);
                writeMessage(outData, Long.parseLong(args[0]), Long.parseLong(args[1]), sõnum);
                readMessage(inData);
            } else {
                System.out.println("pole õige type");
            }
        }
    }

    static void writeMessage(DataOutputStream socketOut, long sender, long receiver, String text) throws Exception {
        if (text.equals("")) {
            socketOut.writeInt(0);
        } else {
            socketOut.writeInt(1);
        }
        socketOut.writeLong(sender);
        socketOut.writeLong(receiver);
        if (!text.equals("")) {
            socketOut.writeUTF(text);
        }
    }
    static void readMessage(DataInputStream socketIn) throws Exception {
        int msgcount = socketIn.readInt();
        for (int i = 0; i < msgcount; i++) {
            System.out.print("saatja id " + socketIn.readLong());
            System.out.print(" saaja id " + socketIn.readLong());
            System.out.print(" sõnum " + socketIn.readUTF());
            System.out.println(" ");
        }
    }

    static void processMessageEcho(byte[] value) throws Exception {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(value));
        String important = dis.readUTF();
        System.out.println(important);
    }
}