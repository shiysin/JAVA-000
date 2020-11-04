package com.geektime.week3.gateway.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public class NioRequestFilter implements HttpRequestFilter{

    @Override
    public void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        if(null != fullRequest) {
            fullRequest.headers().add("nio", "shiysin");
        }
    }
}
