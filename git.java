public class git {
    public static void main(String[] args) {
        //判断是否输入有效命令
        if(args.length == 0){
            System.out.println("未输入有效命令");
            System.exit(0);
        }

        switch (args[0]){
            //init命令，validLen判断命令长度是否合规
            case "init":
                validLen(args,1);
                command.init();
                break;

            //add命令，isInitial判断是否初始化仓库
            case "add":
                validLen(args,2);
                isInitial();
                command.add(args[1]);
                break;

            //commit命令
            case "commit":
                validLen(args,3);
                if(!args[1].equals("-m")){
                    System.out.println("第二个参数为固定参数-m，请在第二个参数的位置输入-m。");
                    System.exit(0);
                }
                isInitial();
                command.commit(args[2]);
                break;

            //rm命令，如果是 rm --cashed <file> ,validLen = 3,args[2]为文件名
            //如果是rm <file> , validLen = 2, args[1]为文件名
            case "rm":
                if("--cached".equals(args[1])) {
                    validLen(args, 3);
                    isInitial();
                    command.rm_cached(args[2]);
                }else if("--branch".equals(args[1])){
                    validLen(args,3);
                    isInitial();
                    command.rm_branch(args[2]);
                }else{
                    validLen(args,2);
                    isInitial();
                    command.rm(args[1]);
                }
                break;

            //log命令
            case "log":
                validLen(args,1);
                isInitial();
                command.log();
                break;

            //reset命令
            //git reset --soft 哈希值 ，validLen = 3，args[2]为文件名
            //git reset --mixed 哈希值 ，validLen = 3，args[2]为文件名
            //git reset --hard 哈希值 ，validLen = 3，args[2]为文件名
            //git reset 哈希值 ，validLen = 2，args[1]为文件名
            case "reset":
                if("--soft".equals(args[1])){
                    validLen(args,3);
                    isInitial();
                    command.reset_soft(args[2]);
                }else if("--hard".equals(args[1])){
                    validLen(args,3);
                    isInitial();
                    command.reset_hard(args[2]);
                }else if("--mixed".equals(args[1])){
                    validLen(args,3);
                    isInitial();
                    command.reset_mixed(args[2]);
                }else{
                    validLen(args,2);
                    isInitial();
                    command.reset_mixed(args[1]);
                }
                break;

            //find [commitmessage]
            //打印所有与输入message相同的Commit的ID，如果有多个结果，一行一个。
            case "find":
                validLen(args,2);
                isInitial();
                command.find(args[1]);
                break;

            case "reflog":
                validLen(args,1);
                isInitial();
                command.reflog();
                break;

            case "status":
                validLen(args,1);
                isInitial();
                command.status();
                break;

            case "diff":
                if(args.length == 2 && "--staged".equals(args[1])){
                    isInitial();
                    command.diff_staged();
                }else{
                    validLen(args,1);
                    isInitial();
                    command.diff();
                }
                break;

            //branch [branchname]
            //夹中添加一个新的名为branchname的文件，内容为当前的commitID。
            // 此操作不改变HEAD指向，只是单纯增加一个Branch。
            case "branch":
                validLen(args,2);
                isInitial();
                command.branch(args[1]);
                break;

            case "push":
                validLen(args,1);
                isInitial();
                if(!Tools.isRemoteGitInitial()){
                    Tools.RemoteGitInit();
                    System.out.println("该远程仓库不存在，已为您初始化一个远程仓库");
                }
                command.push();
                break;

            case "pull":
                validLen(args,1);
                isInitial();
                if(!Tools.isRemoteGitInitial()){
                    System.out.println("该远程仓库不存在，请先初始化远程仓库或者换一个远程仓库");
                    System.exit(0);
                }
                command.pull();
                break;

            default:
                System.out.println("该命令不存在，请输入正确的命令。");
                System.exit(0);
        }
    }

    //判断输入命令长度是否为有效长度
    public static void validLen(String[] args, int n){
        if(args.length != n) {
            System.out.println("命令无效");
            System.exit(0);
        }
    }

    //判断仓库是否初始化
    public static void isInitial(){
        if(!command.git_dir.exists()){
            System.out.println("未初始化仓库，请先初始化仓库");
            System.exit(0);
        }
    }
}
