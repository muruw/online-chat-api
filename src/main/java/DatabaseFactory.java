

import org.h2.tools.RunScript;

import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.*;

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

    public static void main(String[] args) throws Exception {

        // Connecting to the database
        try(Connection connection = connectToDatabase()){

            // Setting up the database
            try(Reader setupDatabase = createDatabase("setup.sql")){
                RunScript.execute(connection, setupDatabase);
            }
            display(connection);
        }
    }

    /**
     * @return connection to the database
     * @throws Exception
     **/
    private static Connection connectToDatabase() throws Exception {

        // Connect to 'users.db' if possible, otherwise create a new database called 'users.db'
        return DriverManager.getConnection("jdbc:h2:./db/users.db");
    }


    private static InputStreamReader createDatabase(String name) throws Exception{
        ClassLoader classLoader = DatabaseFactory.class.getClassLoader();
        return new InputStreamReader(classLoader.getResourceAsStream(name), "UTF-8");
    }

    /**
     * Display data from the database
     * @param connection
     * @throws SQLException
     */
    private static void display(Connection connection) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM Users;");
        try(ResultSet rs = ps.executeQuery()){
            while(rs.next()){
                String username = rs.getString("username");
                String password = rs.getString("password");
                System.out.println("username: " + username + "; password: " + password);
            }
        }
    }
}
