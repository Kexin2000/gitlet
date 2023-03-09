import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    private String hashCode;
    private byte[] content;

    public Blob(String fileName){
        File file = new File(command.user_dir.getAbsolutePath() + File.separator + fileName);
        this.content = Tools.readContents(file);
        this.hashCode = Tools.sha1(content);
    }

    public String getHashCode() {
        return hashCode;
    }

    public byte[] getContent() {
        return content;
    }
}
