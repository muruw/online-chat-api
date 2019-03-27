import java.io.*;
import java.net.Socket;
import java.nio.file.Path;

import static java.nio.file.Files.readAllBytes;

public class serverThread implements Runnable {
    Socket socket;

    public serverThread(Socket socket) {
        this.socket = socket;
    }

    static void writeMessageEcho(DataOutputStream socketOut, String text) throws Exception {
        var buffer = new ByteArrayOutputStream();
        try (var out = new DataOutputStream(buffer)) {
            out.writeUTF(text);
        }
        byte[] value = buffer.toByteArray();
        socketOut.writeUTF("echo");
        socketOut.writeInt(value.length);
        socketOut.write(value);
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
            String type = socketIn.readUTF();
            String text = socketIn.readUTF();
            System.out.println(type);
            if (type.equals("echo")) {
                writeMessageEcho(socketOut, text);
                System.out.println("saadetud echo");
            } else if (type.equals("file")) {
                writeMessageFile(socketOut, processMessageFile(text));
            } else {
                throw new IllegalArgumentException("type " + type + " pole sobiv");
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}



