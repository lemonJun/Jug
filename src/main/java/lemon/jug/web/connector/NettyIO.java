package lemon.jug.web.connector;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

@Singleton
public class NettyIO {

    private static final Logger logger = LoggerFactory.getLogger(NettyIO.class);

    private EventLoopGroup workerGroup = null;
    private EventLoopGroup bossGroup = null;
    private ServerBootstrap serverBootstrap = null;

    private NioConfig nioconfig = new NioConfig();
    public static final ConcurrentMap<Integer, ResponseFuture> responseTable = Maps.newConcurrentMap();

    private final ScheduledExecutorService respScheduler;

    public NettyIO() {
        respScheduler = new ScheduledThreadPoolExecutor(1);
    }

    public void initialize() {
        this.workerGroup = new NioEventLoopGroup();
        this.bossGroup = new NioEventLoopGroup();
        this.serverBootstrap = new ServerBootstrap();
        this.serverBootstrap.group(bossGroup, workerGroup);
        this.serverBootstrap.channel(NioServerSocketChannel.class);
        this.serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel sc) throws Exception {
                //				if (SimpleNettyServer.this.serviceConfig.useSSL) {
                //					SSLEngine engine = new MySSlEngine();
                //		            engine.setUseClientMode(false);
                //		            sc.pipeline.addLast("ssl", new SslHandler(engine));
                //				}
                sc.pipeline().addLast("idleStateHandler", new IdleStateHandler(60, 60, 60));
                sc.pipeline().addLast("http-decoder", new HttpRequestDecoder());
                sc.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
                sc.pipeline().addLast("http-encoder", new HttpResponseEncoder());
                sc.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
                sc.pipeline().addLast("netty-handler", new NettyHttpHandler());
            }
        });
        this.serverBootstrap.option(ChannelOption.SO_BACKLOG, nioconfig.getSo_back_log());
        this.serverBootstrap.option(ChannelOption.SO_KEEPALIVE, nioconfig.isSo_keep_alive());
    }

    public void start(String ip, int port) {
        try {
            this.serverBootstrap.bind(ip, port).sync();
            respScheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    scanResponseTable(5000);
                }
            }, 5 * 1000, 5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.info("", e);
        }
    }

    /**
     * 
     * 描述所有的对外请求
     * 看是否有超时未处理情况
     * 此超时时间已经考虑了 任务的默认等待时间
     */
    public void scanResponseTable(long timeout) {
        Iterator<Entry<Integer, ResponseFuture>> it = responseTable.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, ResponseFuture> next = it.next();
            ResponseFuture rep = next.getValue();

            if ((rep.getBeginTimestamp() + rep.getTimeoutMillis() + timeout) <= SystemClock.now()) {
                logger.info(String.format("remove responsefuture ", rep.getOpaque()));
                it.remove();
            }
        }
    }

    public void stop() {
        try {
            logger.info("receive shutdown listener ");
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
                logger.info("shotdown bossGroup");
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
                logger.info("shotdown workerGroup");
            }
        } catch (Exception e) {
            logger.error("shutsown error", e);
        }

    }

}
