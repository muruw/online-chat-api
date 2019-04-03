package client;

/* Holds data about the current user, set when running the client.
 */

public class UserData {
    private int userID;

    private int port;
    private String host;

    public UserData(int userID, int port, String host) {
        this.userID = userID;
        this.port = port;
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }
}
