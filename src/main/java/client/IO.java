package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/*
This class handles pinging the server. Works on the premise that while Client is running, sends a message to server every n seconds and that server send message back.
IF messageType from server is ping then do nothing. IF no messageArrives, then display that server is down. If messageType indicates that there is also a payload forward the message to ResponseReciever.
Immediately sends a ping with payload, if Response Sever calls IO out.
 */
public class IO {

    public static List<String> sendMessage(int messageType, String message, String userID, String chatID, DataOutputStream outData, DataInputStream inData) throws Exception {
        System.out.println("Sending Message");

        try {
            outData.writeInt(messageType);
            outData.writeUTF(userID);
            outData.writeUTF(chatID);
            if (!message.equals("")) {
                outData.writeUTF(message);
            }
            return readMessage(inData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static List<Long> getChat(String mainUser, DataOutputStream out, DataInputStream in) throws Exception {
        List<Long> test = new ArrayList<>();

        out.writeInt(6);
        out.writeUTF(mainUser);
        //Mingi imelik asi
        out.writeUTF("");


        int chatCount = in.readInt();
        System.out.println("chatCount " +chatCount);
        for (int i = 0; i < chatCount; i++) {
            Long line = in.readLong();
            test.add(line);
        }
        return test;
    }

    public static void removeChat(String chatID, DataOutputStream outData) throws IOException {
        outData.writeInt(4);
        outData.writeUTF(chatID);
        outData.writeUTF("");
    }

    public static void addPerson(String chatID,String username, DataOutputStream outData) throws IOException {
        outData.writeInt(3);
        outData.writeUTF(chatID);
        outData.writeUTF(username);
    }

    public static void removeFromChat(String chatID,String remove, DataOutputStream outData) throws IOException {
        outData.writeInt(5);
        outData.writeUTF(chatID);
        outData.writeUTF(remove);
    }

    public static String newChat(String userID, String receiverID, DataOutputStream outData, DataInputStream inData) throws IOException {
        String chatId;

        outData.writeInt(2);
        outData.writeUTF(userID);
        outData.writeUTF(receiverID);

        System.out.println("here");
        chatId = inData.readUTF();
        return chatId;
    }

    public static Long register(String username, String password, DataOutputStream outData, DataInputStream inData) throws Exception {
        Long usersId;

        outData.writeInt(9);
        return getString(username, password, outData, inData);
    }

    private static Long getString(String username, String password, DataOutputStream outData, DataInputStream inData) throws IOException {
        Long usersId;
        outData.writeUTF(username);
        //mingi imelik serverThreadi asi igaksjuhuks ei näpi
        outData.writeUTF("");
        outData.writeUTF(password);


        usersId = inData.readLong();
        System.out.println(usersId +" log in code");
        return usersId;
    }


    public static Long login(String username, String password, DataOutputStream outData, DataInputStream inData) throws Exception {
        outData.writeInt(8);
        return getString(username, password, outData, inData);
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

