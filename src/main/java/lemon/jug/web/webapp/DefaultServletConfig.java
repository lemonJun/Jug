package lemon.jug.web.webapp;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import lemon.jug.web.util.EnumerationImplIterator;

/**
 * 原来是还继承ServletContextConfig接口的
 * 但这个接口存在的很奇怪,因为不知道应该对应的是哪个对象
 * servletcontext对应的是一个应用
 * servletconfig对应的是一个servlet配置
 * 
 *
 * @author lemon
 * @date  2016年11月4日 下午4:24:42
 * @see
 */
public class DefaultServletConfig implements ServletConfig {
    
    private Map<String, String> initParams = null;
    //    private ServletContextConfig servletContextConfig = null;
    private ServletContext servletContext = null;
    private String servletName = null;

    public DefaultServletConfig() {
        initParams = new HashMap<String, String>();
    }

    public void setInitParams(Map<String, String> initParams) {
        this.initParams = initParams;
    }

    //    public void setServletContextConfig(ServletContextConfig servletContextConfig) {
    //        this.servletContextConfig = servletContextConfig;
    //    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    @Override
    public String getServletName() {
        return this.servletName;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public String getInitParameter(String name) {
        return this.initParams.get(name);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Enumeration getInitParameterNames() {
        return new EnumerationImplIterator<String>(this.initParams.keySet().iterator());
    }

}
