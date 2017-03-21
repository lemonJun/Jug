package lemon.jug.web.servlet.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lemon.jug.web.util.URLMatch;

/**
 * 真正的filter是可以处理多个的 这样肯定是不对的
 * 
 * @author lemon
 * @date  2016年10月29日 下午6:39:05
 * @see
 */
public class ApplicationFilterChain implements FilterChain {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationFilterChain.class);

    private List<FilterDef> filterDefs = new ArrayList<FilterDef>();

    private ThreadLocal<Integer> curFilterIndex = new ThreadLocal<Integer>();

    private static final ApplicationFilterChain filterchain = new ApplicationFilterChain();

    public static ApplicationFilterChain getInstance() {
        return filterchain;
    }

    public void addFilterDef(FilterDef filterDef) {
        filterDefs.add(filterDef);
    }

    /**
     * 按加入的过滤以器的顺序 逐个调用
     * 原来的方式是 只找一个进行调用
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        logger.info("do filter");

        String pathInfo = ((HttpServletRequest) request).getPathInfo();
        for (int i = 0; i < filterDefs.size(); i++) {
            FilterDef filterDef = filterDefs.get(i);
            if (filterDef.getClass() == null || filterDef.getConfig() == null) {
                continue;
            }
            logger.info(String.format("filter:%s path:%s urlpattern:%s", filterDef.getClass().getName(), pathInfo, filterDef.getConfig().getUrlPattern()));
            if ("/*".equals(filterDef.getConfig().getUrlPattern()) || URLMatch.match(filterDef.getConfig().getUrlPattern(), pathInfo)) {
                filterDef.getFilter().doFilter(request, response, this);
            }
        }

    }

    /**
     * invoke by filterContainer
     */
    public void reset() {
        curFilterIndex.set(0);
    }

    public void destroy() {
        for (FilterDef filterDef : filterDefs) {
            filterDef.destroy();
        }
    }

}
