package com.rxf113.miraiqqbot.chat;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class MiraiConfig implements SmartInitializingSingleton {
    private static final Logger logger = LoggerFactory.getLogger(MiraiConfig.class);

    /**
     * qq号
     */
    private static final long QQ_NUMBER = 13443877;
    /**
     * 群号
     */
    private static final long GROUP_NUMBER = 575048591;
    /**
     * qq密码
     */
    private static final String PASSWORD = "xxx";

    private Bot bot = null;

    private Group group = null;

    /**
     * 机器人账号
     *
     * @return
     */
    @Bean
    public Bot bot() {
        Bot bot = BotFactory.INSTANCE.newBot(QQ_NUMBER, PASSWORD);
        bot.login();
        this.bot = bot;
        logger.info(" === qq机器人 启动 ===");
        return bot;
    }

    /**
     * qq群
     *
     * @param bot
     * @return
     */
    @Bean
    public Group group(Bot bot) {
        //qq群号
        Group group = bot.getGroup(GROUP_NUMBER);
        this.group = group;
        logger.info(" === qq群 启动 ===");
        return group;

    }

    @Override
    public void afterSingletonsInstantiated() {
        bot.getEventChannel().subscribeAlways(GroupMessageEvent.class, (event) -> {
            //收到的消息 message
            MessageChain message = event.getMessage();
            //判断是艾特我的
            int atIdx = -1;
            for (int i = 0; i < message.size(); i++) {
                if (message.get(i) instanceof At && ((At) message.get(i)).getTarget() == QQ_NUMBER) {
                    atIdx = i;
                    break;
                }
            }
            if (atIdx == -1) {
                logger.info("it is not at me");
                return;
            }
            //是艾特我的，找到艾特我之后的一句话，视为内容
            int instIdx = atIdx + 1;
            if (instIdx == message.size()) {
                //艾特我了，但是没有指令
                logger.warn("at me but not exists query");
                return;
            }
            //艾特我了,他的昵称是
            logger.info("who at me , nickName:[{}]", event.getSender().getNick());

            SingleMessage singleMessage = message.get(instIdx);
            String query = singleMessage.contentToString();

            //将他的内容，作为query,去查询 chatgpt,最后回复给他
            String resp = getAnswerByChatGPT(query);
            event.getSubject().sendMessage(new MessageChainBuilder()
                    .append(new QuoteReply(event.getMessage()))
                    .append(resp)
                    .build());
            logger.info("received query: {}, and chatGPT resp: {}", query, resp);
        });

        logger.info("===============群消息监听器已启动=================");
        //qq登录后，发送气派的标语
        //这里直接艾特群主
        group.sendMessage(new MessageChainBuilder().append(new At(group.getOwner().getId())).append("我已上线，支持ChatGPT，快来与我互动吧。").build());

    }

    public String getAnswerByChatGPT(String query) {
        String requestBody = "{\"parent_message_id\":\"" + UUID.randomUUID()
                + "\",\"action\":\"next\",\"messages\":[{\"role\":\"user\",\"id\":\""
                + UUID.randomUUID() + "\",\"content\":{\"content_type\":\"text\",\"parts\":[\"" + query + "\"]}}]," +
                "\"model\":\"text-davinci-002-render\"}";

        try {
            String[] auths = HttpUtil.sessionReq("__Secure-next-auth.session-token=" + SESSION_TOKEN);
            return HttpUtil.conversation(auths, requestBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "出错了 草!";
    }


    //todo 最开始，从浏览器控制台复制
    //从浏览器请求的请求头，或者 application -> cookie 复制出 __Secure-next-auth.session-token 的value。
    public static String SESSION_TOKEN = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2R0NNIn0..S9lAXRP5Di4nEl9h.hfAhRkkhCIulqqXWGjgqf3RihzD16_后面省略。。。";

}
