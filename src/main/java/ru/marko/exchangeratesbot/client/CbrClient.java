package ru.marko.exchangeratesbot.client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.marko.exchangeratesbot.exception.ServiceException;

import java.io.IOException;

@Component
public class CbrClient {

    private final OkHttpClient client;

    @Value("${cbr.currency.rates.xml.url}") // тут путь к курсам валют
    private String url;

    public CbrClient(OkHttpClient client) {
        this.client = client;
    }

    public String getCurrencyRatesXML() throws ServiceException {
        var request = new Request.Builder()
                .url(url)
                .build();

        try (var response = client.newCall(request).execute()) {
            var body = response.body();
            return body == null ? null : body.string();
        } catch (IOException e) {
            throw new ServiceException("Ошибка получения курсов валют от ЦБ РФ", e);
        }
    }
}