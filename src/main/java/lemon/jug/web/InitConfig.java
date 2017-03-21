package lemon.jug.web;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class InitConfig<T> {

    private <T> T init(String path, T t) {
        try {
            PropertiesConfiguration config = new PropertiesConfiguration("");
            config.getString("");
            return null;
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

}
