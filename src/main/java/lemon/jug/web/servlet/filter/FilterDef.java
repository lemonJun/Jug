package lemon.jug.web.servlet.filter;

import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * 一个filter定义
 *
 * @author lemon
 * @date  2016年10月29日 下午6:43:42
 * @see
 */
public class FilterDef {

    private Filter filter = null;
    private String filterClass = null;
    private IApplicationFilterConfig applicationFilterConfig = null;
    private ClassLoader classLoader = null;

    public FilterDef(ClassLoader classLoader, String filterClass) throws Exception {
        this.classLoader = classLoader;
        if (this.classLoader == null) {
            this.classLoader = this.getClass().getClassLoader();
        }
        this.filterClass = filterClass;
        initFilter();
    }

    public void initConfig(IApplicationFilterConfig filterConfig) throws Exception {
        this.filter.init(filterConfig);
    }

    public void initConfig(String filterName, ServletContext servletContext, Map<String, String> initParams) throws ServletException {
        applicationFilterConfig = new ApplicationFilterConfig();
        applicationFilterConfig.setFilterName(filterName);
        applicationFilterConfig.setServletContext(servletContext);
        applicationFilterConfig.setInitParameter(initParams);
        this.filter.init(applicationFilterConfig);
    }

    public IApplicationFilterConfig getConfig() {
        return applicationFilterConfig;
    }

    private void initFilter() throws Exception {
        this.filter = (Filter) classLoader.loadClass(filterClass).newInstance();
    }

    public void destroy() {
        this.filter.destroy();
    }

    public Filter getFilter() {
        return filter;
    }

    @Override
    public String toString() {
        return "FilterDef [filter=" + filter + ", filterClass=" + filterClass + ", classLoader=" + classLoader + "]";
    }

}
