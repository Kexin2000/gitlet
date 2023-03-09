import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class command {
    public static final File user_dir = new File("C:\\Users\\t1897\\Desktop\\test");
    public static final File git_dir = new File(user_dir.getAbsolutePath() + File.separator + ".git");
    public static final File obj_dir = new File(git_dir.getAbsolutePath() + File.separator + "objects");
    public static final String head_file_path = git_dir.getAbsolutePath() + File.separator + "head.txt";
    public static final String index_file_path = git_dir.getAbsolutePath() + File.separator + "index.txt";
    //分支管理中需要heads文件夹存放分支指针
    public static final File heads_dir = new File(git_dir.getAbsolutePath() + File.separator + "refs" + File.separator + "heads");
    //head_changes用来存放head的每一次变动，该文件用来实现reflog命令
    public static final String head_changes_file_path = git_dir.getAbsolutePath() + File.separator + "head_changes.txt";

    public static void init() {
        //题目要求：判断工作区是否存在.git目录，已存在则打印提示信息
        if (git_dir.exists()) {
            System.out.println("本地仓库已存在");
            System.exit(0);
        }
        //题目要求：创建.git目录，在其中创建objects目录
        git_dir.mkdir();
        obj_dir.mkdir();
        heads_dir.mkdirs();
        Tools.stringToFile("", index_file_path);
        Tools.stringToFile("", head_file_path);
        Tools.ArrayList_serialization(new ArrayList(), head_changes_file_path);
        System.out.println("本地仓库初始化成功。");
    }

    //    题目要求：在index对象中添加/修改/删除  文件名-hash值 条目；
    //    题目要求：创建对应blob对象序列化到objects文件夹下
    //    题目要求：当add的文件，只存在于暂存区（index），而不存在于工作区时，在暂存区中删除对应条目；
    public static void add_singleFile(String fileName, TreeOrIndex idx) {
        File file = new File(user_dir.getAbsolutePath() + File.separator + fileName);
        if (!file.exists()) {
            if (Tools.isFilenameInIndex(fileName, idx)) {
                idx.remove(fileName);
            } else {
                System.out.println("在工作区和暂存区中都不存在该文件，请先在工作区中添加该文件。");
                System.exit(0);
            }
        } else {
            //如果add的文件内容之前add过，就会生成一模一样的blob文件覆盖之前的blob文件
            Blob blob = new Blob(fileName);
            Tools.blob_serialization(blob);
            idx.addOrModify(fileName, blob.getHashCode());
        }
    }

    //题目要求：
    //输入add . 时，对工作区的全部文件进行一次add操作；
    //输入add . 时，将只存在于暂存区中，而不存在于工作区的文件记录，从index中删除
    public static void add(String s) {
        File indexFile = new File(index_file_path);
        String indexContent = new String(Tools.readContents(indexFile));
        TreeOrIndex idx = null;
        if ("".equals(indexContent)) {
            idx = new TreeOrIndex(new HashMap(), index_file_path);
        } else {
            idx = Tools.treeOrIndex_Deserialization(index_file_path);
        }
        if (".".equals(s)) {
            //1、对只存在于暂存区中，而不存在于工作区的文件记录，从index中删除
            //备注：最开始用的增强for遍历是否删除hashmap index 中的条目，使用map.remove(),会抛出异常ConcurrentModificationException
            //查阅资料后，使用迭代器Iterator遍历,然后用Iterator.remove()删除，没有异常
            Set keyset = idx.getTOI().keySet();
            Iterator it = keyset.iterator();
            while (it.hasNext()) {
                Object key = it.next();
                String lastFileName = key.toString();
                if (!Tools.isFilenameInDirectory(lastFileName, user_dir)) {
                    it.remove();
                }
            }

            //2、对存在于暂存区中，且存在于工作区的文件记录，在index中修改
            // 对不存在暂存区中的文件记录，添加至index
            File[] files = user_dir.listFiles();
            for (File f : files) {
                if (f.isFile()) {
                    String filename = f.getName(); //获取文件名
                    add_singleFile(filename, idx);
                }
            }
            System.out.println("已add工作区所有文件至暂存区。");
        } else {
            add_singleFile(s, idx);
            System.out.println("已经add文件" + s + "至暂存区");
        }
        Tools.treeOrIndex_serialization(idx);
    }

    public static void commit(String message) {
        if("".equals(new String(Tools.readContents(new File(index_file_path))))){
            System.out.println("当前暂存区中没有文件，无法执行commit命令，请先执行add命令。");
            System.exit(0);
        }
        String treeHash = Tools.creatTreeFile(index_file_path);
        Commit cmt = new Commit(message, treeHash);
        String head = Tools.readHeadFile();
        if (head.equals(cmt.getCurrCommit())) {
            System.out.println("和上次提交的文件以及message完全相同。请勿重复提交。");
            System.exit(0);
        }
        Tools.commit_serialization(cmt);

        if (!cmt.getPreCommit().equals("")) Tools.print_changes(cmt);

        //head变动，将新head加入head_changes.txt中
        Tools.head_changes_add(head, cmt.getCurrCommit());

        //题目要求： 更新HEAD文件储存最近一次的commit id
        Tools.stringToFile(cmt.getCurrCommit(), head_file_path);
        System.out.println("提交成功~");
    }

    //题目要求：rm --cached：仅删除index中对应条目
    public static void rm_cached(String fileName) {
        File indexFile = new File(index_file_path);
        String indexContent = new String(Tools.readContents(indexFile));
        if ("".equals(indexContent)) {
            System.out.println("暂存区为空，删除失败");
            System.exit(0);
        }
        TreeOrIndex idx = Tools.treeOrIndex_Deserialization(index_file_path);
        Set keyset = idx.getTOI().keySet();
        for (Object key : keyset) {
            if (fileName.equals(key.toString())) {
                idx.remove(fileName);
                Tools.treeOrIndex_serialization(idx);
                System.out.println(fileName + "文件在暂存区中删除成功。");
                return;
            }
        }
        //index中不存在fileName，给出提示信息
        System.out.println("index暂存区中没有文件名为" + fileName + "的文件。删除失败。");
    }

    //题目要求：rm指令，在index对象中删除对应条目，在工作区中删除该文件
    public static void rm(String fileName) {
        //在index中的删除操作直接引用rm_cashed函数
        rm_cached(fileName);
        //在工作区中删除文件
        File file = new File(user_dir.getAbsolutePath() + File.separator + fileName);
        if (file.exists()) {
            //只实现单层文件夹，因此不再判断file是否为目录
            file.delete();
            System.out.println(fileName + "文件在工作区中删除成功。");
        } else {
            System.out.println("工作区中没有文件名为" + fileName + "的文件。删除失败。");
        }
    }

    public static void log() {
        //题目要求：从HEAD文件中读到最近一次的commit id，若HEAD为空打印提示信息
        String head = Tools.readHeadFile();
        if (head.trim().equals("")) {
            System.out.println("未提交到本地仓库过，无法打印日志");
            return;
        }
        //    题目要求：
        //    反序列化对应的commit对象，打印commit id，message，commit时间，
        //    读出该commit中存放的前一次commit id，
        //    反复执行2、3直到打印完第一次commit的内容。
        String commitID = head;
        while (!commitID.equals("")) {
            Commit cmt = Tools.commit_Deserialization(obj_dir.getAbsolutePath() +
                    File.separator + commitID + ".txt");
            System.out.println("commit id:" + cmt.getCurrCommit()
                    + "\nmessage:" + cmt.getMessage() + "\ncommit time:" + cmt.getTime());
            System.out.println("===============================");//分割线
            commitID = cmt.getPreCommit();
        }
    }

    public static void reset_soft(String commitID) {
        //题目要求：判断objects文件夹中是否存在对应的commit对象
        //思路：先判断objects文件夹里是否有以该Id命名的文件，没有提示错误信息
        //如果有，再判断该文件是不是Commit文件，如果不是则提示错误信息
        String fileName = commitID + ".txt";
        if (Tools.isFilenameInDirectory(fileName, obj_dir)) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(obj_dir.getAbsolutePath() + File.separator + fileName));
                Object o = ois.readObject();
                if (o instanceof Commit) {
                    //head变动，将新head加入head_changes.txt中
                    String head = Tools.readHeadFile();
                    Tools.head_changes_add(head, commitID);

                    //题目要求：reset --soft：修改HEAD文件内容为给定commit id
                    Tools.stringToFile(commitID, head_file_path);
                    System.out.println("HEAD文件内容被修改为" + commitID);
                } else {
                    System.out.println("这个哈希值命名的文件是tree或者blob文件，请输入正确的commitID");
                    System.exit(0);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("objects文件夹中没有以该commitID命名的文件，请输入正确的commitID");
            System.exit(0);
        }
    }

    //题目要求：reset --mixed：在soft的基础上，重置index文件到对应commit
    public static void reset_mixed(String commitID) {
        reset_soft(commitID);
        String commitFilePath = obj_dir.getAbsolutePath() + File.separator + commitID + ".txt";
        Commit cmt = Tools.commit_Deserialization(commitFilePath);
        String treeFilePath = obj_dir.getAbsolutePath() + File.separator + cmt.getTreeHash() + ".txt";
        TreeOrIndex tree = Tools.treeOrIndex_Deserialization(treeFilePath);
        //单层文件夹，tree内容和index内容相同
        TreeOrIndex idx = tree;
        Tools.treeOrIndex_serialization(idx);
        System.out.println("index文件已被重置。");
    }

    //题目要求：在mixed的基础上，重置工作区与暂存区内容一致。
    //思路：先清空工作区，再按照暂存区里的条目依次生成文件
    public static void reset_hard(String commitID) {
        reset_mixed(commitID);
        Tools.clearFile(user_dir);
        TreeOrIndex idx = Tools.treeOrIndex_Deserialization(index_file_path);
        Set keyset = idx.getTOI().keySet();
        for (Object fileName : keyset) {
            String hashCode = idx.getTOI().get(fileName).toString();
            String blobFilePath = obj_dir.getAbsolutePath() + File.separator + hashCode + ".txt";
            Blob blob = Tools.blob_Deserialization(blobFilePath);
            String txtFilePath = user_dir.getAbsolutePath() + File.separator + fileName.toString();
            Tools.stringToFile(new String(blob.getContent()), txtFilePath);
        }
        System.out.println("工作区重置成功。");
    }

    public static void pull() {
        Thread threadOne = new Thread(new Runnable() {
            public void run() {
                Server_pullOrPush.pull();
            }
        });

        Thread threadTwo = new Thread(new Runnable() {
            public void run() {
                Client_pullOrPush.pull();
            }
        });
        threadOne.start();
        threadTwo.start();
    }

    public static void push() {
        Thread threadOne = new Thread(new Runnable() {
            public void run() {
                Server_pullOrPush.push();
            }
        });

        Thread threadTwo = new Thread(new Runnable() {
            public void run() {
                Client_pullOrPush.push();
            }
        });
        threadOne.start();
        threadTwo.start();
    }

    public static void status() {
        File indexFile = new File(index_file_path);
        String indexContent = new String(Tools.readContents(indexFile));
        TreeOrIndex idx = null;
        if ("".equals(indexContent)) {
            idx = new TreeOrIndex(new HashMap(), index_file_path);
        } else {
            idx = Tools.treeOrIndex_Deserialization(index_file_path);
        }
        System.out.println("===Changes not staged for commit ===");

        //使用迭代器Iterator遍历
        Set keyset1 = idx.getTOI().keySet();
        Iterator it1 = keyset1.iterator();
        while (it1.hasNext()) {
            Object key = it1.next();
            String fileName = key.toString();
            //1、Changes not staged for commit :deleted情况
            //只存在于暂存区中，而不存在于工作区的文件记录
            if (!Tools.isFilenameInDirectory(fileName, user_dir)) {
                System.out.println(fileName + "(deleted)");
            } else {
                //2、Changes not staged for commit :modified情况
                //存在于暂存区也存在于工作区，但它两哈希值不同的文件记录
                File f = new File(user_dir.getAbsolutePath() + File.separator + fileName);
                if (!idx.getTOI().get(key).toString().equals(Tools.sha1(f))) {
                    System.out.println(fileName + "(modified)");
                }
            }
        }

        //3、Untracked files:
        //工作区中新增还未add的文件，也就是目前index中没有该条目
        System.out.println("===Untracked files===");
        File[] files = user_dir.listFiles();
        for (File f : files) {
            if (f.isFile()) {
                if (!Tools.isFilenameInIndex(f.getName(), idx)) {
                    System.out.println(f.getName());
                }
            }
        }

        //4、Changes to be committed：
        //输出已经add但还没commit的文件，分modified、deleted和new file三种情况
        //思路：通过head指针反序列化得到上一次提交的commit对象，通过commit对象里
        // 的treeHash，反序列化得到上一次提交的tree对象，遍历tree对象里的条目，
        // 可以得到上一次提交的所有文件，和index文件里的条目作对比
        //对于文件条目来说，存在以下四种情况：
        //情况1：如果在index里，但是不在上一次的tree里，说明是new file
        //情况2：如果即在index里又在上一次的tree里，但哈希值不同，说明是modified文件
        //情况3：如果即在index里又在上一次的tree里，并且哈希值不同，说明是unmodified文件，不需要输出任何信息
        //情况4：如果不在index里但是在上一次的tree里，说明是deleted文件
        System.out.println("===Changes to be committed===");
        String head = Tools.readHeadFile();
        Set keyset2 = idx.getTOI().keySet();
        Iterator it2 = keyset2.iterator();
        if (head.trim().equals("")) {
            //目前还没有commit过，所有文件都是new file
            while (it2.hasNext()) {
                Object key = it2.next();
                String fileName = key.toString();
                System.out.println(fileName + "(new file)");
            }
        } else {
            String commitFilePath = obj_dir.getAbsolutePath() + File.separator + head + ".txt";
            Commit cmt = Tools.commit_Deserialization(commitFilePath);
            String treeFilePath = obj_dir.getAbsolutePath() + File.separator + cmt.getTreeHash() + ".txt";
            TreeOrIndex tree = Tools.treeOrIndex_Deserialization(treeFilePath);
            while (it2.hasNext()) {
                Object key = it2.next();
                String fileName = key.toString();
                if (!Tools.isFilenameInTree(fileName, tree)) {
                    System.out.println(fileName + "(new file)");
                } else {
                    String idx_hash = idx.getTOI().get(key).toString();
                    String tree_hash = tree.getTOI().get(key).toString();
                    if (!idx_hash.equals(tree_hash)) {
                        System.out.println(fileName + "(modified)");
                    }
                }
            }

            Set keyset_ = tree.getTOI().keySet();
            for (Object key : keyset_) {
                if (!Tools.isFilenameInIndex(key.toString(), idx)) {
                    System.out.println(key.toString() + "(deleted)");
                }
            }
        }
    }

    //diff命令用来比较未add的文件和上一次add的文件的修改的具体内容
    //思路：先遍历index文件中的条目，查看index文件中的fileName是否在工作区有相同的fileName
    // 如果是，再判断index条目对应的哈希值与工作区文件内容所生成的哈希值是否不同
    //如果不同，说明这个文件是修改的文件，那么输出上一次和这一次的文件内容，方便用户比较它们的不同之处
    //这一次文件内容直接输出，add过的文件需要通过index中的哈希值找到对应的Blob文件，然后反序列化为blob对象
    //通过blob.getContent()可以得到上一次的文件内容
    public static void diff() {
        File indexFile = new File(index_file_path);
        String indexContent = new String(Tools.readContents(indexFile));
        TreeOrIndex idx = null;
        if ("".equals(indexContent)) {
            System.out.println("目前还没有文件add至暂存区，无法查看文件修改的具体内容");
            System.exit(0);
        } else {
            idx = Tools.treeOrIndex_Deserialization(index_file_path);
        }

        //使用迭代器Iterator遍历
        Set keyset = idx.getTOI().keySet();
        Iterator it = keyset.iterator();
        //遍历index文件中的条目
        while (it.hasNext()) {
            Object key = it.next();
            String fileName = key.toString();
            //查看index文件中的fileName是否在工作区有相同的fileName
            if (Tools.isFilenameInDirectory(fileName, user_dir)) {
                File f = new File(user_dir.getAbsolutePath() + File.separator + fileName);
                String idx_hash = idx.getTOI().get(key).toString();
                //index条目对应的哈希值与工作区文件内容所生成的哈希值是否不同
                if (!idx_hash.equals(Tools.sha1(f))) {
                    String blobFilePath = obj_dir.getAbsolutePath() + File.separator + idx_hash + ".txt";
                    Blob blob = Tools.blob_Deserialization(blobFilePath);
                    //输出结果前面带 - 号，代表是修改前的内容
                    //输出结果前面带 + 号，代表是修改后的内容
                    System.out.println("-" + new String(blob.getContent()));
                    System.out.println("+" + new String(Tools.readContents(f)));
                }
            }
        }
    }

    //diff_staged命令用来比较暂存区和上一次提交的文件的具体修改内容
    //思路：如果即在index里又在上一次的tree里，但哈希值不同，说明是modified文件
    //通过哈希值找到Blob文件，反序列化为blob对象，通过blob.getContent()输出修改前后的内容
    //index里的条目对应的哈希值生成的blob文件是修改后的
    //tree里的条目对应的哈希值生成的blob文件是修改前的
    public static void diff_staged() {
        String head = Tools.readHeadFile();
        if (head.trim().equals("")) {
            //目前还没有commit过，所有文件都是new file，输出提示信息
            System.out.println("当前没有历史提交版本，没有暂存区文件和上一次提交的比较信息。请提交过后再输入该命令。");
            System.exit(0);
        } else {
            String commitFilePath = obj_dir.getAbsolutePath() + File.separator + head + ".txt";
            Commit cmt = Tools.commit_Deserialization(commitFilePath);
            String treeFilePath = obj_dir.getAbsolutePath() + File.separator + cmt.getTreeHash() + ".txt";
            TreeOrIndex tree = Tools.treeOrIndex_Deserialization(treeFilePath);

            TreeOrIndex idx = Tools.treeOrIndex_Deserialization(index_file_path);
            ;
            //使用迭代器Iterator遍历
            Set keyset = idx.getTOI().keySet();
            Iterator it = keyset.iterator();
            while (it.hasNext()) {
                Object key = it.next();
                String fileName = key.toString();
                if (Tools.isFilenameInTree(fileName, tree)) {
                    String idx_hash = idx.getTOI().get(key).toString();
                    String tree_hash = tree.getTOI().get(key).toString();
                    if (!idx_hash.equals(tree_hash)) {
                        Blob preModifiedBlob = Tools.blob_Deserialization(obj_dir.getAbsolutePath() + File.separator + tree_hash + ".txt");
                        Blob afterModifiedBlob = Tools.blob_Deserialization(obj_dir.getAbsolutePath() + File.separator + idx_hash + ".txt");
                        //输出结果前面带 - 号，代表是修改前的内容
                        //输出结果前面带 + 号，代表是修改后的内容
                        System.out.println("-" + new String(preModifiedBlob.getContent()));
                        System.out.println("+" + new String(afterModifiedBlob.getContent()));
                    }
                }
            }
        }
    }

    //branch命令：增加一个Branch，即在heads文件夹中添加一个新的名为branchname的文件
    // 内容为当前的commitID。此操作不改变HEAD指向，只是单纯增加一个Branch。
    public static void branch(String branchName) {
        String branchName_ = branchName + ".txt";
        if (Tools.isFilenameInDirectory(branchName_, heads_dir)) {
            System.out.println("该分支名已存在，您可以换一个分支名。");
            System.exit(0);
        }
        String newBranchHeadPath = heads_dir.getAbsolutePath() + File.separator + branchName_;
        String curCommitID = Tools.readHeadFile();
        Tools.stringToFile(curCommitID, newBranchHeadPath);
        System.out.println("分支创建成功。");
    }

    //rm --branch [branchname]
    //rm_branch命令：删除一个Branch，此Branch不能为现在的HEAD指向的Branch。
    // 思路：删除heads文件夹中的branchname文件。
    //健壮性：1、考虑输入的branchName不存在。2、要删除的branch是当前的branch，提示不能删除
    public static void rm_branch(String branchName) {
        String branchName_ = branchName + ".txt";
        if (!Tools.isFilenameInDirectory(branchName_, heads_dir)) {
            System.out.println("不存在该分支，请输入正确的分支名。");
        } else {
            //健壮性2
            //思路：以branchName文件中的head指针为表头，遍历该分支链表
            //判断该链表上的每一个commitID是否是head文件里的commitID
            //如果是，则提示不能删除，退出程序。
            // 如果遍历完链表发现链表上的每一个commitID都不是head文件里的commitID，说明该分支不是当前分支，可以删除
            String branchPath = heads_dir.getAbsolutePath() + File.separator + branchName + ".txt";
            String branchHead = Tools.readBranchHeadFile(branchPath);
            String head = Tools.readHeadFile();
            String commitID = branchHead;
            while (!commitID.equals("")) {
                if (commitID.equals(head)) {
                    System.out.println(branchName + "分支是当前分支，无法删除。");
                    System.exit(0);
                }
                Commit cmt = Tools.commit_Deserialization(obj_dir.getAbsolutePath() +
                        File.separator + commitID + ".txt");
                commitID = cmt.getPreCommit();
            }

            File branchFile = new File(heads_dir.getAbsolutePath() + File.separator + branchName + ".txt");
            branchFile.delete();
            System.out.println("分支删除成功。");
        }
    }

    //find [commitmessage]
    //命令要求：打印所有与输入message相同的Commit的ID，如果有多个结果，一行一个。
    //思路：遍历读出objects文件夹里的文件，判断对象是否是commit类。
    // 如果是，就反序列化为commit对象，通过cmt.getMessage()获取这次commit的message信息，
    // 如果这个message和targetMessage相同，输出这次的commitID
    public static void find(String targetMessage) {
        File[] files = obj_dir.listFiles();
        boolean flag = true;
        for (File f : files) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(f));
                Object o = ois.readObject();
                if (o instanceof Commit) {
                    Commit cmt = Tools.commit_Deserialization(f.getAbsolutePath());
                    if (cmt.getMessage().equals(targetMessage)) {
                        System.out.println(cmt.getCurrCommit());
                        flag = false;
                    }
                }
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
            }
        }
        if (flag) System.out.println("未找到message为 " + targetMessage + " 的历史提交版本。");
    }

    //reflog:log命令只能输出head指针为表头的链表信息
    //而reflog命令输出head的每一次变动信息
    //思路：运用ArrayList，在head变动时，把head文件里新的哈希值写入ArrayList中
    // 再把ArrayList序列化到.gits目录下的head_changes.txt文件里
    // head变动分三种情况，commit，reset，pull，其中pull命令内置了reset hard命令
    //因此只需要更改commit命令和reset命令
    public static void reflog() {
        String head = Tools.readHeadFile();
        if (head.trim().equals("")) {
            System.out.println("目前没有commit过，没有提交的历史版本。");
            System.exit(0);
        }
        //如果head不为空，那么head_changes文件中也不为空
        String filePath = head_changes_file_path;
        ArrayList list = Tools.ArrayList_Deserialization(filePath);
        Iterator it = list.iterator();
        int k = 0;
        while (it.hasNext()) {
            String commitID = (String) it.next();
            String commitFilePath = obj_dir.getAbsolutePath() + File.separator + commitID + ".txt";
            Commit cmt = Tools.commit_Deserialization(commitFilePath);
            System.out.println(cmt.getCurrCommit() + "\tHEAD@{" + k + "}" + "\tmessage:" + cmt.getMessage());
            k++;
        }
    }
}


