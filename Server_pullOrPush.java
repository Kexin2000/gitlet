import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server_pullOrPush {
    //push是Server是接收端，Client是发送端
    public static void push() {
        //题目要求：
        //server端：通过命令行：设置/打印 监听的端口号，打印本机ip地址
        // 直接将当前运行路径作为远程仓库路径
        String remote_path = System.getProperty("user.dir");
        ServerSocket ss = null;
        Socket sk_server = null;
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            //服务器端打开
            ss = new ServerSocket(6666);
            System.out.println("server ip：" + InetAddress.getLocalHost() + "\tserver port：6666");
            //服务器连接
            sk_server = ss.accept();
            System.out.println("服务器客户端成功连接！");

            //在服务端接收
            //接收objects文件夹
            int objectsFileCount = 0;
            ois = new ObjectInputStream(sk_server.getInputStream());
            objectsFileCount = ois.readInt();
            for(int i = 1;i <= objectsFileCount;i++){
                Object o = ois.readObject();
                String fileName = ois.readUTF();
                File file = new File(remote_path+File.separator+".git"+File.separator+"objects"+File.separator+fileName);

                oos = new ObjectOutputStream(new FileOutputStream(file));
                oos.writeObject(o);
            }
            System.out.println("objects文件夹传输成功。");

            //接收head文件
            String headFilePath = remote_path+File.separator+".git"+File.separator+"head.txt";
            Tools.stringToFile(ois.readUTF(),headFilePath);
            System.out.println("head文件传输成功。");

            //接收heads文件夹
            int headsFileCount = 0;
            headsFileCount = ois.readInt();
            for(int i = 1;i <= headsFileCount;i++){
                String branchHeadString = ois.readUTF();
                String fileName = ois.readUTF();
                String filePath = remote_path+File.separator+".git"+File.separator+"refs"+File.separator+"heads"+File.separator+fileName;
                Tools.stringToFile(branchHeadString,filePath);
            }
            System.out.println("heads文件夹传输成功。");

            //接收head_changes文件
            String filePath = remote_path+File.separator+".git"+File.separator+"head_changes.txt";
            Tools.ArrayList_serialization((ArrayList)ois.readObject(),filePath);
            System.out.println("head_changes文件接收成功。");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                if(ois !=null){
                    try {
                        ois.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(oos != null){
                    try {
                        oos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                sk_server.close();
                ss.close();
                System.out.println("传输完毕，服务器终止连接。");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }



    //pull命令Client是接收端，Server是发送端
    public static void pull() {
        //题目要求：
        //server端：通过命令行：设置/打印 监听的端口号，打印本机ip地址
        // 直接将当前运行路径作为远程仓库路径
        String remote_path = System.getProperty("user.dir");
        ServerSocket ss = null;
        Socket sk_server = null;
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            //服务器端打开
            ss = new ServerSocket(6666);
            System.out.println("server ip：" + InetAddress.getLocalHost() + "server port：6666");
            //服务器连接
            sk_server = ss.accept();
            System.out.println("服务器客户端成功连接！");

            //服务器发送文件，把远程仓库里的文件发送至Client
            File remote_obj_dir = new File(remote_path+File.separator+".git"+File.separator+"objects" );
            File[] files = remote_obj_dir.listFiles();
            int count = 0;
            for (File file : files) {
                count++;
            }
            oos = new ObjectOutputStream(sk_server.getOutputStream());
            oos.writeInt(count);//远程仓库objects文件夹里有count个文件，需要把文件的个数发给客户端，这样客户端才知道什么时候关闭连接

            //传送objects文件夹里的文件
            for (File file : files) {
                String fileName = file.getName();
                ois = new ObjectInputStream(new FileInputStream(file));
                oos.writeObject(ois.readObject());
                oos.writeUTF(fileName);
            }

            //发送head文件
            File remoteHeadFile = new File(remote_path+File.separator+".git"+File.separator+"head.txt" );
            String head = new String(Tools.readContents(remoteHeadFile));
            oos.writeUTF(head);

            //发送heads文件夹
            File remote_heads_dir = new File(remote_path+File.separator+".git"+File.separator+"refs"+File.separator+"heads");
            File[] heads_files = remote_heads_dir.listFiles();
            int k = 0;
            for (File file : heads_files) {
                k++;
            }
            oos.writeInt(k);

            for (File file : heads_files) {
                String fileName = file.getName();
                String branchHeadString = Tools.readBranchHeadFile(file.getAbsolutePath());
                oos.writeUTF(branchHeadString);
                oos.writeUTF(fileName);
            }

            //传送head_changes文件
            String filePath = remote_path+File.separator+".git"+File.separator+"head_changes.txt";
            oos.writeObject(Tools.ArrayList_Deserialization(filePath));

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if(ois != null){
                    try {
                        ois.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(oos != null){
                    try {
                        oos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                sk_server.close();
                ss.close();
                System.out.println("传输完毕，服务器终止连接。");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

