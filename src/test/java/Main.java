
import org.h2.tools.RunScript;

import java.io.Reader;
import java.sql.Connection;
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
                        System.out.println("write ur loginUsername: ");
                        String loginUsername = input.next();
                        System.out.println("write ur password: ");
                        String password = input.next();
                        if (factory.login(connection, loginUsername, password)) {
                            System.out.println("Logged in");
                            System.out.println("Id of that user: " + factory.getUserId(connection, loginUsername));
                            break;
                        } else {
                            System.out.println("loginUsername or password incorrect");
                        }
                    }
                } else if (userChoice.equals("R")) {
                    while (true) {
                        System.out.println("Choose an regUsername");
                        String regUsername = input.next();

                        System.out.println("Choose an password");
                        String password1 = input.next();

                        System.out.println("confirm your password");
                        String password2 = input.next();

                        if (factory.register(connection, regUsername, password1, password2)) {
                            System.out.println("Account created");
                            System.out.println("Id of that user: " + factory.getUserId(connection, regUsername));
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
