package lemon.jug.web.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

/**
 * netty的接收处理类
 * 
 * @author lemon
 * @date  2016年10月28日 下午5:52:39
 * @see
 */
public class NettyHandler extends ChannelHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
        FullHttpResponse fullHttpresponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        if (!fullHttpRequest.decoderResult().isSuccess()) {
            fullHttpresponse.setStatus(HttpResponseStatus.BAD_REQUEST);
            NettyHelper.flushAndClose(ctx, fullHttpresponse);
            return;
        }

        if (!fullHttpRequest.uri().startsWith("/")) {
            logger.info(String.format("uri:%s should begin with:/ ", fullHttpRequest.uri()));
            fullHttpresponse.setStatus(HttpResponseStatus.NOT_FOUND);
            NettyHelper.flushAndClose(ctx, fullHttpresponse);
            return;
        }

        //Default content-type: text/html; charset=utf-8
        fullHttpresponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8");
        //构建request、response

        //        HttpRequest httpRequest = new HttpRequest(fullHttpRequest);
        //        HttpResponse httpResponse = new HttpResponse(response);
        //        httpRequest.setConnectionConfig(this.getServer().getParent().getConnectorConfig());
        //        httpRequest.setServiceConfig(this.server.getConfig());
        //        httpRequest.setCtx(ctx);
        //        httpResponse.setConnectionConfig(this.getServer().getParent().getConnectorConfig());
        //        httpResponse.setServiceConfig(this.server.getConfig());
        //        httpResponse.setCtx(ctx);
        HttpRequestFacade request = new HttpRequestFacade(fullHttpRequest);
        HttpResponseFacade response = new HttpResponseFacade(fullHttpresponse);
        //区分静态资源请求与servlet请求
        String pathInfo = NettyHelper.getPathInfo(fullHttpRequest);
        //        if (URLMatch.match(pathInfo, this.server.getConfig().staticResourceUrlPattern)) {//静态资源请求
        //            //static resource
        //            StaticResourceProcessor srp = (StaticResourceProcessor) this.server.getParent().getParent().getStaticResourceProcessor();
        //            srp.process(request, response);
        //        } else {
        //servlet resource
        response.setHttpRequestFacade(request);

        //真正的处理入口类
        HttpServletProcessor hsp = HttpServletProcessor.getInstance();
        hsp.process(request, response);
        NettyHelper.flushAndClose(ctx, response);

        //         }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            NettyHelper.flushAndClose(ctx, response);
        }
    }

    public static class NettyHelper {
        
        public static void flushAndClose(ChannelHandlerContext ctx, FullHttpResponse response) {
            try {
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE).sync();
            } catch (InterruptedException e) {
                logger.info("", e);
            }
        }

        public static void flushAndClose(ChannelHandlerContext ctx, Object response) {
            try {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE).sync();
            } catch (InterruptedException e) {
                logger.info("", e);
            }
        }

        public static String getPathInfo(FullHttpRequest fullHttpRequest) {
            String uri = fullHttpRequest.uri();
            int slashIndex = uri.indexOf("/", 1);
            int queIndex = uri.indexOf("?", 0);
            if (slashIndex > queIndex && queIndex != -1) {
                slashIndex = -1;
            } else if (slashIndex + 1 == queIndex) {
                slashIndex = -1;
            }
            if (slashIndex == -1) {
                return "/";
            } else {
                return uri.substring(slashIndex, (queIndex == -1 ? uri.length() : queIndex));
            }
        }

        public static void setResponseContent(FullHttpResponse response, String content) {
            ByteBuf buf = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
            response.content().writeBytes(buf);
        }
    }
}
