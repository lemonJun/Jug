package lemon.jug.web.connector;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

import lemon.jug.web.container.DefaultContainer;
import lemon.jug.web.container.FilterContainer;
import lemon.jug.web.container.PipeLine;

/**
 * http请求的主处理类
 * 
 * @author lemon
 * @date  2016年10月28日 下午5:32:16
 * @see
 */
@Singleton
public class HttpServletProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletProcessor.class);

    public static final HttpServletProcessor instance = new HttpServletProcessor();

    private PipeLine pipeLine = null;

    private volatile AtomicBoolean once = new AtomicBoolean(false);

    public static HttpServletProcessor getInstance() {
        return instance;
    }

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) {
        if (once.compareAndSet(false, true)) {
            initialize();
        }
        HttpRequestFacade httpRequestFacade = (HttpRequestFacade) request;
        HttpResponseFacade httpResponseFacade = (HttpResponseFacade) response;
        this.pipeLine.invoke(httpRequestFacade, httpResponseFacade);
        logger.info("process ");
        //        httpResponseFacade.setCommitted();
        //        httpResponseFacade.setOnCommitted();
    }

    public void initialize() {
        this.pipeLine = new PipeLine();
        //        this.pipeLine.setConfig(serviceConfig);
        this.pipeLine.initialize();
        this.pipeLine.addContainer(new FilterContainer());
        this.pipeLine.addContainer(new DefaultContainer());
    }

    public void start() {
        pipeLine.start();
    }

    public void stop() {
        pipeLine.stop();
    }
}
