import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.Files.readAllBytes;

public class serverThread implements Runnable {
    Socket socket;
    DatabaseJSON database;
    JSONObject usersObject;
    JSONArray arrayJSON;


    public serverThread(Socket socket, DatabaseJSON database) throws Exception {
        this.socket = socket;
        this.database = database;
        this.usersObject = database.createDatabase();
        this.arrayJSON = database.getArrayJSON(usersObject, "client");
    }

    // saadab hetkel tagasi mõlema id-d vastavalt kas nad olid sõnumi saatja v saaja
    // ja ss sõnumi enda ka saadab
    void writeMessage(DataOutputStream socketOut, long senderID, long receiverID) throws Exception {
        List<Message> messagesBetweentheIDsInorder = this.database.userReceivedMessages(senderID,this.arrayJSON);
        //messaged võiks uusimast vanimani sortitud ka olla (võiks olla messagel ka kas sent v saadud küljes olla)
        //ning mitte ainult received aga prgu lihtsalt kohahoidjaks
        for (Message message : messagesBetweentheIDsInorder) {
            var buffer = new ByteArrayOutputStream();
            try (var out = new DataOutputStream(buffer)) {
                out.writeUTF(message.getMessage());
            }
            byte[] value = buffer.toByteArray();
            if (message.getMessageType() == 0) { //kui saatis ise
                socketOut.writeLong(senderID);
                socketOut.writeLong(receiverID);
            }
            if (message.getMessageType() == 1) { //kui talle saadeti
                socketOut.writeLong(receiverID);
                socketOut.writeLong(senderID);
            }
            socketOut.write(value);
        }
    }

    static void writeMessageFile(DataOutputStream socketOut, byte[] content) throws Exception {
        var buffer = new ByteArrayOutputStream();
        if (content != null) {
            try (var out = new DataOutputStream(buffer)) {
                out.write(content);
            }
            byte[] value = buffer.toByteArray();
            socketOut.writeUTF("file");
            socketOut.writeInt(value.length);
            socketOut.write(value);
            System.out.println("saadetud");
        } else {
            try (var out = new DataOutputStream(buffer)) {
                out.writeUTF("faili ei leitud või on absolute path");
            }
            byte[] value = buffer.toByteArray();
            socketOut.writeUTF("error");
            socketOut.writeInt(value.length);
            socketOut.write(value);
            System.out.println("kaka");
        }
    }

    static byte[] processMessageFile(String filename) throws Exception {
        System.out.println(filename);
        File f = new File(filename);
        Path path = f.toPath();
        if (f.isFile() && !f.isDirectory() && !f.isAbsolute()) {
            byte[] content = readAllBytes(path);
            System.out.println("leitud");
            return content;
        }
        System.out.println("ei leidnud faili");
        return null;
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
                //database.addSentMessage(senderID, receiverID, text); ei tea kas veel töötab
                writeMessage(socketOut, senderID, receiverID);
                System.out.println("saadetud sõnumid tagasi");
            } else if (type == 0) {
                writeMessage(socketOut, senderID, receiverID);
            } else {
                throw new IllegalArgumentException("type " + type + " pole sobiv");
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}



