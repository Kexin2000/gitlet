import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Client_pullOrPush {
    public static void push() {
        //题目要求：
        // client端：通过命令行：设置/打印准备连接的套接字（ip+端口号）
        Socket sk_client = null;
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            //客户端链接
            sk_client = new Socket(InetAddress.getLocalHost(), 6666);
            System.out.println("client ip：" + InetAddress.getLocalHost() + "\tclient port：6666");
            System.out.println("连接成功~");

            //push命令是client端发送，server端接收
            File[] obj_files = command.obj_dir.listFiles();
            int count = 0;
            for (File file : obj_files) {
                count++;
            }
            oos = new ObjectOutputStream(sk_client.getOutputStream());
            oos.writeInt(count);//objects文件夹里有count个文件，需要把文件的个数发给服务器，这样服务器才知道什么时候关闭


            //传送Objects文件
            for (File file : obj_files) {
                String fileName = file.getName();
                ois = new ObjectInputStream(new FileInputStream(file));
                oos.writeObject(ois.readObject());
                oos.writeUTF(fileName);
            }

            //传输head指针
            oos.writeUTF(Tools.readHeadFile());

            //传送heads文件夹，思路同objects文件夹
            File[] heads_files = command.heads_dir.listFiles();
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
            oos.writeObject(Tools.ArrayList_Deserialization(command.head_changes_file_path));

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
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
                sk_client.close();
                System.out.println("客户端终止连接。");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void pull(){
        //题目要求：
        // client端：通过命令行：设置/打印准备连接的套接字（ip+端口号）
        Socket sk_client = null;
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            //客户端链接
            sk_client = new Socket(InetAddress.getLocalHost(), 6666);
            System.out.println("client ip：" + InetAddress.getLocalHost() + "client port：6666");
            System.out.println("连接成功~");

            //pull命令是server端发送，client端接收
            //在client端接收
            int objectsFileCount = 0;
            ois = new ObjectInputStream(sk_client.getInputStream());
            objectsFileCount = ois.readInt();

            //接收objects文件夹里的文件
            for(int i = 1;i <= objectsFileCount;i++){
                Object o = ois.readObject();
                String fileName = ois.readUTF();
                File file = new File(command.obj_dir.getAbsolutePath()+File.separator+fileName);
                oos = new ObjectOutputStream(new FileOutputStream(file));
                oos.writeObject(o);
            }
            System.out.println("objects文件夹传输成功。");

            //接收head文件
            Tools.stringToFile(ois.readUTF(),command.head_file_path);
            System.out.println("head文件传输成功。");

            //接收heads文件夹
            int headsFileCount = 0;
            headsFileCount = ois.readInt();
            for(int i = 1;i <= headsFileCount;i++){
                String branchHeadString = ois.readUTF();
                String fileName = ois.readUTF();
                String filePath = command.heads_dir.getAbsolutePath()+File.separator+fileName;
                Tools.stringToFile(branchHeadString,filePath);
            }
            System.out.println("heads文件夹传输成功。");

            //接收head_changes文件
            Tools.ArrayList_serialization((ArrayList)ois.readObject(),command.head_changes_file_path);
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
                sk_client.close();
                System.out.println("客户端终止连接。");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(Tools.readHeadFile().trim().equals("")){
            System.out.println("远程仓库是一个新仓库，head指针为空，因此无法执行重置reset操作");
            System.exit(0);
        }
        command.reset_hard(Tools.readHeadFile());
    }
}

