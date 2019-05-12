package client;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
This class handles pinging the server. Works on the premise that while Client is running, sends a message to server every n seconds and that server send message back.
IF messageType from server is ping then do nothing. IF no messageArrives, then display that server is down. If messageType indicates that there is also a payload forward the message to ResponseReciever.
Immediately sends a ping with payload, if Response Sever calls IO out.
 */
public class IO {

    public static List<String> sendMessage(String message, String userID, String chatID, DataOutputStream outData, DataInputStream inData) throws Exception {
        System.out.println("Sending Message");

        System.out.println("message: " + message);
        System.out.println("userid " + userID);
        System.out.println("chatid " + chatID);
        outData.writeInt(1);
        outData.writeUTF(userID);
        outData.writeUTF(chatID);

        if (!message.equals("")) {
            outData.writeUTF(message);
        }
        System.out.println("Reading message in IO");
        return readMessage(inData);


    }

    public static List<String> refreshMessage(String userID, String chatID, DataOutputStream outData, DataInputStream inData) throws Exception {
        outData.writeInt(0);
        outData.writeUTF(userID);
        outData.writeUTF(chatID);

        return readMessage(inData);


    }

    public static HashMap<String, String> getChat(String mainUser, DataOutputStream out, DataInputStream in) throws Exception {
        HashMap<String, String> chats = new HashMap<>();

        out.writeInt(6);
        out.writeUTF(mainUser);
        //Mingi imelik asi
        out.writeUTF("");


        int chatCount = in.readInt();
        System.out.println("chatCount " + chatCount);
        for (int i = 0; i < chatCount; i+=2) {
            String name = in.readUTF();
            String time = in.readUTF();
            chats.put(name, time);
        }
        return chats;
    }

    public static void removeChat(String chatID, DataOutputStream outData) throws IOException {
        outData.writeInt(4);
        outData.writeUTF("");
        outData.writeUTF(chatID);
    }

    public static String addPerson(String chatID, String username, DataOutputStream outData, DataInputStream inData) throws IOException {
        outData.writeInt(3);
        outData.writeUTF(username);
        outData.writeUTF(chatID);
        return inData.readUTF();
    }

    public static String removeFromChat(String chatID, String remove, DataOutputStream outData, DataInputStream inData) throws IOException {
        outData.writeInt(5);
        outData.writeUTF(remove);
        outData.writeUTF(chatID);
        return inData.readUTF();
    }

    public static String newChat(String userID, String receiverID, DataOutputStream outData, DataInputStream inData) throws IOException {
        String chatId;

        outData.writeInt(2);
        outData.writeUTF(userID);
        outData.writeUTF(receiverID);

        System.out.println("Message sent out");

        chatId = inData.readUTF();

        System.out.println("Message got in");
        return chatId;
    }

    public static Long register(String username, String password, DataOutputStream outData, DataInputStream inData) throws Exception {
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
        System.out.println(usersId + " log in code");
        return usersId;
    }


    public static Long login(String username, String password, DataOutputStream outData, DataInputStream inData) throws Exception {
        outData.writeInt(8);
        return getString(username, password, outData, inData);
    }

    static List<String> readMessage(DataInputStream socketIn) throws Exception {
        System.out.println("Reading IO in readMessage");
        List<String> dataList = new ArrayList<>();

        int msgcount = socketIn.readInt();
        for (int i = 0; i < msgcount; i++) {
            String senderID = socketIn.readUTF();
            String message = socketIn.readUTF();
            dataList.add(senderID);
            dataList.add(message);
            System.out.print("saatja id " + senderID);
            System.out.print(" sõnum " + message);
            System.out.println(" ");
        }
        return dataList;
    }

    public static String setCustomName(String chatId, String newCustomChatname, DataOutputStream outData, DataInputStream inData) throws Exception {
        outData.writeInt(10);
        outData.writeUTF("");
        outData.writeUTF(chatId);
        outData.writeUTF(newCustomChatname);
        return inData.readUTF();
    }

    public static String getChatParticipants(String chatID,DataOutputStream outDAta, DataInputStream inData) throws IOException {
        outDAta.writeInt(11);
        outDAta.writeUTF("");
        outDAta.writeUTF(chatID);
        return inData.readUTF();
    }
}

