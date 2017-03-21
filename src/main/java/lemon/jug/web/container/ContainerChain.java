package lemon.jug.web.container;

/**
 * Created by lemon on 16/1/22.
 */
public interface ContainerChain {

    public void setNext(Container container);

    public Container getNext();
}
