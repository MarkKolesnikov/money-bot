package ru.marko.exchangeratesbot.service;

import ru.marko.exchangeratesbot.exception.ServiceException;

public interface ExchangeRatesService {

    String getUSDExchangeRate() throws ServiceException;

    String getEURExchangeRate() throws ServiceException;

    String getGBPExchangeRate() throws ServiceException;
}