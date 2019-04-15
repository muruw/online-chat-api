package client;

/*
Main client class that sets everything up and looks up saved data

How Client should work
1. Launch client
2. Client checks if previous userData exists (username, server information)
3a.If Info exists loads it up
3b.If info Does not exists or server does not respond ask for new userID and server Information
4. After launching Client user can select with whom to chat
5. After selecting someone Client starts to ping the server. It will Ping the message in some kind of format like this {MessageType, senderID,ReceiverID, payload}
If messageType is 0 for example payload does not exist or client is just pinging(Can display if connected to server.Additionally in the future maybe we can see who is online)
6.If user sends a message then the message is sent where the payload is the entered text, senderID is the client, Receiver is the person currently selected and MessageType is 1 if message no empty.
7. Server should respond to every ping so that means that client should be always listening
8. Server sends either a small ping back or sends a json file with relevant data
9. Client side has a JSON parser that decodes the message
10.Latest JSON file is stored and displayed to user in GUI
 */

// TODO: 3/30/19 get saved data
public class ClientMain {
    //miks static siin on hea halb? Ei ole enam private?
    private static boolean running = false;
    private static UserData user;

    //Threade peaks vist kasutama, et pingi checkimine toimuks kuskil mujal, Hetkel sleepib kogu clienti xd
// TODO: 4/13/19 make a new thread or solve ping problem some other way.

    public static void setUser() {
        ClientMain.user = new UserData(1, 1337, "");
    }

    public static void main(String[] args) throws Exception {
        setUser();
/*        while (true){
            TimeUnit.SECONDS.sleep(5);
            checkRun();
            System.out.println(isRunning());
        }*/
        SendMessage("test", 2);
    }

    public static void checkRun() throws Exception {
        if (IO.ping()) {
            running = true;
        } else {
            running = false;
        }
    }

    public static boolean isRunning() {
        return running;
    }

    public static void SendMessage(String message, long receiver) throws Exception {
        IO.sendMessage(message, user.getUserID(), receiver);
    }
}
