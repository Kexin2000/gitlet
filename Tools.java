import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class Tools {
    // 求哈希值，输入参数为字节数组
    public static String sha1(byte[] content) {
        MessageDigest complete = null;
        try {
            complete = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        complete.update(content);
        byte[] sha1 = complete.digest();

        String hashValue = "";
        for (int j = 0; j < sha1.length; j++) {
            hashValue += Integer.toString((sha1[j] >> 4) & 0x0F, 16) + Integer.toString(sha1[j] & 0x0F, 16);
        }
        return hashValue;
    }

    //求sha1的方法重写，输入参数类型为File
    public static String sha1(File file) {
        byte[] content = readContents(file);
        return sha1(content);
    }

    //输入文本文件，返回文件内容
    public static byte[] readContents(File file) {
        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        byte[] buf = new byte[1024];
        int readLen = 0;
        byte[] content = null;
        try {
            fis = new FileInputStream(file);
            baos = new ByteArrayOutputStream();
            while ((readLen = fis.read(buf)) != -1) {
                baos.write(buf, 0, readLen);
            }
            content = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    //传入参数字符串和文件路径，把字符串写进文件里
    public static void stringToFile(String s, String filePath) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(filePath);
            fw.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //获得当前时间
    public static String getTime() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd :hh:mm:ss");
        return sdf.format(d);
    }

    //判断文件名是否在文件夹中
    public static boolean isFilenameInDirectory(String fileName, File dir) {
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isFile()) {
                String filenameInDirectory = f.getName(); //获取文件名
                if (filenameInDirectory.equals(fileName)) return true;
            }
        }
        return false;
    }

    //判断一个文件名在index暂存区中是否存在
    public static boolean isFilenameInIndex(String fileName, TreeOrIndex idx) {
        Set keyset = idx.getTOI().keySet();
        for (Object key : keyset) {
            if (fileName.equals(key.toString())) return true;
        }
        return false;
    }

    //判断一个文件名在tree中是否存在
    public static boolean isFilenameInTree(String fileName, TreeOrIndex tree) {
        Set keyset = tree.getTOI().keySet();
        for (Object key : keyset) {
            if (fileName.equals(key.toString())) return true;
        }
        return false;
    }

    //blob对象序列化
    public static void blob_serialization(Blob blob){
        ObjectOutputStream oos = null;
        String filePath = command.obj_dir.getAbsolutePath()+File.separator+blob.getHashCode()+".txt";
        try {
            oos = new ObjectOutputStream(new FileOutputStream(filePath));
            oos.writeObject(blob);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(oos != null){
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //blob对象反序列化
    public static Blob blob_Deserialization(String filePath){
        File blobFile = new File(filePath);
        ObjectInputStream ois = null;
        Blob blob = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(blobFile));
            blob = (Blob)ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }finally{
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return blob;
    }

    //commit对象序列化
    public static void commit_serialization(Commit commit){
        ObjectOutputStream oos = null;
        String filePath = command.obj_dir.getAbsolutePath()+File.separator+commit.getCurrCommit()+".txt";
        try {
            oos = new ObjectOutputStream(new FileOutputStream(filePath));
            oos.writeObject(commit);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //commit对象反序列化
    public static Commit commit_Deserialization(String filePath){
        File commitFile = new File(filePath);
        ObjectInputStream ois = null;
        Commit commit = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(commitFile));
            commit = (Commit)ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }finally{
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return commit;
    }

    //TreeOrIndex对象序列化
    public static void treeOrIndex_serialization(TreeOrIndex treeOrIndex){
        ObjectOutputStream oos = null;
        String filePath = treeOrIndex.getFilePath();
        try {
            oos = new ObjectOutputStream(new FileOutputStream(filePath));
            oos.writeObject(treeOrIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //TreeOrIndex对象反序列化
    public static TreeOrIndex treeOrIndex_Deserialization(String filePath){
        File treeOrIndexFile = new File(filePath);
        ObjectInputStream ois = null;
        TreeOrIndex treeOrIndex= null;
        try {
            ois = new ObjectInputStream(new FileInputStream(treeOrIndexFile));
            treeOrIndex = (TreeOrIndex)ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }finally{
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return treeOrIndex;
    }

    //ArrayList序列化
    public static void ArrayList_serialization(ArrayList list,String filePath){
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(filePath));
            oos.writeObject(list);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(oos != null){
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //ArrayList反序列化
    public static ArrayList ArrayList_Deserialization(String filePath){
        ObjectInputStream ois = null;
        ArrayList list = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(filePath));
            list = (ArrayList)ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            if(ois != null){
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    //head_changes.txt中新增一条head变动记录，输入的第一个参数为变动前的head值，第二个参数为变动后的head值
    public static void head_changes_add(String preHead,String afterHead){
        ArrayList list = null;
        if ("".equals(preHead)) {
            list = new ArrayList();
        } else {
            String filePath = command.head_changes_file_path;
            list = ArrayList_Deserialization(filePath);
        }
        list.add(0, afterHead);
        String filePath = command.head_changes_file_path;
        ArrayList_serialization(list,filePath);
    }


    //题目要求：tree对象内容与index一致
    //题目要求：将index中所有条目生成tree对象序列化到objects文件夹下；
    //git commit时根据index文件生成tree文件
    //这两文件内容相同，路径和文件名不同
    //index.txt在git_dir下，tree文件在obj_dir下，且文件名为文件内容的哈希值
    //思路:本质是文件拷贝，这里需额外求出文件内容的哈希值，作为新文件的名字。
    //思路：拷贝用对象流处理，便于后续需把tree文件转换为tree对象
    public static String creatTreeFile(String index_file_path) {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        String hashcode = "";
        try {
            ois = new ObjectInputStream(new FileInputStream(index_file_path));
            File indexTxt = new File(index_file_path);
            hashcode = sha1(indexTxt);
            oos = new ObjectOutputStream(new FileOutputStream(
                    command.obj_dir.getAbsolutePath() + File.separator + hashcode + ".txt"));
            oos.writeObject(ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return hashcode;
        }
    }

    //题目要求：打印本次commit相对上一次commit的文件变动情况（增加、删除、修改）
    //思路：传进参数的commit是本次提交的commit对象
    // 通过commit对象的preCommit属性找到上一次commit的文件名
    //找到上一次commit文件后，对其反序列化生成commit对象 precmt，表示上一次的commit对象
    //查找precmt和cmt里的treeHash属性，找到两次提交的根树文件
    //对根树文件进行反序列化得到tree的HashMap，其中this_time_tree和last_time_tree作比较
    //情况1：如果this_time_tree里的fileName能在last_time_tree中找到，但哈希值不同，则这个file是修改过的文件，输出提示
    // 情况2：如果this_time_tree里的fileName不能在last_time_tree中找到，说明是这个file是新增的文件，输出提示信息
    // 情况3:如果this_time_tree里的fileName能在last_time_tree中找到，且哈希值相同，
    //      则这个file上次提交这次也提交，没有变动，不输出提示信息
    // 情况4：如果last_time_tree里的fileName不能在this_time_tree中找到，说明这个file被删除，输出提示信息
    public static void print_changes(Commit cmt) {
        String treeFilePath = command.obj_dir.getAbsolutePath() +
                File.separator + cmt.getTreeHash() + ".txt";
        TreeOrIndex this_time_tree = treeOrIndex_Deserialization(treeFilePath);
        String preCommitFilePath = command.obj_dir.getAbsolutePath() +
                File.separator + cmt.getPreCommit() + ".txt";
        Commit precmt = commit_Deserialization(preCommitFilePath);
        String lastTreeFilePath = command.obj_dir.getAbsolutePath() +
                File.separator + precmt.getTreeHash() + ".txt";
        TreeOrIndex last_time_tree = treeOrIndex_Deserialization(lastTreeFilePath);

        Set keyset_this_time_tree = this_time_tree.getTOI().keySet();
        Set keyset_last_time_tree = last_time_tree.getTOI().keySet();
        for (Object key1 : keyset_this_time_tree) {
            boolean flag = true;       //如果在内层循环中找到与key1相等的key2，flag被置为false
            //flag为false表示，符合情况1或情况3，在内层循环结束后不再输出提示信息
            //flag为true表示符合情况2，在内层循环结束后，输出“新添加文件”的提示信息，
            for (Object key2 : keyset_last_time_tree) {
                String value1 = this_time_tree.getTOI().get(key1).toString();
                String value2 = last_time_tree.getTOI().get(key2).toString();
                if (key1.toString().equals(key2.toString())) {
                    flag = false;
                    if (!value1.equals(value2)) {                     //文件名相同，哈希值不同
                        System.out.println(key1.toString() + "是被修改的文件");
                        break;
                    } else {           //文件名相同，哈希值相同，不输出提示，直接返回
                        break;
                    }
                }
            }
            //文件名在keyset_last_time_tree没有找到，输出提示
            if (flag) System.out.println(key1.toString() + "是新添加的文件");
        }
        //上面先遍历keyset_this_time_tree，再遍历keyset_last_time_tree，无法判断删除情况
        //删除情况要先遍历keyset_last_time_tree，再遍历keyset_this_time_tree

        for (Object key2 : keyset_last_time_tree) {
            boolean flag = true;
            for (Object key1 : keyset_this_time_tree) {
                if (key2.toString().equals(key1.toString())) {
                    flag = false;
                    break;
                }
            }
            if (flag) System.out.println(key2.toString() + "是被删除的文件");
        }
    }

    //读取head文件中的hashcode
    public static String readHeadFile() {

        FileInputStream fis = null;
        String hashCode = "";
        byte[] buf = new byte[40];
        try {
            fis =new FileInputStream(command.head_file_path);
            fis.read(buf);
            hashCode =new String(buf);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return hashCode;
    }

    //读取branchHead文件中的hashcode
    public static String readBranchHeadFile(String branchPath) {

        FileInputStream fis = null;
        String hashCode = "";
        byte[] buf = new byte[40];
        try {
            fis =new FileInputStream(branchPath);
            fis.read(buf);
            hashCode =new String(buf);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return hashCode;
    }


    //清空文件夹下的文件，目录保留
    public static void clearFile(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File f : files) {
                if (f.isFile()) f.delete();
            }
        }
    }

    //判断远程仓库是否存在
    public static boolean isRemoteGitInitial(){
        String remote_path = System.getProperty("user.dir");
        File remote_git_dir = new File(remote_path+File.separator+".git");
        if(remote_git_dir.exists()) return true;
        return false;
    }

    //初始化远程仓库，因为远程仓库里没有工作区，因此没必要置index文件
        public static void RemoteGitInit() {
        String remote_path = System.getProperty("user.dir");
        File remote_git_dir = new File(remote_path+File.separator+".git");
        File remote_obj_dir = new File(remote_git_dir.getAbsolutePath()+File.separator+"objects");
        File remote_heads_dir = new File(remote_git_dir.getAbsolutePath()+File.separator+"refs"+File.separator+"heads");
        remote_git_dir.mkdir();
        remote_obj_dir.mkdir();
        remote_heads_dir.mkdirs();
        String remote_head_file_path = remote_git_dir.getAbsolutePath() +File.separator+"head.txt";
        Tools.stringToFile("", remote_head_file_path);
    }

}
