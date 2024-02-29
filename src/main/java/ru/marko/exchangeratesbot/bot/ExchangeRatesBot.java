package ru.marko.exchangeratesbot.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.marko.exchangeratesbot.exception.ServiceException;
import ru.marko.exchangeratesbot.service.ExchangeRatesService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Component
public class ExchangeRatesBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeRatesBot.class);

    private static final String START = "/start";
    private static final String HELP = "/help";

    private final ExchangeRatesService exchangeRatesService;

    public ExchangeRatesBot(@Value("${bot.token}") String botToken, ExchangeRatesService exchangeRatesService) {
        super(botToken);
        this.exchangeRatesService = exchangeRatesService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        var message = update.getMessage().getText();
        var chatId = update.getMessage().getChatId();
        switch (message) {
            case START -> {
                String userName = update.getMessage().getChat().getUserName();
                startCommand(chatId, userName);
            }
            case "/usd" -> usdCommand(chatId);
            case "/eur" -> eurCommand(chatId);
            case "/gbp" -> gbpCommand(chatId);
            case HELP -> helpCommand(chatId);
            default -> unknowCommand(chatId);
        }
    }

    @Override
    public String getBotUsername() {
        return "marko_exchange_rates_bot";
    }


    private void startCommand(Long chatId, String userName) {
        var text = """
                Добро пожаловать в бот, %s!
                
                🏦 Здесь Вы сможете узнать официальные курсы валют на сегодня, установленные ЦБ РФ 🏦
                
                Для этого воспользуйтесь командами на панели клавиатуры:\s
                
                💵 /usd - курс доллара
                💶 /eur - курс евро
                💷 /gbp - курс фунта
                
                ℹ Дополнительные команды ℹ
                /help - получение справки
                """;
        var formattedText = String.format(text, userName);
        sendMessage(chatId, formattedText);
    }

    private void usdCommand(Long chatId) {
        String formattedText;
        try {
            var usd = exchangeRatesService.getUSDExchangeRate();
            var text = "Курс доллара на %s составляет %s рублей \uD83E\uDE99";
            formattedText = String.format(text, LocalDate.now(), usd);
        } catch (ServiceException e) {
            LOG.error("Ошибка получения курса доллара ❗", e);
            formattedText = "Не удалось получить текущий курс доллара. Попробуйте позже";
        }
        sendMessage(chatId, formattedText);
    }

    private void eurCommand(Long chatId) {
        String formattedText;
        try {
            var usd = exchangeRatesService.getEURExchangeRate();
            var text = "Курс евро на %s составляет %s рублей \uD83E\uDE99";
            formattedText = String.format(text, LocalDate.now(), usd);
        } catch (ServiceException e) {
            LOG.error("Ошибка получения курса евро ❗", e);
            formattedText = "Не удалось получить текущий курс евро. Попробуйте позже";
        }
        sendMessage(chatId, formattedText);
    }

    private void gbpCommand(Long chatId) {
        String formattedText;
        try {
            var gbp = exchangeRatesService.getGBPExchangeRate();
            var text = "Курс фунта на %s составляет %s рублей \uD83E\uDE99";
            formattedText = String.format(text, LocalDate.now(), gbp);
        } catch (ServiceException e) {
            LOG.error("Ошибка получения курса фунта ❗", e);
            formattedText = "Не удалось получить текущий курс фунта. Попробуйте позже";
        }
        sendMessage(chatId, formattedText);
    }

    private void helpCommand(Long chatId) {
        var text = """
                ❗ Справочная информация по боту ❗
                
                Для получения текущих курсов валют воспользуйтесь командами:
                на панели клавиатуры:
                
                💵 /usd - курс доллара
                💶 /eur - курс евро
                💷 /gbp - курс фунта
                """;
        sendMessage(chatId, text);
    }

    private void unknowCommand(Long chatId) {
        var text = "Наш бот реагирует только на команды!";
        var text1 = "Выберите валюту:";
        sendMessage(chatId, text);
        sendMessage(chatId, text1);
    }

    private void sendMessage(Long chatId, String text) {

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("/usd"));
        row.add(new KeyboardButton("/eur"));
        row.add(new KeyboardButton("/gbp"));
        keyboard.add(row);

        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add(new KeyboardButton("❌ Назад"));
        keyboard.add(secondRow);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки сообщения", e);
        }
    }
}