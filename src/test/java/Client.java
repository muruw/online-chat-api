import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Client {

    public static void main(String[] args) throws Exception {
        // Clientis tuleb muuta saatmist natuke
        try (Socket socket = new Socket("localhost", 1337);
             DataOutputStream outData = new DataOutputStream(socket.getOutputStream());
             DataInputStream inData = new DataInputStream(socket.getInputStream())) {
            System.out.println(args[0]);
            if (args[0].equals("echo")) {
                writeMessage(outData, String.join(" ", args).substring(5), args[0]);
                readMessage(inData);
            } else if (args[0].equals("file")) {
                writeMessage(outData, args[1], args[0]);
                Path file = Paths.get(args[2]);
                byte[] recieved = readMessage(inData);
                if (recieved != null) {
                    Files.write(file, recieved);
                    System.out.println("faili sisu saadud ja salvestatud soovitud asukohta");
                } else {
                    System.out.println("sisu puudus");
                }
            } else {
                System.out.println("pole õige type");
            }
        }
    }

    static void writeMessage(DataOutputStream socketOut, String text, String type) throws Exception {
        socketOut.writeUTF(type);
        socketOut.writeUTF(text);
    }
    static byte[] readMessage(DataInputStream socketIn) throws Exception {
        String type = socketIn.readUTF();
        int length = socketIn.readInt();
        byte[] value = new byte[length];
        socketIn.readFully(value);
        if (type.equals("echo")) {
            processMessageEcho(value);

        } else if (type.equals("file")) {
            return value;
        } else if (type.equals("error")) {
            System.out.println("ei leidnud faili või anti absolute path");
        } else {
            System.out.println("serverilt tuli pask tagasi");
        }

        return null;
    }

    static void processMessageEcho(byte[] value) throws Exception {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(value));
        String important = dis.readUTF();
        System.out.println(important);
    }
}