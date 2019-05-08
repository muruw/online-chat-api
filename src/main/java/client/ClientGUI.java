package client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientGUI extends Application {
    private String mainUser;
    private List<String> options;
    private Socket mainSocket;
    private DataOutputStream mainOutStream;
    private DataInputStream mainInStream;

    public ClientGUI() {
        this.options = new ArrayList<>();
    }

    @Override
    public void start(Stage peaLava) {
        try{mainSocket = new Socket("localhost", 1337);
            mainOutStream = new DataOutputStream(mainSocket.getOutputStream());
            mainInStream= new DataInputStream(mainSocket.getInputStream());
        }catch (Exception e){
            throw new RuntimeException(e);
        }

        ObservableList<String> users = FXCollections.observableArrayList();
        System.out.println(this.options);
        peaLava.setTitle("Online Chat");
        BorderPane border = new BorderPane();
        HBox navigationBar = new HBox();

        //ChatBoxmis
        TextArea chat = new TextArea();
        chat.setMaxSize(550, 350);
        chat.setDisable(true);
        chat.setText("test");


        // TODO: 4/19/19  usernames from database
        //usernames
        ListView<String> userNames = new ListView<>(users);
        userNames.setPrefSize(200, 550);
        userNames.setOrientation(Orientation.VERTICAL);
        userNames.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> chat.setText(s + t1));



        //add and remove buttons
        Button add = new Button("Add chat");
        add.setPrefSize(100,20);
        add.setOnAction(actionEvent -> {
            TextInputDialog newChat = new TextInputDialog();
            newChat.setTitle("Create a new chat");
            newChat.setHeaderText("Give userName or chatName with what you want to connect");
            Optional<String> answer = newChat.showAndWait();

            answer.ifPresent(personsID -> {
                try {
                    // TODO: 4/21/19 Server should have a check that no duplicate chats
                    String chatId = IO.newChat(mainUser, personsID, mainOutStream,mainInStream);
                    users.add(String.valueOf(chatId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        Button addPeople = new Button("Add person");
        addPeople.setPrefSize(100,20);
        addPeople.setOnAction(actionEvent -> {
            TextInputDialog newChat = new TextInputDialog();
            newChat.setTitle("Add an user");
            newChat.setHeaderText("Enter the users name who you wish to add");
            Optional<String> answer = newChat.showAndWait();

            answer.ifPresent(personsID -> {
                try {
                    // TODO: 4/21/19 Server should have a check that no duplicate chats
                    IO.addPerson(userNames.getSelectionModel().getSelectedItem(),personsID,mainOutStream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        Button removePerson = new Button("Delete user");
        removePerson.setPrefSize(100,20);
        removePerson.setOnAction(actionEvent -> {
            TextInputDialog newChat = new TextInputDialog();
            newChat.setTitle("Remove person");
            newChat.setHeaderText("Enter the username you want to remove");
            Optional<String> answer = newChat.showAndWait();

            answer.ifPresent(personsID -> {
                try {
                    IO.removeChat(userNames.getSelectionModel().getSelectedItem(),mainOutStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });

        Button deleteChat = new Button("Delete Chat");
        deleteChat.setPrefSize(100,20);
        deleteChat.setOnAction(actionEvent -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete chat");
            alert.setHeaderText("Are you sure you want to delete this chat");
            alert.setContentText("This will delete the chat and its contents for everyone");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                try {
                    IO.removeChat(userNames.getSelectionModel().getSelectedItem(),mainOutStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });




        TextField textArea = new TextField();
        textArea.setPrefSize(480,40);
        //fetch data
        Button refresh = new Button("Refresh");
        refresh.setPrefSize(100,20);
        refresh.setOnAction(actionEvent -> {
                              // TODO: 5/8/19 Lisada id saatmine ka edasi
            updateChats(users);
        });

        //SendButton
        Button sendButton = new Button("Send");
        sendButton.setPrefSize(70,40);
        sendButton.setOnAction(event -> {
            try {
                List<String> messages = IO.sendMessage(1, textArea.getText(), mainUser, userNames.getSelectionModel().getSelectedItem(), mainOutStream, mainInStream);
                textArea.clear();
                chat.setText(messageParser(messages));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        HBox addRow = new HBox();
        HBox removeRow = new HBox();
        removeRow.getChildren().addAll(deleteChat,removePerson);
        addRow.getChildren().addAll(add,addPeople);
        VBox userNamesAndButtons = new VBox();
        userNamesAndButtons.getChildren().addAll(userNames, addRow,removeRow,refresh );
        VBox userMessages = new VBox();
        HBox textAreaWithSend = new HBox();

/*
        options.addListener((ListChangeListener<String>) change -> {
            System.out.println("change!");
            userNames.setItems(options);
        });
*/


        //Setup
        border.setPadding(new Insets(15, 12, 15, 12));
        border.setStyle("-fx-background-color: #fff;");
        textAreaWithSend.getChildren().addAll(textArea, sendButton);
        border.setTop(navigationBar);
        border.setLeft(chat);
        border.setRight(userNamesAndButtons);
        border.setBottom(textAreaWithSend);
        border.setCenter(userMessages);


        Scene tseen1 = new Scene(border, 800, 400, Color.SNOW);


        //register
        BorderPane border2 = new BorderPane();

        Label registerLabel = new Label("Register to our online-chat");
        registerLabel.setMinSize(100, 100);


        TextField usernameRegister = new TextField("username");
        usernameRegister.setMaxSize(100, 100);
        TextField passwordRegister = new TextField("password");
        passwordRegister.setMaxSize(100, 100);
        TextField passwordConfirm = new TextField("password confirm");
        passwordConfirm.setMaxSize(100, 100);

        Button registerConfirm = new Button("Register");
        registerConfirm.setOnAction(actionEvent -> {
            if (passwordConfirm.getText().equals(passwordRegister.getText())) {
                try {
                    Long userId = IO.register(usernameRegister.getText(), passwordConfirm.getText(), mainOutStream,mainInStream);
                    mainUser = usernameRegister.getText();
                    if(userId != -1) {
                        System.out.println(userId + " logged in");
                        // TODO: 5/8/19 Lisada id saatmine ka edasi
                        updateChats(users);
                        peaLava.setScene(tseen1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        VBox registerDetails = new VBox();
        registerDetails.getChildren().addAll(registerLabel, usernameRegister, passwordRegister, passwordConfirm, registerConfirm);
        registerDetails.setAlignment(Pos.CENTER);

        border2.setCenter(registerDetails);
        Scene tseen3 = new Scene(border2, 800, 400, Color.SNOW);


        //

        BorderPane border1 = new BorderPane();

        Label welcomeLabel = new Label("Welcome to our online-chat");
        welcomeLabel.setMinSize(100, 100);


        TextField username = new TextField("username");
        username.setMaxSize(100, 100);
        TextField password = new TextField("password");
        password.setMaxSize(100, 100);

        Button login = new Button("Login");
        login.setOnAction(actionEvent -> {
            try {
                Long userId = IO.login(username.getText(), password.getText(), mainOutStream,mainInStream);
                mainUser = username.getText();
                if (userId != -1) {
                    // TODO: 5/8/19 Lisada id saatmine ka edasi
                    updateChats(users);
                    peaLava.setScene(tseen1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Button register = new Button("Register");
        register.setOnAction(actionEvent -> peaLava.setScene(tseen3));

        VBox loginDetails = new VBox();
        HBox buttons = new HBox();
        buttons.getChildren().addAll(login, register);
        buttons.setAlignment(Pos.CENTER);
        loginDetails.getChildren().addAll(welcomeLabel, username, password, buttons);
        loginDetails.setAlignment(Pos.CENTER);

        border1.setCenter(loginDetails);
        Scene tseen2 = new Scene(border1, 800, 400, Color.SNOW);


        //
        peaLava.setScene(tseen2);
        peaLava.show();
        //

    }

    private void updateChats(ObservableList<String> users) {
        List<Long> userIDS;
        try {
            userIDS = IO.getChat(mainUser,mainOutStream,mainInStream);

            for(Long id : userIDS){
                System.out.println(id);
                if(!users.contains(String.valueOf(id))) {
                    users.add(String.valueOf(id));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }

    //Someone made chats and did not inform me. Atm does not not display other users info. Will work together next week to resolve
    public String messageParser(List<String> inputData) {
        StringBuilder textToDisplay = new StringBuilder();
        for (int i = 0; i < inputData.size(); i++) {
            if (i % 2 == 0) {
                if (inputData.get(i).equals(mainUser)) {
                    textToDisplay.append(inputData.get(i)).append(": ");
                } else {
                    textToDisplay.append("                                                                               ").append(inputData.get(i)).append(": ");
                }
            } else {
                textToDisplay.append(inputData.get(i)).append("\n");
            }
        }
        return textToDisplay.toString();
    }

}
