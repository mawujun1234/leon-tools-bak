package test.mawujun.yml;

import com.mawujun.yml.YmlUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YmlUtilTest {
    @Test
    public void test() throws IOException {
        URL url=this.getClass().getClassLoader().getResource("YmlUtilTest.yml");
        YmlUtil ymlUtil=YmlUtil.getInstance().initMap(url.getPath());
        Assert.assertEquals("dev", ymlUtil.getYmlValue("spring.profiles.active"));
        List<Map<String,Object>> routes=(List<Map<String,Object>>)ymlUtil.getYmlMap("spring.cloud.gateway.routes");
        //
        int targetSize=4;
        if(routes.size()==targetSize){

        } else {
            //第一次测试的时候，因为idea在测试的时候不会对target中东西进行复原
            Assert.assertEquals(3, routes.size());
        }

        boolean exists=false;
        for(Map<String,Object> map:routes){
            if("leon-cloud-aaa".equals(map.get("id"))){
                exists=true;
                break;
            }
        }
if(!exists){
    Map<String,Object> router=new LinkedHashMap<>();
    router.put("id", "leon-cloud-aaa");
    router.put("uri", "lb://leon-cloud-aaa");
    List<String> predicates=new ArrayList<>();
    predicates.add("predicates");
    List<String> Path=new ArrayList<>();
    Path.add("Path=/aaa/**");
    router.put("predicates", Path);
    routes.add(router);

    ymlUtil.save();
}


        YmlUtil ymlUtil1=YmlUtil.getInstance().initMap(url.getPath());
       routes=(List<Map<String,Object>>)ymlUtil1.getYmlMap("spring.cloud.gateway.routes");
        Assert.assertEquals(targetSize, routes.size());

    }
}
