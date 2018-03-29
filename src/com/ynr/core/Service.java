package com.ynr.core;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.ynr.parser.IExcelParser;

import redis.clients.jedis.Jedis;

public class Service {

	private Map<String, IExcelParser> parserMap = new HashMap<>();
	
	private boolean initParser(){
		boolean initSuccess = false;
		try {
			List<Class<?>> parserClassList = new ArrayList<>();
			//getAllClassByInterface(Class.forName("com.ynr.parser.IExcelParser"));
			parserClassList.add(Class.forName("com.ynr.parser.AustraliaParser"));
			parserClassList.add(Class.forName("com.ynr.parser.EVUSParser"));
			parserClassList.add(Class.forName("com.ynr.parser.FranceParser"));
			for(Class<?> parserClass : parserClassList){
				String simpleName = parserClass.getSimpleName();
				System.out.println(simpleName);
				parserMap.put(simpleName.replace("Parser", "").toLowerCase(), (IExcelParser)parserClass.newInstance());
			}
			initSuccess = true;
		} catch (InstantiationException e) {
			System.out.println(e.toString());
		} catch (IllegalAccessException e) {
			System.out.println(e.toString());
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}
		System.out.println(parserMap.toString());
		return initSuccess;
	}
	
	public void startService(){
		System.out.println("init excel parser ......");
		if(initParser()) {
			System.out.println("init excel parser success.");
			while(true){
				try {
					Date nowTime=new Date(); 
					System.out.println(nowTime); 
					SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
					System.out.println("Current Time : " + time.format(nowTime)); 
					System.out.println("Get a task from PARSE_TASK...");
					Jedis jedis = RedisPool.pool.getResource();
					String task = jedis.lpop("PARSE_TASK");
					jedis.close();
					if(task == null || task.isEmpty()){
						System.out.println("no task, sleep 5s...");
						Thread.sleep(5000);
					} else {
						System.out.println("Get a task from PARSE_TASK, task info : " + task);
						@SuppressWarnings("unchecked")
						Map<String, Object> taskMap = JSON.parseObject(task, Map.class);
						String task_type = (String)taskMap.get("task_type");
						String task_id = (String)taskMap.get("task_id");
						String task_excel_file_path = (String)taskMap.get("task_excel_file_path");
						IExcelParser excelParser = parserMap.get(task_type);
						if(excelParser == null) {
							System.out.println("Can not find the taskType parser : " + task_type);
						} else {
							System.out.println("parsing task ...");
							//String data = parserMap.get(task_type).parseDemo();
							String data = parserMap.get(task_type).parseTask(task_excel_file_path);
							System.out.println("parsed data : " + data);
							@SuppressWarnings("unchecked")
							Map<String, Object> jsonDataMap = JSON.parseObject(data, Map.class);
							jsonDataMap.putAll(taskMap);
							jedis = RedisPool.pool.getResource();
							jedis.rpush("AUTO_FILL_FORM_JSON_DATA", JSON.toJSONString(jsonDataMap));
							jedis.close();
							if(MysqlUtils.updateTaskRecordStatus(Integer.valueOf(task_id), "数据解析已完成，等待自动填表")) {
								System.out.println("updateTaskRecordStatus success.");
							} else {
								System.out.println("updateTaskRecordStatus fail!");
							}
							System.out.println("finish parse task.");
						}
					} 
				} catch(Exception e){
					System.out.println(e.toString());
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						System.out.println(e1.toString());
					}
				}
			}
		} else {
			System.out.println("init excel parser fail!");
		}
	}
	
	private List<Class<?>> getAllClassByInterface(Class<?> c) {  
        List<Class<?>>  returnClassList = null;
        if(c.isInterface()) {  
            String packageName = c.getPackage().getName();  
            List<Class<?>> allClass = getClasses(packageName);  
            if(allClass != null) {  
                returnClassList = new ArrayList<Class<?>>();  
                for(Class<?> classes : allClass) {  
                    if(c.isAssignableFrom(classes)) {  
                        if(!c.equals(classes)) {  
                            returnClassList.add(classes);          
                        }  
                    }  
                }  
            }  
        }  
        return returnClassList;  
    }
	
	private List<Class<?>> getClasses(String packageName){  
        List<Class<?>> classes = new ArrayList<Class<?>>();  
        boolean recursive = true;  
        String packageDirName = packageName.replace('.', '/');  
        Enumeration<URL> dirs;  
        try {  
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);  
            while (dirs.hasMoreElements()){  
                URL url = dirs.nextElement();  
                String protocol = url.getProtocol();  
                if ("file".equals(protocol)) {  
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");  
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);  
                } 
            }  
        } catch (IOException e) {  
        	System.out.println(e.toString());
        }  
        return classes;  
    }  
	
	private void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, List<Class<?>> classes){  
        File dir = new File(packagePath);  
        if (!dir.exists() || !dir.isDirectory()) {  
            return;  
        }  
        File[] dirfiles = dir.listFiles(new FileFilter() {  
              public boolean accept(File file) {  
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));  
              }  
            });  
        for (File file : dirfiles) {  
            if (file.isDirectory()) {  
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(),  
                                      file.getAbsolutePath(),  
                                      recursive,  
                                      classes);  
            }  
            else {  
                String className = file.getName().substring(0, file.getName().length() - 6);  
                try {  
                    classes.add(Class.forName(packageName + '.' + className));  
                } catch (ClassNotFoundException e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
    }  
	
	public static void main(String[] args) {
		Service service = new Service();
		service.startService();
	}
	
}
