package lemon.jug.web.servlet;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;

public class MappingData {
    
    public String urlPattern = "";
    public String servletClass = "";
    public String servletName = "";
    public ServletConfig servletConfig = null;
    public GenericServlet servlet = null;
    public int loadOnStartUp = -1;

    public MappingData(String servletName, String servletClass) {
        this.servletClass = servletClass;
        this.servletName = servletName;
    }

    public MappingData(String servletName, String servletClass, String urlPattern) {
        this.urlPattern = urlPattern;
        this.servletClass = servletClass;
        this.servletName = servletName;
    }

    public MappingData(String servletName, String servletClass, String urlPattern, int startUp) {
        this.urlPattern = urlPattern;
        this.servletClass = servletClass;
        this.servletName = servletName;
        this.loadOnStartUp = startUp;
    }

    public String toString() {
        return String.format("[MappingData(servletName: %s; servletClass: %s; urlPattern: %s; loadOnStartUp: %d)]", servletName, servletClass, urlPattern, loadOnStartUp);
    }
}
