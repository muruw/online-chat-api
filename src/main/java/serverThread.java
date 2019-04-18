import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;

public class serverThread implements Runnable {
    Socket socket;
    DatabaseJSON database;
    JSONObject databaseObject;
    JSONArray usersJSON;
    JSONArray orderJSON;
    JSONArray chatsJSON;


    public serverThread(Socket socket, DatabaseJSON database) throws Exception {
        this.socket = socket;
        this.database = database;
        this.databaseObject = database.createDatabase();
        this.usersJSON = database.getArrayJSON(databaseObject, "client");
        this.orderJSON = database.getArrayJSON(databaseObject, "client_order");
        this.chatsJSON = database.getArrayJSON(databaseObject, "chats");
    }

    // saadab hetkel tagasi mõlema id-d vastavalt kas nad olid sõnumi saatja v saaja
    // ja ss sõnumi enda ka saadab (kõikide id vaheliste sõnumitega tehakse nii)
    void writeMessage(DataOutputStream socketOut, long senderID, long receiverID) throws Exception {
        List<Message> messagesBetweentheIDs = this.database.userConvoSent(senderID, receiverID, this.usersJSON);
        List<Message> recieved = this.database.userConvoRecieved(senderID, receiverID, this.usersJSON);
        messagesBetweentheIDs.addAll(recieved);
        //messaged võiks uusimast vanimani sortitud ka olla (võiks olla messagel ka kas sent v saadud küljes olla)
        socketOut.writeInt(messagesBetweentheIDs.size());
        for (Message message : messagesBetweentheIDs) {
            socketOut.writeLong(message.getSenderid());
            socketOut.writeUTF(message.getMessage());
        }
    }


    public void run() {
        Socket socket = this.socket;
        try (socket;
             DataInputStream socketIn = new DataInputStream(socket.getInputStream());
             DataOutputStream socketOut = new DataOutputStream(socket.getOutputStream())) {
            int type = socketIn.readInt();
            long firstid = socketIn.readLong(); //üldiselt ühendajaid, kui tahad chati inimesi lisada/removeida ss chatiid
            long secondid = socketIn.readLong(); //üldielt chatiid, kui tahad chati inimesi lisada/removeida ss selle inimese id
            System.out.println(type);
            if (type == 1) {
                String text = socketIn.readUTF();
                database.addSentMessage(firstid, secondid, text, databaseObject,usersJSON,orderJSON); //saatja id ja chati kuhu saadab id
                database.addReceivedMessage(secondid, firstid, text, databaseObject,usersJSON,orderJSON,chatsJSON); //chati id ja see kes sinna saadab id
                writeMessage(socketOut, firstid, secondid);
                System.out.println("saadetud sõnumid tagasi");
            } else if (type == 0) {
                writeMessage(socketOut, firstid, secondid);
            } else if (type == 2) {
                database.newChat(firstid, secondid, databaseObject, chatsJSON); // mõlema inimese id-d
            } else if (type == 3) {
                database.addToChat(firstid, secondid, databaseObject, chatsJSON); // chatiid ja lisatava id
            } else if (type == 4) {
                database.deleteChat(firstid, databaseObject, usersJSON, chatsJSON); //chati id mida kustutame
            } else if (type == 5) {
                database.removeFromChat(firstid, secondid, databaseObject, usersJSON,chatsJSON); //chati id ja inimese id keda eemaldame chatist
            } else if (type == 6) {
                String username = socketIn.readUTF();
                database.addUser(username, databaseObject, usersJSON);
            } else if (type == 7) {
                database.deleteUser(firstid, databaseObject, usersJSON);
            } else {
                throw new IllegalArgumentException("type " + type + " pole sobiv");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}



