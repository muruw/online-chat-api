
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


    public serverThread(Socket socket, DatabaseObjectJSON databaseObjectJSON) throws Exception {
        this.socket = socket;
        this.database = databaseObjectJSON.getDatabase();
        this.databaseObject = database.createDatabase();
        this.usersJSON = databaseObjectJSON.getUsersJSON();
        this.orderJSON = databaseObjectJSON.getOrderJSON();
        this.chatsJSON = databaseObjectJSON.getChatsJSON();
    }

    // saadab hetkel tagasi mõlema id-d vastavalt kas nad olid sõnumi saatja v saaja
    // ja ss sõnumi enda ka saadab (kõikide id vaheliste sõnumitega tehakse nii)
    void writeMessage(DataOutputStream socketOut, String chatId) throws Exception {
        List<Message> messagesChat = this.database.chatConvo(chatId, this.chatsJSON);
        Collections.sort(messagesChat);
        socketOut.writeInt(messagesChat.size());
        for (Message message : messagesChat) {
            socketOut.writeUTF(message.getSenderid());
            socketOut.writeUTF(message.getMessage());
        }
    }

    public void run() {
        Socket socket = this.socket;
        try (socket;
             DataInputStream socketIn = new DataInputStream(socket.getInputStream());
             DataOutputStream socketOut = new DataOutputStream(socket.getOutputStream())) {
            // TODO: 5/8/19 add a check for logout. Otherwise loop will always close while throwing an exception
            while (true) {
                int type = socketIn.readInt();
                String senderId = socketIn.readUTF();
                String chatId = socketIn.readUTF();
                System.out.println(type);
                System.out.println("Senderid " + senderId);
                System.out.println("Chatid " + chatId);
                System.out.println("Server is running");
                if (type == 1) {
                    synchronized (databaseObject) {
                        System.out.println("in send message");
                        String text = socketIn.readUTF();
                        System.out.println(text);
                        String time = Instant.now().toString();
                        database.addMessage(chatId, senderId, text, time, databaseObject, chatsJSON, orderJSON); //saatja id ja chati kuhu saadab id
                        writeMessage(socketOut, chatId);
                        System.out.println("saadetud sõnumid tagasi");
                    }
                } else if (type == 0) {
                    writeMessage(socketOut, chatId);
                } else if (type == 2) {
                    synchronized (databaseObject) {
                        if (database.getUser(senderId, usersJSON) != null && database.getUser(chatId, usersJSON) != null) {
                            String thischatid = database.newChat(senderId, chatId, databaseObject, chatsJSON, usersJSON); // mõlema inimese id-d
                            socketOut.writeUTF(thischatid);
                        } else {
                            socketOut.writeUTF("");
                        }
                    }
                } else if (type == 3) {
                    synchronized (databaseObject) {
                        if (database.getUser(senderId, usersJSON) != null && database.getChat(chatId, chatsJSON) != null) {
                            String thischatid = database.addToChat(chatId, senderId, databaseObject, usersJSON, chatsJSON);
                            socketOut.writeUTF(thischatid);
                        } else {
                            socketOut.writeUTF("");
                        }
                    }
                } else if (type == 4) {
                    synchronized (databaseObject) {
                        database.deleteChat(chatId, databaseObject, usersJSON, chatsJSON); //chati id mida kustutame
                    }
                } else if (type == 5) {
                    synchronized (databaseObject) {
                        if (database.getUser(senderId, usersJSON) != null && database.getChat(chatId, chatsJSON) != null) {
                            database.removeFromChat(chatId, senderId, databaseObject, usersJSON, chatsJSON); //chati id ja inimese id keda eemaldame chatist
                            socketOut.writeUTF("removedfromchat");
                        } else {
                            socketOut.writeUTF("");
                        }
                    }
                } else if (type == 6) {
                    synchronized (databaseObject) {
                        String[] chats = database.getChats(senderId, usersJSON, chatsJSON);
                        if (chats.length != 0) {
                            socketOut.writeInt(chats.length);
                            for (String chat : chats) {
                                System.out.println(chat);
                                socketOut.writeUTF(chat);
                            }
                        } else {
                            socketOut.writeInt(0);
                        }
                    }
                } else if (type == 7) {
                    synchronized (databaseObject) {
                        database.deleteUser(senderId, databaseObject, usersJSON);
                    }
                } else if (type == 8 || type == 9) {
                    synchronized (databaseObject) {
                        DatabaseFactory factory = new DatabaseFactory();
                        try (Connection connection = factory.connectToDatabase()) {
                            try (Reader setupDatabase = factory.createDatabase("setup.sql")) {
                                RunScript.execute(connection, setupDatabase);
                            }
                            String pw = socketIn.readUTF();
                            if (type == 8) {
                                boolean success = factory.login(connection, senderId, pw);

                                if (success) {
                                    socketOut.writeLong(factory.getUserId(connection, senderId));
                                } else {
                                    socketOut.writeLong(-1);
                                }
                            }
                            if (type == 9) {
                                boolean success = factory.register(connection, senderId, pw, pw);
                                if (success) {
                                    long id = factory.getUserId(connection, senderId);
                                    database.addUser(senderId, databaseObject, usersJSON);
                                    socketOut.writeLong(id);
                                    System.out.println("database add was success");
                                } else {
                                    socketOut.writeLong(-1);
                                }
                            }
                        }
                    }
                } else if (type == 10) {
                    synchronized (databaseObject) {
                        String newChatid = socketIn.readUTF();
                        if (database.getChat(chatId, chatsJSON) != null) {
                            newChatid = database.customName(chatId, newChatid, databaseObject, chatsJSON, usersJSON);
                            socketOut.writeUTF(newChatid);
                        } else {
                            socketOut.writeUTF("");
                        }
                    }
                } else if (type==11) {
                    synchronized (databaseObject) {
                        String chatNames = database.newChatName((JSONArray) (database.getChat(chatId, chatsJSON).get("users")));
                        socketOut.writeUTF(chatNames);
                    }
                } else {
                    throw new IllegalArgumentException("type " + type + " pole sobiv");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}





