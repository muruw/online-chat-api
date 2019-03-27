import java.net.*;

public class Server {
    public static void main(String[] args) throws Exception {
        try (ServerSocket ss = new ServerSocket(1337)) {
            while (true) {
                Socket socket = ss.accept();
                new Thread(new serverThread(socket)).start();
            }
        }
    }
}

