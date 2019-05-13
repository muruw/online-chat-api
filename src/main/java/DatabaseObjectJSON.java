import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DatabaseObjectJSON {

    DatabaseJSON database;
    JSONObject databaseObject;
    JSONArray usersJSON;
    JSONArray orderJSON;
    JSONArray chatsJSON;

    public DatabaseObjectJSON(DatabaseJSON database) throws Exception {
        this.database = database;
        this.databaseObject = database.createDatabase();
        this.usersJSON = database.getArrayJSON(databaseObject, "client");
        this.orderJSON = database.getArrayJSON(databaseObject, "client_order");
        this.chatsJSON = database.getArrayJSON(databaseObject, "chats");
    }

    public DatabaseJSON getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseJSON database) {
        this.database = database;
    }

    public JSONObject getDatabaseObject() {
        return databaseObject;
    }

    public void setDatabaseObject(JSONObject databaseObject) {
        this.databaseObject = databaseObject;
    }

    public JSONArray getUsersJSON() {
        return usersJSON;
    }

    public void setUsersJSON(JSONArray usersJSON) {
        this.usersJSON = usersJSON;
    }

    public JSONArray getOrderJSON() {
        return orderJSON;
    }

    public void setOrderJSON(JSONArray orderJSON) {
        this.orderJSON = orderJSON;
    }

    public JSONArray getChatsJSON() {
        return chatsJSON;
    }

    public void setChatsJSON(JSONArray chatsJSON) {
        this.chatsJSON = chatsJSON;
    }
}
