package com.mawujun.yml;

import com.mawujun.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class YmlUtil {
    static  Logger logger= LoggerFactory.getLogger(YmlUtil.class);

    private LinkedHashMap<String, Object> result =null;
    Yaml yaml = new Yaml();
    private String filePath=null;

    /**
     *  YmlUtil ymlUtil=YmlUtil.getInstance().initMap(url.getPath());
     * @return
     */
    public static YmlUtil getInstance(){
        return new YmlUtil();
    }
    /**
     *
     * @param filePath 绝对路径
     */
    public YmlUtil initMap(String filePath){
        this.filePath=filePath;
        try {
            logger.info("读取的yml文件地址是:"+filePath);

            //System.out.println("读取到的profile是:"+current_profile);
            //先读取默认的application.yml
            InputStream in=new FileInputStream(filePath);
            result = (LinkedHashMap<String, Object>) yaml.load(in);
            //在读取带prifle的，如果有同名的就覆盖默认的
            //in=new FileInputStream(current_resources_dir+File.separator+"application-"+current_profile+".yml");
            LinkedHashMap<String, Object> profile_yml_map = (LinkedHashMap<String, Object>) yaml.load(in);
            //current_yml_map.putAll(profile_yml_map);
        } catch (FileNotFoundException e) {
            logger.error("文件不存在", e);
        }
        return this;
    }

    /**
     *
     * @param key  类似这样的值：spring.datasource.druid.url
     * @return
     */
    public  String getYmlValue(String key) {
        String[] keys = key.split("[.]");
        Map<String, Object> map = result;//(Map<String, Object>) sourceMap.clone();
        int length = keys.length;
        Object resultValue = null;
        for (int i = 0; i < length; i++) {
            Object value = map.get(keys[i]);
            if (i < length - 1) {
                map = ((Map<String, Object>) value);
            } else if (value == null) {
                //throw new RuntimeException("key is not exists:"+key);
                return null;
            } else {
                resultValue = value;
            }
        }
        return resultValue.toString();
    }

    /**
     * 获取某个键值对,可能返回Map，List，String
     * @return
     */
    public Object getYmlMap(String key){
       String[] keys = key.split("[.]");
        Map<String, Object> map = result;//(Map<String, Object>) sourceMap.clone();
        int length = keys.length;
        Object resultValue = null;
        for (int i = 0; i < length; i++) {
            if(map==null){
                return null;
            }
            Object value = map.get(keys[i]);
            if (i < length - 1) {
                map = ((Map<String, Object>) value);
            } else if (value == null) {
                //throw new RuntimeException("key is not exists:"+key);
                return null;
            } else {
                resultValue = value;
            }
        }
        return resultValue;
    }

    /**
     * 通过引用关系设置后，调用后就可以写入到读取的文件
     */
    public void save(){
        //try {
            //yaml.dump(result, new FileWriter(filePath));
            String aaa=yaml.dumpAsMap(result);
            System.out.println(aaa);
            FileUtil.writeString(aaa, filePath, "UTF-8");
//        } catch (IOException e) {
//            logger.error("yml文件写入失败", e);
//        }
    }


}
