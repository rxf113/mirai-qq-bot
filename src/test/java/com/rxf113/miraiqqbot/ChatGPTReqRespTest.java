package com.rxf113.miraiqqbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import com.theokanning.openai.service.OpenAiService;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import retrofit2.Retrofit;
import retrofit2.http.HEAD;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.stream.Collectors;

import static com.theokanning.openai.service.OpenAiService.*;

class ChatGPTReqRespTest {

    @Test
    void testOpenAiService() {

        ObjectMapper mapper = defaultObjectMapper();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));
        OkHttpClient client = defaultClient("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",
                Duration.ofSeconds(120))
                .newBuilder()
                .proxy(proxy)
                .build();
        OpenAiApi api = defaultRetrofit(client, mapper).create(OpenAiApi.class);
        OpenAiService service = new OpenAiService(api);

        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt("c语言写一个冒泡排序")
                .model("text-davinci-003")
//                .model("text-davinci-002-render")
//                .presencePenalty(2.0)
//                .frequencyPenalty(2.0)
//                .temperature(0.9)
                .maxTokens(255)
                .build();
        CompletionResult completionResult = service.createCompletion(completionRequest);

        String result = completionResult.getChoices()
                .stream()
                .map(CompletionChoice::getText)
                .collect(Collectors.joining("\n"));
        System.out.println(result);
    }
}
