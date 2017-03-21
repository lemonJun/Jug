package lemon.jug.web.container;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lemon.jug.web.component.LifeCycle;

public interface Container extends LifeCycle {

    public void invoke(HttpServletRequest request, HttpServletResponse response);

    public void initialize();

    public void setNext(Container container);

    public Container getNext();
}
