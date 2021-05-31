package com.mawujun.io;

import com.mawujun.util.RandomUtil;
import com.mawujun.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * @author mawujun 16064988 16064988@qq.com
 * @date 2020-07-23
 **/
public class JarUtil {
    private static Logger logger= LoggerFactory.getLogger(JarUtil.class);
//    /**
//     * 测试案例
//     * @param args
//     * @throws Exception
//     */
//    public static void main(String args[]) throws Exception{
//
//        //
//        readJarFile("D:\\esjavaclient-0.0.1-SNAPSHOT.jar","es-info.properties");
//
//        String data = "helloBabydsafsadfasdfsdafsdgasdgweqtqwegtqwfwefasdfasfadfasf";
//
//        long start = System.currentTimeMillis();
//
//        writeJarFile("D:\\esjavaclient-0.0.1-SNAPSHOT.jar","es-info.properties",data.getBytes());
//
//        long end = System.currentTimeMillis();
//
//        System.out.println(end-start);
//
//        readJarFile("D:\\esjavaclient-0.0.1-SNAPSHOT.jar","es-info.properties");
//
//    }

    //

    /**
     * 返回类所在的jar文件的绝对路径,D:/apache-maven-3.5.4/repository/org/springframework/spring-core/5.0.10.RELEASE/spring-core-5.0.10.RELEASE.jar
     * 如果不是jar中的class，则返回该class所在的classpath：E:/my-workspace/leon-repository/target/test-classes/
     * 如果jar被加密了，获取的时候报错(SecurityException: SHA-256 digest error for xxxxx)，可以换一个类试下。
     * @param clazz
     * @return
     */
    public static String getJarAbstractPath(Class<?> clazz){
        return FileUtil.getJarAbstractPath(clazz);
    }

    /**
     * 读取jar包所有的文件内容，显示JAR文件内容列表
     * @param jarFilePath
     * @throws IOException
     */
    public static void readJARList(String jarFilePath) throws IOException {
        // 创建JAR文件对象
        JarFile jarFile = new JarFile(jarFilePath);
        // 枚举获得JAR文件内的实体,即相对路径
        Enumeration en = jarFile.entries();
        System.out.println("文件名\t文件大小\t压缩后的大小");
        // 遍历显示JAR文件中的内容信息
        while (en.hasMoreElements()) {
            // 调用方法显示内容
            process(en.nextElement());
        }
    }

    // 显示对象信息
    private static void process(Object obj) {
        // 对象转化成Jar对象
        JarEntry entry = (JarEntry) obj;
        // 文件名称
        String name = entry.getName();
        // 文件大小
        long size = entry.getSize();
        // 压缩后的大小
        long compressedSize = entry.getCompressedSize();
        System.out.println(name + "\t" + size + "\t" + compressedSize);
    }

    /**
     * 读取jar包里面指定文件的内容
     * @param jarFilePath jar包文件路径
     * @param fileName  文件名
     * @throws IOException
     */
    public static void readJarFile(String jarFilePath,String fileName) throws IOException{
        JarFile jarFile = new JarFile(jarFilePath);
        JarEntry entry = jarFile.getJarEntry(fileName);
        InputStream input = jarFile.getInputStream(entry);
        readFile(input);
        jarFile.close();
    }


    public static void readFile(InputStream input) throws IOException{
        InputStreamReader in = new InputStreamReader(input);
        BufferedReader reader = new BufferedReader(in);
        String line ;
        while((line = reader.readLine())!=null){
            System.out.println(line);
        }
        reader.close();
    }

    /**
     * 读取流
     *
     * @param inStream
     * @return 字节数组
     * @throws Exception
     */
    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        outSteam.close();
        inStream.close();
        return outSteam.toByteArray();
    }

    /**
     * 先解压然后再压缩回去
     * @param jarFilePath
     * @param entryNames 最好带上路径，例如：META-INF/37E3C32D.RSA，META-INF/37E3C32D.SF
     */
    public static void removeJarFile2(String jarFilePath,String... entryNames) throws IOException {
        String dirPath= FileUtil.getTmpDirPath()+File.separator+ RandomUtil.randomString(4);//"D:\\aaaa\\temp";//
        File dirFile=new File(dirPath);

        //unzipJar(dirPath,jarFilePath);
        ZipUtil.unzip(new File(jarFilePath),dirFile);
        List<File> files= FileUtil.listLoopFiles(dirPath);
        for(File file:files){
            for(String entryName:entryNames){
                if(!File.separator.equals("/")){
                    entryName=entryName.replaceAll("\\/", "\\\\");
                }

                if(file.getPath().endsWith(entryName)){
                    file.delete();
                }
            }
        }
        //重新压缩
        ZipUtil.zip(dirPath,jarFilePath);
        dirFile.delete();
    }

    /**
     * 把指定的文件entryName从jar中删除.
     * 如果jar加密过，将会报错
     * @param jarFilePath
     * @param entryName
     * @throws Exception
     */
    public static void removeJarFile(String jarFilePath,String entryName) throws Exception{
//1、首先将原Jar包里的所有内容读取到内存里，用TreeMap保存
        JarFile  jarFile = new JarFile(jarFilePath);
        //可以保持排列的顺序,所以用TreeMap 而不用HashMap
        TreeMap tm = new TreeMap();
        Enumeration es = jarFile.entries();
        while(es.hasMoreElements()){
            JarEntry je = (JarEntry)es.nextElement();
            byte[] b = readStream(jarFile.getInputStream(je));
            if(je.getName().equals(entryName)){
               continue;
            }
            tm.put(je.getName(),b);
        }

        JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFilePath));
        Iterator it = tm.entrySet().iterator();
        boolean has = false;

        //2、将TreeMap重新写到原jar里，如果TreeMap里已经有entryName文件那么覆盖，否则在最后添加
        while(it.hasNext()){
            Map.Entry item = (Map.Entry) it.next();
            String name = (String)item.getKey();
            JarEntry entry = new JarEntry(name);
            jos.putNextEntry(entry);
            byte[] temp =null;
            if(name.equals(entryName)){
                //覆盖
                //temp = data;
                has = true ;
            }else{
                temp = (byte[])item.getValue();
            }
            if(temp!=null){
                jos.write(temp, 0, temp.length);
            }
        }

//        if(!has){
//            //最后添加
//            JarEntry newEntry = new JarEntry(entryName);
//            jos.putNextEntry(newEntry);
//            jos.write(data, 0, data.length);
//        }
        jos.finish();
        jos.close();

    }

    /**
     * 还没有测试过
     * jar -uf a.jar com/a.class
     * a.class 文件在 jar 包中的目录是 com/a.class。
     * a.class 文件在本地路径，相对 a.jar 包，也是 com/a.class。
     * 快速覆盖jar包内的文件，
     * https://www.cnblogs.com/pfblog/p/7227184.html  Java如何快速修改Jar包里的文件内容
     * https://blog.csdn.net/young_kim1/article/details/50482398
     * @param jarFilePath
     * @param filePath
     */
    public static void writeJarFileCmd(String jarFilePath,String filePath){
        //String cmd = "jar uf esjavaclient-0.0.1-SNAPSHOT.jar config.properties";
        String cmd = "jar uf "+jarFilePath+" "+filePath+"";
        String[] cmds = {"/bin/sh","-c",cmd};
        Process pro;
        try {
            pro = Runtime.getRuntime().exec(cmds);
            pro.waitFor();
            InputStream in = pro.getInputStream();
            BufferedReader read = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while((line = read.readLine())!=null){
                System.out.println(line);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     *
     * @param jarFilePath
     * @param entryName
     * @param filepath 把filepath中的内容读取文件覆盖entryName的内容
     * @throws Exception
     */
    public static void writeJarFile(String jarFilePath,String entryName,String filepath) throws Exception{
        byte[] data=FileUtil.readBytes(filepath);
        writeJarFile(jarFilePath,entryName,data);
    }
    /**
     * 修改Jar包里的文件或者添加文件
     * @param jarFilePath jar包路径
     * @param entryName 要写的文件名:com/mawujun/AAA.class，必须使用/这种斜杠
     * @param data   文件内容
     * @throws Exception
     */
    public static void writeJarFile(String jarFilePath,String entryName,byte[] data) throws Exception{

        //1、首先将原Jar包里的所有内容读取到内存里，用TreeMap保存
        JarFile  jarFile = new JarFile(jarFilePath);
        //可以保持排列的顺序,所以用TreeMap 而不用HashMap
        TreeMap tm = new TreeMap();
        Enumeration es = jarFile.entries();
        while(es.hasMoreElements()){
            JarEntry je = (JarEntry)es.nextElement();
            byte[] b = readStream(jarFile.getInputStream(je));
            tm.put(je.getName(),b);
        }

        JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFilePath));
        Iterator it = tm.entrySet().iterator();
        boolean has = false;

        //2、将TreeMap重新写到原jar里，如果TreeMap里已经有entryName文件那么覆盖，否则在最后添加
        while(it.hasNext()){
            Map.Entry item = (Map.Entry) it.next();
            String name = (String)item.getKey();
            JarEntry entry = new JarEntry(name);
            jos.putNextEntry(entry);
            byte[] temp ;
            if(name.equals(entryName)){
                //覆盖
                temp = data;
                has = true ;
            }else{
                temp = (byte[])item.getValue();
            }
            jos.write(temp, 0, temp.length);
        }

        if(!has){
            //最后添加
            JarEntry newEntry = new JarEntry(entryName);
            jos.putNextEntry(newEntry);
            jos.write(data, 0, data.length);
        }
        jos.finish();
        jos.close();

    }



}
