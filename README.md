## qq群聊机器人接入ChatGPT

一个简单的 mirai qq机器人 接入 ChatGPT 的 springboot 示例程序 。

### 运行方式

1. 修改 ``` com.rxf113.miraiqqbot.chat.MiraiConfig ``` 类里的 qq. 密码 和 群号。
2. 从浏览器控制台请求的请求头，或者 application -> cookie 复制出 __Secure-next-auth.session-token 的 value。
   copy到 ``` com.rxf113.miraiqqbot.chat.MiraiConfig ``` 的变量 ```SESSION_TOKEN```
3. 运行 springboot 项目即可

### other

[聊天机器人-mirai](https://github.com/mamoe/mirai)

[ChatGPT](https://chat.openai.com/)