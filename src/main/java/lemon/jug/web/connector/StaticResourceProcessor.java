package lemon.jug.web.connector;
//package lemon.jug.web.handler;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.jnetty.core.Config.ServiceConfig;
//import com.jnetty.core.container.DefaultContainer;
//import com.jnetty.core.response.HttpResponse;
//
//public class StaticResourceProcessor implements Processor {
//
//    private static final Logger logger = LoggerFactory.getLogger(DefaultContainer.class);
//
//    private ServiceConfig serviceConfig = null;
//
//    @Override
//    public void process(HttpServletRequest request, HttpServletResponse response) {
//
//        String uri = request.getRequestURI();
//        String resourcePath = "";
//        int indexSrc = uri.indexOf(this.serviceConfig.staticResourceUrlPattern);
//        int indexSlash = uri.indexOf("/", indexSrc + this.serviceConfig.staticResourceUrlPattern.length());
//        int queIndex = uri.indexOf("?");
//        if (queIndex == -1) {
//            if (indexSlash == -1) {
//                resourcePath = "/";
//            } else {
//                resourcePath = uri.substring(indexSlash);
//            }
//        } else {
//            resourcePath = uri.substring(indexSlash, queIndex);
//        }
//
//        resourcePath = this.serviceConfig.staticResourceLoc + resourcePath;
//        try {
//            ((HttpResponse) response).sendResource(resourcePath);
//        } catch (Exception e) {
//            logger.info("", e);
//        }
//    }
//
//    public void initialize() {
//
//    }
//
//    public ServiceConfig getConfig() {
//        return this.serviceConfig;
//    }
//
//    public void setConfig(ServiceConfig config) {
//        this.serviceConfig = config;
//    }
//
//    public void start() {
//
//    }
//
//    public void stop() {
//
//    }
//}
