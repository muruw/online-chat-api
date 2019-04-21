import java.time.Instant;
public class Message implements Comparable {

    private long chatid;
    private long senderid;
    private String message;
    private String time;

    public Message(long chatid, long senderid, String message, String time){
        this.chatid = chatid;
        this.senderid = senderid;
        this.message = message;
        this.time = time;
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

    public String getTime() {
        return time;
    }

    @Override
    public int compareTo(Object o) {
        return Instant.parse(this.getTime()).compareTo(Instant.parse(((Message) o).getTime()));
    }
}
