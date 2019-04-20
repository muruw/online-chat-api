package client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ClientGUI extends Application {
    // TODO: 3/30/19 Get userdata from ClientMain
    //if no user data exists GUI should prompt a window where to add new data
    private List<String> options;

    public ClientGUI() {
        this.options = new ArrayList<>();
    }

    public void addUserNames(String name) {
        options.add(name);
    }

    @Override
    public void start(Stage peaLava) {



        ObservableList<String> users = FXCollections.observableArrayList();
        System.out.println(this.options);
        peaLava.setTitle("Online Chat");
        BorderPane border = new BorderPane();


        // TODO: 3/30/19 Navigation bar selection and screens 
        HBox navigationBar = new HBox();



        //ChatBox
        TextArea chat = new TextArea();
        chat.setMaxSize(500, 350);
        chat.setDisable(true);
        chat.setText("test");


        // TODO: 4/19/19  usernames from database
        //usernames
        ListView<String> userNames = new ListView<>(users);
        userNames.setPrefSize(100, 100);
        userNames.setOrientation(Orientation.VERTICAL);
        userNames.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> chat.setText(s + t1));



        VBox userMessages = new VBox();
        HBox textAreaWithSend = new HBox();
        // TODO: 4/13/19 Change online circle color using ClientMain.getRunning() boolean value 
        Circle online = new Circle(30, Color.RED);

        TextField textArea = new TextField();

        //SendButton
        Button sendButton = new Button("Send");
        sendButton.setOnAction(event -> {
            try {
                List<String> messages = IO.sendMessage(textArea.getText(), 1, 1);
                textArea.clear();
                chat.setText(messageParser(messages));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


/*
        options.addListener((ListChangeListener<String>) change -> {
            System.out.println("change!");
            userNames.setItems(options);
        });
*/


        //Setup
        border.setPadding(new Insets(15, 12, 15, 12));
        border.setStyle("-fx-background-color: #fff;");
        textAreaWithSend.getChildren().addAll(textArea, sendButton, online);
        border.setTop(navigationBar);
        border.setLeft(chat);
        border.setRight(userNames);
        border.setBottom(textAreaWithSend);
        border.setCenter(userMessages);


        Scene tseen1 = new Scene(border, 700, 400, Color.SNOW);

        //
        BorderPane border1 = new BorderPane();

        TextField username = new TextField("username");
        username.setMaxSize(100,100);
        TextField password = new TextField("password");
        password.setMaxSize(100,100);

        Button login = new Button("Login");
        login.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                peaLava.setScene(tseen1);
            }
        });
        VBox loginDetails = new VBox();
        loginDetails.getChildren().addAll(username,password,login);
        loginDetails.setAlignment(Pos.CENTER);
        border1.setCenter(loginDetails);
        Scene tseen2 = new Scene(border1, 700, 400, Color.SNOW);






        //
        peaLava.setScene(tseen2);
        peaLava.show();

    }
/*
    public static void main(String[] args) {
        launch(args);

    }*/

    public void launcher(String[] args) {
        launch(args);
    }

    public String messageParser(List<String> inputData) {
        //hardcoded
        long mainUserID = 1;
        StringBuilder textToDisplay = new StringBuilder();
        for (int i = 0; i < inputData.size(); i++) {
            if (i % 2 == 0) {
                if (Long.parseLong(inputData.get(i)) == mainUserID) {
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
