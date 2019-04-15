package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

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
            writeMessage(outData, 1, 2, "");
            return readMessage(inData);
        } catch (Exception e) {
            return false;
        }
    }

    public static void sendMessage(String message, long userID, long receiverID) throws Exception {
        int messageType = 1;

        System.out.println("test");

        // TODO: 4/15/19 Use inData to confirm if message has arrived. If not send message again.
        try (Socket socket = new Socket("localhost", 1337);
             DataOutputStream outData = new DataOutputStream(socket.getOutputStream());
             DataInputStream inData = new DataInputStream(socket.getInputStream())) {
            writeMessage(outData, userID, receiverID, message);
        }

    }


    static void writeMessage(DataOutputStream socketOut, long sender, long receiver, String text) throws Exception {
        //if sent message is empty consider it a ping
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

    static boolean readMessage(DataInputStream socketIn) throws Exception {
        try {
            int messageType = socketIn.readInt();
            //if is empty ping
            if (messageType == 0) {
                System.out.println("Ping got");
                return true;
            }

            //if message is returned
            // TODO: 4/15/19 Change that one big message is accepted. If that is the case parseIt 
            int msgcount = socketIn.readInt();
            for (int i = 0; i < msgcount; i++) {
                System.out.print("saatja id " + socketIn.readLong());
                System.out.print(" saaja id " + socketIn.readLong());
                System.out.print(" sõnum " + socketIn.readUTF());
                System.out.println(" ");
            }
            //for testing purposes this is false.
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
