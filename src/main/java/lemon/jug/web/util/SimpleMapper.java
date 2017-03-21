//package lemon.jug.web.util;
//
//import java.util.List;
//
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.jnetty.core.Config.MappingData;
//import com.jnetty.core.Config.ServiceConfig;
//
//public class SimpleMapper {
//
//    private static final Logger logger = LoggerFactory.getLogger(SimpleMapper.class);
//
//    private List<MappingData> servletList = null;
//    private ClassLoader classLoader = null;
//
//    public SimpleMapper(ServiceConfig config) {
//        this.servletList = config.servletList;
//        this.classLoader = config.servletClassLoader;
//    }
//
//    public HttpServlet getHttpServlet(HttpServletRequest request) {
//        HttpServlet servlet = null;
//        String pathInfo = request.getPathInfo();
//        for (MappingData mapping : servletList) {
//            String path = mapping.urlPattern;
//            if (!URLMatch.match(pathInfo, path))
//                continue;
//            try {
//                servlet = (HttpServlet) mapping.servlet;
//                if (servlet == null) {
//                    servlet = (HttpServlet) Class.forName(mapping.servletClass, true, this.classLoader).newInstance();
//                    servlet.init(mapping.servletConfig);
//                    mapping.servlet = servlet;
//                }
//                break;
//            } catch (Exception e) {
//                logger.error("", e);
//                break;
//            }
//        }
//        return servlet;
//    }
//}
