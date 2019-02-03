package main.java.main;

import main.java.bot.MyListDBBot;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class StartBot {

    public static void main(String[] args) {

        // init api context
        ApiContextInitializer.init();

        // instantiate Tg bot api
        TelegramBotsApi botsApi = new TelegramBotsApi();

        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext("spring-hibernate-bot-config.xml");

        MyListDBBot bot = context.getBean("myListDBBot", MyListDBBot.class);

        // register our bot
        try {
            botsApi.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }
}
