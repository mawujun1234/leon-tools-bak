package com.mawujun.reflect;

import com.mawujun.collection.CollectionUtil;
import com.mawujun.convert.Convert;
import com.mawujun.exception.UtilException;
import com.mawujun.io.FileUtil;
import com.mawujun.lang.Assert;
import com.mawujun.lang.Filter;
import com.mawujun.lang.SimpleCache;
import com.mawujun.util.ArrayUtil;
import com.mawujun.util.ClassUtil;
import com.mawujun.util.StrUtil;
import com.mawujun.util.StringUtil;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * 反射工具类
 * 
 * @author mawujun
 * 
 */
public class ReflectUtil {
	private static Logger logger=LoggerFactory.getLogger(FileUtil.class);
	
	/** 构造对象缓存 */
	private static final SimpleCache<Class<?>, Constructor<?>[]> CONSTRUCTORS_CACHE = new SimpleCache<>();
	/** 字段缓存 */
	private static final SimpleCache<Class<?>, Field[]> FIELDS_CACHE = new SimpleCache<>();
	/** 方法缓存 */
	private static final SimpleCache<Class<?>, Method[]> METHODS_CACHE = new SimpleCache<>();

	// --------------------------------------------------------------------------------------------------------- Constructor
	/**
	 * 查找类中的指定参数的构造方法，如果找到构造方法，会自动设置可访问为true
	 * 
	 * @param <T> 对象类型
	 * @param clazz 类
	 * @param parameterTypes 参数类型，只要任何一个参数是指定参数的父类或接口或相等即可，此参数可以不传
	 * @return 构造方法，如果未找到返回null
	 */
	@SuppressWarnings("unchecked")
	public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
		if (null == clazz) {
			return null;
		}

		final Constructor<?>[] constructors = getConstructors(clazz);
		Class<?>[] pts;
		for (Constructor<?> constructor : constructors) {
			pts = constructor.getParameterTypes();
			if (ClassUtil.isAllAssignableFrom(pts, parameterTypes)) {
				// 构造可访问
				constructor.setAccessible(true);
				return (Constructor<T>) constructor;
			}
		}
		return null;
	}

	/**
	 * 获得一个类中所有构造列表
	 * 
	 * @param <T> 构造的对象类型
	 * @param beanClass 类
	 * @return 字段列表
	 * @throws SecurityException 安全检查异常
	 */
	@SuppressWarnings("unchecked")
	public static <T> Constructor<T>[] getConstructors(Class<T> beanClass) throws SecurityException {
		Assert.notNull(beanClass);
		Constructor<?>[] constructors = CONSTRUCTORS_CACHE.get(beanClass);
		if (null != constructors) {
			return (Constructor<T>[]) constructors;
		}

		constructors = getConstructorsDirectly(beanClass);
		return (Constructor<T>[]) CONSTRUCTORS_CACHE.put(beanClass, constructors);
	}

	/**
	 * 获得一个类中所有字段列表，直接反射获取，无缓存
	 * 
	 * @param beanClass 类
	 * @return 字段列表
	 * @throws SecurityException 安全检查异常
	 */
	public static Constructor<?>[] getConstructorsDirectly(Class<?> beanClass) throws SecurityException {
		Assert.notNull(beanClass);
		return beanClass.getDeclaredConstructors();
	}
	//===============================================================================
	/**
	 * 是不是包装类型
	 * @param clz
	 * @return
	 */
	public static boolean isWrapClass(Object value) { 
        try { 
           Class clz=value.getClass();
           return ((Class) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) { 
            return false; 
        } 
    } 

	public static boolean isWrapClass(Class clz) {
		try {
			return ((Class) clz.getField("TYPE").get(null)).isPrimitive();
		} catch (Exception e) {
			return false;
		}
	}
	
	
	/**
	 * 如果是基本类型和基本类型的包装类，或是String，Date，就返回true
	 * @param value
	 * @return
	 */
	public static boolean isBaseType(Object value){
		//如果是基本类型就返回true
		if(value instanceof String || value instanceof Date || value.getClass().isPrimitive() || isWrapClass(value.getClass())){
			return true;
		}
		return false;
	}
	
	/**
	 * 如果是基本类型和基本类型的包装类，或是string，Date，就返回true
	 * @param clz
	 * @return
	 */
	public static boolean isBaseType(Class clz){
		//如果是基本类型就返回true
		if(clz == String.class || clz==Date.class || clz==java.sql.Date.class || clz==java.sql.Timestamp.class || clz.isPrimitive() || isWrapClass(clz)){
			return true;
		}
		return false;
	}
	/**
	 * 判断是不是集合类型
	 * @param value
	 * @return
	 */
	public static boolean  isCollectionMap(Object value){
		if(Collection.class.isInstance(value) || Map.class.isInstance(value)){
			return true;
		}
		return false;
	}
	/**
	 * 判断当前类型是否为数组
	 * 
	 * @return true of false
	 */
	public static boolean isArray(Object obj) {
		return obj.getClass().isArray();
	}
	/**
	 * 判断当前类型是否为容器，包括 Map，Collection, 以及数组
	 * 
	 * @return true of false
	 */
	public static boolean isContainer(Object obj) {
		return isColl(obj) || isMap(obj);
	}
	/**
	 * @return 当前类型是否是数组或者集合
	 */
	public static boolean isColl(Object obj) {
		return isArray(obj) || isCollection(obj);
	}
	/**
	 * 判断当前类型是否为 Map
	 * 
	 * @return true of false
	 */
	public static boolean isMap(Object obj) {
		return isOf(obj,Map.class);
	}
	/**
	 * 判断当前类型是否为 Collection
	 * 
	 * @return true of false
	 */
	public static boolean isCollection(Object obj) {
		return isOf(obj,Collection.class);
	}
	/**
	 * @param type
	 *            类型或接口名
	 * @return 当前对象是否为一个类型的子类，或者一个接口的实现类
	 */
	public static boolean isOf(Object obj,Class<?> type) {
		return type.isAssignableFrom(obj.getClass());
	}
	
	/**
	 * 判断当前对象是否为一个类型。精确匹配，即使是父类和接口，也不相等
	 * 
	 * @param type
	 *            类型
	 * @return 是否相等
	 */
	public static boolean is(Object obj,Class<?> type) {
		return null != type && obj.getClass() == type;
	}
	
	
	/**
	 * @return 当前对象是否为字符串
	 */
	public static boolean isString(Object obj) {
		return is(obj,String.class);
	}

	/**
	 * @return 当前对象是否为CharSequence的子类
	 */
	public static boolean isStringLike(Object obj) {
		return CharSequence.class.isAssignableFrom(obj.getClass());
	}

	/**
	 * @return 当前对象是否为字符
	 */
	public static boolean isChar(Object obj) {
		return is(obj,char.class) || is(obj,Character.class);
	}

	/**
	 * @return 当前对象是否为枚举
	 */
	public static boolean isEnum(Object obj) {
		return obj.getClass().isEnum();
	}

	/**
	 * @return 当前对象是否为布尔
	 */
	public static boolean isBoolean(Object obj) {
		return is(obj,boolean.class) || is(obj,Boolean.class);
	}

	/**
	 * @return 当前对象是否为浮点
	 */
	public static boolean isFloat(Object obj) {
		return is(obj,float.class) || is(obj,Float.class);
	}

	/**
	 * @return 当前对象是否为双精度浮点
	 */
	public static boolean isDouble(Object obj) {
		return is(obj,double.class) || is(obj,Double.class);
	}

	/**
	 * @return 当前对象是否为整型
	 */
	public static boolean isInt(Object obj) {
		return is(obj,int.class) || is(obj,Integer.class);
	}

	/**
	 * @return 当前对象是否为整数（包括 int, long, short, byte）
	 */
	public static boolean isIntLike(Object obj) {
		return isInt(obj) || isLong(obj) || isShort(obj) || isByte(obj) || is(obj,BigDecimal.class);
	}
	
	public static boolean isBigDecimal(Object obj) {
		return is(obj,BigDecimal.class);
	}

	/**
	 * @return 当前类型是不是接口
	 */
	public static boolean isInterface(Object obj) {
		return null == obj ? null : obj.getClass().isInterface();
	}

	/**
	 * @return 当前对象是否为小数 (float, dobule)
	 */
	public static boolean isDecimal(Object obj) {
		return isFloat(obj) || isDouble(obj);
	}

	/**
	 * @return 当前对象是否为长整型
	 */
	public static boolean isLong(Object obj) {
		return is(obj,long.class) || is(obj,Long.class);
	}

	/**
	 * @return 当前对象是否为短整型
	 */
	public static boolean isShort(Object obj) {
		return is(obj,short.class) || is(obj,Short.class);
	}

	/**
	 * @return 当前对象是否为字节型
	 */
	public static boolean isByte(Object obj) {
		return is(obj,byte.class) || is(obj,Byte.class);
	}

	/**
	 * @return 当前对象是否在表示日期或时间
	 */
	public static boolean isDateTimeLike(Object obj) {
		Class klass=obj.getClass();
		return Calendar.class.isAssignableFrom(klass)
				|| java.util.Date.class.isAssignableFrom(klass)
				|| java.sql.Date.class.isAssignableFrom(klass)
				|| java.sql.Time.class.isAssignableFrom(klass);
	}
	/**
	 * 如果不是容器，也不是 POJO，那么它必然是个 Obj
	 * 
	 * @return true or false
	 */
	public static boolean isObj(Object obj) {
		return isContainer(obj) || isPojo(obj);
	}
	/**
	 * 判断当前类型是否为POJO。 除了下面的类型，其他均为 POJO
	 * <ul>
	 * <li>原生以及所有包裹类
	 * <li>类字符串
	 * <li>类日期
	 * <li>非容器
	 * </ul>
	 * 
	 * @return true or false
	 */
	public static boolean isPojo(Object obj) {
		Class klass=obj.getClass();
		if (klass.isPrimitive() || isEnum(obj))
			return false;

		if (isStringLike(obj) || isDateTimeLike(obj))
			return false;

		if (isPrimitiveNumber(obj) || isBoolean(obj) || isChar(obj))
			return false;

		return !isContainer(obj);
	}
	/**
	 * @return 当前对象是否为原生的数字类型 （即不包括 boolean 和 char）
	 */
	public static boolean isPrimitiveNumber(Object obj) {
		return isInt(obj) || isLong(obj) || isFloat(obj) || isDouble(obj) || isByte(obj) || isShort(obj);
	}
	/**
	 * 判断一个字符串是不是数字
	 * @author mawujun email:16064988@163.com qq:16064988
	 * @param input
	 * @return
	 */
	public static boolean isNumber(String input){  
		return StringUtil.isNumber(input);
	}


	/**
	 * 判断value实例是不是clz类型，具有继承关系的检查能力，即使value是clz的子类也会返回true
	 * @param clz
	 * @param value
	 * @return
	 */
	public static boolean isInstance(Class clz,Object value){
		return clz.isInstance(value);
	}
	/**
	 * 通过反射,获得Class定义中声明的父类的泛型参数的类型.
	 * 如无法找到, 返回Object.class.
	 * eg.
	 * public UserDao extends HibernateDao<User>
	 *
	 * @param clazz The class to introspect
	 * @return the first generic declaration, or Object.class if cannot be determined
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getSuperClassGenricType(final Class clazz) {
		return getSuperClassGenricType(clazz, 0);
	}

	/**
	 * 通过反射,获得定义Class时声明的父类的泛型参数的类型.
	 * 如无法找到, 返回Object.class.
	 * 
	 * 如public UserDao extends HibernateDao<User,Long>
	 *
	 * @param clazz clazz The class to introspect
	 * @param index the Index of the generic ddeclaration,start from 0.
	 * @return the index generic declaration, or Object.class if cannot be determined
	 */
	@SuppressWarnings("unchecked")
	public static Class getSuperClassGenricType(final Class clazz, final int index) {

		Type genType = clazz.getGenericSuperclass();

		if (!(genType instanceof ParameterizedType)) {
			logger.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
			return Object.class;
		}

		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

		if (index >= params.length || index < 0) {
			logger.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
					+ params.length);
			return Object.class;
		}
		if (!(params[index] instanceof Class)) {
			logger.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
			return Object.class;
		}

		return (Class) params[index];
	}
	/**
	 * 获取某个接口上定义的泛型类
	 * @param clazz
	 * @param index
	 * @return 如果没有泛型类，就返回null
	 */
	public static Class getGenericInterfaces(final Class clazz, final int index) {
		Type[] genTypes = clazz.getGenericInterfaces();
		if(genTypes==null || genTypes.length==0) {
			return null;
		}
		Type genType=genTypes[0];
		
		if (!(genType instanceof ParameterizedType)) {
			logger.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
			return Object.class;
		}

		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

		if (index >= params.length || index < 0) {
			logger.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
					+ params.length);
			return Object.class;
		}
		if (!(params[index] instanceof Class)) {
			logger.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
			return Object.class;
		}

		return (Class) params[index];
	}
	

	/**
	 * 提取集合中的对象的属性(通过getter函数), 组合成List.
	 * 
	 * @param collection 来源集合.
	 * @param propertyName 要提取的属性名.
	 */
	@SuppressWarnings("unchecked")
	public static List convertElementPropertyToList(final Collection collection, final String propertyName) {
		List list = new ArrayList();

		try {
			for (Object obj : collection) {
				list.add(PropertyUtils.getProperty(obj, propertyName));
			}
		} catch (Exception e) {
			throw convertReflectionExceptionToUnchecked(e);
		}

		return list;
	}

	/**
	 * 提取集合中的对象的属性(通过getter函数), 组合成由分割符分隔的字符串.
	 * 
	 * @param collection 来源集合.
	 * @param propertyName 要提取的属性名.
	 * @param separator 分隔符.
	 */
	@SuppressWarnings("unchecked")
	public static String convertElementPropertyToString(final Collection collection, final String propertyName,
			final String separator) {
		List list = convertElementPropertyToList(collection, propertyName);
		return StringUtil.join(list, separator);
	}

	/**
	 * 转换字符串到相应类型.
	 * 
	 * @param value 待转换的字符串
	 * @param toType 转换目标类型
	 */
	public static Object convertStringToObject(String value, Class<?> toType) {
		try {
			return Convert.convert(toType,value);
		} catch (Exception e) {
			throw convertReflectionExceptionToUnchecked(e);
		}
	}

	/**
	 * 将反射时的checked exception转换为unchecked exception.
	 */
	public static RuntimeException convertReflectionExceptionToUnchecked(Exception e) {
		if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException
				|| e instanceof NoSuchMethodException) {
			return new IllegalArgumentException("Reflection Exception.", e);
		} else if (e instanceof InvocationTargetException) {
			return new RuntimeException("Reflection Exception.", ((InvocationTargetException) e).getTargetException());
		} else if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		}
		return new RuntimeException("Unexpected Checked Exception.", e);
	}
	

	// --------------------------------------------------------------------------------------------------------- Field
	/**
	 * 查找指定类中是否包含指定名称对应的字段，包括所有字段（包括非public字段），也包括父类和Object类的字段
	 * 
	 * @param beanClass 被查找字段的类,不能为null
	 * @param name 字段名
	 * @return 是否包含字段
	 * @throws SecurityException 安全异常
	 * @since 4.1.21
	 */
	public static boolean hasField(Class<?> beanClass, String name) throws SecurityException {
		return null != getField(beanClass, name);
	}
	
	/**
	 * 查找指定类中的所有字段（包括非public字段），也包括父类和Object类的字段， 字段不存在则返回<code>null</code>
	 * 
	 * @param beanClass 被查找字段的类,不能为null
	 * @param name 字段名
	 * @return 字段
	 * @throws SecurityException 安全异常
	 */
	public static Field getField(Class<?> beanClass, String name) throws SecurityException {
		final Field[] fields = getFields(beanClass);
		if (ArrayUtil.isNotEmpty(fields)) {
			for (Field field : fields) {
				if ((name.equals(field.getName()))) {
					return field;
				}
			}
		}
		return null;
	}

	/**
	 * 获得一个类中所有字段列表，包括其父类中的字段
	 * 
	 * @param beanClass 类
	 * @return 字段列表
	 * @throws SecurityException 安全检查异常
	 */
	public static Field[] getFields(Class<?> beanClass) throws SecurityException {
		Field[] allFields = FIELDS_CACHE.get(beanClass);
		if (null != allFields) {
			return allFields;
		}

		allFields = getFieldsDirectly(beanClass, true);
		return FIELDS_CACHE.put(beanClass, allFields);
	}

	/**
	 * 获得一个类中所有字段列表，直接反射获取，无缓存
	 * 
	 * @param beanClass 类
	 * @param withSuperClassFieds 是否包括父类的字段列表
	 * @return 字段列表
	 * @throws SecurityException 安全检查异常
	 */
	public static Field[] getFieldsDirectly(Class<?> beanClass, boolean withSuperClassFieds) throws SecurityException {
		Assert.notNull(beanClass);

		Field[] allFields = null;
		Class<?> searchType = beanClass;
		Field[] declaredFields;
		while (searchType != null) {
			declaredFields = searchType.getDeclaredFields();
			if (null == allFields) {
				allFields = declaredFields;
			} else {
				allFields = ArrayUtil.append(allFields, declaredFields);
			}
			searchType = withSuperClassFieds ? searchType.getSuperclass() : null;
		}

		return allFields;
	}

	/**
	 * 获取字段值
	 * 
	 * @param obj 对象
	 * @param fieldName 字段名
	 * @return 字段值
	 * @throws UtilException 包装IllegalAccessException异常
	 */
	public static Object getFieldValue(Object obj, String fieldName) throws UtilException {
		if (null == obj || StrUtil.isBlank(fieldName)) {
			return null;
		}
		return getFieldValue(obj, getField(obj.getClass(), fieldName));
	}

	/**
	 * 获取字段值
	 * 
	 * @param obj 对象
	 * @param field 字段
	 * @return 字段值
	 * @throws UtilException 包装IllegalAccessException异常
	 */
	public static Object getFieldValue(Object obj, Field field) throws UtilException {
		if (null == obj || null == field) {
			return null;
		}
		field.setAccessible(true);
		Object result = null;
		try {
			result = field.get(obj);
		} catch (IllegalAccessException e) {
			throw new UtilException(e, "IllegalAccess for {}.{}", obj.getClass(), field.getName());
		}
		return result;
	}

	/**
	 * 获取所有字段的值
	 * @param obj bean对象
	 * @return 字段值数组
	 * @since 4.1.17
	 */
	public static Object[] getFieldsValue(Object obj) {
		if (null != obj) {
			final Field[] fields = getFields(obj.getClass());
			if (null != fields) {
				final Object[] values = new Object[fields.length];
				for (int i = 0; i < fields.length; i++) {
					values[i] = getFieldValue(obj, fields[i]);
				}
				return values;
			}
		}
		return null;
	}

	/**
	 * 为注解设置值
	 * @param obj
	 * @param fieldName
	 * @param value
	 * @throws UtilException
	 */
	public static void setAnnotationFieldValue(Object obj, String fieldName, Object value)  {
		Assert.notNull(obj);
		Assert.notBlank(fieldName);

		try {
			//获取 foo 这个代理实例所持有的 InvocationHandler
			InvocationHandler h = Proxy.getInvocationHandler(obj);
			// 获取 AnnotationInvocationHandler 的 memberValues 字段
			Field hField = h.getClass().getDeclaredField("memberValues");
			// 因为这个字段事 private final 修饰，所以要打开权限
			hField.setAccessible(true);
			// 获取 memberValues
			Map memberValues = (Map) hField.get(h);
			// 修改 value 属性值
			memberValues.put(fieldName, value);
		} catch(Exception e) {
			throw new RuntimeException("反射为注解设置值的时候报错:",e);
		}
	}

	/**
	 * 设置字段值
	 * 
	 * @param obj 对象
	 * @param fieldName 字段名
	 * @param value 值，值类型必须与字段类型匹配，不会自动转换对象类型
	 * @throws UtilException 包装IllegalAccessException异常
	 */
	public static void setFieldValue(Object obj, String fieldName, Object value) throws UtilException {
		Assert.notNull(obj);
		Assert.notBlank(fieldName);
		setFieldValue(obj, getField(obj.getClass(), fieldName), value);
	}

	/**
	 * 设置字段值
	 * 
	 * @param obj 对象
	 * @param field 字段
	 * @param value 值，值类型必须与字段类型匹配，不会自动转换对象类型
	 * @throws UtilException UtilException 包装IllegalAccessException异常
	 */
	public static void setFieldValue(Object obj, Field field, Object value) throws UtilException {
		Assert.notNull(obj);
		Assert.notNull(field);
		field.setAccessible(true);
		
		if(null != value) {
			Class<?> fieldType = field.getType();
			if(false == fieldType.isAssignableFrom(value.getClass())) {
				//对于类型不同的字段，尝试转换，转换失败则使用原对象类型
				final Object targetValue = Convert.convert(fieldType, value);
				if(null != targetValue) {
					value = targetValue;
				}
			}
		}
		
		try {
			field.set(obj, value);
		} catch (IllegalAccessException e) {
			throw new UtilException(e, "IllegalAccess for {}.{}", obj.getClass(), field.getName());
		}
	}

	// --------------------------------------------------------------------------------------------------------- method
	/**
	 * 获得指定类本类及其父类中的Public方法名<br>
	 * 去重重载的方法
	 * 
	 * @param clazz 类
	 * @return 方法名Set
	 */
	public static Set<String> getPublicMethodNames(Class<?> clazz) {
		final HashSet<String> methodSet = new HashSet<String>();
		final Method[] methodArray = getPublicMethods(clazz);
		if(ArrayUtil.isNotEmpty(methodArray)) {
			for (Method method : methodArray) {
				methodSet.add(method.getName());
			}
		}
		return methodSet;
	}

	/**
	 * 获得本类及其父类所有Public方法
	 * 
	 * @param clazz 查找方法的类
	 * @return 过滤后的方法列表
	 */
	public static Method[] getPublicMethods(Class<?> clazz) {
		return null == clazz ? null : clazz.getMethods();
	}

	/**
	 * 获得指定类过滤后的Public方法列表
	 * 
	 * @param clazz 查找方法的类
	 * @param filter 过滤器
	 * @return 过滤后的方法列表
	 */
	public static List<Method> getPublicMethods(Class<?> clazz, Filter<Method> filter) {
		if (null == clazz) {
			return null;
		}

		final Method[] methods = getPublicMethods(clazz);
		List<Method> methodList;
		if (null != filter) {
			methodList = new ArrayList<>();
			for (Method method : methods) {
				if (filter.accept(method)) {
					methodList.add(method);
				}
			}
		} else {
			methodList = CollectionUtil.newArrayList(methods);
		}
		return methodList;
	}

	/**
	 * 获得指定类过滤后的Public方法列表
	 * 
	 * @param clazz 查找方法的类
	 * @param excludeMethods 不包括的方法
	 * @return 过滤后的方法列表
	 */
	public static List<Method> getPublicMethods(Class<?> clazz, Method... excludeMethods) {
		final HashSet<Method> excludeMethodSet = CollectionUtil.newHashSet(excludeMethods);
		return getPublicMethods(clazz, new Filter<Method>() {
			@Override
			public boolean accept(Method method) {
				return false == excludeMethodSet.contains(method);
			}
		});
	}

	/**
	 * 获得指定类过滤后的Public方法列表
	 * 
	 * @param clazz 查找方法的类
	 * @param excludeMethodNames 不包括的方法名列表
	 * @return 过滤后的方法列表
	 */
	public static List<Method> getPublicMethods(Class<?> clazz, String... excludeMethodNames) {
		final HashSet<String> excludeMethodNameSet = CollectionUtil.newHashSet(excludeMethodNames);
		return getPublicMethods(clazz, new Filter<Method>() {
			@Override
			public boolean accept(Method method) {
				return false == excludeMethodNameSet.contains(method.getName());
			}
		});
	}

	/**
	 * 查找指定Public方法 如果找不到对应的方法或方法不为public的则返回<code>null</code>
	 * 
	 * @param clazz 类
	 * @param methodName 方法名
	 * @param paramTypes 参数类型
	 * @return 方法
	 * @throws SecurityException 无权访问抛出异常
	 */
	public static Method getPublicMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) throws SecurityException {
		try {
			return clazz.getMethod(methodName, paramTypes);
		} catch (NoSuchMethodException ex) {
			return null;
		}
	}
	
	/**
	 * 查找指定对象中的所有方法（包括非public方法），也包括父对象和Object类的方法
	 * 
	 * <p>
	 * 此方法为精准获取方法名，即方法名和参数数量和类型必须一致，否则返回<code>null</code>。
	 * </p>
	 * 
	 * @param obj 被查找的对象，如果为{@code null}返回{@code null}
	 * @param methodName 方法名，如果为空字符串返回{@code null}
	 * @param args 参数
	 * @return 方法
	 * @throws SecurityException 无访问权限抛出异常
	 */
	public static Method getMethodOfObj(Object obj, String methodName, Object... args) throws SecurityException {
		if (null == obj || StrUtil.isBlank(methodName)) {
			return null;
		}
		return getMethod(obj.getClass(), methodName, ClassUtil.getClasses(args));
	}

	/**
	 * 忽略大小写查找指定方法，如果找不到对应的方法则返回<code>null</code>
	 * 
	 * <p>
	 * 此方法为精准获取方法名，即方法名和参数数量和类型必须一致，否则返回<code>null</code>。
	 * </p>
	 * 
	 * @param clazz 类，如果为{@code null}返回{@code null}
	 * @param methodName 方法名，如果为空字符串返回{@code null}
	 * @param paramTypes 参数类型，指定参数类型如果是方法的子类也算
	 * @return 方法
	 * @throws SecurityException 无权访问抛出异常
	 * @since 3.2.0
	 */
	public static Method getMethodIgnoreCase(Class<?> clazz, String methodName, Class<?>... paramTypes) throws SecurityException {
		return getMethod(clazz, true, methodName, paramTypes);
	}

	/**
	 * 查找指定方法 如果找不到对应的方法则返回<code>null</code>
	 * 
	 * <p>
	 * 此方法为精准获取方法名，即方法名和参数数量和类型必须一致，否则返回<code>null</code>。
	 * </p>
	 * 
	 * @param clazz 类，如果为{@code null}返回{@code null}
	 * @param methodName 方法名，如果为空字符串返回{@code null}
	 * @param paramTypes 参数类型，指定参数类型如果是方法的子类也算
	 * @return 方法
	 * @throws SecurityException 无权访问抛出异常
	 */
	public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) throws SecurityException {
		return getMethod(clazz, false, methodName, paramTypes);
	}

	/**
	 * 查找指定方法 如果找不到对应的方法则返回<code>null</code>
	 * 
	 * <p>
	 * 此方法为精准获取方法名，即方法名和参数数量和类型必须一致，否则返回<code>null</code>。
	 * </p>
	 * 
	 * @param clazz 类，如果为{@code null}返回{@code null}
	 * @param ignoreCase 是否忽略大小写
	 * @param methodName 方法名，如果为空字符串返回{@code null}
	 * @param paramTypes 参数类型，指定参数类型如果是方法的子类也算
	 * @return 方法
	 * @throws SecurityException 无权访问抛出异常
	 * @since 3.2.0
	 */
	public static Method getMethod(Class<?> clazz, boolean ignoreCase, String methodName, Class<?>... paramTypes) throws SecurityException {
		if (null == clazz || StrUtil.isBlank(methodName)) {
			return null;
		}

		final Method[] methods = getMethods(clazz);
		if (ArrayUtil.isNotEmpty(methods)) {
			for (Method method : methods) {
				if (StrUtil.equals(methodName, method.getName(), ignoreCase)) {
					if (ClassUtil.isAllAssignableFrom(method.getParameterTypes(), paramTypes)) {
						return method;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * 按照方法名查找指定方法名的方法，只返回匹配到的第一个方法，如果找不到对应的方法则返回<code>null</code>
	 * 
	 * <p>
	 * 此方法只检查方法名是否一致，并不检查参数的一致性。
	 * </p>
	 * 
	 * @param clazz 类，如果为{@code null}返回{@code null}
	 * @param methodName 方法名，如果为空字符串返回{@code null}
	 * @return 方法
	 * @throws SecurityException 无权访问抛出异常
	 * @since 4.3.2
	 */
	public static Method getMethodByName(Class<?> clazz, String methodName) throws SecurityException {
		return getMethodByName(clazz, false, methodName);
	}
	
	/**
	 * 按照方法名查找指定方法名的方法，只返回匹配到的第一个方法，如果找不到对应的方法则返回<code>null</code>
	 * 
	 * <p>
	 * 此方法只检查方法名是否一致（忽略大小写），并不检查参数的一致性。
	 * </p>
	 * 
	 * @param clazz 类，如果为{@code null}返回{@code null}
	 * @param methodName 方法名，如果为空字符串返回{@code null}
	 * @return 方法
	 * @throws SecurityException 无权访问抛出异常
	 * @since 4.3.2
	 */
	public static Method getMethodByNameIgnoreCase(Class<?> clazz, String methodName) throws SecurityException {
		return getMethodByName(clazz, true, methodName);
	}
	
	/**
	 * 按照方法名查找指定方法名的方法，只返回匹配到的第一个方法，如果找不到对应的方法则返回<code>null</code>
	 * 
	 * <p>
	 * 此方法只检查方法名是否一致，并不检查参数的一致性。
	 * </p>
	 * 
	 * @param clazz 类，如果为{@code null}返回{@code null}
	 * @param ignoreCase 是否忽略大小写
	 * @param methodName 方法名，如果为空字符串返回{@code null}
	 * @return 方法
	 * @throws SecurityException 无权访问抛出异常
	 * @since 4.3.2
	 */
	public static Method getMethodByName(Class<?> clazz, boolean ignoreCase, String methodName) throws SecurityException {
		if (null == clazz || StrUtil.isBlank(methodName)) {
			return null;
		}

		final Method[] methods = getMethods(clazz);
		if (ArrayUtil.isNotEmpty(methods)) {
			for (Method method : methods) {
				if (StrUtil.equals(methodName, method.getName(), ignoreCase)) {
					return method;
				}
			}
		}
		return null;
	}

	/**
	 * 获得指定类中的Public方法名<br>
	 * 去重重载的方法
	 * 
	 * @param clazz 类
	 * @return 方法名Set
	 * @throws SecurityException 安全异常
	 */
	public static Set<String> getMethodNames(Class<?> clazz) throws SecurityException {
		final HashSet<String> methodSet = new HashSet<String>();
		final Method[] methods = getMethods(clazz);
		for (Method method : methods) {
			methodSet.add(method.getName());
		}
		return methodSet;
	}

	/**
	 * 获得指定类过滤后的Public方法列表
	 * 
	 * @param clazz 查找方法的类
	 * @param filter 过滤器
	 * @return 过滤后的方法列表
	 * @throws SecurityException 安全异常
	 */
	public static Method[] getMethods(Class<?> clazz, Filter<Method> filter) throws SecurityException {
		if (null == clazz) {
			return null;
		}
		return ArrayUtil.filter(getMethods(clazz), filter);
	}

	/**
	 * 获得一个类中所有方法列表，包括其父类中的方法
	 * 
	 * @param beanClass 类
	 * @return 方法列表
	 * @throws SecurityException 安全检查异常
	 */
	public static Method[] getMethods(Class<?> beanClass) throws SecurityException {
		Method[] allMethods = METHODS_CACHE.get(beanClass);
		if (null != allMethods) {
			return allMethods;
		}

		allMethods = getMethodsDirectly(beanClass, true);
		return METHODS_CACHE.put(beanClass, allMethods);
	}

	/**
	 * 获得一个类中所有方法列表，直接反射获取，无缓存
	 * 
	 * @param beanClass 类
	 * @param withSuperClassMethods 是否包括父类的方法列表
	 * @return 方法列表
	 * @throws SecurityException 安全检查异常
	 */
	public static Method[] getMethodsDirectly(Class<?> beanClass, boolean withSuperClassMethods) throws SecurityException {
		Assert.notNull(beanClass);

		Method[] allMethods = null;
		Class<?> searchType = beanClass;
		Method[] declaredMethods;
		while (searchType != null) {
			declaredMethods = searchType.getDeclaredMethods();
			if (null == allMethods) {
				allMethods = declaredMethods;
			} else {
				allMethods = ArrayUtil.append(allMethods, declaredMethods);
			}
			searchType = withSuperClassMethods ? searchType.getSuperclass() : null;
		}

		return allMethods;
	}

	/**
	 * 是否为equals方法
	 * 
	 * @param method 方法
	 * @return 是否为equals方法
	 */
	public static boolean isEqualsMethod(Method method) {
		if (method == null || false == method.getName().equals("equals")) {
			return false;
		}
		final Class<?>[] paramTypes = method.getParameterTypes();
		return (1 == paramTypes.length && paramTypes[0] == Object.class);
	}

	/**
	 * 是否为hashCode方法
	 * 
	 * @param method 方法
	 * @return 是否为hashCode方法
	 */
	public static boolean isHashCodeMethod(Method method) {
		return (method != null && method.getName().equals("hashCode") && method.getParameterTypes().length == 0);
	}

	/**
	 * 是否为toString方法
	 * 
	 * @param method 方法
	 * @return 是否为toString方法
	 */
	public static boolean isToStringMethod(Method method) {
		return (method != null && method.getName().equals("toString") && method.getParameterTypes().length == 0);
	}

	// --------------------------------------------------------------------------------------------------------- newInstance
	/**
	 * 实例化对象
	 * 
	 * @param <T> 对象类型
	 * @param clazz 类名
	 * @return 对象
	 * @throws UtilException 包装各类异常
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(String clazz) throws UtilException {
		try {
			return (T) Class.forName(clazz).newInstance();
		} catch (Exception e) {
			throw new UtilException(e, "实例化 class [{}] 错误!", clazz);
		}
	}

	/**
	 * 实例化对象
	 * 
	 * @param <T> 对象类型
	 * @param clazz 类
	 * @param params 构造函数参数
	 * @return 对象
	 * @throws UtilException 包装各类异常
	 */
	public static <T> T newInstance(Class<T> clazz, Object... params) throws UtilException {
		if (ArrayUtil.isEmpty(params)) {
			final Constructor<T> constructor = getConstructor(clazz);
			try {
				return constructor.newInstance();
			} catch (Exception e) {
				throw new UtilException(e, "Instance class [{}] error!", clazz);
			}
		}

		final Class<?>[] paramTypes = ClassUtil.getClasses(params);
		final Constructor<T> constructor = getConstructor(clazz, paramTypes);
		if (null == constructor) {
			throw new UtilException("No Constructor matched for parameter types: [{}]", new Object[] { paramTypes });
		}
		try {
			return constructor.newInstance(params);
		} catch (Exception e) {
			throw new UtilException(e, "Instance class [{}] error!", clazz);
		}
	}

	/**
	 * 尝试遍历并调用此类的所有构造方法，直到构造成功并返回
	 * 
	 * @param <T> 对象类型
	 * @param beanClass 被构造的类
	 * @return 构造后的对象
	 */
	public static <T> T newInstanceIfPossible(Class<T> beanClass) {
		Assert.notNull(beanClass);
		try {
			return newInstance(beanClass);
		} catch (Exception e) {
			// ignore
			// 默认构造不存在的情况下查找其它构造
		}
		
		final Constructor<T>[] constructors = getConstructors(beanClass);
		Class<?>[] parameterTypes;
		for (Constructor<T> constructor : constructors) {
			parameterTypes = constructor.getParameterTypes();
			if (0 == parameterTypes.length) {
				continue;
			}
			constructor.setAccessible(true);
			try {
				return constructor.newInstance(ClassUtil.getDefaultValues(parameterTypes));
			} catch (Exception e) {
				// 构造出错时继续尝试下一种构造方式
				continue;
			}
		}
		return null;
	}

	// --------------------------------------------------------------------------------------------------------- invoke
	/**
	 * 执行静态方法
	 * 
	 * @param <T> 对象类型
	 * @param method 方法（对象方法或static方法都可）
	 * @param args 参数对象
	 * @return 结果
	 * @throws UtilException 多种异常包装
	 */
	public static <T> T invokeStatic(Method method, Object... args) throws UtilException {
		return invoke(null, method, args);
	}

	/**
	 * 执行方法<br>
	 * 执行前要检查给定参数：
	 * 
	 * <pre>
	 * 1. 参数个数是否与方法参数个数一致
	 * 2. 如果某个参数为null但是方法这个位置的参数为原始类型，则赋予原始类型默认值
	 * </pre>
	 * 
	 * @param <T> 返回对象类型
	 * @param obj 对象，如果执行静态方法，此值为<code>null</code>
	 * @param method 方法（对象方法或static方法都可）
	 * @param args 参数对象
	 * @return 结果
	 * @throws UtilException 一些列异常的包装
	 */
	public static <T> T invokeWithCheck(Object obj, Method method, Object... args) throws UtilException {
		final Class<?>[] types = method.getParameterTypes();
		if (null != types && null != args) {
			Assert.isTrue(args.length == types.length, "Params length [{}] is not fit for param length [{}] of method !", args.length, types.length);
			Class<?> type;
			for (int i = 0; i < args.length; i++) {
				type = types[i];
				if (type.isPrimitive() && null == args[i]) {
					// 参数是原始类型，而传入参数为null时赋予默认值
					args[i] = ClassUtil.getDefaultValue(type);
				}
			}
		}

		return invoke(obj, method, args);
	}

	/**
	 * 执行方法
	 * 
	 * @param <T> 返回对象类型
	 * @param obj 对象，如果执行静态方法，此值为<code>null</code>
	 * @param method 方法（对象方法或static方法都可）
	 * @param args 参数对象
	 * @return 结果
	 * @throws UtilException 一些列异常的包装
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invoke(Object obj, Method method, Object... args) throws UtilException {
		if (false == method.isAccessible()) {
			method.setAccessible(true);
		}

		try {
			return (T) method.invoke(ClassUtil.isStatic(method) ? null : obj, args);
		} catch (Exception e) {
			throw new UtilException(e);
		}
	}

	/**
	 * 执行对象中指定方法
	 * 
	 * @param <T> 返回对象类型
	 * @param obj 方法所在对象
	 * @param methodName 方法名
	 * @param args 参数列表
	 * @return 执行结果
	 * @throws UtilException IllegalAccessException包装
	 * @since 3.1.2
	 */
	public static <T> T invoke(Object obj, String methodName, Object... args) throws UtilException {
		final Method method = getMethodOfObj(obj, methodName, args);
		if (null == method) {
			throw new UtilException(StrUtil.format("No such method: [{}]", methodName));
		}
		return invoke(obj, method, args);
	}
}
