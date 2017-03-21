package lemon.jug.web.servlet.filter;

import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

/**
 * Created by lemon on 16/1/22.
 */
public interface IApplicationFilterConfig extends FilterConfig {
    
    public void setFilterName(String name);

    public void setServletContext(ServletContext servletContext);

    public void setInitParameter(Map<String, String> params);

    public void setUrlPattern(String pattern);

    public String getUrlPattern();
}
