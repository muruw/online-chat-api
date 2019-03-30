package Client;

import java.io.IOException;

/*
This class handles pinging the server. Works on the premise that while Client is running, sends a message to server every n seconds and that server send message back.
IF messageType from server is ping then do nothing. IF no messageArrives, then display that server is down. If messageType indicates that there is also a payload forward the message to ResponseReciever.
Immediately sends a ping with payload, if Response Sever calls IO out.
 */
public class IO {
    private boolean runningState;

    public void ping(){

    }

    public static void sendMessage(String message, UserData user) throws IOException {
        int messageType;

        if(message.isEmpty()){
            messageType = 0;
        }else{
            messageType = 1;
        }
        System.out.println("test");
//        try (Socket socket = new Socket(user.getHost(), user.getPort());
//             DataOutputStream outData = new DataOutputStream(socket.getOutputStream());
//             DataInputStream inData = new DataInputStream(socket.getInputStream())) {
//
//            outData.writeInt(messageType);
//
//        }

    }
}
