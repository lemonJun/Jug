package lemon.jug.web.webapp;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 每个webapp对应一个classloader 负责加载context的web-info/lib和 web-info/classes 
 * context之间的隔离就是通过不同的webappclassloader来实现的
 * 
 *
 * @author lemon
 * @date  2016年11月21日 下午9:18:05
 * @see
 */
public class WebAppClassLoader extends URLClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(WebAppClassLoader.class);

    private URL[] urls = null;
    private ClassLoader parent = null;
    private ClassLoader system = null;
    private String webAppPath;//工程的路径
    private List<JarFileEntry> jarFileList = new ArrayList<JarFileEntry>();
    private ConcurrentHashMap<String, Class<?>> classBuffer = new ConcurrentHashMap<String, Class<?>>();

    public WebAppClassLoader(String webAppPath, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.urls = urls;
        this.webAppPath = webAppPath;
        this.parent = parent;
        this.system = getSystemClassLoader();
        for (URL url : urls) {
            String path = url.getPath();
            if (path.endsWith(".jar")) {
                try {
                    JarFileEntry jarFileEntry = new JarFileEntry();
                    jarFileEntry.jarFile = new JarFile(path);
                    jarFileEntry.originPath = path;
                    jarFileList.add(jarFileEntry);
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
        }
    }

    /**
     * try to find class in /WEB-INF/classes/ and /lib/
     * 所以这个加载器也是不对的  应该是webapps目录下的类才对
     */
    @Override
    public Class<?> findClass(String name) {
        String originName = name;
        name = name.replace(".", "/") + ".class";
        Class<?> _class = null;
        try {
            String classPathName = this.getFullPath("WEB-INF/classes/" + name);
            //            logger.info("find:" + classPathName);
            File classFile = new File(classPathName);
            if (classFile.exists()) {
                byte[] classData = getByteArrayFromStream(new FileInputStream(classFile));
                _class = this.defineClass(originName, classData, 0, classData.length);
                classBuffer.put(originName, _class);
            }

            if (_class == null) {
                for (JarFileEntry jarFileEntry : jarFileList) {
                    JarFile jarFile = jarFileEntry.jarFile;
                    JarEntry jarEntry = jarFile.getJarEntry(name);
                    if (jarEntry != null) {
                        byte[] classData = getByteArrayFromStream(jarFile.getInputStream(jarEntry));
                        _class = this.defineClass(originName, classData, 0, classData.length);
                        classBuffer.put(originName, _class);
                        break;
                    }
                }
            }

            if (_class == null) {
                _class = super.findClass(originName);
            }
        } catch (IOException e) {
            logger.error("", e);
            return _class;
        } catch (ClassNotFoundException e) {
            logger.error("", e);
            return _class;
        }
        return _class;
    }

    private byte[] getByteArrayFromStream(InputStream ins) throws IOException {
        BufferedInputStream bufIns = new BufferedInputStream(ins);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count = -1;
        while ((count = bufIns.read(buffer)) != -1) {
            byteOut.write(buffer, 0, count);
        }
        bufIns.close();
        return byteOut.toByteArray();
    }

    public Class<?> findLoadedClass0(String name) {
        return classBuffer.get(name);
    }

    //用些类加载器加载此工程下的所有类
    public void loadAllClass() {

    }

    /**
     * 这一点就搞不明白了
     * 既然是委托加载机制，那么定义一个类加载器是怎么保证context的隔离的
     * 这一点应该参考  TOMCAT的实现的 
     * 具体内容在这可以找到http://www.open-open.com/lib/view/open1480924357104.html  讲的很清楚
     * 
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        //检查是否已经装载过此类
        Class<?> _class = findLoadedClass0(name);
        if (_class != null) {
            return _class;
        }

        //检查系统是否已经加载过   加载过的话   就不用加载
        //按这一块的逻辑  类名还不能重？？？？？
        _class = findLoadedClass(name);
        if (_class != null) {
            return _class;
        }
        //先是系统加载
        try {
            _class = system.loadClass(name);
            if (_class != null) {
                return _class;
            }
        } catch (ClassNotFoundException e) {

        }
        //然后再父类加载
        try {
            _class = parent != null ? parent.loadClass(name) : null;
            if (_class != null) {
                return _class;
            }
        } catch (ClassNotFoundException e) {

        }
        //最后再自已加载
        _class = findClass(name);
        if (_class != null) {
            return _class;
        }

        throw new ClassNotFoundException(name);
    }

    private String getFullPath(String url) {
        return webAppPath + "/" + url;
    }

    @Override
    public URL getResource(String name) {
        URL url = super.getResource(name);
        if (url != null) {
            return url;
        }
        url = findResource(name);
        return url;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        InputStream ins = super.getResourceAsStream(name);
        if (ins != null) {
            return ins;
        }
        File file = findFile(name);
        if (file != null) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                return fileInputStream;
            } catch (IOException e) {
                logger.error("", e);
            }
        }
        JarFileEntry jarFileEntry = findJarFileEntry(name);
        if (jarFileEntry != null) {
            try {
                InputStream inputStream = jarFileEntry.jarFile.getInputStream(jarFileEntry.curJarEntry);
                return inputStream;
            } catch (IOException e) {
                logger.error("", e);
            }
        }

        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> urls = super.getResources(name);
        if (urls != null) {
            return urls;
        }
        return findResources(name);
    }

    @Override
    public URL findResource(String name) {
        URL url = null;

        File file = findFile(name);
        if (file != null) {
            try {
                url = file.toURI().toURL();
            } catch (MalformedURLException e) {
                logger.error("", e);
            }
            return url;
        }

        JarFileEntry jarFileEntry = findJarFileEntry(name);
        if (jarFileEntry != null) {
            JarEntry jarEntry = jarFileEntry.curJarEntry;
            String originPath = jarFileEntry.originPath;
            try {
                url = new URL("jar", null, originPath + "!/" + jarEntry.getName());
            } catch (MalformedURLException e) {
                logger.error("", e);
            }
            return url;
        }

        return null;
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        return super.findResources(name);
    }

    private File findFile(String name) {
        if (name.endsWith(".class")) {
            name = "WEB-INF/classes/" + name;
        }
        String path = this.getFullPath(name);
        File file = new File(path);
        if (file.exists()) {
            return file;
        }
        return null;
    }

    private JarFileEntry findJarFileEntry(String name) {
        for (JarFileEntry jarFileEntry : jarFileList) {
            JarFile jarFile = jarFileEntry.jarFile;
            JarEntry jarEntry = jarFile.getJarEntry(name);
            if (jarEntry != null) {
                JarFileEntry jarFileEntryReturn = new JarFileEntry();
                jarFileEntryReturn.jarFile = jarFile;
                jarFileEntryReturn.curJarEntry = jarEntry;
                jarFileEntryReturn.originPath = jarFileEntry.originPath;
                return jarFileEntryReturn;
            }
        }
        return null;
    }

    @Override
    public Package getPackage(String name) {
        logger.info("getPackage: " + name);
        return super.getPackage(name);
    }

    @Override
    public Package[] getPackages() {
        Package[] packs = super.getPackages();
        for (int i = 0; i < packs.length; i++) {
            //  logger.info(packs[i].getName());
        }
        return packs;
    }

    public String toString() {
        for (URL url : urls) {
            logger.info(url.toString());
        }
        return "";
    }

    private class JarFileEntry {
        public JarFile jarFile = null;
        public JarEntry curJarEntry = null;
        public String originPath = "";
    }
}
