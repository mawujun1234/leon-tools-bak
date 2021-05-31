package com.mawujun.utils.file;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class FileUtils extends org.apache.commons.io.FileUtils {
	static Logger logger = LoggerFactory.getLogger(FileUtils.class);
	
	 public static final String FILE_SEPARATOR =  System.getProperty("file.separator");
	  /**
	     * The number of bytes in a kilobyte.
	     */
	    public static final long ONE_KB = 1024;
	 /**
	     * The number of bytes in a megabyte.
	     */
	    public static final long ONE_MB = ONE_KB * ONE_KB;
	  /**
	     * The file copy buffer size (30 MB)
	     */
	    private static final long FILE_COPY_BUFFER_SIZE = ONE_MB * 30;
	
	    /** 
	     * 递归查找文件 
	     *  //    在此目录中找文件  
	        String baseDIR = "d:/file";   
	        //    找扩展名为txt的文件  
	        String fileName = "*.txt";   
	        FileUtils.findFiles(baseDIR, fileName);   
	        if (resultList.size() == 0) {  
	            System.out.println("No File Fount.");  
	        } else {  
	            for (int i = 0; i < resultList.size(); i++) {  
	                System.out.println(resultList.get(i));//显示查找结果。   
	            }  
	        }  
	     * @param baseDirName  查找的文件夹路径 
	     * @param targetFileName  需要查找的文件名 
	     * @param fileList  查找到的文件集合 
	     */  
	    public static List<File> findFiles(String baseDirName, String targetFileName) {
	    	List<File> files=new ArrayList<File>();
	    	findFiles(baseDirName,targetFileName,files);
	    	return files;
	    }
	   
	    private  static void findFiles(String baseDirName, String targetFileName, List<File> fileList) {  
	        /** 
	         * 算法简述： 
	         * 从某个给定的需查找的文件夹出发，搜索该文件夹的所有子文件夹及文件， 
	         * 若为文件，则进行匹配，匹配成功则加入结果集，若为子文件夹，则进队列。 
	         * 队列不空，重复上述操作，队列为空，程序结束，返回结果。 
	         */  
	        String tempName = null;  
	        //判断目录是否存在  
	        File baseDir = new File(baseDirName);  
	        if (!baseDir.exists() || !baseDir.isDirectory()){  
	            //System.out.println();  
	        	logger.info("文件查找失败：{} 不是一个目录！或者不存在这个目录",baseDirName);
	        } else {  
	            String[] filelist = baseDir.list();  
	            for (int i = 0; i < filelist.length; i++) {  
	                File readfile = new File(baseDirName + File.separator + filelist[i]);  
	                //System.out.println(readfile.getName());  
	                if(!readfile.isDirectory()) {  
	                    tempName =  readfile.getName();   
	                    if (FileUtils.wildcardMatch(targetFileName, tempName)) {  
	                        //匹配成功，将文件名添加到结果集  
	                        fileList.add(readfile.getAbsoluteFile());   
	                    }  
	                } else if(readfile.isDirectory()){  
	                    findFiles(baseDirName + File.separator + filelist[i],targetFileName,fileList);  
	                }  
	            }  
	        }  
	    }  
	      
	    /** 
	     * 通配符匹配 
	     * @param pattern    通配符模式 
	     * @param str    待匹配的字符串 
	     * @return    匹配成功则返回true，否则返回false 
	     */  
	    private static boolean wildcardMatch(String pattern, String str) {  
	        int patternLength = pattern.length();  
	        int strLength = str.length();  
	        int strIndex = 0;  
	        char ch;  
	        for (int patternIndex = 0; patternIndex < patternLength; patternIndex++) {  
	            ch = pattern.charAt(patternIndex);  
	            if (ch == '*') {  
	                //通配符星号*表示可以匹配任意多个字符  
	                while (strIndex < strLength) {  
	                    if (wildcardMatch(pattern.substring(patternIndex + 1),  
	                            str.substring(strIndex))) {  
	                        return true;  
	                    }  
	                    strIndex++;  
	                }  
	            } else if (ch == '?') {  
	                //通配符问号?表示匹配任意一个字符  
	                strIndex++;  
	                if (strIndex > strLength) {  
	                    //表示str中已经没有字符匹配?了。  
	                    return false;  
	                }  
	            } else {  
	                if ((strIndex >= strLength) || (ch != str.charAt(strIndex))) {  
	                    return false;  
	                }  
	                strIndex++;  
	            }  
	        }  
	        return (strIndex == strLength);  
	    }  
	  
 
	/**
	 * 获取class所在的根目录
	 * 获取到的是class目录所在的位置:webapp/class
	 * @return
	 */
	public static String getClassRootPath(Object obj){
		//return obj.getClass().getResource("/").getPath() ;
		return FileUtils.getClassRootPath(obj.getClass());
	}
	
	/**
	 * 获取class所在的根目录
	 * 获取到的是class目录所在的位置:webapp/class
	 * @return
	 */
	public static String getClassRootPath(Class<?> clazz){
		return clazz.getResource("/").getPath() ;
	}
	
	/**
	 * 获取class文件所在的位置
	 * 如果类在jar中，也会返回位置，例如：file:/D:/apache-maven-3.5.4/repository/org/springframework/spring-core/5.0.10.RELEASE/spring-core-5.0.10.RELEASE.jar!/org/springframework/util/
	 * 如果是普通的，返回：/E:/my-workspace/leon-repository/target/test-classes/test/mawujun/jpa/
	 * @param clazz
	 * @return
	 */
	public static String getClassPath(Class<?> clazz){
		return clazz.getResource("").getPath();
	}
	/**
	 * 获取项目的绝对路径
	 * @param obj
	 * @return
	 */
	public static String getProjectPath(){
		return System.getProperty("user.dir");
//		File file = new File("");
//        String filePath=null;
//		try {
//			filePath = file.getCanonicalPath();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        System.out.println(filePath);
//        return filePath;
	}
	
	/**
	 * 获取类所在的jar路径,或者依赖的项目的路径
	 * file:/D:/apache-maven-3.5.4/repository/org/springframework/spring-core/5.0.10.RELEASE/spring-core-5.0.10.RELEASE.jar!/org/springframework/util/
	 * @param clazz
	 * @return
	 */
	public static URL getJarPath(Class<?> clazz){
		ProtectionDomain pd = clazz.getProtectionDomain();  
		CodeSource cs = pd.getCodeSource();  
		//System.out.println(cs.getLocation()); 
		return cs.getLocation();
	}
	
	/**
	 * 返回类所在的jar文件的绝对路径,D:/apache-maven-3.5.4/repository/org/springframework/spring-core/5.0.10.RELEASE/spring-core-5.0.10.RELEASE.jar
	 * 如果不是jar中的class，则返回该class所在的classpath：E:/my-workspace/leon-repository/target/test-classes/
	 * @param clazz
	 * @return
	 */
	public static String getJarAbstractPath(Class<?> clazz){
		ProtectionDomain pd = clazz.getProtectionDomain();  
		CodeSource cs = pd.getCodeSource();  
//		cs.getLocation().getPath();
//		try {
//			cs.getLocation().toURI();
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		URL url=cs.getLocation();
		if(url.getFile().indexOf(".jar!")==-1) {
			return url.getFile();
		} else {
			return url.getFile().substring(0, url.getFile().indexOf(".jar!")+4);
		}

	}
	/**
	 * 返回某个目录下面的所有文件和目录，不包括子文件夹中的文件
	 * @author mawujun email:16064988@163.com qq:16064988
	 * @param directory
	 * @return
	 */
	public static File[] listFiles(File directory){
		return directory.listFiles();
	}
	/**
	 * 返回某个目录下面的所有文件和目录，不包括子文件夹中的文件
	 * @author mawujun email:16064988@163.com qq:16064988
	 * @param directory
	 * @return
	 */
	public static File[] listFiles(String dirPath){
		return listFiles(new File(dirPath));
	}
	
	/**
     * The extension separator character.
     * @since 1.4
     */
    public static final char EXTENSION_SEPARATOR = '.';

    /**
     * The extension separator String.
     * @since 1.4
     */
    public static final String EXTENSION_SEPARATOR_STR = Character.toString(EXTENSION_SEPARATOR);

    /**
     * The Unix separator character.
     */
    private static final char UNIX_SEPARATOR = '/';

    /**
     * The Windows separator character.
     */
    private static final char WINDOWS_SEPARATOR = '\\';

    /**
     * The system separator character.
     */
    private static final char SYSTEM_SEPARATOR = File.separatorChar;

    /**
     * The separator character that is the opposite of the system separator.
     */
    private static final char OTHER_SEPARATOR;
    static {
        if (isSystemWindows()) {
            OTHER_SEPARATOR = UNIX_SEPARATOR;
        } else {
            OTHER_SEPARATOR = WINDOWS_SEPARATOR;
        }
    }
    
  //-----------------------------------------------------------------------
    /**
     * Determines if Windows file system is in use.
     * 
     * @return true if the system is Windows
     */
    static boolean isSystemWindows() {
        return SYSTEM_SEPARATOR == WINDOWS_SEPARATOR;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the character is a separator.
     * 
     * @param ch  the character to check
     * @return true if it is a separator character
     */
    private static boolean isSeparator(char ch) {
        return ch == UNIX_SEPARATOR || ch == WINDOWS_SEPARATOR;
    }
	 /**
     * Returns the index of the last directory separator character.
     * <p>
     * This method will handle a file in either Unix or Windows format.
     * The position of the last forward or backslash is returned.
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.
     * 
     * @param filename  the filename to find the last path separator in, null returns -1
     * @return the index of the last separator character, or -1 if there
     * is no such character
     */
    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }
	
	 /**
     * Returns the index of the last extension separator character, which is a dot.
     * <p>
     * This method also checks that there is no directory separator after the last dot.
     * To do this it uses {@link #indexOfLastSeparator(String)} which will
     * handle a file in either Unix or Windows format.
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.
     * 
     * @param filename  the filename to find the last path separator in, null returns -1
     * @return the index of the last separator character, or -1 if there
     * is no such character
     */
    public static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        }
        int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
        int lastSeparator = indexOfLastSeparator(filename);
        return lastSeparator > extensionPos ? -1 : extensionPos;
    }
  //-----------------------------------------------------------------------
    /**
     * Normalizes a path, removing double and single dot path steps.
     * <p>
     * This method normalizes a path to a standard format.
     * The input may contain separators in either Unix or Windows format.
     * The output will contain separators in the format of the system.
     * <p>
     * A trailing slash will be retained.
     * A double slash will be merged to a single slash (but UNC names are handled).
     * A single dot path segment will be removed.
     * A double dot will cause that path segment and the one before to be removed.
     * If the double dot has no parent path segment to work with, {@code null}
     * is returned.
     * <p>
     * The output will be the same on both Unix and Windows except
     * for the separator character.
     * <pre>
     * /foo//               -->   /foo/
     * /foo/./              -->   /foo/
     * /foo/../bar          -->   /bar
     * /foo/../bar/         -->   /bar/
     * /foo/../bar/../baz   -->   /baz
     * //foo//./bar         -->   /foo/bar
     * /../                 -->   null
     * ../foo               -->   null
     * foo/bar/..           -->   foo/
     * foo/../../bar        -->   null
     * foo/../bar           -->   bar
     * //server/foo/../bar  -->   //server/bar
     * //server/../bar      -->   null
     * C:\foo\..\bar        -->   C:\bar
     * C:\..\bar            -->   null
     * ~/foo/../bar/        -->   ~/bar/
     * ~/../bar             -->   null
     * </pre>
     * (Note the file separator returned will be correct for Windows/Unix)
     *
     * @param filename  the filename to normalize, null returns null
     * @return the normalized filename, or null if invalid
     */
    public static String normalize(String filename) {
        return doNormalize(filename, SYSTEM_SEPARATOR, true);
    }
    /**
     * Normalizes a path, removing double and single dot path steps.
     * <p>
     * This method normalizes a path to a standard format.
     * The input may contain separators in either Unix or Windows format.
     * The output will contain separators in the format specified.
     * <p>
     * A trailing slash will be retained.
     * A double slash will be merged to a single slash (but UNC names are handled).
     * A single dot path segment will be removed.
     * A double dot will cause that path segment and the one before to be removed.
     * If the double dot has no parent path segment to work with, {@code null}
     * is returned.
     * <p>
     * The output will be the same on both Unix and Windows except
     * for the separator character.
     * <pre>
     * /foo//               -->   /foo/
     * /foo/./              -->   /foo/
     * /foo/../bar          -->   /bar
     * /foo/../bar/         -->   /bar/
     * /foo/../bar/../baz   -->   /baz
     * //foo//./bar         -->   /foo/bar
     * /../                 -->   null
     * ../foo               -->   null
     * foo/bar/..           -->   foo/
     * foo/../../bar        -->   null
     * foo/../bar           -->   bar
     * //server/foo/../bar  -->   //server/bar
     * //server/../bar      -->   null
     * C:\foo\..\bar        -->   C:\bar
     * C:\..\bar            -->   null
     * ~/foo/../bar/        -->   ~/bar/
     * ~/../bar             -->   null
     * </pre>
     * The output will be the same on both Unix and Windows including
     * the separator character.
     *
     * @param filename  the filename to normalize, null returns null
     * @param unixSeparator {@code true} if a unix separator should
     * be used or {@code false} if a windows separator should be used.
     * @return the normalized filename, or null if invalid
     * @since 2.0
     */
    public static String normalize(String filename, boolean unixSeparator) {
        char separator = unixSeparator ? UNIX_SEPARATOR : WINDOWS_SEPARATOR;
        return doNormalize(filename, separator, true);
    }

    //-----------------------------------------------------------------------
    /**
     * Normalizes a path, removing double and single dot path steps,
     * and removing any final directory separator.
     * <p>
     * This method normalizes a path to a standard format.
     * The input may contain separators in either Unix or Windows format.
     * The output will contain separators in the format of the system.
     * <p>
     * A trailing slash will be removed.
     * A double slash will be merged to a single slash (but UNC names are handled).
     * A single dot path segment will be removed.
     * A double dot will cause that path segment and the one before to be removed.
     * If the double dot has no parent path segment to work with, {@code null}
     * is returned.
     * <p>
     * The output will be the same on both Unix and Windows except
     * for the separator character.
     * <pre>
     * /foo//               -->   /foo
     * /foo/./              -->   /foo
     * /foo/../bar          -->   /bar
     * /foo/../bar/         -->   /bar
     * /foo/../bar/../baz   -->   /baz
     * //foo//./bar         -->   /foo/bar
     * /../                 -->   null
     * ../foo               -->   null
     * foo/bar/..           -->   foo
     * foo/../../bar        -->   null
     * foo/../bar           -->   bar
     * //server/foo/../bar  -->   //server/bar
     * //server/../bar      -->   null
     * C:\foo\..\bar        -->   C:\bar
     * C:\..\bar            -->   null
     * ~/foo/../bar/        -->   ~/bar
     * ~/../bar             -->   null
     * </pre>
     * (Note the file separator returned will be correct for Windows/Unix)
     *
     * @param filename  the filename to normalize, null returns null
     * @return the normalized filename, or null if invalid
     */
    public static String normalizeNoEndSeparator(String filename) {
        return doNormalize(filename, SYSTEM_SEPARATOR, false);
    }

    /**
     * Normalizes a path, removing double and single dot path steps,
     * and removing any final directory separator.
     * <p>
     * This method normalizes a path to a standard format.
     * The input may contain separators in either Unix or Windows format.
     * The output will contain separators in the format specified.
     * <p>
     * A trailing slash will be removed.
     * A double slash will be merged to a single slash (but UNC names are handled).
     * A single dot path segment will be removed.
     * A double dot will cause that path segment and the one before to be removed.
     * If the double dot has no parent path segment to work with, {@code null}
     * is returned.
     * <p>
     * The output will be the same on both Unix and Windows including
     * the separator character.
     * <pre>
     * /foo//               -->   /foo
     * /foo/./              -->   /foo
     * /foo/../bar          -->   /bar
     * /foo/../bar/         -->   /bar
     * /foo/../bar/../baz   -->   /baz
     * //foo//./bar         -->   /foo/bar
     * /../                 -->   null
     * ../foo               -->   null
     * foo/bar/..           -->   foo
     * foo/../../bar        -->   null
     * foo/../bar           -->   bar
     * //server/foo/../bar  -->   //server/bar
     * //server/../bar      -->   null
     * C:\foo\..\bar        -->   C:\bar
     * C:\..\bar            -->   null
     * ~/foo/../bar/        -->   ~/bar
     * ~/../bar             -->   null
     * </pre>
     *
     * @param filename  the filename to normalize, null returns null
     * @param unixSeparator {@code true} if a unix separator should
     * be used or {@code false} if a windows separtor should be used.
     * @return the normalized filename, or null if invalid
     * @since 2.0
     */
    public static String normalizeNoEndSeparator(String filename, boolean unixSeparator) {
         char separator = unixSeparator ? UNIX_SEPARATOR : WINDOWS_SEPARATOR;
        return doNormalize(filename, separator, false);
    }

    /**
     * Internal method to perform the normalization.
     *
     * @param filename  the filename
     * @param separator The separator character to use
     * @param keepSeparator  true to keep the final separator
     * @return the normalized filename
     */
    private static String doNormalize(String filename, char separator, boolean keepSeparator) {
        if (filename == null) {
            return null;
        }
        int size = filename.length();
        if (size == 0) {
            return filename;
        }
        int prefix = getPrefixLength(filename);
        if (prefix < 0) {
            return null;
        }
        
        char[] array = new char[size + 2];  // +1 for possible extra slash, +2 for arraycopy
        filename.getChars(0, filename.length(), array, 0);
        
        // fix separators throughout
        char otherSeparator = separator == SYSTEM_SEPARATOR ? OTHER_SEPARATOR : SYSTEM_SEPARATOR;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == otherSeparator) {
                array[i] = separator;
            }
        }
        
        // add extra separator on the end to simplify code below
        boolean lastIsDirectory = true;
        if (array[size - 1] != separator) {
            array[size++] = separator;
            lastIsDirectory = false;
        }
        
        // adjoining slashes
        for (int i = prefix + 1; i < size; i++) {
            if (array[i] == separator && array[i - 1] == separator) {
                System.arraycopy(array, i, array, i - 1, size - i);
                size--;
                i--;
            }
        }
        
        // dot slash
        for (int i = prefix + 1; i < size; i++) {
            if (array[i] == separator && array[i - 1] == '.' &&
                    (i == prefix + 1 || array[i - 2] == separator)) {
                if (i == size - 1) {
                    lastIsDirectory = true;
                }
                System.arraycopy(array, i + 1, array, i - 1, size - i);
                size -=2;
                i--;
            }
        }
        
        // double dot slash
        outer:
        for (int i = prefix + 2; i < size; i++) {
            if (array[i] == separator && array[i - 1] == '.' && array[i - 2] == '.' &&
                    (i == prefix + 2 || array[i - 3] == separator)) {
                if (i == prefix + 2) {
                    return null;
                }
                if (i == size - 1) {
                    lastIsDirectory = true;
                }
                int j;
                for (j = i - 4 ; j >= prefix; j--) {
                    if (array[j] == separator) {
                        // remove b/../ from a/b/../c
                        System.arraycopy(array, i + 1, array, j + 1, size - i);
                        size -= i - j;
                        i = j + 1;
                        continue outer;
                    }
                }
                // remove a/../ from a/../c
                System.arraycopy(array, i + 1, array, prefix, size - i);
                size -= i + 1 - prefix;
                i = prefix + 1;
            }
        }
        
        if (size <= 0) {  // should never be less than 0
            return "";
        }
        if (size <= prefix) {  // should never be less than prefix
            return new String(array, 0, size);
        }
        if (lastIsDirectory && keepSeparator) {
            return new String(array, 0, size);  // keep trailing separator
        }
        return new String(array, 0, size - 1);  // lose trailing separator
    }
    //-----------------------------------------------------------------------
    /**
     * Returns the length of the filename prefix, such as <code>C:/</code> or <code>~/</code>.
     * <p>
     * This method will handle a file in either Unix or Windows format.
     * <p>
     * The prefix length includes the first slash in the full filename
     * if applicable. Thus, it is possible that the length returned is greater
     * than the length of the input string.
     * <pre>
     * Windows:
     * a\b\c.txt           --> ""          --> relative
     * \a\b\c.txt          --> "\"         --> current drive absolute
     * C:a\b\c.txt         --> "C:"        --> drive relative
     * C:\a\b\c.txt        --> "C:\"       --> absolute
     * \\server\a\b\c.txt  --> "\\server\" --> UNC
     *
     * Unix:
     * a/b/c.txt           --> ""          --> relative
     * /a/b/c.txt          --> "/"         --> absolute
     * ~/a/b/c.txt         --> "~/"        --> current user
     * ~                   --> "~/"        --> current user (slash added)
     * ~user/a/b/c.txt     --> "~user/"    --> named user
     * ~user               --> "~user/"    --> named user (slash added)
     * </pre>
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.
     * ie. both Unix and Windows prefixes are matched regardless.
     *
     * @param filename  the filename to find the prefix in, null returns -1
     * @return the length of the prefix, -1 if invalid or null
     */
    public static int getPrefixLength(String filename) {
        if (filename == null) {
            return -1;
        }
        int len = filename.length();
        if (len == 0) {
            return 0;
        }
        char ch0 = filename.charAt(0);
        if (ch0 == ':') {
            return -1;
        }
        if (len == 1) {
            if (ch0 == '~') {
                return 2;  // return a length greater than the input
            }
            return isSeparator(ch0) ? 1 : 0;
        } else {
            if (ch0 == '~') {
                int posUnix = filename.indexOf(UNIX_SEPARATOR, 1);
                int posWin = filename.indexOf(WINDOWS_SEPARATOR, 1);
                if (posUnix == -1 && posWin == -1) {
                    return len + 1;  // return a length greater than the input
                }
                posUnix = posUnix == -1 ? posWin : posUnix;
                posWin = posWin == -1 ? posUnix : posWin;
                return Math.min(posUnix, posWin) + 1;
            }
            char ch1 = filename.charAt(1);
            if (ch1 == ':') {
                ch0 = Character.toUpperCase(ch0);
                if (ch0 >= 'A' && ch0 <= 'Z') {
                    if (len == 2 || isSeparator(filename.charAt(2)) == false) {
                        return 2;
                    }
                    return 3;
                }
                return -1;
                
            } else if (isSeparator(ch0) && isSeparator(ch1)) {
                int posUnix = filename.indexOf(UNIX_SEPARATOR, 2);
                int posWin = filename.indexOf(WINDOWS_SEPARATOR, 2);
                if (posUnix == -1 && posWin == -1 || posUnix == 2 || posWin == 2) {
                    return -1;
                }
                posUnix = posUnix == -1 ? posWin : posUnix;
                posWin = posWin == -1 ? posUnix : posWin;
                return Math.min(posUnix, posWin) + 1;
            } else {
                return isSeparator(ch0) ? 1 : 0;
            }
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Concatenates a filename to a base path using normal command line style rules.
     * <p>
     * The effect is equivalent to resultant directory after changing
     * directory to the first argument, followed by changing directory to
     * the second argument.
     * <p>
     * The first argument is the base path, the second is the path to concatenate.
     * The returned path is always normalized via {@link #normalize(String)},
     * thus <code>..</code> is handled.
     * <p>
     * If <code>pathToAdd</code> is absolute (has an absolute prefix), then
     * it will be normalized and returned.
     * Otherwise, the paths will be joined, normalized and returned.
     * <p>
     * The output will be the same on both Unix and Windows except
     * for the separator character.
     * <pre>
     * /foo/ + bar          -->   /foo/bar
     * /foo + bar           -->   /foo/bar
     * /foo + /bar          -->   /bar
     * /foo + C:/bar        -->   C:/bar
     * /foo + C:bar         -->   C:bar (*)
     * /foo/a/ + ../bar     -->   foo/bar
     * /foo/ + ../../bar    -->   null
     * /foo/ + /bar         -->   /bar
     * /foo/.. + /bar       -->   /bar
     * /foo + bar/c.txt     -->   /foo/bar/c.txt
     * /foo/c.txt + bar     -->   /foo/c.txt/bar (!)
     * </pre>
     * (*) Note that the Windows relative drive prefix is unreliable when
     * used with this method.
     * (!) Note that the first parameter must be a path. If it ends with a name, then
     * the name will be built into the concatenated path. If this might be a problem,
     * use {@link #getFullPath(String)} on the base path argument.
     *
     * @param basePath  the base path to attach to, always treated as a path
     * @param fullFilenameToAdd  the filename (or path) to attach to the base
     * @return the concatenated path, or null if invalid
     */
    public static String concat(String basePath, String fullFilenameToAdd) {
        int prefix = getPrefixLength(fullFilenameToAdd);
        if (prefix < 0) {
            return null;
        }
        if (prefix > 0) {
            return normalize(fullFilenameToAdd);
        }
        if (basePath == null) {
            return null;
        }
        int len = basePath.length();
        if (len == 0) {
            return normalize(fullFilenameToAdd);
        }
        char ch = basePath.charAt(len - 1);
        if (isSeparator(ch)) {
            return normalize(basePath + fullFilenameToAdd);
        } else {
            return normalize(basePath + '/' + fullFilenameToAdd);
        }
    }
    /**
     * Gets the name minus the path from a full filename.
     * <p>
     * This method will handle a file in either Unix or Windows format.
     * The text after the last forward or backslash is returned.
     * <pre>
     * a/b/c.txt --> c.txt
     * a.txt     --> a.txt
     * a/b/c     --> c
     * a/b/c/    --> ""
     * </pre>
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename  the filename to query, null returns null
     * @return the name of the file without the path, or an empty string if none exists
     */
    public static String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfLastSeparator(filename);
        return filename.substring(index + 1);
    }

    /**
     * Gets the base name, minus the full path and extension, from a full filename.
     * <p>
     * This method will handle a file in either Unix or Windows format.
     * The text after the last forward or backslash and before the last dot is returned.
     * <pre>
     * a/b/c.txt --> c
     * a.txt     --> a
     * a/b/c     --> c
     * a/b/c/    --> ""
     * </pre>
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename  the filename to query, null returns null
     * @return the name of the file without the path, or an empty string if none exists
     */
    public static String getBaseName(String filename) {
        return removeExtension(getName(filename));
    }

    /**
     * 获取文件的后缀名
     * Gets the extension of a filename.
     * <p>
     * This method returns the textual part of the filename after the last dot.
     * There must be no directory separator after the dot.
     * <pre>
     * foo.txt      --> "txt"
     * a/b/c.jpg    --> "jpg"
     * a/b.txt/c    --> ""
     * a/b/c        --> ""
     * </pre>
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename the filename to retrieve the extension of.
     * @return the extension of the file or an empty string if none exists or {@code null}
     * if the filename is {@code null}.
     */
    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Removes the extension from a filename.
     * <p>
     * This method returns the textual part of the filename before the last dot.
     * There must be no directory separator after the dot.
     * <pre>
     * foo.txt    --> foo
     * a\b\c.jpg  --> a\b\c
     * a\b\c      --> a\b\c
     * a.b\c      --> a.b\c
     * </pre>
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename  the filename to query, null returns null
     * @return the filename minus the extension
     */
    public static String removeExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return filename;
        } else {
            return filename.substring(0, index);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Checks whether two filenames are equal exactly.
     * <p>
     * No processing is performed on the filenames other than comparison,
     * thus this is merely a null-safe case-sensitive equals.
     *
     * @param filename1  the first filename to query, may be null
     * @param filename2  the second filename to query, may be null
     * @return true if the filenames are equal, null equals null
     * @see IOCase#SENSITIVE
     */
    public static boolean equals(String filename1, String filename2) {
        return equals(filename1, filename2, false, IOCase.SENSITIVE);
    }

    /**
     * Checks whether two filenames are equal using the case rules of the system.
     * <p>
     * No processing is performed on the filenames other than comparison.
     * The check is case-sensitive on Unix and case-insensitive on Windows.
     *
     * @param filename1  the first filename to query, may be null
     * @param filename2  the second filename to query, may be null
     * @return true if the filenames are equal, null equals null
     * @see IOCase#SYSTEM
     */
    public static boolean equalsOnSystem(String filename1, String filename2) {
        return equals(filename1, filename2, false, IOCase.SYSTEM);
    }

    //-----------------------------------------------------------------------
    /**
     * Checks whether two filenames are equal after both have been normalized.
     * <p>
     * Both filenames are first passed to {@link #normalize(String)}.
     * The check is then performed in a case-sensitive manner.
     *
     * @param filename1  the first filename to query, may be null
     * @param filename2  the second filename to query, may be null
     * @return true if the filenames are equal, null equals null
     * @see IOCase#SENSITIVE
     */
    public static boolean equalsNormalized(String filename1, String filename2) {
        return equals(filename1, filename2, true, IOCase.SENSITIVE);
    }

    /**
     * Checks whether two filenames are equal after both have been normalized
     * and using the case rules of the system.
     * <p>
     * Both filenames are first passed to {@link #normalize(String)}.
     * The check is then performed case-sensitive on Unix and
     * case-insensitive on Windows.
     *
     * @param filename1  the first filename to query, may be null
     * @param filename2  the second filename to query, may be null
     * @return true if the filenames are equal, null equals null
     * @see IOCase#SYSTEM
     */
    public static boolean equalsNormalizedOnSystem(String filename1, String filename2) {
        return equals(filename1, filename2, true, IOCase.SYSTEM);
    }

    /**
     * Checks whether two filenames are equal, optionally normalizing and providing
     * control over the case-sensitivity.
     *
     * @param filename1  the first filename to query, may be null
     * @param filename2  the second filename to query, may be null
     * @param normalized  whether to normalize the filenames
     * @param caseSensitivity  what case sensitivity rule to use, null means case-sensitive
     * @return true if the filenames are equal, null equals null
     * @since 1.3
     */
    public static boolean equals(
            String filename1, String filename2,
            boolean normalized, IOCase caseSensitivity) {
        
        if (filename1 == null || filename2 == null) {
            return filename1 == null && filename2 == null;
        }
        if (normalized) {
            filename1 = normalize(filename1);
            filename2 = normalize(filename2);
            if (filename1 == null || filename2 == null) {
                throw new NullPointerException(
                    "Error normalizing one or both of the file names");
            }
        }
        if (caseSensitivity == null) {
            caseSensitivity = IOCase.SENSITIVE;
        }
        return caseSensitivity.checkEquals(filename1, filename2);
    }

    //-----------------------------------------------------------------------
    /**
     * Checks whether the extension of the filename is that specified.
     * <p>
     * This method obtains the extension as the textual part of the filename
     * after the last dot. There must be no directory separator after the dot.
     * The extension check is case-sensitive on all platforms.
     *
     * @param filename  the filename to query, null returns false
     * @param extension  the extension to check for, null or empty checks for no extension
     * @return true if the filename has the specified extension
     */
    public static boolean isExtension(String filename, String extension) {
        if (filename == null) {
            return false;
        }
        if (extension == null || extension.length() == 0) {
            return indexOfExtension(filename) == -1;
        }
        String fileExt = getExtension(filename);
        return fileExt.equals(extension);
    }
    /**
     * Checks whether the extension of the filename is one of those specified.
     * <p>
     * This method obtains the extension as the textual part of the filename
     * after the last dot. There must be no directory separator after the dot.
     * The extension check is case-sensitive on all platforms.
     *
     * @param filename  the filename to query, null returns false
     * @param extensions  the extensions to check for, null checks for no extension
     * @return true if the filename is one of the extensions
     */
    public static boolean isExtension(String filename, String[] extensions) {
        if (filename == null) {
            return false;
        }
        if (extensions == null || extensions.length == 0) {
            return indexOfExtension(filename) == -1;
        }
        String fileExt = getExtension(filename);
        for (String extension : extensions) {
            if (fileExt.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the extension of the filename is one of those specified.
     * <p>
     * This method obtains the extension as the textual part of the filename
     * after the last dot. There must be no directory separator after the dot.
     * The extension check is case-sensitive on all platforms.
     *
     * @param filename  the filename to query, null returns false
     * @param extensions  the extensions to check for, null checks for no extension
     * @return true if the filename is one of the extensions
     */
    public static boolean isExtension(String filename, Collection<String> extensions) {
        if (filename == null) {
            return false;
        }
        if (extensions == null || extensions.isEmpty()) {
            return indexOfExtension(filename) == -1;
        }
        String fileExt = getExtension(filename);
        for (String extension : extensions) {
            if (fileExt.equals(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 创建目录
     * @author mawujun email:16064988@163.com qq:16064988
     * @param path
     * @throws IOException 
     */
    public static boolean createDir(String path) throws IOException {
    	File directory = new File(path);
    	if (directory.exists()) {
            if (!directory.isDirectory()) {
                String message =
                    "File "
                        + directory
                        + " exists and is "
                        + "not a directory. Unable to create directory.";
                throw new IOException(message);
            }
        } else {
            if (!directory.mkdirs()) {
                // Double-check that some other thread or process hasn't made
                // the directory in the background
                if (!directory.isDirectory())
                {
                    String message =
                        "Unable to create directory " + directory;
                    throw new IOException(message);
                }
            }
        }
    	return true;
    }
    
    /**
     * 创建临时文件
     *
     * @param prefix
     *            临时文件名的前缀
     * @param suffix
     *            临时文件名的后缀
     * @param dirName
     *            临时文件所在的目录，如果输入null，则在用户的文档目录下创建临时文件
     * @return 临时文件创建成功返回true，否则返回false
     */
    public static String createTempFile(String prefix, String suffix,
            String dirName) {
        File tempFile = null;
        if (dirName == null) {
            try {
                // 在默认文件夹下创建临时文件
                tempFile = File.createTempFile(prefix, suffix);
                // 返回临时文件的路径
                return tempFile.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("创建临时文件失败!" + e.getMessage());
                return null;
            }
        } else {
        	try {
            File dir = new File(dirName);
            // 如果临时文件所在目录不存在，首先创建
            if (!dir.exists()) {
                if (FileUtils.createDir(dirName)) {
                    System.out.println("创建临时文件失败，不能创建临时文件所在的目录！");
                    return null;
                }
            }
            
                // 在指定目录下创建临时文件
                tempFile = File.createTempFile(prefix, suffix, dir);
                return tempFile.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("创建临时文件失败!" + e.getMessage());
                return null;
            }
        }
    }

    /**
     * 创建文件
     * @author mawujun email:16064988@163.com qq:16064988
     * @param path
     */
    public static boolean createFile(String path){
    	File file = new File(path);
    	try {
			return file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
    }
    /** @return true if file exists  */
   	public static boolean isExists(File file) {
   		return file.exists();
   	}
    /** @return true if file exists  */
   	public static boolean isExists(String path) {
   		File file=new File(path);
   		return file.exists();
   	}
    /** @return true if file exists and is a zip file */
	public static boolean isZipFile(File file) {
		try {
			return (null != file) && new ZipFile(file) != null;
		} catch (IOException e) {
			return false;
		}
	}
	/** @return true if path ends with .zip or .jar */
	// public static boolean hasZipSuffix(String path) {
	// return ((null != path) && (0 != zipSuffixLength(path)));
	// }
	/** @return 0 if file has no zip/jar suffix or 4 otherwise */
	public static int zipSuffixLength(File file) {
		return (null == file ? 0 : zipSuffixLength(file.getPath()));
	}

	/** @return 0 if no zip/jar suffix or 4 otherwise */
	public static int zipSuffixLength(String path) {
		if ((null != path) && (4 < path.length())) {
			String test = path.substring(path.length() - 4).toLowerCase();
			if (".zip".equals(test) || ".jar".equals(test)) {
				return 4;
			}
		}
		return 0;
	}
	/**
	 * 删除文件
	 * @author mawujun email:16064988@163.com qq:16064988
	 * @param path
	 * @return
	 */
	 public static boolean deleteFile(String path) {
		 File file=new File(path);
		 if (file.isDirectory()) {
	            String message = file + " is not a directory";
	            throw new IllegalArgumentException(message);
	        }
		 if(file.exists()){
			 return file.delete();
		 }
		 return true;
	 }
	 
//	 /** do line-based copying */
//		@SuppressWarnings("deprecation")
//		public static void copyStream(DataInputStream in, PrintStream out) throws IOException {
//			//LangUtil.throwIaxIfNull(in, "in");
//			//LangUtil.throwIaxIfNull(in, "out");
//			String s;
//			while (null != (s = in.readLine())) {
//				out.println(s);
//			}
//		}

		public static void copyStream(InputStream in, OutputStream out) throws IOException {
			final int MAX = 4096;
			byte[] buf = new byte[MAX];
			for (int bytesRead = in.read(buf, 0, MAX); bytesRead != -1; bytesRead = in.read(buf, 0, MAX)) {
				out.write(buf, 0, bytesRead);
			}
		}

		public static void copyStream(Reader in, Writer out) throws IOException {
			final int MAX = 4096;
			char[] buf = new char[MAX];
			for (int bytesRead = in.read(buf, 0, MAX); bytesRead != -1; bytesRead = in.read(buf, 0, MAX)) {
				out.write(buf, 0, bytesRead);
			}
		}
	 /**
	/**
	 *
	 * d把字符串写到文件当中
	 * 
	 * @param file the File to write (not null)
	 * @param contents the String to write (use "" if null)
	 * @return String null on no error, error otherwise
	 */
	public static String writeAsString(File file, String contents) {
		//LangUtil.throwIaxIfNull(file, "file");
		if (null == contents) {
			contents = "";
		}
		Writer out = null;
		try {
			File parentDir = file.getParentFile();
			if (!parentDir.exists() && !parentDir.mkdirs()) {
				return "unable to make parent dir for " + file;
			}
			Reader in = new StringReader(contents);
			out = new FileWriter(file);
			//FileUtil.copyStream(in, out);
			FileUtils.copyStream(in, out);
			return null;
		} catch (IOException e) {
			return " writing " + file + ": " + e.getMessage();
		} finally {
			if (null != out) {
				try {
					out.close();
				} catch (IOException e) {
				} // ignored
			}
		}
	}
	

	/**
	 * Reads a boolean array with our encoding
	 */
	public static boolean[] readBooleanArray(DataInputStream s) throws IOException {
		int len = s.readInt();
		boolean[] ret = new boolean[len];
		for (int i = 0; i < len; i++) {
			ret[i] = s.readBoolean();
		}
		return ret;
	}

	/**
	 * Writes a boolean array with our encoding
	 */
	public static void writeBooleanArray(boolean[] a, DataOutputStream s) throws IOException {
		int len = a.length;
		s.writeInt(len);
		for (int i = 0; i < len; i++) {
			s.writeBoolean(a[i]);
		}
	}

	/**
	 * Reads an int array with our encoding
	 */
	public static int[] readIntArray(DataInputStream s) throws IOException {
		int len = s.readInt();
		int[] ret = new int[len];
		for (int i = 0; i < len; i++) {
			ret[i] = s.readInt();
		}
		return ret;
	}

	/**
	 * Writes an int array with our encoding
	 */
	public static void writeIntArray(int[] a, DataOutputStream s) throws IOException {
		int len = a.length;
		s.writeInt(len);
		for (int i = 0; i < len; i++) {
			s.writeInt(a[i]);
		}
	}

	/**
	 * Reads an int array with our encoding
	 */
	public static String[] readStringArray(DataInputStream s) throws IOException {
		int len = s.readInt();
		String[] ret = new String[len];
		for (int i = 0; i < len; i++) {
			ret[i] = s.readUTF();
		}
		return ret;
	}

	/**
	 * Writes an int array with our encoding
	 */
	public static void writeStringArray(String[] a, DataOutputStream s) throws IOException {
		if (a == null) {
			s.writeInt(0);
			return;
		}
		int len = a.length;
		s.writeInt(len);
		for (int i = 0; i < len; i++) {
			s.writeUTF(a[i]);
		}
	}

	/**
	 * Returns the contents of this file as a String
	 */
	public static String readAsString(File file) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(file));
		StringBuffer b = new StringBuffer();
		while (true) {
			int ch = r.read();
			if (ch == -1) {
				break;
			}
			b.append((char) ch);
		}
		r.close();
		return b.toString();
	}

	// /**
	// * Returns the contents of this stream as a String
	// */
	// public static String readAsString(InputStream in) throws IOException {
	// BufferedReader r = new BufferedReader(new InputStreamReader(in));
	// StringBuffer b = new StringBuffer();
	// while (true) {
	// int ch = r.read();
	// if (ch == -1)
	// break;
	// b.append((char) ch);
	// }
	// in.close();
	// r.close();
	// return b.toString();
	// }

	/**
	 * Returns the contents of this file as a byte[]
	 */
	public static byte[] readAsByteArray(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		int size = 1024;
		byte[] ba = new byte[size];
		int readSoFar = 0;

		while (true) {
			int nRead = in.read(ba, readSoFar, size - readSoFar);
			if (nRead == -1) {
				break;
			}
			readSoFar += nRead;
			if (readSoFar == size) {
				int newSize = size * 2;
				byte[] newBa = new byte[newSize];
				System.arraycopy(ba, 0, newBa, 0, size);
				ba = newBa;
				size = newSize;
			}
		}

		byte[] newBa = new byte[readSoFar];
		System.arraycopy(ba, 0, newBa, 0, readSoFar);
		in.close();
		return newBa;
	}

	/**
	 * Reads this input stream and returns contents as a byte[]
	 */
	public static byte[] readAsByteArray(InputStream inStream) throws IOException {
		int size = 1024;
		byte[] ba = new byte[size];
		int readSoFar = 0;

		while (true) {
			int nRead = inStream.read(ba, readSoFar, size - readSoFar);
			if (nRead == -1) {
				break;
			}
			readSoFar += nRead;
			if (readSoFar == size) {
				int newSize = size * 2;
				byte[] newBa = new byte[newSize];
				System.arraycopy(ba, 0, newBa, 0, size);
				ba = newBa;
				size = newSize;
			}
		}

		byte[] newBa = new byte[readSoFar];
		System.arraycopy(ba, 0, newBa, 0, readSoFar);
		return newBa;
	}


	 //-----------------------------------------------------------------------
    /**
     * Copies a file to a directory preserving the file date.
     * <p>
     * This method copies the contents of the specified source file
     * to a file of the same name in the specified destination directory.
     * The destination directory is created if it does not exist.
     * If the destination file exists, then this method will overwrite it.
     * <p>
     * <strong>Note:</strong> This method tries to preserve the file's last
     * modified date/times using {@link File#setLastModified(long)}, however
     * it is not guaranteed that the operation will succeed.
     * If the modification operation fails, no indication is provided.
     *
     * @param srcFile  an existing file to copy, must not be {@code null}
     * @param destDir  the directory to place the copy in, must not be {@code null}
     *
     * @throws NullPointerException if source or destination is null
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @see #copyFile(File, File, boolean)
     */
    public static void copyFileToDirectory(File srcFile, File destDir) throws IOException {
        copyFileToDirectory(srcFile, destDir, true);
    }

    /**
     * Copies a file to a directory optionally preserving the file date.
     * <p>
     * This method copies the contents of the specified source file
     * to a file of the same name in the specified destination directory.
     * The destination directory is created if it does not exist.
     * If the destination file exists, then this method will overwrite it.
     * <p>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
     * {@code true} tries to preserve the file's last modified
     * date/times using {@link File#setLastModified(long)}, however it is
     * not guaranteed that the operation will succeed.
     * If the modification operation fails, no indication is provided.
     *
     * @param srcFile  an existing file to copy, must not be {@code null}
     * @param destDir  the directory to place the copy in, must not be {@code null}
     * @param preserveFileDate  true if the file date of the copy
     *  should be the same as the original
     *
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @see #copyFile(File, File, boolean)
     * @since 1.3
     */
    public static void copyFileToDirectory(File srcFile, File destDir, boolean preserveFileDate) throws IOException {
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (destDir.exists() && destDir.isDirectory() == false) {
            throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
        }
        File destFile = new File(destDir, srcFile.getName());
        copyFile(srcFile, destFile, preserveFileDate);
    }

    /**
     * Copies a file to a new location preserving the file date.
     * <p>
     * This method copies the contents of the specified source file to the
     * specified destination file. The directory holding the destination file is
     * created if it does not exist. If the destination file exists, then this
     * method will overwrite it.
     * <p>
     * <strong>Note:</strong> This method tries to preserve the file's last
     * modified date/times using {@link File#setLastModified(long)}, however
     * it is not guaranteed that the operation will succeed.
     * If the modification operation fails, no indication is provided.
     * 
     * @param srcFile  an existing file to copy, must not be {@code null}
     * @param destFile  the new file, must not be {@code null}
     * 
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @see #copyFileToDirectory(File, File)
     */
    public static void copyFile(File srcFile, File destFile) throws IOException {
        copyFile(srcFile, destFile, true);
    }

    /**
     * Copies a file to a new location.
     * <p>
     * This method copies the contents of the specified source file
     * to the specified destination file.
     * The directory holding the destination file is created if it does not exist.
     * If the destination file exists, then this method will overwrite it.
     * <p>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
     * {@code true} tries to preserve the file's last modified
     * date/times using {@link File#setLastModified(long)}, however it is
     * not guaranteed that the operation will succeed.
     * If the modification operation fails, no indication is provided.
     *
     * @param srcFile  an existing file to copy, must not be {@code null}
     * @param destFile  the new file, must not be {@code null}
     * @param preserveFileDate  true if the file date of the copy
     *  should be the same as the original
     *
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @see #copyFileToDirectory(File, File, boolean)
     */
    public static void copyFile(File srcFile, File destFile,
            boolean preserveFileDate) throws IOException {
        if (srcFile == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destFile == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (srcFile.exists() == false) {
            throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
        }
        if (srcFile.isDirectory()) {
            throw new IOException("Source '" + srcFile + "' exists but is a directory");
        }
        if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
            throw new IOException("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
        }
        File parentFile = destFile.getParentFile();
        if (parentFile != null) {
            if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
                throw new IOException("Destination '" + parentFile + "' directory cannot be created");
            }
        }
        if (destFile.exists() && destFile.canWrite() == false) {
            throw new IOException("Destination '" + destFile + "' exists but is read-only");
        }
        doCopyFile(srcFile, destFile, preserveFileDate);
    }
    /**
     * Internal copy file method.
     * 
     * @param srcFile  the validated source file, must not be {@code null}
     * @param destFile  the validated destination file, must not be {@code null}
     * @param preserveFileDate  whether to preserve the file date
     * @throws IOException if an error occurs
     */
    private static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel input = null;
        FileChannel output = null;
        try {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(destFile);
            input  = fis.getChannel();
            output = fos.getChannel();
            long size = input.size();
            long pos = 0;
            long count = 0;
            while (pos < size) {
                count = size - pos > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : size - pos;
                pos += output.transferFrom(input, pos, count);
            }
        } finally {
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(fis);
        }

        if (srcFile.length() != destFile.length()) {
            throw new IOException("Failed to copy full contents from '" +
                    srcFile + "' to '" + destFile + "'");
        }
        if (preserveFileDate) {
            destFile.setLastModified(srcFile.lastModified());
        }
    }
    /**
     * Returns the path to the system temporary directory.
     * 
     * @return the path to the system temporary directory.
     * 
     * @since 2.0
     */
    public static String getTempDirectoryPath() {
        return System.getProperty("java.io.tmpdir");
    }
    
    /**
     * Returns a {@link File} representing the system temporary directory.
     * 
     * @return the system temporary directory. 
     * 
     * @since 2.0
     */
    public static File getTempDirectory() {
        return new File(getTempDirectoryPath());
    }
}
