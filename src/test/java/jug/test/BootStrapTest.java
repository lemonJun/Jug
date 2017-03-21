package jug.test;

import org.apache.log4j.PropertyConfigurator;

import lemon.jug.web.BootStrap;
import lemon.jug.web.connector.NettyServerConnector;
import lemon.jug.web.webapp.WebApp;

public class BootStrapTest {

    public static void main(String[] args) {
        try {
            PropertyConfigurator.configure("D:/log4j.properties");

            BootStrap boot = new BootStrap();
            NettyServerConnector connector = new NettyServerConnector();
            connector.setHost("localhost");
            connector.setPort(8080);
            boot.setConnector(connector);

            WebApp appcontext = new WebApp();
            appcontext.setContextPath("/");
            appcontext.setResourceBase("E:\\server\\webapps\\lemon.mvc-1.0.0");
            boot.setAppcontext(appcontext);
            boot.init();
            boot.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
