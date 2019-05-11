package client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
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

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage peaLava) {
        try {
            mainSocket = new Socket("localhost", 1338);
            mainOutStream = new DataOutputStream(mainSocket.getOutputStream());
            mainInStream = new DataInputStream(mainSocket.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ObservableList<String> users = FXCollections.observableArrayList();
        System.out.println(this.options);
        peaLava.setTitle("Online Chat");
        BorderPane border = new BorderPane();
        HBox navigationBar = new HBox();

        //ChatBoxmis
        Text chat = new Text();


        //chat.setMaxSize(550, 350);

        //chat.setDisable(true);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefSize(550, 350);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setContent(chat);

        // TODO: 4/19/19  usernames from database
        //usernames
        ListView<String> userNames = new ListView<>(users);
        userNames.setPrefSize(200, 550);
        userNames.setOrientation(Orientation.VERTICAL);
        userNames.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> {
            try {
                if (userNames.getSelectionModel().getSelectedItem() != null) {
                    chat.setText(messageParser(IO.refreshMessage(mainUser, userNames.getSelectionModel().getSelectedItem(), mainOutStream, mainInStream)));
                    scrollPane.setVvalue(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        //add and remove buttons
        Button add = new Button("New chat");
        add.setPrefSize(100, 20);
        add.setOnAction(actionEvent -> {
            TextInputDialog newChat = new TextInputDialog();
            newChat.setTitle("Create a new chat");
            newChat.setHeaderText("Give userName or chatName with what you want to connect");
            Optional<String> answer = newChat.showAndWait();

            answer.ifPresent(personsID -> {
                try {
                    if (!personsID.isEmpty()) {
                        String result = null;
                        try {
                            result = IO.newChat(mainUser, personsID, mainOutStream, mainInStream);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        users.clear();
                        updateChats(users);
                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        });

        Button addPeople = new Button("Add person");
        addPeople.setPrefSize(100, 20);
        addPeople.setOnAction(actionEvent ->

        {
            TextInputDialog addNewPerson = new TextInputDialog();
            addNewPerson.setTitle("Add an user");
            addNewPerson.setHeaderText("Enter the users name who you wish to add");
            Optional<String> answer = addNewPerson.showAndWait();

            answer.ifPresent(personsID -> {
                try {
                    String result = IO.addPerson(userNames.getSelectionModel().getSelectedItem(), personsID, mainOutStream, mainInStream);
                    if (result.equals("")) {
                        //TODO Some popup to let user know user wasnt added
                    }
                    users.clear();
                    updateChats(users);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        Button removePerson = new Button("Delete user");
        removePerson.setPrefSize(100, 20);
        removePerson.setOnAction(actionEvent ->

        {
            TextInputDialog newChat = new TextInputDialog();
            newChat.setTitle("Remove person");
            newChat.setHeaderText("Enter the username you want to remove");
            Optional<String> answer = newChat.showAndWait();

            answer.ifPresent(personsID -> {
                try {
                    String result = IO.removeFromChat(userNames.getSelectionModel().getSelectedItem(), personsID, mainOutStream, mainInStream);
                    if (result.equals("")) {
                        //TODO Some popup to let user know user wasnt removed from chat
                    }
                    users.clear();
                    updateChats(users);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });

        Button deleteChat = new Button("Delete Chat");
        deleteChat.setPrefSize(100, 20);
        deleteChat.setOnAction(actionEvent ->

        {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete chat");
            alert.setHeaderText("Are you sure you want to delete this chat");
            alert.setContentText("This will delete the chat and its contents for everyone");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                try {
                    IO.removeChat(userNames.getSelectionModel().getSelectedItem(), mainOutStream);
                    users.remove(userNames.getSelectionModel().getSelectedItem());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Button customName = new Button("New name");
        customName.setPrefSize(100, 20);
        customName.setOnAction(actionEvent ->

        {
            TextInputDialog newChatName = new TextInputDialog();
            newChatName.setTitle("Change chat name");
            newChatName.setHeaderText("Enter the new chatname");
            Optional<String> answer = newChatName.showAndWait();

            answer.ifPresent(newChatname -> {
                try {
                    String result = IO.setCustomName(userNames.getSelectionModel().getSelectedItem(), newChatname, mainOutStream, mainInStream);
                    if (result.equals("")) {
                        //TODO Some popup to let user know chat name wasnt chagned
                    }
                    users.clear();
                    updateChats(users);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });


        TextField textArea = new TextField();
        textArea.setPrefSize(480, 40);
        //fetch data
        Button refresh = new Button("Refresh");
        refresh.setPrefSize(100, 20);
        refresh.setOnAction(actionEvent ->

        {
            try {
                chat.setText(messageParser(IO.refreshMessage(mainUser, userNames.getSelectionModel().getSelectedItem(), mainOutStream, mainInStream)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            users.clear();
            updateChats(users);
            scrollPane.setVvalue(1);
        });

        //SendButton
        Button sendButton = new Button("Send");
        sendButton.setPrefSize(70, 40);
        sendButton.setOnAction(event ->

        {
            sendMessage(userNames, textArea, chat, scrollPane);
        });

        //Sends message on enter key press
        textArea.setOnKeyPressed(keyEvent ->

        {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                sendMessage(userNames, textArea, chat, scrollPane);
            }
        });

        HBox addRow = new HBox();
        HBox removeRow = new HBox();
        HBox lastRow = new HBox();
        lastRow.getChildren().

                addAll(refresh, customName);
        removeRow.getChildren().

                addAll(deleteChat, removePerson);
        addRow.getChildren().

                addAll(add, addPeople);

        VBox userNamesAndButtons = new VBox();
        userNamesAndButtons.getChildren().

                addAll(userNames, addRow, removeRow, lastRow);

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
        textAreaWithSend.getChildren().

                addAll(textArea, sendButton);
        border.setTop(navigationBar);
        border.setLeft(scrollPane);
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
        registerConfirm.setOnAction(actionEvent ->

        {
            if (passwordConfirm.getText().equals(passwordRegister.getText())) {
                try {
                    Long userId = IO.register(usernameRegister.getText(), passwordConfirm.getText(), mainOutStream, mainInStream);
                    if (userId != -1) {
                        mainUser = usernameRegister.getText();
                        // TODO: 5/8/19 Lisada id saatmine ka edasi
                        updateChats(users);
                        chat.setText("Logged in as " + mainUser);
                        peaLava.setScene(tseen1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        VBox registerDetails = new VBox();
        registerDetails.getChildren().

                addAll(registerLabel, usernameRegister, passwordRegister, passwordConfirm, registerConfirm);
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
        login.setOnAction(actionEvent ->

        {
            try {
                Long userId = IO.login(username.getText(), password.getText(), mainOutStream, mainInStream);
                if (userId != -1) {
                    mainUser = username.getText();
                    updateChats(users);
                    chat.setText("Logged in as " + mainUser);
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
        buttons.getChildren().

                addAll(login, register);
        buttons.setAlignment(Pos.CENTER);
        loginDetails.getChildren().

                addAll(welcomeLabel, username, password, buttons);
        loginDetails.setAlignment(Pos.CENTER);

        border1.setCenter(loginDetails);
        Scene tseen2 = new Scene(border1, 800, 400, Color.SNOW);


        //
        peaLava.setScene(tseen2);

        peaLava.show();
        //

    }

    private void sendMessage(ListView<String> userNames, TextField textArea, Text chat, ScrollPane scrollPane) {
        try {
            if (userNames.getSelectionModel().getSelectedItem() != null) {
                List<String> messages = IO.sendMessage(textArea.getText(), mainUser, userNames.getSelectionModel().getSelectedItem(), mainOutStream, mainInStream);
                textArea.clear();
                chat.setText(messageParser(messages));
                scrollPane.setVvalue(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateChats(ObservableList<String> users) {
        List<String> userIDS;
        try {
            userIDS = IO.getChat(mainUser, mainOutStream, mainInStream);
            for (String id : userIDS) {
                System.out.println(id);
                if (!users.contains(String.valueOf(id))) {
                    users.add(String.valueOf(id));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String messageParser(List<String> inputData) {
        StringBuilder textToDisplay = new StringBuilder();
        for (int i = 0; i < inputData.size(); i++) {
            if (i % 2 == 0) {
                textToDisplay.append(" " + inputData.get(i)).append(": ");

            } else {
                textToDisplay.append(inputData.get(i)).append("\n");
            }
        }
        return textToDisplay.toString();
    }

}
