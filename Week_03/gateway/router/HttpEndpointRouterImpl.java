package com.geektime.week3.gateway.router;

import java.util.List;
import java.util.Random;

public class HttpEndpointRouterImpl implements HttpEndpointRouter{
    @Override
    public String route(List<String> endpoints) {
        Random random = new Random(System.currentTimeMillis());
        String selectedEndpoint = endpoints.get(random.nextInt(endpoints.size()));
        System.out.println(String.format("select endpoint:%s, from endpoints:%s", selectedEndpoint, endpoints));
        return selectedEndpoint;
    }
}
