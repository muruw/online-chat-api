import java.time.Instant;
public class Message implements Comparable {

    private long chatid;
    private long senderid;
    private String message;
    private Instant time;

    public Message(long chatid, long senderid, String message, String time){
        this.chatid = chatid;
        this.senderid = senderid;
        this.message = message;
        this.time = Instant.parse(time);
    }

    public long getChatid() {
        return chatid;
    }

    public long getSenderid() {
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
