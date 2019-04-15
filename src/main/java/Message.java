public class Message {

    private long chatid;
    private long senderid;
    private String message;

    public Message(long chatid, long senderid,String message){
        this.chatid = chatid;
        this.senderid = senderid;
        this.message = message;
    }

    public long getChatid() {
        return chatid;
    }

    public long getSenderid() { return senderid;}

    public String getMessage() {
        return message;
    }
}
