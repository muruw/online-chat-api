import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        return (JSONObject) parser.parse(new FileReader(filePath));
    }

    /**
     * @param jsonObj   Database object
     * @param jsonTable name of the column
     * @return arrayJson
     */
    public JSONArray getArrayJSON(JSONObject jsonObj, String jsonTable) {

        return (JSONArray) jsonObj.get(jsonTable);
    }

    /**
     * @param arrayJSON JSONArray of users
     * @return List of users currently in the database
     */
    public List<User> getUsers(JSONArray arrayJSON) {
        List<User> users = new ArrayList<>();
        for (Object o : arrayJSON) {
            JSONObject obj = (JSONObject) o;

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
        return getJsonObject(neededId, arrayJSON);
    }

    private JSONObject getJsonObject(String neededId, JSONArray arrayJSON) {
        for (Object o : arrayJSON) {
            JSONObject obj = (JSONObject) o;
            String userId = (String) obj.get("id");
            if (userId.equals(neededId)) {
                return obj;
            }
        }
        return null;
    }

    public JSONObject getUser(String neededId, JSONArray arrayJSON) {
        return getJsonObject(neededId, arrayJSON);
    }

    //gets messages in a chat
    public List<Message> chatConvo(String chatid, JSONArray arrayJSON) {
        List<Message> messages = new ArrayList<>();

        // Object that holds the user data
        JSONObject obj = this.getChat(chatid, arrayJSON);

        // Creating an array of sent_messages
        JSONArray messagesArrayJSON = (JSONArray) obj.get("messages");
        for (Object o : messagesArrayJSON) {
            JSONObject data = (JSONObject) o;
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
        users.add(participant1);
        users.add(participant2);
        chat.put("users", users);
        chatsJson.add(chat);

        usersJson = modifyChatName("", chatid, users, usersJson);
        putIntoDatabase(databaseJSON, chatsJson, usersJson);
        return chatid;
    }

    public void addToChat(String chatid, String participant, JSONObject databaseJson, JSONArray usersJson, JSONArray chatsJson) throws Exception {
        JSONArray users = new JSONArray();
        String newChatid = chatid + ";" + participant;
        for (int i = 0; i < chatsJson.size(); i++) {
            JSONObject data = (JSONObject) chatsJson.get(i);
            String id = (String) data.get("id");
            if (id.equals(chatid)) {
                chatsJson.remove(data);
                users = (JSONArray) data.get("users");
                users.add(participant);
                data.put("users", users);
                data.put("id", newChatid);
                chatsJson.add(data);
            }
        }

        JSONArray chats = new JSONArray();
        usersJson = modifyChatName(chatid, newChatid, users, usersJson);
        putIntoDatabase(databaseJson, chatsJson, usersJson);
    }

    public JSONArray removeFromChat(String chatid, String participant, JSONObject databaseJson, JSONArray usersJson, JSONArray chatsJson) throws Exception {
        JSONArray users = new JSONArray();
        String newChatid;
        if (chatid.startsWith(participant)) {
            newChatid = chatid.replaceFirst(participant + ";", "");
        } else if (chatid.endsWith(participant)) {
            newChatid = replaceLast(chatid,";" + participant, "");
        } else {
            newChatid = chatid.replace(";"+participant+ ";", ";");
        }
        for (int i = 0; i < chatsJson.size(); i++) {
            JSONObject data = (JSONObject) chatsJson.get(i);
            String id = (String) data.get("id");
            if (id.equals(chatid)) {
                chatsJson.remove(data);
                users = (JSONArray) data.get("users");
                users.remove(participant);
                data.put("users", users);
                data.put("id", newChatid);
                chatsJson.add(data);
            }
        }
        JSONObject removed = getUser(participant, usersJson);
        usersJson.remove(removed);
        JSONArray chatsIds = (JSONArray) removed.get("chatsIds");
        chatsIds.remove(chatid);
        removed.put("chatsIds", chatsIds);
        usersJson.add(removed);

        usersJson = modifyChatName(chatid, newChatid, users, usersJson);
        putIntoDatabase(databaseJson, chatsJson, usersJson);
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
        putIntoDatabase(databaseJson, chatsJson, newusersJson);
    }

    private void putIntoDatabase(JSONObject databaseJson, JSONArray chatsJson, JSONArray newusersJson) throws IOException {
        databaseJson.put("client", newusersJson);
        databaseJson.put("chats", chatsJson);
        try (FileWriter file = new FileWriter("client_db.json")) {
            file.write(databaseJson.toJSONString());
        }
    }

    public void addUser(String username, JSONObject databaseJson, JSONArray usersJson) throws Exception {
        JSONObject user = new JSONObject();
        user.put("id", username);
        user.put("chatsIds", new JSONArray());
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
        // TODO: 5/9/19 Get rid of null from getUse return 
        JSONObject user = getUser(senderId, usersJson);
        if (user == null) {
            return new String[0];
        }
        JSONArray chats = (JSONArray) user.get("chatsIds");

        //int size = chats.size();
        if (chats != null) {
            int size = chats.size();
            String[] chatIds = new String[size];
            for (int i = 0; i < chats.size(); i++) {
                chatIds[i] = (String) chats.get(i);
            }
            return chatIds;
        }
        return new String[0];
    }

    public JSONArray modifyChatName(String oldChatid, String newChatId, JSONArray users, JSONArray usersJson) {
        JSONArray chats = new JSONArray();
        for (int i = 0; i < users.size(); i++) {
            JSONObject data = getUser((String) users.get(i), usersJson);
            usersJson.remove(data);
            chats = (JSONArray) data.get("chatsIds");
            chats.remove(oldChatid);
            chats.add(newChatId);
            data.put("chatsIds", chats);
            usersJson.add(data);
        }
        return usersJson;
    }

    public static String replaceLast(String string, String toReplace, String replacement) {
        int pos = string.lastIndexOf(toReplace);
        if (pos > -1) {
            return string.substring(0, pos)
                    + replacement
                    + string.substring(pos + toReplace.length(), string.length());
        } else {
            return string;
        }
    }
}
