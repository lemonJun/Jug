package lemon.jug.web.webapp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import lemon.jug.web.servlet.filter.ApplicationFilterChain;
import lemon.jug.web.servlet.filter.ApplicationFilterConfig;
import lemon.jug.web.servlet.filter.FilterDef;
import lemon.jug.web.util.AnnotationUtils;
import lemon.jug.web.util.ClassUtils;

/**
 * 一个detector归一个webappcontext所有
 * 并完成所有的初始化操作
 * @author lemon
 *  
 *  根路径为JNettyBase
 *	  探测Webapp，解压缩WAR文件（JARFile），初始化ClassLoader，配置ServiceConfig
 */
public class WebAppIniter {

    private static final Logger logger = LoggerFactory.getLogger(WebAppIniter.class);

    //    private String absoluteStaticResourcePath = null;
    private String webAppPath = null;
    private WebXmlParser webXmlParser = null;
    List<Class<?>> clazzlist = Lists.newArrayList();

    private WebAppClassLoader classLoader = null;

    public WebAppIniter(String webAppPath) {
        this.webAppPath = webAppPath;
    }

    public void scan() throws Exception {
        logger.info("webAppPath:" + webAppPath);
        File webappFile = new File(this.webAppPath);

        if (webappFile.isDirectory()) {
            if (!webappFile.exists()) {
                throw new FileNotFoundException(webappFile.getAbsolutePath());
            }
        } else {//如果是war文件  刚解压
            webappFile = new File(this.webAppPath + ".war");
            if (!webappFile.exists()) {
                throw new FileNotFoundException("Unsupport file type[must be a directory or a war file.]: " + webappFile.getAbsolutePath());
            }
            this.extractWarFile(webappFile);
        }
        //create and init servlet class loader
        //下面这一步 应该就直接给config了，但是这样的话就给不了了 
        createClassLoader();

        //parse web.xml
        webXmlParser = new WebXmlParser(new File(webAppPath + "/WEB-INF/web.xml"));
        webXmlParser.parse();

        //parse anno 从注解中提取webfilter
        parseAnno();

    }

    /**
     * 解析MVC框架中的WEBFILTER类
     * 
     */
    private void parseAnno() {
        try {
            Set<Class<?>> classSet = ClassUtils.getLocalClasses("");//默认只找当前类加载器下的

            List<Class<?>> filters = Lists.newArrayList();
            List<Class<?>> servlets = Lists.newArrayList();
            List<Class<?>> listeners = Lists.newArrayList();

            for (Class<?> clazz : classSet) {
                if (AnnotationUtils.isClassAnnotationed(clazz, WebFilter.class)) {
                    filters.add(clazz);
                } else if (AnnotationUtils.isClassAnnotationed(clazz, WebServlet.class)) {
                    logger.info("add servlet:" + clazz.getName());
                    servlets.add(clazz);
                } else if (AnnotationUtils.isClassAnnotationed(clazz, WebListener.class)) {
                    listeners.add(clazz);
                }
            }
            for (Class<?> cla : servlets) {

            }
            dealFilter("");
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    /**
     * 应该是本程序来扫描  然后用simpleclassloader去加载这个类
     * 不过 在一个正规则的web容器中应该是会 全部加载所有的类的啊
     * @param classNames
     */
    private void dealFilter(String classNames) {
        try {
            String className = "lemon.mvc.mvc.MvcFilter";
            Class<?> clazz = classLoader.findClass(className);
            Preconditions.checkNotNull(clazz);
            WebFilter webfilter = clazz.getAnnotation(WebFilter.class);
            Preconditions.checkNotNull(clazz);
            ApplicationFilterConfig config = new ApplicationFilterConfig();
            ServletContext servletContext = new DefaultServletContext();

            config.setUrlPattern(webfilter.urlPatterns()[0]);
            config.setFilterName(className);
            config.setServletContext(servletContext);
            //            config.setInitParameter();
            FilterDef filterDef = new FilterDef(classLoader, className);
            filterDef.initConfig(config);
            ApplicationFilterChain.getInstance().addFilterDef(filterDef);
            logger.info(String.format("deal filter:%s", className));
        } catch (Exception e) {
            logger.error("deal filter:{} error", classNames, e);
        }
    }

    private void extractWarFile(File warFile) throws Exception {
        //make a dir for webapp
        File dir = new File(this.webAppPath);
        dir.mkdir();
        JarFile jarFile = new JarFile(warFile);
        Enumeration<JarEntry> entries = jarFile.entries();
        byte[] buffer = new byte[1024];
        //copy file to dir
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                File dirFile = new File(webAppPath + "/" + entry.getName());
                if (!dirFile.exists()) {
                    dirFile.mkdir();
                }
            } else {
                File newFile = new File(webAppPath + "/" + entry.getName());
                newFile.createNewFile();
                BufferedInputStream bufferedIns = new BufferedInputStream(jarFile.getInputStream(entry));
                BufferedOutputStream bufferedOus = new BufferedOutputStream(new FileOutputStream(newFile));
                int count = -1;
                while ((count = bufferedIns.read(buffer)) > 0) {
                    bufferedOus.write(buffer, 0, count);
                }
                bufferedOus.close();
                bufferedIns.close();
            }
        }
        jarFile.close();
    }

    private void createClassLoader() {
        //record jar file url
        try {
            File libFile = new File(webAppPath + "/WEB-INF/lib/");
            List<URL> urlList = Lists.newArrayList();

            if (libFile.exists()) {
                File[] jarFiles = libFile.listFiles();
                for (File jarFile : jarFiles) {
                    if (jarFile.isFile() && jarFile.getName().endsWith(".jar")) {
                        urlList.add(jarFile.toURI().toURL());
                    }
                }
            }
            urlList.add(new File(webAppPath + "/WEB-INF/classes/").toURI().toURL());
            URL[] urls = new URL[urlList.size()];
            urlList.toArray(urls);
            for (URL u : urlList) {
                logger.info("use:" + u.toString());
            }
            WebAppClassLoader classloader = new WebAppClassLoader(webAppPath, urls, Thread.currentThread().getContextClassLoader());
            this.classLoader = classloader;
            logger.info("classloader init...");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    //    private String getAbsoluteStaticResourcePath() {
    //        String path = "";
    //
    //        serviceConfig.WebAppName.replaceAll("/", "");
    //        if (serviceConfig.JNettyBase.endsWith("/")) {
    //            path = serviceConfig.JNettyBase + serviceConfig.WebAppName;
    //        } else {
    //            path = serviceConfig.JNettyBase + "/" + serviceConfig.WebAppName;
    //        }
    //
    //        this.webAppPath = path;
    //
    //        if (serviceConfig.staticResourceLoc.startsWith("/")) {
    //            path = path + serviceConfig.staticResourceLoc;
    //        } else {
    //            path = path + "/" + serviceConfig.staticResourceLoc;
    //        }
    //
    //        //remove the last /
    //        if (path.endsWith("/")) {
    //            path = path.substring(0, path.length() - 1);
    //        }
    //
    //        return path;
    //    }
}
