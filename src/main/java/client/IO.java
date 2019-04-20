package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/*
This class handles pinging the server. Works on the premise that while Client is running, sends a message to server every n seconds and that server send message back.
IF messageType from server is ping then do nothing. IF no messageArrives, then display that server is down. If messageType indicates that there is also a payload forward the message to ResponseReciever.
Immediately sends a ping with payload, if Response Sever calls IO out.
 */
public class IO {
    public static void main(String[] args) throws Exception {
        ping();
    }

    private boolean runningState;

    public static boolean ping() throws Exception {
        try (Socket socket = new Socket("localhost", 1337);
             DataOutputStream outData = new DataOutputStream(socket.getOutputStream());
             DataInputStream inData = new DataInputStream(socket.getInputStream())) {
            System.out.println("Ping sent");
            writeMessage(outData, 1, 1, "");
            readMessage(inData);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static List<String> sendMessage(String message, long userID, long receiverID) throws Exception {
        int messageType = 1;

        System.out.println("test");

        try (Socket socket = new Socket("localhost", 1337);
             DataOutputStream outData = new DataOutputStream(socket.getOutputStream());
             DataInputStream inData = new DataInputStream(socket.getInputStream())) {
            writeMessage(outData, userID, receiverID, message);
            return readMessage(inData);
        }

    }


    static void writeMessage(DataOutputStream socketOut, long sender, long chatID, String text) throws Exception {
        //if sent message is empty consider it a ping
        if (text.equals("")) {
            socketOut.writeInt(0);
        } else {
            socketOut.writeInt(1);
        }
        socketOut.writeLong(sender);
        socketOut.writeLong(chatID);
        if (!text.equals("")) {
            socketOut.writeUTF(text);
        }
    }

    static List<String> readMessage(DataInputStream socketIn) throws Exception {
        List<String> dataList = new ArrayList<>();
                int msgcount = socketIn.readInt();
                for (int i = 0; i < msgcount; i++) {
                    long id = socketIn.readLong();
                    String message = socketIn.readUTF();
                    dataList.add(String.valueOf(id));
                    dataList.add(message);
                    System.out.print("saatja id " + id);
                    System.out.print(" sõnum " + message);
                    System.out.println(" ");
                }
                return dataList;
            }
}

