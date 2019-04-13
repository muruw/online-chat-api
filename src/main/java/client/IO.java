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
        }catch (Exception e){
            return false;
        }
    }

    public static void sendMessage(String message, UserData user) throws Exception {
       /* int messageType;

        if (message.isEmpty()) {
            messageType = 0;
        } else {
            messageType = 1;
        }
        System.out.println("test");

        try (Socket socket = new Socket(user.getHost(), user.getPort());
             DataOutputStream outData = new DataOutputStream(socket.getOutputStream());
             DataInputStream inData = new DataInputStream(socket.getInputStream())) {

            outData.writeInt(messageType);

        }*/
/*
        try (Socket socket = new Socket("localhost", 1337);
             DataOutputStream outData = new DataOutputStream(socket.getOutputStream());
             DataInputStream inData = new DataInputStream(socket.getInputStream())) {
            ArrayList<String> sõnumisisu = new ArrayList<>(Arrays.asList(// TODO: 4/1/19 message siin ));
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
*/
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

    static boolean readMessage(DataInputStream socketIn) throws Exception {
        try {
            int messageType = socketIn.readInt();
            if (messageType == 0) {
                System.out.println("Ping got");
                return true;
            }
            int msgcount = socketIn.readInt();
            for (int i = 0; i < msgcount; i++) {
                System.out.print("saatja id " + socketIn.readLong());
                System.out.print(" saaja id " + socketIn.readLong());
                System.out.print(" sõnum " + socketIn.readUTF());
                System.out.println(" ");
            }
            //for testing purposes this is false.
            return false;
        }catch(Exception e){
            return false;
        }
    }
}
