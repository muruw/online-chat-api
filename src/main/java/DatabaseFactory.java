

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.h2.tools.RunScript;

import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/*
                                       _
                                      / \
                                     :oo >      How did we get into this mess?
                                    __\ /__     We seem to be made to suffer.
               ___                 /  _    \
              / <>\               // :/ \: \\
            _:_____:_             \\  \_/  ::
           : : === : :             \ :\ /: ::
           :_:  0  :_:             #  _ _/ #
            ::  0  ::                : : :
            ::__*__::                : : :
           :~ \___/ ~:               []:[]
           /=\ /=\ /=\               : : :
___________[_]_[_]_[_]______________/_]_[_\___________________________________
*/

public class DatabaseFactory {

    final Argon2 argon2 = Argon2Factory.create();
    /**
     * @return connection to the database
     * @throws Exception
     **/
    public Connection connectToDatabase() throws Exception {

        // Connect to 'users.db' if possible, otherwise create a new database called 'users.db'
        return DriverManager.getConnection("jdbc:h2:./db/users.db");
    }


    public InputStreamReader createDatabase(String name) throws Exception {
        ClassLoader classLoader = DatabaseFactory.class.getClassLoader();
        return new InputStreamReader(classLoader.getResourceAsStream(name), "UTF-8");
    }

    /**
     * Display data from the database
     *
     * @param connection
     * @throws SQLException
     */
    private List<String> getUsernames(Connection connection) throws SQLException {
        List<String> usernames = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement("SELECT username FROM Users;");

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String username = rs.getString("username");
                usernames.add(username);
            }
        }
        return usernames;
    }

    public boolean register(Connection connection, String username, String password1, String password2) throws SQLException {
        if (!password1.equals(password2)) {
            return false;
        }
        // Checking whether an username exists already
        if (getUsernames(connection).contains(username)) {
            return false;
        }

        String passwordHash = argon2.hash(30, 65536, 1, password1.toCharArray());

        PreparedStatement ps = connection.prepareStatement("INSERT INTO Users(username, password) VALUES(?, ?);");

        ps.setString(1, username);
        ps.setString(2, passwordHash);
        ps.executeUpdate();

        // generate a confirmation code
        int code = ThreadLocalRandom.current().nextInt(500, 999);

        // every time a user has been created we send a mail to the user
        String msg = "Thank you for registering! YOUR CODE IS: " + code + ". Please contact looga.krister@gmail.com";
        SendMail sendMail = new SendMail("magnarvares@gmail.com", msg);
        sendMail.sendEmail();

        return true;
    }

    public boolean login(Connection connection, String username, String password) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT username, password FROM Users;");
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String rs_username = rs.getString("username");
                String rs_password = rs.getString("password");
                if (rs_username.equals(username) && argon2.verify(rs_password, password)) {
                    return true;
                }
            }
            System.out.println("fail");
        }
        return false;
    }

    public long getUserId(Connection connection, String username) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT id FROM Users WHERE username = ?;");
        ps.setString(1, username);

        long id = -1;

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                id = rs.getInt("id");
            }
        }
        return id;
    }
}
