package lemon.jug.web.connector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Processor {

    public void initialize();

    public void process(HttpServletRequest request, HttpServletResponse response);
    
    
}
