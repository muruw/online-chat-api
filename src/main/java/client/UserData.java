package client;

/* Holds data about the current user, set when running the client.
 */

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class UserData {
    private long userID;


    public UserData(int userID, int port, String host) {
        this.userID = userID;

    }


    public long getUserID() {
        return userID;
    }

}
