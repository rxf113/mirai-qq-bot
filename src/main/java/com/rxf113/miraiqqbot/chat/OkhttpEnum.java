package com.rxf113.miraiqqbot.chat;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

enum OkhttpEnum {
    //**//
    INSTANCE;

    private final OkHttpClient client;

    OkhttpEnum() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        // dispatcher
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(200);
        dispatcher.setMaxRequestsPerHost(200);

        // connectionPool
        ConnectionPool connectionPool = new ConnectionPool(2000, 5, TimeUnit.MINUTES);

        builder.dispatcher(dispatcher);
        builder.connectionPool(connectionPool);
        //超时时间长点，得等请求完全返回
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);
        client = builder.build();
    }

    public OkHttpClient getClient() {
        return client;
    }
}