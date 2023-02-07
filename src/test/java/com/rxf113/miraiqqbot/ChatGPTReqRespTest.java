package com.rxf113.miraiqqbot;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import org.junit.jupiter.api.Test;
import retrofit2.http.HEAD;

import java.util.stream.Collectors;

class ChatGPTReqRespTest {

    @Test
    void testOpenAiService() {
        OpenAiService service = new OpenAiService("需要自己去 https://platform.openai.com/account/api-keys 创建一个 SECRET KEY");
        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt("写一行代码 java 输出 hello world")
                .model("text-davinci-003")
                .presencePenalty(2.0)
                .frequencyPenalty(2.0)
                .temperature(0.9)
                .maxTokens(255)
                .build();
        CompletionResult completionResult = service.createCompletion(completionRequest);

        //System.out.println(completionResult.getChoices().toString());

        String result = completionResult.getChoices()
                .stream()
                .map(CompletionChoice::getText)
                .collect(Collectors.joining("\n"));
        System.out.println(result);
    }
}
