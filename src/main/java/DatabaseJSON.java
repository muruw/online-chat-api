import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class DatabaseJSON {

    private String filePath;

    public DatabaseJSON(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Creates and returns a database object
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
     *
     * @param jsonObj Database object
     * @param jsonTable name of the column
     * @return
     */
    public JSONArray getArrayJSON(JSONObject jsonObj, String jsonTable) {

        JSONArray arrayJSON = (JSONArray) jsonObj.get(jsonTable);

        return arrayJSON;
    }

    /**
     *
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
    public JSONObject getUser(long neededId, JSONArray arrayJSON) {
        for (int i = 0; i < arrayJSON.size(); i++) {
            JSONObject obj = (JSONObject) arrayJSON.get(i);
            long userId = (long) obj.get("id");
            if (userId == neededId) {
                return obj;
            }
        }
        return null;
    }

    /**
     *
     * @param userId Id of the user, whose sent messages the application shows
     * @param arrayJSON Arrays of users
     * @return List of messages sent by the specific user
     */
    public List<Message> userAllSentMessages(long userId, JSONArray arrayJSON) {

        List<Message> messages = new ArrayList<>();

        // Object that holds the user data
        JSONObject obj = this.getUser(userId, arrayJSON);

        // Creating an array of sent_messages
        JSONArray messagesArrayJSON = (JSONArray) obj.get("sent_messages");
        for (int i = 0; i < messagesArrayJSON.size(); i++) {
            JSONObject data = (JSONObject) messagesArrayJSON.get(i);

            String userMessage = (String) data.get("message");
            long chatid = (long) data.get("receiver");
            String time = (String) data.get("time");

            Message message = new Message(chatid, userId, userMessage, time);
            messages.add(message);
        }

        return messages;
    }

    /**
     *
     * @param userId Id of the user, whose received messages the application shows
     * @param arrayJSON Arrays of users
     * @return List of messages sent by the specific user
     */
    public List<Message> userAllReceivedMessages(long userId, JSONArray arrayJSON) {

        List<Message> messages = new ArrayList<>();

        // Object that holds the user data
        JSONObject obj = this.getUser(userId, arrayJSON);

        // Creating an array of messages(sent or received, depends on the parameter)
        JSONArray messagesArrayJSON = (JSONArray) obj.get("received_messages");
        for (int i = 0; i < messagesArrayJSON.size(); i++) {
            JSONObject data = (JSONObject) messagesArrayJSON.get(i);

            String userMessage = (String) data.get("message");
            long chatid = (long) data.get("chat-id");
            long sender = (long) data.get("sender");
            String time = (String) data.get("time");

            Message message = new Message(chatid, sender, userMessage, time);
            messages.add(message);
        }

        return messages;
    }

    //gets all the sent messages of a person in a specific private chat/convo
    public List<Message> userConvoSent(long userId, long recieverId, JSONArray arrayJSON) {
        List<Message> messages = new ArrayList<>();

        // Object that holds the user data
        JSONObject obj = this.getUser(userId, arrayJSON);

        // Creating an array of sent_messages
        JSONArray messagesArrayJSON = (JSONArray) obj.get("sent_messages");
        for (int i = 0; i < messagesArrayJSON.size(); i++) {
            JSONObject data = (JSONObject) messagesArrayJSON.get(i);

            long receiver = (long) data.get("receiver");
            if (receiver == recieverId) {
                String userMessage = (String) data.get("message");
                String time = (String) data.get("time");
                Message message = new Message(recieverId, userId, userMessage, time);
                messages.add(message);
            }
        }
        return messages;
    }

    //gets all the received messages of a person in a specific private chat/convo
    public List<Message> userConvoRecieved(long userId, long chatid, JSONArray arrayJSON) {
        List<Message> messages = new ArrayList<>();

        // Object that holds the user data
        JSONObject obj = this.getUser(userId, arrayJSON);

        // Creating an array of sent_messages
        JSONArray messagesArrayJSON = (JSONArray) obj.get("received_messages");
        for (int i = 0; i < messagesArrayJSON.size(); i++) {
            JSONObject data = (JSONObject) messagesArrayJSON.get(i);

            long chat = (long) data.get("chat-id");
            if (chat == chatid) {
                String userMessage = (String) data.get("message");
                long sender = (long) data.get("sender");
                String time = (String) data.get("time");
                Message message = new Message(chatid, sender, userMessage, time);
                messages.add(message);
            }
        }
        return messages;
    }

    /**
     * Adds a message to the JSON database sent by user
     *
     * @param userId       Id of the user, whose sent the message
     * @param receiverId   Id of the user to whom the message is sent to
     * @param message      String type message
     * @param databaseJSON JSONObject type HashMap that shows the whole database
     * @param usersJSON    JSONArray type, has all of the users in it
     * @param orderJSON    JSONArray type, shows the order how the data should be shown
     * @throws Exception
     */
     void addSentMessage(long userId, long receiverId, String message, String time, JSONObject databaseJSON, JSONArray usersJSON, JSONArray orderJSON) throws Exception {

        JSONArray userSentMessages = (JSONArray) (this.getUser(userId, usersJSON).get("sent_messages"));

        // Adding the message to the JSON file
        JSONObject messageData = new JSONObject();
        messageData.put("receiver", receiverId);
        messageData.put("message", message);
        messageData.put("time", time);
        userSentMessages.add(messageData);

        databaseJSON.put("client", usersJSON);
        databaseJSON.put("client_order", orderJSON);

        // Rewriting the file
        try (FileWriter file = new FileWriter("client_db.json")) {
            file.write(databaseJSON.toJSONString());
        }
    }
    /**
     * Adds a message to the JSON database sent by user
     * @param chatId Id of the chat, whose received the message
     * @param senderId Id of the user who sent the message
     * @param message String type message
     * @param databaseJSON JSONObject type HashMap that shows the whole database
     * @param usersJSON JSONArray type, has all of the users in it
     * @param orderJSON JSONArray type, shows the order how the data should be shown
     * @throws Exception
     */
    public void addReceivedMessage(long chatId, long senderId, String message, String time, JSONObject databaseJSON, JSONArray usersJSON, JSONArray orderJSON, JSONArray chatsJSON) throws Exception {
        JSONArray users = chatParticipants(chatId, chatsJSON);
        for (Object id : users) {
            long userid = Long.parseLong(id.toString());
            if (userid != senderId) {
                JSONArray userSentMessages = (JSONArray) (this.getUser(userid, usersJSON).get("received_messages"));
                JSONObject messageData = new JSONObject();
                messageData.put("sender", senderId);
                messageData.put("message", message);
                messageData.put("chat-id", chatId);
                messageData.put("time", time);
                userSentMessages.add(messageData);
            }
        }

        databaseJSON.put("client", usersJSON);
        databaseJSON.put("client_order", orderJSON);


        // Rewriting the file
        try (FileWriter file = new FileWriter("client_db.json")) {
            file.write(databaseJSON.toJSONString());
        }

    }

    public JSONArray chatParticipants(long chatId, JSONArray chatsJson) {
        // Looping the chats array to get the array of users
        JSONArray users = new JSONArray();
        for (int i = 0; i < chatsJson.size(); i++) {
            JSONObject data = (JSONObject) chatsJson.get(i);
            long id = (long) data.get("id");
            if (id == chatId) {
                users = (JSONArray) data.get("users");
            }
        }
        return users;
    }

    public void newChat(long participant1, long participant2, JSONObject databaseJSON, JSONArray chatsJson) throws Exception {
        long biggestId = biggestId(chatsJson);
        JSONObject chat = new JSONObject();
        JSONArray participants = new JSONArray();
        participants.add(participant1);
        participants.add(participant2);
        chat.put("id", biggestId + 1);
        chat.put("users", participants);
        chatsJson.add(chat);
        databaseJSON.put("chats", chatsJson);
        try (FileWriter file = new FileWriter("client_db.json")) {
            file.write(databaseJSON.toJSONString());
        }
    }

    public void addToChat(long chatid, long participant, JSONObject databaseJson, JSONArray chatsJson) throws Exception {
        JSONArray users = new JSONArray();
        for (int i = 0; i < chatsJson.size(); i++) {
            JSONObject data = (JSONObject) chatsJson.get(i);
            long id = (long) data.get("id");
            if (id == chatid) {
                chatsJson.remove(data);
                users = (JSONArray) data.get("users");
                users.add(participant);
                data.put("users", users);
                chatsJson.add(data);
            }
        }
        databaseJson.put("chats", chatsJson);
        try (FileWriter file = new FileWriter("client_db.json")) {
            file.write(databaseJson.toJSONString());
        }
    }

    public void removeFromChat(long chatid, long participant, JSONObject databaseJson, JSONArray usersJson, JSONArray chatsJson) throws Exception {
        JSONArray users = new JSONArray();
        for (int i = 0; i < chatsJson.size(); i++) {
            JSONObject data = (JSONObject) chatsJson.get(i);
            long id = (long) data.get("id");
            if (id == chatid) {
                chatsJson.remove(data);
                users = (JSONArray) data.get("users");
                for (int j = 0; j < users.size(); j++) {
                    long userid = (long) users.get(j);
                    if (userid == participant) {
                        deleteChatHistory(chatid, participant, databaseJson, usersJson);
                        users.remove(userid);
                        break;
                    }
                }
                data.put("users", users);
                chatsJson.add(data);
            }
        }
        databaseJson.put("chats", chatsJson);
        try (FileWriter file = new FileWriter("client_db.json")) {
            file.write(databaseJson.toJSONString());
        }

    }

    public void deleteChat(long chatid, JSONObject databaseJson, JSONArray usersJson, JSONArray chatsJson) throws Exception {
        for (int i = 0; i < chatsJson.size(); i++) {
            JSONObject data = (JSONObject) chatsJson.get(i);
            long id = (long) data.get("id");
            if (id == chatid) {
                JSONArray users = (JSONArray) data.get("users");
                for (int j = 0; j < users.size(); j++) {
                    System.out.println((long) users.get(j));
                    deleteChatHistory(chatid, (long) users.get(j), databaseJson, usersJson);
                }
                chatsJson.remove(data);
            }
        }
        databaseJson.put("chats", chatsJson);
        try (FileWriter file = new FileWriter("client_db.json")) {
            file.write(databaseJson.toJSONString());
        }
    }

    public void addUser(String username, JSONObject databaseJson, JSONArray usersJson) throws Exception {
        long biggestid = biggestId(usersJson);
        JSONObject user = new JSONObject();
        user.put("id", biggestid + 1);
        user.put("sent_messages", new JSONArray());
        user.put("received_messages", new JSONArray());
        user.put("client_name", username);
        usersJson.add(user);
        databaseJson.put("client", usersJson);
        try (FileWriter file = new FileWriter("client_db.json")) {
            file.write(databaseJson.toJSONString());
        }
    }

    public void deleteUser(long userid, JSONObject databaseJson, JSONArray usersJson) throws Exception {
        for (int i = 0; i < usersJson.size(); i++) {
            JSONObject data = (JSONObject) usersJson.get(i);
            long id = (long) data.get("id");
            if (id == userid) {
                usersJson.remove(data);
            }
        }
        databaseJson.put("client", usersJson);
        try (FileWriter file = new FileWriter("client_db.json")) {
            file.write(databaseJson.toJSONString());
        }
    }

    public void deleteChatHistory(long chatid, long userid, JSONObject databaseJson, JSONArray usersJson) throws Exception {
        for (int i = 0; i < usersJson.size(); i++) {
            JSONObject data = (JSONObject) usersJson.get(i);
            long id = (long) data.get("id");
            if (id == userid) {
                usersJson.remove(data);
                JSONArray sent = (JSONArray) data.get("sent_messages");
                JSONArray newSent = new JSONArray();
                JSONArray received = (JSONArray) data.get("received_messages");
                JSONArray newRec = new JSONArray();
                for (int j = 0; j < sent.size(); j++) {
                    JSONObject msg = (JSONObject) sent.get(j);
                    long thischatid = (long) msg.get("receiver");
                    if (thischatid != chatid) {
                        System.out.println(thischatid + " " + chatid);
                        newSent.add(msg);
                    }
                }
                for (int j = 0; j < received.size(); j++) {
                    JSONObject msg = (JSONObject) received.get(j);
                    long thischatid = (long) msg.get("chat-id");
                    if (thischatid != chatid) {
                        System.out.println(thischatid + " " + chatid);
                        newRec.add(msg);
                    }
                }
                data.put("sent_messages", newSent);
                data.put("received_messages", newRec);
                usersJson.add(data);
            }
        }
        databaseJson.put("client", usersJson);
        try (FileWriter file = new FileWriter("client_db.json")) {
            file.write(databaseJson.toJSONString());
        }

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
