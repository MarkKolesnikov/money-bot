package ru.marko.exchangeratesbot.bot;

import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class ExchangeRatesBot extends TelegramLongPollingBot {

    public ExchangeRatesBot(@Value("$b{bot.token}") String botToken) {
        super(botToken);
    }
    @Override
    public void onUpdateReceived(Update update) {

    }

    @Override
    public String getBotUsername() {
        return null;
    }
}
