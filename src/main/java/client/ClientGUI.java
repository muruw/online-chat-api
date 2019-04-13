package client;

import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientGUI extends Application {
    // TODO: 3/30/19 Get userdata from ClientMain
    //if no user data exists GUI should prompt a window where to add new data
    private UserData userData;
     
    @Override
    public void start(Stage peaLava) throws Exception {
        peaLava.setTitle("Online Chat");

        BorderPane border = new BorderPane();
        // TODO: 3/30/19 Navigation bar selection and screens 
        HBox navigationBar = new HBox();
        // TODO: 3/30/19 Show usernames on the left that can be selected. Should change receiverID and messages displayed based on that 
        VBox userNames = new VBox();
        //Change to for loop later
        Label name1 = new Label("D. Trump");
        Label name2 = new Label("T. May");
        userNames.getChildren().addAll(name1,name2);

        TextField textArea = new TextField();
        // TODO: 3/30/19 show user displayed messages
        VBox userMessages = new VBox();
        HBox textAreaWithSend = new HBox();
        // TODO: 4/13/19 Change online circle color using ClientMain.getRunning() boolean value 
        Circle online = new Circle(30,Color.RED);
        


        Button sendButton = new Button("Send");
        sendButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(textArea.getText().isEmpty()){
                     //if no message has been entered while textArea is empty/ do nothing
                }else{
                    try {
                        IO.sendMessage(textArea.getText(),userData);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    textArea.clear();
                }
            }
        });

        border.setPadding(new Insets(15, 12, 15, 12));
        border.setStyle("-fx-background-color: #336699;");

        textAreaWithSend.getChildren().addAll(textArea,sendButton,online);
        border.setTop(navigationBar);
        border.setLeft(userNames);
        border.setBottom(textAreaWithSend);
        border.setCenter(userMessages);

        Scene tseen1 = new Scene(border,700,400,Color.SNOW);
        peaLava.setScene(tseen1);
        peaLava.show();


    }

    public static void main(String[] args){
        launch(args);

    }
}
