package lemon.jug.web.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class NettyHttpHandler extends ChannelHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyHttpHandler.class);
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            FullHttpResponse fullHttpresponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            if (!fullHttpRequest.decoderResult().isSuccess()) {
                fullHttpresponse.setStatus(HttpResponseStatus.BAD_REQUEST);
                ctx.writeAndFlush(fullHttpresponse);
                return;
            }

            if (!fullHttpRequest.uri().startsWith("/")) {
                logger.info(String.format("uri:%s should begin with:/ ", fullHttpRequest.uri()));
                fullHttpresponse.setStatus(HttpResponseStatus.NOT_FOUND);
                ctx.writeAndFlush(fullHttpresponse);
                return;
            }

            fullHttpresponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8");

            HttpRequestFacade request = new HttpRequestFacade(fullHttpRequest);
            HttpResponseFacade response = new HttpResponseFacade(fullHttpresponse);
            //区分静态资源请求与servlet请求
            response.setHttpRequestFacade(request);
            
            //真正的处理入口类
            HttpServletProcessor hsp = HttpServletProcessor.getInstance();
            hsp.process(request, response);
            ctx.writeAndFlush(response);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
