import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.net.*;

public class Server {
    public static void main(String[] args) throws Exception {
        String filepath = "client_db.json";
        DatabaseJSON databaseJSON = new DatabaseJSON(filepath);

        try (ServerSocket ss = new ServerSocket(1337)) {
            while (true) {
                Socket socket = ss.accept();
                new Thread(new serverThread(socket, databaseJSON)).start();
            }
        }
    }
}

