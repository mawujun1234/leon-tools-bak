package test.mawujun.http;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mawujun.lang.Assert;

@RestController
public class HttpCilentController {
	@RequestMapping(value="/doget",method= {RequestMethod.GET})
    String doget(String age,String name) {
		
        return age+name;
    }
	
	@RequestMapping(value="/dopost",method= {RequestMethod.POST})
    String dopost(String age,String name) {
		
        return age+name;
    }
	
	@RequestMapping(value="/doPostJson",method= {RequestMethod.POST})
    String doPostJson(@RequestBody String age) {	
        return age;
    }
	
	@RequestMapping(value="/doPostJson1",method= {RequestMethod.POST})
	Person doPostJson1(@RequestBody Person person) {	
        return person;
    }
	
	@RequestMapping(value="/doPostJson2",method= {RequestMethod.POST})
	String doPostJson2(@RequestBody Person person,@RequestHeader("aaa") String aaa,@RequestHeader("bbb") String bbb ) {	
        return person.getAge()+person.getName()+aaa+bbb;
    }
	
	
	@RequestMapping(value="/doPostFile",method= {RequestMethod.POST})
	String doPostFile(@RequestParam("file") MultipartFile file,String age,String name,HttpServletRequest request) {	
		System.out.println(request.getParameter("age"));
		
        return age+name+file.getOriginalFilename();
    }
	
	
	@RequestMapping(value="/doPostFile1",method= {RequestMethod.POST})
	String doPostFile1(@RequestParam("file") MultipartFile file,byte[] file1, InputStream file2,String age,String name,HttpServletRequest request) {	
		Assert.notNull(file1);
		Assert.notNull(file2);
		
        return age+name+file.getOriginalFilename();
    }
}
