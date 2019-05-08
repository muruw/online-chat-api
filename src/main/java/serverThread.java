import client.DatabaseFactory;
import org.h2.tools.RunScript;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Reader;
import java.net.Socket;
import java.sql.Connection;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class serverThread implements Runnable {
    Socket socket;
    DatabaseJSON database;
    JSONObject databaseObject;
    JSONArray usersJSON;
    JSONArray orderJSON;
    JSONArray chatsJSON;


    public serverThread(Socket socket, DatabaseJSON database) throws Exception {
        this.socket = socket;
        this.database = database;
        this.databaseObject = database.createDatabase();
        this.usersJSON = database.getArrayJSON(databaseObject, "client");
        this.orderJSON = database.getArrayJSON(databaseObject, "client_order");
        this.chatsJSON = database.getArrayJSON(databaseObject, "chats");
    }

    // saadab hetkel tagasi mõlema id-d vastavalt kas nad olid sõnumi saatja v saaja
    // ja ss sõnumi enda ka saadab (kõikide id vaheliste sõnumitega tehakse nii)
    void writeMessage(DataOutputStream socketOut, long chatId) throws Exception {
        List<Message> messagesChat = this.database.chatConvo(chatId, this.chatsJSON);
        Collections.sort(messagesChat);
        //messaged võiks uusimast vanimani sortitud ka olla (võiks olla messagel ka kas sent v saadud küljes olla)
        socketOut.writeInt(messagesChat.size());
        for (Message message : messagesChat) {
            socketOut.writeLong(message.getSenderid());
            socketOut.writeUTF(message.getMessage());
        }
    }

    public void run() {
        Socket socket = this.socket;
        try (socket;
             DataInputStream socketIn = new DataInputStream(socket.getInputStream());
             DataOutputStream socketOut = new DataOutputStream(socket.getOutputStream())) {
            int type = socketIn.readInt();
            long senderId = socketIn.readLong();
            long chatId = socketIn.readLong();
            System.out.println(type);
            if (type == 1) {
                String text = socketIn.readUTF();
                System.out.println(text);
                String time = Instant.now().toString();
                database.addMessage(chatId, senderId, text, time, databaseObject, chatsJSON, orderJSON); //saatja id ja chati kuhu saadab id
                writeMessage(socketOut, chatId);
                System.out.println("saadetud sõnumid tagasi");
            } else if (type == 0) {
                writeMessage(socketOut, chatId);
            } else if (type == 2) {
                long thischatid = database.newChat(senderId, chatId, databaseObject, chatsJSON, usersJSON); // mõlema inimese id-d
                System.out.println("serverHere");
                socketOut.writeLong(thischatid);
            } else if (type == 3) {
                database.addToChat(chatId, senderId, databaseObject, usersJSON, chatsJSON); // chatiid ja lisatava id
            } else if (type == 4) {
                database.deleteChat(chatId, databaseObject, usersJSON, chatsJSON); //chati id mida kustutame
            } else if (type == 5) {
                database.removeFromChat(chatId, senderId, databaseObject, usersJSON, chatsJSON); //chati id ja inimese id keda eemaldame chatist
            } else if (type == 6) {
                long[] chats = database.getChats(senderId, usersJSON);
                socketOut.writeInt(chats.length);
                for (int i = 0; i < chats.length; i++) {
                    socketOut.writeLong(chats[i]);
                }
            } else if (type == 7) {
                database.deleteUser(senderId, databaseObject, usersJSON);
            } else if (type == 8 || type == 9) {
                DatabaseFactory factory = new DatabaseFactory();
                try (Connection connection = factory.connectToDatabase()) {
                    try (Reader setupDatabase = factory.createDatabase("setup.sql")) {
                        RunScript.execute(connection, setupDatabase);
                    }
                    String un = socketIn.readUTF();
                    String pw = socketIn.readUTF();
                    if (type == 8) {
                        boolean success = factory.login(connection, un, pw);

                        if (success) {
                            socketOut.writeLong(factory.getUserId(connection, un));
                        } else {
                            socketOut.writeLong(-1);
                        }
                    }
                    if (type == 9) {
                        boolean success = factory.register(connection, un, pw, pw);
                        if (success) {
                            long id = factory.getUserId(connection, un);
                            database.addUser(un, id, databaseObject, usersJSON);
                            socketOut.writeLong(id);
                            System.out.println("database add was success");
                        } else {
                            socketOut.writeLong(-1);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("type " + type + " pole sobiv");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}



