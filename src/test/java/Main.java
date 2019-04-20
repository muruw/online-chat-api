import org.h2.tools.RunScript;

import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {


        DatabaseFactory factory = new DatabaseFactory();

        // Connecting to the database
        try (Connection connection = factory.connectToDatabase()) {

            // Setting up the database
            try (Reader setupDatabase = factory.createDatabase("setup.sql")) {
                RunScript.execute(connection, setupDatabase);
            }

            System.out.println("Do you want to login(L) or register(R)? ");
            try (Scanner input = new Scanner(System.in)) {
                String userChoice = input.next().toUpperCase();
                if (userChoice.equals("L")) {
                    while (true) {
                        System.out.println("write ur username: ");
                        String username = input.next();
                        System.out.println("write ur password: ");
                        String password = input.next();
                        if (factory.login(connection, username, password)) {
                            System.out.println("Logged in");
                            break;
                        } else {
                            System.out.println("username or password incorrect");
                        }
                    }
                } else if (userChoice.equals("R")) {
                    while (true) {
                        System.out.println("Choose an username");
                        String username = input.next();

                        System.out.println("Choose an password");
                        String password1 = input.next();

                        System.out.println("confirm your password");
                        String password2 = input.next();

                        if (factory.register(connection, username, password1, password2)) {
                            System.out.println("Account created");
                            break;
                        } else {
                            System.out.println("Try again!");
                        }
                    }

                }
            }
        }
    }
}
