package tw.com.example.ben.sharehouse.CHAT.dataModel;

/**
 * Created by Ben on 16/7/24.
 */
public class Chat {

    private String message;
    private String author;
    private String photoUrl;

    // Required default constructor for Firebase object mapping
    @SuppressWarnings("unused")
    private Chat() {
    }

    public Chat(String message, String author, String photoUrl) {
        this.message = message;
        this.author = author;
        this.photoUrl = photoUrl;
    }

    public String getMessage() {
        return message;
    }
    public String getPhotoUrl(){ return photoUrl;}
    public String getAuthor() {
        return author;
    }
    public void setMessage(String message) { this.message = message;}
    public void setAuthor(String author) { this.author = author ;}
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl;}
}
