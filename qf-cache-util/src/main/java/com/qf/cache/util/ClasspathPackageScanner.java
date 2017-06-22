package com.qf.cache.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 包扫描器
 * <br>
 * File Name: ClasspathPackageScanner.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年6月19日 下午4:26:08 
 * @version: v1.0
 *
 */
public class ClasspathPackageScanner {
	
	private String basePackage;
	private ClassLoader cl;

	public ClasspathPackageScanner(String basePackage) {
		this.basePackage = basePackage;
		this.cl = getClass().getClassLoader();

	}

	public ClasspathPackageScanner(String basePackage, ClassLoader cl) {
		this.basePackage = basePackage;
		this.cl = cl;
	}

	public List<String> getFullyQualifiedClassNameList() throws IOException {
		return doScan(basePackage, new ArrayList<String>());
	}

	private List<String> doScan(String basePackage, List<String> nameList) throws IOException {
		String splashPath = dotToSplash(basePackage);

		Enumeration<URL> urls = cl.getResources(splashPath);
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			String protocol = url.getProtocol();

			if ("file".equals(protocol)) {
				String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
				readFromDirectory(basePackage, filePath, nameList);
			} 
			else if ("jar".equals(protocol)) {
				readFromJarFile(url, splashPath);
			}
		}
		return nameList;
	}
	
	private List<String> readFromDirectory(String packageName, String path, List<String> clazzList) {
		File dir = new File(path);
		if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }
		File[] dirfiles = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory() || (file.getName().endsWith(".class"));
            }
        });
		
		if (dirfiles != null && dirfiles.length > 0) {
			for (File file : dirfiles) {
	            if (file.isDirectory()) {
	            	readFromDirectory(packageName + "." + file.getName(), file.getAbsolutePath(), clazzList);
	            } 
	            else {
	                String className = file.getName().substring(0, file.getName().length() - 6);
	               	clazzList.add(packageName + '.' + className);
	            }
	        }
		}
		return clazzList;
	}

	private List<String> readFromJarFile(URL url, String splashedPackageName) throws IOException {
		JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
		Enumeration<JarEntry> entries = jar.entries();

		String packageName = splashedPackageName;
		List<String> clazzList = new ArrayList<String>();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.charAt(0) == '/') {
                name = name.substring(1);
            }
            if (name.startsWith(splashedPackageName)) {
                int idx = name.lastIndexOf('/');
                if (idx != -1) {
                    packageName = name.substring(0, idx).replace('/', '.');
                    if (name.endsWith(".class") && !entry.isDirectory()) {
                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                        clazzList.add(packageName + '.' + className);
                    }
                }
            }            
		}
		jar.close();
		
		return clazzList;
	}
	
	private static String dotToSplash(String name) {
		return name.replaceAll("\\.", "/");
	}

}
