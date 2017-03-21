package lemon.jug.web.webapp;

public class WebApp {

    private String resourceBase;//工程目录
    private String contextPath;//匹配的访问路径
    private String servername;//服务名称
    private WebAppIniter appiniter;

    public void init() throws Exception {
        //扫描服务配置
        appiniter = new WebAppIniter(resourceBase);
        appiniter.scan();
        //初始化webfilter servlet listener 
        //
    }

    public String getResourceBase() {
        return resourceBase;
    }

    public void setResourceBase(String resourceBase) {
        this.resourceBase = resourceBase;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getServername() {
        return servername;
    }

    public void setServername(String servername) {
        this.servername = servername;
    }

}
