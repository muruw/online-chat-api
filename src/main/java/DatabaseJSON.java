import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

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

    /**
     *
     * @param userId Id of the user, whose sent messages the application shows
     * @param arrayJSON Arrays of users
     * @return List of messages sent by the specific user
     */
    public List<Message> userSentMessages(long userId, JSONArray arrayJSON) {

        List<Message> messages = new ArrayList<>();

        // Object that holds the user data
        JSONObject obj = (JSONObject) arrayJSON.get((int) (userId - 1));

        // Creating an array of sent_messages
        JSONArray messagesArrayJSON = (JSONArray) obj.get("sent_messages");
        for (int i = 0; i < messagesArrayJSON.size(); i++) {
            JSONObject data = (JSONObject) messagesArrayJSON.get(i);

            long sender = (long) data.get("receiver");
            String userMessage = (String) data.get("message");

            Message message = new Message(sender, userMessage);
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
    public List<Message> userReceivedMessages(long userId, JSONArray arrayJSON) {

        List<Message> messages = new ArrayList<>();

        // Object that holds the user data
        JSONObject obj = (JSONObject) arrayJSON.get((int) (userId - 1));

        // Creating an array of messages(sent or received, depends on the parameter)
        JSONArray messagesArrayJSON = (JSONArray) obj.get("received_messages");
        for (int i = 0; i < messagesArrayJSON.size(); i++) {
            JSONObject data = (JSONObject) messagesArrayJSON.get(i);

            long messageType = (long) data.get("user");
            String userMessage = (String) data.get("message");

            Message message = new Message(messageType, userMessage);
            messages.add(message);
        }

        return messages;
    }

    /**
     * Adds a message to the JSON database sent by user
     * @param userId Id of the user, whose sent the message
     * @param receiverId Id of the user to whom the message is sent to
     * @param message String type message
     * @param databaseJSON JSONObject type HashMap that shows the whole database
     * @param usersJSON JSONArray type, has all of the users in it
     * @param orderJSON JSONArray type, shows the order how the data should be shown
     * @throws Exception
     */
    public void addSentMessage(long userId, long receiverId, String message, JSONObject databaseJSON, JSONArray usersJSON, JSONArray orderJSON) throws Exception {

        JSONArray userSentMessages = (JSONArray) ((JSONObject) usersJSON.get((int) (userId - 1))).get("sent_messages");

        // Adding the message to the JSON file
        JSONObject messageData = new JSONObject();
        messageData.put("receiver", receiverId);
        messageData.put("message", message);
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
     * @param userId Id of the user, whose received the message
     * @param receiverId Id of the user who sent the message
     * @param message String type message
     * @param databaseJSON JSONObject type HashMap that shows the whole database
     * @param usersJSON JSONArray type, has all of the users in it
     * @param orderJSON JSONArray type, shows the order how the data should be shown
     * @throws Exception
     */
    public void addReceivedMessage(long userId, long receiverId, String message, JSONObject databaseJSON, JSONArray usersJSON, JSONArray orderJSON) throws Exception {

        JSONArray userSentMessages = (JSONArray) ((JSONObject) usersJSON.get((int) (userId - 1))).get("received_messages");

        // Adding the message to the JSON file
        JSONObject messageData = new JSONObject();
        messageData.put("user", receiverId);
        messageData.put("message", message);
        userSentMessages.add(messageData);

        databaseJSON.put("client", usersJSON);
        databaseJSON.put("client_order", orderJSON);

        // Rewriting the file
        try (FileWriter file = new FileWriter("client_db.json")) {
            file.write(databaseJSON.toJSONString());
        }
    }
}
