package com.geektime.week2;


import okhttp3.*;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

public class HttpClientTest {

    private static final String url = "http://localhost:8801/";

    public static void main(String[] args) {
        try {
            httpClient();
//            OkHttpClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void httpClient() throws IOException {
        CloseableHttpClient client = HttpClients.custom().build();
        HttpUriRequest request = RequestBuilder.get()
                .setUri(url)
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
        HttpResponse response = client.execute(request);

        Scanner scanner = new Scanner(response.getEntity().getContent());

        System.out.println(response.getStatusLine());
        while (scanner.hasNext()) {
            System.out.println(scanner.nextLine());
        }
    }

    public static void OkHttpClient() throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent","")
                .build();
        Response response = okHttpClient.newCall(request).execute();
        System.out.println(response.isSuccessful());
        System.out.println(response.headers());
        System.out.println("responseBody: " + response.body().string());
    }
}
