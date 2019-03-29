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

        // JSON Stuff
        String filepath = "client_db.json";
        DatabaseJSON databaseJSON = new DatabaseJSON(filepath);
        JSONObject database = databaseJSON.createDatabase();
        JSONArray usersJSON = databaseJSON.getArrayJSON(database, "client");
        System.out.println(usersJSON);
        JSONArray orderJSON = databaseJSON.getArrayJSON(database, "client_order");
        System.out.println(orderJSON);

        List<User> users = databaseJSON.getUsers(usersJSON);
        System.out.println("Users: ");
        for (User user : users) {
            System.out.println(user.getId() + "; " + user.getUsername());
        }

        // Adding the message to the database
        // ARGS[2] - saatja ARGS[3] - SAAJA
        databaseJSON.addSentMessage(Long.parseLong(args[2]) , Long.parseLong(args[3]), args[1], database,  usersJSON, orderJSON);
        databaseJSON.addReceivedMessage(Long.parseLong(args[3]) , Long.parseLong(args[2]), args[1], database,  usersJSON, orderJSON);

        List<Message> sentMessages = databaseJSON.userSentMessages(users.get(0).getId(), usersJSON);
        System.out.println(users.get(0).getUsername() + "'s sent messages");
        for (Message message : sentMessages) {
            System.out.println(message.getMessageType() + "; " + message.getMessage());
        }

        List<Message> receivedMessages = databaseJSON.userReceivedMessages(users.get(1).getId(), usersJSON);
        System.out.println(users.get(1).getUsername() + "'s received messages");
        for (Message receivedMessage : receivedMessages) {
            System.out.println(receivedMessage.getMessageType() + "; " + receivedMessage.getMessage());
        }


        // Echo
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