package com.rxf113.miraiqqbot.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import okhttp3.*;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 请求工具类
 *
 * @author rxf113
 */
public class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    static ObjectMapper objectMapper = new ObjectMapper();
    static OkHttpClient client = OkhttpEnum.INSTANCE.getClient();

    @SuppressWarnings("all")
    public static String[] sessionReq(String copyCookie) throws IOException {
        //从浏览器请求的请求头，或者 application -> cookie 复制出 __Secure-next-auth.session-token 的key和value。   ps: (__Secure-next-auth.callback-url 和 __Host-next-auth.csrf-token 可要可不要)
        //"__Secure-next-auth.session-token=eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2R0NNIn0..R3Kc7SzojKpBGjqD.A8_9Dr后面省略"

        HashMap<String, String> headerMap = Maps.newHashMap();
        headerMap.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
        headerMap.put("cookie", copyCookie);
        headerMap.put("accept-encoding", "gzip, deflate, br");
        //session接口
        Request getRequest = new Request.Builder()
                .url("https://chat.openai.com/api/auth/session")
                .get()
                .headers(Headers.of(headerMap))
                .build();

        Response responseSession = client.newCall(getRequest).execute();
        String result = CharStreams.toString(new InputStreamReader(responseSession.body().byteStream(), StandardCharsets.UTF_8));
        logger.info("session response: {}", result);
        Map map = objectMapper.readValue(result, Map.class);
        String setCookie = responseSession.headers().get("set-cookie");
        Map<String, String> collect = Splitter.on(";").splitToList(setCookie)
                .stream().filter(it -> it.contains("=")).map(it -> it.split("="))
                .collect(Collectors.toMap(it -> it[0], it -> it[1]));
        String nextAuthSessionToken = collect.get("__Secure-next-auth.session-token");
        String authorization = (String) map.get("accessToken");
        logger.info("nextAuthSessionToken: {}", nextAuthSessionToken);
        MiraiConfig.SESSION_TOKEN = nextAuthSessionToken;
        logger.info("authorization: {}", authorization);
        return new String[]{nextAuthSessionToken, authorization};
    }

    public static String conversation(String[] auths, String requestBody) throws IOException {
        //这个 requestBody 可以作为模板写死，不同的请求只需要修改里面的query
//        String requestBody = "{\"parent_message_id\":\"" + UUID.randomUUID()
//                + "\",\"action\":\"next\",\"messages\":[{\"role\":\"user\",\"id\":\""
//                + UUID.randomUUID() + "\",\"content\":{\"content_type\":\"text\",\"parts\":[\"" + "query" + "\"]}}]," +
//                "\"model\":\"text-davinci-002-render\"}";

        String nextAuthSessionToken = auths[0];
        String authorization = auths[1];
        //替换那个 cookie
        Map<String, String> conCookieMap = new HashMap<>(4, 1);
        //这个就是上面复制的cookie里的内容
        conCookieMap.put("__Secure-next-auth.session-token", nextAuthSessionToken);
        StringBuilder sb = new StringBuilder();
        conCookieMap.forEach((k, v) -> sb.append(k).append("=").append(v).append("; "));
        sb.deleteCharAt(sb.length() - 2);


        HashMap<String, String> hashMap = Maps.newHashMap();
        hashMap.put("accept-encoding", "gzip, deflate, br");
        hashMap.put("accept-language", "zh-CN,zh;q=0.9");
        hashMap.put("authorization", "Bearer " + authorization);
        hashMap.put("content-type", "application/json");
        hashMap.put("cookie", sb.toString().trim());
        hashMap.put("origin", "https: //chat.openai.com");
        hashMap.put("referer", "https: //chat.openai.com/chat");
        hashMap.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
        Request post = new Request.Builder()
                .headers(Headers.of(hashMap))
                .url("https://chat.openai.com/backend-api/conversation")
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody)).build();

        Call call = client.newCall(post);
        Response response = call.execute();
        if (response.isSuccessful()) {
            //处理response的响应消息
            String res = CharStreams.toString(new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8));
            //这里是连续多行
            //a
            //a b
            //a b c
            //这直接取倒数第二行
            String[] split = res.split("\n");
            List<String> collect1 = Arrays.stream(split).filter(Strings::isNotBlank)
                    .collect(Collectors.toList());
            String fullLine = collect1.get(collect1.size() - 2);
            Map map1 = objectMapper.readValue(fullLine.substring(5), Map.class);
            ArrayList list = (ArrayList) ((Map) ((Map) map1.get("message")).get("content")).get("parts");
            return (String) list.get(0);
        } else {
            logger.error("fail: " + response.code());
        }
        return "服务出错了, g了";
    }
}
