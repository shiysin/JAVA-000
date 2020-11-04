package com.geektime.week3.gateway.outbound.okhttp;

import com.geektime.week3.gateway.router.HttpEndpointRouter;
import com.geektime.week3.gateway.router.HttpEndpointRouterImpl;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class OkhttpOutboundHandler {

    private List<String> backendUrls;
    private final OkHttpClient client;
    private final HttpEndpointRouter router = new HttpEndpointRouterImpl();

    public OkhttpOutboundHandler(String backendUrl) {
        client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();

        String[] backendUrls = backendUrl.split(",");

        this.backendUrls = Arrays.stream(backendUrls)
                .map(url -> url.endsWith("/") ? url.substring(0, url.length() - 1) : url)
                .collect(Collectors.toList());
    }

    public void handle(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx) {
        final String url = this.router.route(backendUrls) + fullRequest.uri();
        this.fetchGet(fullRequest, ctx, url);
    }

    private void fetchGet(final FullHttpRequest inbound, final ChannelHandlerContext ctx, final String url) {
        Request.Builder requestBuilder = new Request.Builder();
        // copy headers
        inbound.headers().forEach(entry -> requestBuilder.header(entry.getKey(), entry.getValue()));
        Request request = requestBuilder
                .url(url)
                .get()
                .build();
        // print to see if headers have been copy
        System.out.println("----------------");
        System.out.print(request.headers());
        System.out.println("----------------");

        Call call = client.newCall(request);
        Response response = null;
        try {
            response = call.execute();
            handleResponse(inbound, ctx, response);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void handleResponse(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx, final Response endpointResponse) {
        FullHttpResponse response = null;
        try {
            byte[] body = endpointResponse.body().bytes();

            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(body));
            response.headers().set("Content-Type", "application/json");
            response.headers().setInt("Content-Length", Integer.parseInt(endpointResponse.header("Content-Length")));

        } catch (Exception e) {
            e.printStackTrace();
            response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
            exceptionCaught(ctx, e);
        } finally {
            if (fullRequest != null) {
                if (!HttpUtil.isKeepAlive(fullRequest)) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    //response.headers().set(CONNECTION, KEEP_ALIVE);
                    ctx.write(response);
                }
            }
            ctx.flush();
            //ctx.close();
        }

    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}

