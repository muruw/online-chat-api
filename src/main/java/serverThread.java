import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;

public class serverThread implements Runnable {
    Socket socket;
    DatabaseJSON database;
    JSONObject usersObject;
    JSONArray usersJSON;
    JSONArray orderJSON;


    public serverThread(Socket socket, DatabaseJSON database) throws Exception {
        this.socket = socket;
        this.database = database;
        this.usersObject = database.createDatabase();
        this.usersJSON = database.getArrayJSON(usersObject, "client");
        this.orderJSON = database.getArrayJSON(usersObject, "client_order");
    }

    // saadab hetkel tagasi mõlema id-d vastavalt kas nad olid sõnumi saatja v saaja
    // ja ss sõnumi enda ka saadab (kõikide id vaheliste sõnumitega tehakse nii)
    void writeMessage(DataOutputStream socketOut, long senderID, long receiverID, int messageType) throws Exception {
        if(messageType == 0) {
            socketOut.writeInt(0);
            return;
        }

        List<Message> messagesBetweentheIDs = this.database.userConvoSent(senderID, receiverID, this.usersJSON);
        List<Message> recieved = this.database.userConvoRecieved(senderID, receiverID, this.usersJSON);
        messagesBetweentheIDs.addAll(recieved);
        //messaged võiks uusimast vanimani sortitud ka olla (võiks olla messagel ka kas sent v saadud küljes olla)
        //Pigem timestamp küljes
        for (Message message : messagesBetweentheIDs) {
            if (message.getMessageType() == 0) { //kui saatis ise
                socketOut.writeLong(senderID);
                socketOut.writeLong(receiverID);
            }
            if (message.getMessageType() == 1) { //kui talle saadeti
                socketOut.writeLong(receiverID);
                socketOut.writeLong(senderID);
            }
            socketOut.writeUTF(message.getMessage());
        }
    }


    public void run() {
        Socket socket = this.socket;
        try (socket;
             DataInputStream socketIn = new DataInputStream(socket.getInputStream());
             DataOutputStream socketOut = new DataOutputStream(socket.getOutputStream())) {
            int type = socketIn.readInt();
            long senderID = socketIn.readLong();
            long receiverID = socketIn.readLong();
            System.out.println(type);
            if (type == 1) {
                String text = socketIn.readUTF();
                database.addSentMessage(senderID, receiverID, text, usersObject, usersJSON, orderJSON);
                database.addReceivedMessage(receiverID, senderID, text, usersObject, usersJSON, orderJSON);
                writeMessage(socketOut, senderID, receiverID,1);
                System.out.println("saadetud sõnumid tagasi");
            } else if (type == 0) {
                System.out.println("ping got");
                writeMessage(socketOut, senderID, receiverID, 0);
            } else{
                throw new IllegalArgumentException("type " + type + " pole sobiv");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}



