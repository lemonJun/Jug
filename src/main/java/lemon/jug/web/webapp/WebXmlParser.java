package lemon.jug.web.webapp;

import java.io.File;
import java.util.Comparator;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import javax.servlet.GenericServlet;
import javax.servlet.ServletContext;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import lemon.jug.web.JugConfig;
import lemon.jug.web.servlet.MappingData;
import lemon.jug.web.servlet.filter.ApplicationFilterChain;
import lemon.jug.web.servlet.filter.FilterDef;

/**
 * 解析web.xml参数
 * 但这个不是必须的
 *
 * @author lemon
 * @date  2016年10月28日 下午5:20:28
 * @see
 */
public class WebXmlParser {

    private static final Logger logger = LoggerFactory.getLogger(WebXmlParser.class);

    public static final String CONTEXT_PARAM = "context-param";
    public static final String SERVLET = "servlet";
    public static final String SERVLET_NAME = "servlet-name";
    public static final String SERVLET_CLASS = "servlet-class";
    public static final String SERVLET_MAPPING = "servlet-mapping";
    public static final String LOAD_ON_STARTUP = "load-on-startup";
    public static final String URL_PATTERN = "url-pattern";
    public static final String INIT_PARAM = "init-param";
    public static final String PARAM_NAME = "param-name";
    public static final String PARAM_VALUE = "param-value";
    public static final String FILTER = "filter";
    public static final String FILTER_NAME = "filter-name";
    public static final String FILTER_CLASS = "filter-class";
    public static final String FILTER_MAPPING = "filter-mapping";
    public static final String LISTENER = "listener";
    public static final String LISTENER_CLASS = "listener-class";

    private File webXmlFile = null;
    private Map<String, Integer> servletsMapping = null;
    private Map<String, FilterDef> filtersMapping = null;
    private PriorityQueue<MappingData> servletInitQueue = null;

    public WebXmlParser(File webXmlFile) {
        this.webXmlFile = webXmlFile;
        this.servletInitQueue = new PriorityQueue<MappingData>(new Comparator<MappingData>() {
            public int compare(MappingData o1, MappingData o2) {
                return o1.loadOnStartUp - o2.loadOnStartUp;
            }
        });
    }
    
    public void parse() throws Exception {
        if (!webXmlFile.exists() || webXmlFile.isDirectory()) {
            throw new Exception(webXmlFile.getAbsolutePath() + " doesn't exits or is a directory.");
        }
        logger.info("parse:" + webXmlFile);
        SAXReader xmlReader = new SAXReader();
        //DOCTYPE 中出现不可访问的url资源,read会被阻塞
        Document dom = xmlReader.read(webXmlFile);
        Element rootElement = dom.getRootElement();
        servletsMapping = new HashMap<String, Integer>();
        filtersMapping = new HashMap<String, FilterDef>();

        ServletContext servletcontext = parseContext(rootElement);

        parseListener(rootElement, servletcontext);

        //        serviceConfig.listenerManager.fireEvent(EventUtils.CONTEXT_INITIALIZED, serviceConfig.listenerManager.getEventUtils().buildServletContextEvent());

        parseServlet(rootElement, servletcontext);
        
        parseFilter(rootElement, servletcontext);
    }

    /**
     * 通过web.xml成生上下文配置悠
     * @param rootElement
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private ServletContext parseContext(Element rootElement) throws Exception {
        //ServletContext, parse
        ServletContext servletContext = new DefaultServletContext();
        //        servletContext.setServiceConfig(serviceConfig);
        //        servletContext.setContextPath("/" + this.serviceConfig.WebAppName);
        //        serviceConfig.servletContextConfig = servletContext;

        //context init params, parse
        Map<String, String> contextParams = new HashMap<String, String>();
        Iterator<Element> contextParamIte = rootElement.elementIterator(CONTEXT_PARAM);
        parseInitParam(contextParamIte, contextParams);
        for (String key : contextParams.keySet()) {
            servletContext.setInitParameter(key, contextParams.get(key));
        }
        //        servletContext.setInitParams(contextParams);

        //SessionManager
        //        ISessionManager sessionManager = new SessionManager(serviceConfig);
        //        sessionManager.setServletContextConfig(servletContext);
        //        servletContext.setSessionManager(sessionManager);

        return servletContext;
    }

    private final List<MappingData> mappinglist = Lists.newArrayList();

    @SuppressWarnings("unchecked")
    private void parseServlet(Element rootElement, ServletContext servletContext) throws Exception {
        //parse servlet
        Iterator<Element> servletIte = rootElement.elementIterator(SERVLET);
        //        int servletCount = this.serviceConfig.servletList.size();
        while (servletIte.hasNext()) {
            Element serlvetElement = servletIte.next();
            Iterator<Element> servletNameIte = serlvetElement.elementIterator(SERVLET_NAME);
            Iterator<Element> servletClassIte = serlvetElement.elementIterator(SERVLET_CLASS);
            Iterator<Element> startUpIte = serlvetElement.elementIterator(LOAD_ON_STARTUP);
            String servletName = "";
            String servletClass = "";
            int loadOnStartUp = -1;
            if (servletNameIte.hasNext()) {
                servletName = servletNameIte.next().getTextTrim();
            }
            if (servletClassIte.hasNext()) {
                servletClass = servletClassIte.next().getTextTrim();
            }
            if (startUpIte.hasNext()) {
                loadOnStartUp = Integer.valueOf(startUpIte.next().getTextTrim());
            }
            //ServletConfig, parse
            DefaultServletConfig servletConfig = new DefaultServletConfig();

            //servlet init params, parse
            Map<String, String> params = new HashMap<String, String>();
            Iterator<Element> initParamIte = serlvetElement.elementIterator(INIT_PARAM);
            parseInitParam(initParamIte, params);

            //init servletMappingData
            MappingData mappingData = new MappingData(servletName, servletClass);
            mappingData.servletConfig = servletConfig;
            //            mappingData.servletContextConfig = servletContext;
            mappingData.loadOnStartUp = loadOnStartUp;

            servletConfig.setInitParams(params);
            //            servletConfig.setServletContextConfig(servletContext);
            servletConfig.setServletContext(servletContext);
            servletConfig.setServletName(servletName);

            servletsMapping.put(servletName, 0);//for parse servlet-mapping node
            mappinglist.add(mappingData);
            //            serviceConfig.servletList.add(mappingData);
            //            servletCount++;
        }
        //parse servlet-mapping
        Iterator<Element> servletMappingIte = rootElement.elementIterator(SERVLET_MAPPING);
        while (servletMappingIte.hasNext()) {
            Element servletMappingElement = servletMappingIte.next();
            Iterator<Element> servletNameIte = servletMappingElement.elementIterator(SERVLET_NAME);
            Iterator<Element> urlPatternIte = servletMappingElement.elementIterator(URL_PATTERN);
            String servletName = "";
            String servletUrlPattern = "";
            if (servletNameIte.hasNext()) {
                servletName = servletNameIte.next().getTextTrim();
            }
            if (urlPatternIte.hasNext()) {
                servletUrlPattern = urlPatternIte.next().getTextTrim();
            }
            int index = servletsMapping.get(servletName);
            //            MappingData mappingData = serviceConfig.servletList.get(index);
            //            if (mappingData != null) {
            //                mappingData.urlPattern = servletUrlPattern;
            //            }
        }

        //servlet init
        //        for (MappingData mappingData : serviceConfig.servletList) {
        for (MappingData mappingData : mappinglist) {
            if (mappingData.loadOnStartUp >= 0) {
                servletInitQueue.add(mappingData);
            }
        }
        int queueSize = servletInitQueue.size();
        for (int q_i = 0; q_i < queueSize; q_i++) {
            MappingData initMapping = servletInitQueue.poll();
            initMapping.servlet = (GenericServlet) Class.forName(initMapping.servletClass, true, JugConfig.class.getClassLoader()).newInstance();
            initMapping.servlet.init(initMapping.servletConfig);
        }
        servletInitQueue.clear();
    }

    @SuppressWarnings("unchecked")
    private void parseFilter(Element rootElement, ServletContext servletContext) throws Exception {
        //        serviceConfig.applicationFilterChain = new ApplicationFilterChain();
        //parse filter
        Iterator<Element> filterIte = rootElement.elementIterator(FILTER);
        while (filterIte.hasNext()) {
            Element filterElement = filterIte.next();
            Iterator<Element> filterNameIte = filterElement.elementIterator(FILTER_NAME);
            Iterator<Element> filterClassIte = filterElement.elementIterator(FILTER_CLASS);
            String filterName = "";
            String filterClass = "";
            if (filterNameIte.hasNext()) {
                filterName = filterNameIte.next().getTextTrim();
            }
            if (filterClassIte.hasNext()) {
                filterClass = filterClassIte.next().getTextTrim();
            }
            logger.info(String.format("filter:%s", filterClass));
            //filterDef parse
            //            FilterDef filterDef = new FilterDef(JugConfig.class.getClassLoader(), filterClass);
            FilterDef filterDef = new FilterDef(JugConfig.class.getClassLoader(), filterClass);
            
            //filter init params, parse
            Map<String, String> params = new HashMap<String, String>();
            Iterator<Element> initParamIte = filterElement.elementIterator(INIT_PARAM);
            parseInitParam(initParamIte, params);
            filterDef.initConfig(filterName, servletContext, params);

            logger.info(String.format("add filter to chain:%s", filterDef.toString()));
            
            //加入filter链
            ApplicationFilterChain.getInstance().addFilterDef(filterDef);
            filtersMapping.put(filterName, filterDef);
        }

        //parse filter-mapping
        Iterator<Element> filterMappingIte = rootElement.elementIterator(FILTER_MAPPING);
        while (filterMappingIte.hasNext()) {
            Element filterMappingElement = filterMappingIte.next();
            Iterator<Element> filterNameIte = filterMappingElement.elementIterator(FILTER_NAME);
            Iterator<Element> urlPatternIte = filterMappingElement.elementIterator(URL_PATTERN);
            String filterName = "";
            String filterUrlPattern = "";
            if (filterNameIte.hasNext()) {
                filterName = filterNameIte.next().getTextTrim();
            }
            if (urlPatternIte.hasNext()) {
                filterUrlPattern = urlPatternIte.next().getTextTrim();
            }

            FilterDef filterDef = filtersMapping.get(filterName);
            if (filterDef != null) {
                filterDef.getConfig().setUrlPattern(filterUrlPattern);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void parseListener(Element rootElement, ServletContext servletContext) throws Exception {
        //        serviceConfig.listenerManager = new ListenerManager();
        //        serviceConfig.listenerManager.initEventUtils(servletContext);
        Iterator<Element> listenerIte = rootElement.elementIterator(LISTENER);
        while (listenerIte.hasNext()) {
            Element listenerElement = listenerIte.next();
            Iterator<Element> listenerClassIte = listenerElement.elementIterator(LISTENER_CLASS);
            String listenerClass = "";
            if (listenerClassIte.hasNext()) {
                Element listenerClassElement = listenerClassIte.next();
                listenerClass = listenerClassElement.getTextTrim();
                //                EventListener eventListener = (EventListener) Class.forName(listenerClass, true, serviceConfig.servletClassLoader).newInstance();
                EventListener eventListener = (EventListener) Class.forName(listenerClass, true, JugConfig.class.getClassLoader()).newInstance();
                //                serviceConfig.listenerManager.addListener(eventListener);
            }
        }
    }

    /**
     *  get name and value from <param-name/> and <param-value/> tags
     */
    @SuppressWarnings("unchecked")
    private void parseInitParam(Iterator<Element> paramIte, Map<String, String> params) {
        while (paramIte.hasNext()) {
            Element initParamElement = paramIte.next();
            Iterator<Element> paramNameIte = initParamElement.elementIterator(PARAM_NAME);
            Iterator<Element> paramValueIte = initParamElement.elementIterator(PARAM_VALUE);
            String paramName = "";
            String paramValue = "";
            if (paramNameIte.hasNext()) {
                paramName = paramNameIte.next().getTextTrim();
            }
            if (paramValueIte.hasNext()) {
                paramValue = paramValueIte.next().getTextTrim();
            }
            params.put(paramName, paramValue);
        }
    }
}
