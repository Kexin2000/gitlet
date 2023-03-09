import java.io.Serializable;
import java.util.HashMap;

public class TreeOrIndex implements Serializable {
    private HashMap TOI;
    private String filePath;
    //因为tree和index作为同一个类，但是它们的存放路径不同
    //tree存放在objects文件夹中，index存放在.git文件夹中
    //因此把这个区别作为属性，便于区分

    public TreeOrIndex(HashMap TOI,String filePath){
        this.TOI = TOI;
        this.filePath = filePath;
    }

    public HashMap getTOI() {
        return TOI;
    }

    public String getFilePath() {
        return filePath;
    }

    //题目要求：在index对象中添加/修改 ；
    public Object addOrModify(String filename, String hash) {
        return TOI.put(filename,hash);
    }


    //题目要求：在index对象中有删除方法
    public Object remove(String fileName){
        return TOI.remove(fileName);
    }
}
