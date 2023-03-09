import java.io.File;
import java.io.Serializable;

public class Commit implements Serializable {
    //    题目要求：将commit对象序列化到objects文件夹下，commit对象包括以下属性：
    //    上一次的commit id、本次commit所生成tree对象id，message、commit时间；
    private String preCommit;
    private String message;
    private String treeHash;
    private String time;
    private String currCommit;//本次提交的哈希值

    //提交时的commit对象构造方法
    public Commit(String message, String treeHash) {
        this.message = message;
        this.treeHash = treeHash;
        File head = new File(command.head_file_path);
        preCommit = new String(Tools.readContents(head));
        time = Tools.getTime();
        //计算commit文件的哈希值，因为时间总在变化，所以计算哈希值时内容不包括time
        // 如果某两次提交的版本完全相同，但他们的preCommit不同，这两个版本应该是相同的哈希值
        // 所以计算哈希值不包括preCommit
        String content = message + treeHash;
        this.currCommit = Tools.sha1(content.getBytes());
    }

    public String getPreCommit() {
        return preCommit;
    }

    public String getMessage() {
        return message;
    }

    public String getTreeHash() {
        return treeHash;
    }

    public String getTime() {
        return time;
    }

    public String getCurrCommit() {
        return currCommit;
    }
}

