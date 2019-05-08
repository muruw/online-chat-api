import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DatabaseJSON {

    private String filePath;

    public DatabaseJSON(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Creates and returns a database object
     *
     * @return JSONObject, database object
     * @throws Exception
     */
    public JSONObject createDatabase() throws Exception {

        JSONParser parser = new JSONParser();
        File filePath = new File(this.filePath);
        JSONObject databaseJSONObject = (JSONObject) parser.parse(new FileReader(filePath));

        return databaseJSONObject;
    }

    /**
     * @param jsonObj   Database object
     * @param jsonTable name of the column
     * @return
     */
    public JSONArray getArrayJSON(JSONObject jsonObj, String jsonTable) {

        JSONArray arrayJSON = (JSONArray) jsonObj.get(jsonTable);

        return arrayJSON;
    }

    /**
     * @param arrayJSON JSONArray of users
     * @return List of users currently in the database
     */
    public List<User> getUsers(JSONArray arrayJSON) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < arrayJSON.size(); i++) {
            JSONObject obj = (JSONObject) arrayJSON.get(i);

            // All of the JSON numbers are always in long type 
            long userId = (long) obj.get("id");
            String username = (String) obj.get("client_name");

            User user = new User(userId, username);
            users.add(user);
        }

        return users;
    }

    //with id ja userarray gets user objekt with correct id, if not user with such id return null
    public JSONObject getChat(String neededId, JSONArray arrayJSON) {
        for (int i = 0; i < arrayJSON.size(); i++) {
            JSONObject obj = (JSONObject) arrayJSON.get(i);
            String userId = (String) obj.get("id");
            if (userId.equals(neededId)) {
                return obj;
            }
        }
        return null;
    }

    public JSONObject getUser(String neededId, JSONArray arrayJSON) {
        for (int i = 0; i < arrayJSON.size(); i++) {
            JSONObject obj = (JSONObject) arrayJSON.get(i);
            String userId = (String) obj.get("id");
            if (userId.equals(neededId)) {
                return obj;
            }
        }
        return null;
    }

    //gets messages in a chat
    public List<Message> chatConvo(String chatid, JSONArray arrayJSON) {
        List<Message> messages = new ArrayList<>();

        // Object that holds the user data
        JSONObject obj = this.getChat(chatid, arrayJSON);

        // Creating an array of sent_messages
        JSONArray messagesArrayJSON = (JSONArray) obj.get("messages");
        for (int i = 0; i < messagesArrayJSON.size(); i++) {
            JSONObject data = (JSONObject) messagesArrayJSON.get(i);
            String sender = (String) data.get("sender");
            String userMessage = (String) data.get("message");
            String time = (String) data.get("time");
            Message message = new Message(chatid, sender, userMessage, time);
            messages.add(message);
        }
        return messages;
    }

    /**
     * Adds a message to the JSON database sent by user
     *
     * @param chatid       Id of the user, whose sent the message
     * @param senderID     Id of the user to whom the message is sent to
     * @param message      String type message
     * @param databaseJSON JSONObject type HashMap that shows the whole database
     * @param chatsJson    JSONArray type, has all of the users in it
     * @param orderJSON    JSONArray type, shows the order how the data should be shown
     * @throws Exception
     */
    void addMessage(String chatid, String senderID, String message, String time, JSONObject databaseJSON, JSONArray chatsJson, JSONArray orderJSON) throws Exception {

        JSONArray messages = (JSONArray) (this.getChat(chatid, chatsJson).get("messages"));

        // Adding the message to the JSON file
        JSONObject messageData = new JSONObject();
        messageData.put("sender", senderID);
        messageData.put("message", message);
        messageData.put("time", time);
        messages.add(messageData);

        databaseJSON.put("chats", chatsJson);
        databaseJSON.put("client_order", orderJSON);

        // Rewriting the file
        try (FileWriter file = new FileWriter("client_db.json")) {
            file.write(databaseJSON.toJSONString());
        }
    }

    public String newChat(String participant1, String participant2, JSONObject databaseJSON, JSONArray chatsJson, JSONArray usersJson) throws Exception {
        String chatid = participant1 + ";" + participant2;
        JSONObject chat = new JSONObject();
        chat.put("id", chatid);
        chat.put("messages", new JSONArray());
        JSONArray users = new JSONArray();
        users.add(participant1); users.add(participant2);
        chat.put("users", users);
        chatsJson.add(chat);
        databaseJSON.put("chats", chatsJson);

        JSONArray newUsers = (JSONArray) usersJson.clone();
        for (int i = 0; i < usersJson.size(); i++) {
            JSONObject data = (JSONObject) usersJson.get(i);
            String id = (String) data.get("id");
            if (id.equals(participant1) || id.equals(participant2)) {
                newUsers.remove(data);
                JSONArray chats = (JSONArray) data.get("chatsIds");
                chats.add(chatid);
                data.put("chatsIds", chats);
                newUsers.add(data);//
            }
        }
        databaseJSON.put("client", newUsers);

        try (FileWriter file = new FileWriter("client_db.json")) {
            file.write(databaseJSON.toJSONString());
        }
        return chatid;
    }

    public void addToChat(String chatid, String participant, JSONObject databaseJson, JSONArray usersJson, JSONArray chatsJson) throws Exception {
        JSONArray chats = new JSONArray();
        for (int i = 0; i < usersJson.size(); i++) {
            JSONObject data = (JSONObject) usersJson.get(i);
            String id = (String) data.get("id");
            if (id.equals(participant)) {
                usersJson.remove(data);
                chats = (JSONArray) data.get("chatsIds");
                chats.add(chatid);
                data.put("chatsIds", chats);
                usersJson.add(data);
            }
        }

        JSONArray users = new JSONArray();
        for (int i = 0; i < chatsJson.size(); i++) {
            JSONObject data = (JSONObject) chatsJson.get(i);
            String id = (String) data.get("id");
            if (id.equals(chatid)) {
                chatsJson.remove(data);
                users = (JSONArray) data.get("users");
                users.add(participant);
                data.put("users", users);
                chatsJson.add(data);
            }
        }
        databaseJson.put("client", usersJson);
        databaseJson.put("chats", chatsJson);
        try (FileWriter file = new FileWriter("client_db.json")) {
            file.write(databaseJson.toJSONString());
        }
    }

    public JSONArray removeFromChat(String chatid, String participant, JSONObject databaseJson, JSONArray usersJson, JSONArray chatsJson) throws Exception {
        JSONArray chats = new JSONArray();
        for (int i = 0; i < usersJson.size(); i++) {
            JSONObject data = (JSONObject) usersJson.get(i);
            String id = (String) data.get("id");
            if (id.equals(participant)) {
                usersJson.remove(data);
                chats = (JSONArray) data.get("chatsIds");
                chats.remove(chatid);
                data.put("chatsIds", chats);
                usersJson.add(data);
            }
        }
        JSONArray users = new JSONArray();
        for (int i = 0; i < chatsJson.size(); i++) {
            JSONObject data = (JSONObject) chatsJson.get(i);
            String id = (String) data.get("id");
            if (id.equals(chatid)) {
                chatsJson.remove(data);
                users = (JSONArray) data.get("users");
                users.remove(participant);
                data.put("users", users);
                chatsJson.add(data);
            }
        }

        databaseJson.put("client", usersJson);
        databaseJson.put("chats", chatsJson);
        try (FileWriter file = new FileWriter("client_db.json")) {
            file.write(databaseJson.toJSONString());
        }
        return usersJson;
    }

    public void deleteChat(String chatid, JSONObject databaseJson, JSONArray usersJson, JSONArray chatsJson) throws Exception {
        JSONArray newusersJson = (JSONArray) usersJson.clone();
        for (int i = 0; i < chatsJson.size(); i++) {
            JSONObject data = (JSONObject) chatsJson.get(i);
            String id = (String) data.get("id");
            if (id.equals(chatid)) {
                JSONArray users = (JSONArray) data.get("users");
                for (int j = 0; j < users.size(); j++) { //kui proovida deleteida ss näha et for loop ei tööta
                    JSONObject user = getUser((String) users.get(j), usersJson);
                    newusersJson.remove(user);
                    JSONArray chats = (JSONArray) user.get("chatsIds");
                    chats.remove(chatid);
                    user.put("chatsIds", chats);
                    newusersJson.add(user);
                }
                chatsJson.remove(data);
            }
        }
        databaseJson.put("client", newusersJson);
        databaseJson.put("chats", chatsJson);
        try (FileWriter file = new FileWriter("client_db.json")) {
            file.write(databaseJson.toJSONString());
        }
    }

    public void addUser(String username, JSONObject databaseJson, JSONArray usersJson) throws Exception {
        JSONObject user = new JSONObject();
        user.put("id", username);
        user.put("chats", new JSONArray());
        usersJson.add(user);
        databaseJson.put("client", usersJson);
        try (FileWriter file = new FileWriter("client_db.json")) {
            file.write(databaseJson.toJSONString());
        }
    }

    public void deleteUser(String userid, JSONObject databaseJson, JSONArray usersJson) throws Exception {
        for (int i = 0; i < usersJson.size(); i++) {
            JSONObject data = (JSONObject) usersJson.get(i);
            String id = (String) data.get("id");
            if (id.equals(userid)) {
                usersJson.remove(data);
            }
        }
        databaseJson.put("client", usersJson);
        try (FileWriter file = new FileWriter("client_db.json")) {
            file.write(databaseJson.toJSONString());
        }
    }

    public String[] getChats(String senderId, JSONArray usersJson) {
        JSONObject user = getUser(senderId, usersJson);
        JSONArray chats = (JSONArray) user.get("chatsIds");
        String[] chatIds = new String[chats.size()];
        for (int i = 0; i < chats.size(); i++) {
            chatIds[i] = (String) chats.get(i);
        }
        return chatIds;
    }

    public long biggestId(JSONArray usersOrChatJson) {
        long biggestId = 0;
        for (int i = 0; i < usersOrChatJson.size(); i++) {
            JSONObject data = (JSONObject) usersOrChatJson.get(i);
            long id = (long) data.get("id");
            if (id > biggestId) {
                biggestId = id;
            }
        }
        return biggestId;
    }

}
