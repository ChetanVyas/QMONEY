
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {



  private RestTemplate restTemplate;
  private String apiKey = "c304a89b7a742d1caf9d52b7f40e2797ada137cb";

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  protected PortfolioManagerImpl(RestTemplate restTemplate, String apiKey) {
    this.restTemplate = restTemplate;
    this.apiKey = apiKey;
  }


  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  // Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  // into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  // clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  // CHECKSTYLE:OFF

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) throws JsonProcessingException {

    // calculate annualized return for all stocks
    List<AnnualizedReturn> annualizedReturnsList = new ArrayList<>();
    for (PortfolioTrade trade : portfolioTrades) {

      List<Candle> stockCandles =
          getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate);

      if (stockCandles.size() == 0)
        throw new RuntimeException("Invailis Dates Exception");

      double buyPrice = stockCandles.get(0).getOpen();
      double sellPrice = stockCandles.get(stockCandles.size() - 1).getClose();

      annualizedReturnsList.add(calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice));
    }

    Collections.sort(annualizedReturnsList);
    return annualizedReturnsList;
  }

  protected static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade,
      Double buyPrice, Double sellPrice) {

    double totalReturn = (sellPrice - buyPrice) / buyPrice;

    double years = trade.getPurchaseDate().until(endDate, ChronoUnit.DAYS) / 365.24;

    double annualizedReturns = Math.pow(1 + totalReturn, 1 / years) - 1;

    return new AnnualizedReturn(trade.getSymbol(), annualizedReturns, totalReturn);
  }

  // private Comparator<AnnualizedReturn> getComparator() {
  // return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  // }

  // CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Extract the logic to call Tiingo third-party APIs to a separate function.
  // Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) {

    TiingoCandle[] stockCandles =
        restTemplate.getForObject(buildUri(symbol, from, to), TiingoCandle[].class);
    return Arrays.asList(stockCandles);
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https:api.tiingo.com/tiingo/daily/" + symbol + "/prices?" + "startDate="
        + startDate + "&endDate=" + endDate + "&token=" + apiKey;
    return uriTemplate;
  }
}
