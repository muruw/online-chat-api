import java.time.Instant;
public class Message implements Comparable {

    private String chatid;
    private String senderid;
    private String message;
    private Instant time;

    public Message(String chatid, String senderid, String message, String time){
        this.chatid = chatid;
        this.senderid = senderid;
        this.message = message;
        this.time = Instant.parse(time);
    }

    public String getChatid() {
        return chatid;
    }

    public String getSenderid() {
        return senderid;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTime() {
        return time;
    }

    @Override
    public int compareTo(Object o) {
        return this.getTime().compareTo(((Message) o).getTime());
    }
}
