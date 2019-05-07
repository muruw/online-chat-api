import javax.xml.crypto.Data;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {

    public static void main(String[] args) throws Exception {
        // Clientis tuleb muuta saatmist natuke
        try (Socket socket = new Socket("localhost", 1337);
             DataOutputStream outData = new DataOutputStream(socket.getOutputStream());
             DataInputStream inData = new DataInputStream(socket.getInputStream())) {
            ArrayList<String> sõnumisisu = new ArrayList<>(Arrays.asList(args));
            sõnumisisu.remove(0);
            sõnumisisu.remove(0);
            if (args[0].equals("create")) {
                outData.writeInt(2);
                outData.writeLong(Long.parseLong(args[1]));
                outData.writeLong(Long.parseLong(args[2]));
            } else if (args[0].equals("add")){
                outData.writeInt(3);
                outData.writeLong(Long.parseLong(args[1]));
                outData.writeLong(Long.parseLong(args[2]));
            } else if (args[0].equals("delete")) {
                outData.writeInt(4);
                outData.writeLong(Long.parseLong(args[1]));
                outData.writeLong(Long.parseLong(args[1]));
            } else if (args[0].equals("remove")) {
                outData.writeInt(5);
                outData.writeLong(Long.parseLong(args[1]));
                outData.writeLong(Long.parseLong(args[2]));
            } else if (args[0].equals("chats")) {
                outData.writeInt(6);
                outData.writeLong(Long.parseLong(args[1]));
                outData.writeLong(Long.parseLong(args[1]));
                int suurus = inData.readInt();
                for (int i = 0; i < suurus; i++) {
                    System.out.println(inData.readLong());
                }
            } else if (args[0].equals("deluser")) {
                outData.writeInt(7);
                outData.writeLong(Long.parseLong(args[1]));
                outData.writeLong(Long.parseLong(args[2]));
            } else if (args[0].equals("login") || args[0].equals("reg")) {
                if (args[0].equals("login")) {
                    outData.writeInt(8);
                }
                else {
                    outData.writeInt(9);
                }
                outData.writeLong(0);
                outData.writeLong(0);
                outData.writeUTF(args[1]);
                outData.writeUTF(args[2]);
                System.out.println(inData.readLong());
            }
            else if (args.length == 2) {
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

    static void readChats(DataInputStream socketIn) throws Exception {
        int chatCount = socketIn.readInt();
        for (int i = 0; i < chatCount; i++) {
            System.out.println("chat: " + socketIn.readLong());
        }
    }

    static void readMessage(DataInputStream socketIn) throws Exception {
        int msgcount = socketIn.readInt();
        for (int i = 0; i < msgcount; i++) {
            System.out.print("saatja id " + socketIn.readLong());
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