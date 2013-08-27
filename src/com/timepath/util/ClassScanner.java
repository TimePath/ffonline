package com.timepath.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 *
 * @author timepath
 */
public class ClassScanner {

    public interface ClassFoundCallback {

        void found(Class<?> c);
    }

    public static void scan(ClassFoundCallback cn) throws Exception {
        for (String classpathEntry : System.getProperty("java.class.path").split(System.getProperty("path.separator"))) {
            File path = new File(classpathEntry);
            if (path.isFile()) {
                if (classpathEntry.endsWith(".jar")) {
                    File jar = new File(classpathEntry);
                    JarInputStream is = new JarInputStream(new FileInputStream(jar));
                    JarEntry entry;
                    while ((entry = is.getNextJarEntry()) != null) {
                        if (entry.getName().endsWith(".class")) {
//                            System.out.println(Class.forName(entry.getName()));
                            //   for implementation of the interface
                        }
                    }
                }
            } else {
                scan(path, path, cn);
            }
        }
    }

    private static void scan(File base, File dir, ClassFoundCallback cn) throws ClassNotFoundException {
        File[] c = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".class");
            }
        });
        for (File f : c) {
            if (f.isDirectory()) {
                scan(base, f, cn);
            } else {
                String name = f.getAbsolutePath()
                        .replace(base.getAbsolutePath(), "")
                        .substring(1)
                        .replaceAll("/", "\\.");
                name = name.substring(0, name.length() - ".class".length());
                String sub = "$";
                if (name.contains(sub)) {
                    String[] subs = name.split(sub);
                    if (subs.length > 1) {
                        name = subs[0].substring(0, subs[0].lastIndexOf(".") + 1) + subs[subs.length - 1];
                    }
                }
                cn.found(Class.forName(name));
            }
        }
    }
}
