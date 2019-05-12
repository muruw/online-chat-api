import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
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
        JSONObject chat = this.getChat(chatid, chatsJson);
        chat.put("lastMsg", time);
        JSONArray messages = (JSONArray) (chat.get("messages"));

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
        JSONObject chat = new JSONObject();
        chat.put("messages", new JSONArray());
        JSONArray users = new JSONArray();
        users.add(participant1);
        users.add(participant2);
        chat.put("users", users);
        chat.put("lastMsg", Instant.now().toString());
        String chatid = newChatName(users);
        if (getChat(chatid, chatsJson) != null ) {
            chatid = addDistinguisher(chatid, chatsJson);
        }
        chat.put("id", chatid);
        chatsJson.add(chat);

        usersJson = changeChatNameOnAllParticipants("", chatid, users, usersJson);
        putIntoDatabase(databaseJSON, chatsJson, usersJson);
        return chatid;
    }

    public String addToChat(String chatid, String participant, JSONObject databaseJson, JSONArray usersJson, JSONArray chatsJson) throws Exception {
        JSONArray users = new JSONArray();
        String newChatid = "";
        for (int i = 0; i < chatsJson.size(); i++) {
            JSONObject data = (JSONObject) chatsJson.get(i);
            String id = (String) data.get("id");
            if (id.equals(chatid)) {
                chatsJson.remove(data);
                users = (JSONArray) data.get("users");
                if (isDefaultName(chatid, users)) {
                    users.add(participant);
                    data.put("users", users);
                    newChatid = newChatName(users);
                    if (getChat(newChatid, chatsJson) != null) {
                        newChatid = addDistinguisher(newChatid, chatsJson);
                    }
                } else {
                    users.add(participant);
                    data.put("users", users);
                    newChatid = chatid;
                }
                data.put("id", newChatid);
                chatsJson.add(data);
            }
        }

        usersJson = changeChatNameOnAllParticipants(chatid, newChatid, users, usersJson);
        putIntoDatabase(databaseJson, chatsJson, usersJson);
        return newChatid;
    }

    public JSONArray removeFromChat(String chatid, String participant, JSONObject databaseJson, JSONArray usersJson, JSONArray chatsJson) throws Exception {
        JSONArray users = new JSONArray();
        String newChatid;
        String oldChatid = "";
        JSONObject data = getChat(chatid, chatsJson);
        chatsJson.remove(data);
        users = (JSONArray) data.get("users");
        users.remove(participant);
        data.put("users", users);
        if (chatid.contains(".")) {
            String[] chatAndDistictNumb = chatid.split("\\.");
            oldChatid = chatAndDistictNumb[0];
        } else {
            oldChatid = chatid;
        }
        if (isDefaultName(chatid, users)) {
            if (oldChatid.startsWith(participant)) {
                newChatid = oldChatid.replaceFirst(participant + ";", "");
            } else if (oldChatid.endsWith(participant)) {
                newChatid = replaceLast(oldChatid, ";" + participant, "");
            } else {
                newChatid = oldChatid.replace(";" + participant + ";", ";");
            }
            if (getChat(newChatid, chatsJson) != null) {
                newChatid = addDistinguisher(newChatid, chatsJson);
            }
        } else {
            newChatid = chatid;
        }
        data.put("id", newChatid);
        chatsJson.add(data);

        JSONObject removed = getUser(participant, usersJson);
        usersJson.remove(removed);
        JSONArray chatsIds = (JSONArray) removed.get("chatsIds");
        chatsIds.remove(chatid);
        removed.put("chatsIds", chatsIds);
        usersJson.add(removed);

        usersJson = changeChatNameOnAllParticipants(chatid, newChatid, users, usersJson);
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

    public String[] getChats(String senderId, JSONArray usersJson, JSONArray chatJson) {
        JSONObject user = getUser(senderId, usersJson);
        if (user == null) {
            return new String[0];
        }
        JSONArray chats = (JSONArray) user.get("chatsIds");

        if (chats != null) {
            int size = chats.size() * 2;
            String[] chatIds = new String[size];
            int j = 0;
            for (int i = 0; i < chats.size() * 2; i+=2) {
                String chatId = (String) chats.get(j);
                chatIds[i] = chatId;
                chatIds[i+1] = (String) this.getChat(chatId, chatJson).get("lastMsg");
                j++;
            }
            return chatIds;
        }
        return new String[0];
    }

    public JSONArray changeChatNameOnAllParticipants(String oldChatid, String newChatId, JSONArray users, JSONArray usersJson) {
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

    public String newChatName(JSONArray users) {
        List<String> userslist = new ArrayList<>();
        for (int i=0; i<users.size(); i++) {
            userslist.add( (String) users.get(i) );
        }
        Collections.sort(userslist);
        String newChatid = String.join(";", userslist);
        return newChatid;
    }

    public String addDistinguisher(String chatId, JSONArray chats) {
        int i = 1;
        String newchatId = "";
        while (true) {
            newchatId = chatId + "." + i;
            if (getChat(newchatId, chats) == null) {
                break;
            } else {
                i++;
            }
        }
        return newchatId;
    }

    public String customName(String oldchatid, String newchatid, JSONObject databasejson, JSONArray chatsjson, JSONArray usersjson) throws Exception {
        if (getChat(newchatid, chatsjson) != null) {
            newchatid = addDistinguisher(newchatid, chatsjson);
        }
        JSONObject chat = getChat(oldchatid, chatsjson);
        JSONArray users = (JSONArray) chat.get("users");
        chatsjson.remove(chat);
        chat.put("id", newchatid);
        chatsjson.add(chat);
        usersjson = changeChatNameOnAllParticipants(oldchatid, newchatid, users, usersjson);
        putIntoDatabase(databasejson, chatsjson, usersjson);
        return newchatid;
    }

    public boolean isDefaultName(String chatid, JSONArray users) {
        return chatid.equals(newChatName(users));
    }
}
