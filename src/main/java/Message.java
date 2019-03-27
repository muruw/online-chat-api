public class Message {

    private long messageType;
    private String message;

    public Message(long messageType, String message){
        this.messageType = messageType;
        this.message = message;
    }

    public long getMessageType() {
        return messageType;
    }

    public String getMessage() {
        return message;
    }
}
