import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class DatabaseJSON {

    private String filePath;

    public DatabaseJSON(String filePath){
        this.filePath = filePath;
    }

    public JSONObject createDatabase() throws Exception {
        JSONParser parser = new JSONParser();
        File filePath = new File(this.filePath);
        JSONObject databaseJSONObject = (JSONObject) parser.parse(new FileReader(filePath));

        return databaseJSONObject;
    }

    public JSONArray getArrayJSON(JSONObject jsonObj, String jsonTable) {
        JSONArray arrayJSON = (JSONArray) jsonObj.get(jsonTable);

        return arrayJSON;
    }

    public List<User> getUsers(JSONArray arrayJSON){
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

    public List<Message> userSentMessages(long userId, JSONArray arrayJSON){
        List<Message> messages = new ArrayList<>();

        // Object that holds the user data
        JSONObject obj = (JSONObject) arrayJSON.get((int) (userId - 1));

        // Creating an array of messages(sent or received, depends on the parameter)
        JSONArray messagesArrayJSON = (JSONArray) obj.get("sent_messages");
        for (int i = 0; i < messagesArrayJSON.size(); i++) {
            JSONObject data = (JSONObject) messagesArrayJSON.get(i);

            long messageType = (long) data.get("receiver");
            String userMessage = (String) data.get("message");

            Message message = new Message(messageType, userMessage);
            messages.add(message);
        }

        return messages;
    }

    public List<Message> userReceivedMessages(long userId, JSONArray arrayJSON){
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

    public void addSentMessage(long userId, long receiverId, String message, JSONArray arrayJSON) throws Exception{

        JSONObject userData = (JSONObject) arrayJSON.get((int) (userId - 1));
        JSONArray userSentMessages = (JSONArray) userData.get("sent_messages");

        // Adding the message to the JSON file
        JSONObject messageData = new JSONObject();
        messageData.put("receiver", receiverId);
        messageData.put("message", message);

        userSentMessages.add(messageData);
        System.out.println(messageData);
        Files.write(Paths.get("client_db.json"), userSentMessages.toJSONString().getBytes(), StandardOpenOption.APPEND);

    }

}
