
package com.crio.warmup.stock.quotes;

// import static java.time.temporal.ChronoUnit.DAYS;
// import static java.time.temporal.ChronoUnit.SECONDS;
// import com.crio.warmup.stock.dto.AlphavantageCandle;
import com.crio.warmup.stock.dto.AlphavantageDailyResponse;
import com.crio.warmup.stock.dto.Candle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.Comparator;
import java.util.List;
// import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class AlphavantageService implements StockQuotesService {

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  // Implement the StockQuoteService interface as per the contracts. Call Alphavantage service
  // to fetch daily adjusted data for last 20 years.
  // Refer to documentation here: https://www.alphavantage.co/documentation/
  // --
  // The implementation of this functions will be doing following tasks:
  // 1. Build the appropriate url to communicate with third-party.
  // The url should consider startDate and endDate if it is supported by the provider.
  // 2. Perform third-party communication with the url prepared in step#1
  // 3. Map the response and convert the same to List<Candle>
  // 4. If the provider does not support startDate and endDate, then the implementation
  // should also filter the dates based on startDate and endDate. Make sure that
  // result contains the records for for startDate and endDate after filtering.
  // 5. Return a sorted List<Candle> sorted ascending based on Candle#getDate
  // IMP: Do remember to write readable and maintainable code, There will be few functions like
  // Checking if given date falls within provided date range, etc.
  // Make sure that you write Unit tests for all such functions.
  // Note:
  // 1. Make sure you use {RestTemplate#getForObject(URI, String)} else the test will fail.
  // 2. Run the tests using command below and make sure it passes:
  // ./gradlew test --tests AlphavantageServiceTest
  // CHECKSTYLE:OFF

  private RestTemplate restTemplate;
  private String apiKey = "9DNICBKJGHJ3TVL0";


  protected AlphavantageService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {

    String stockCandlesString = restTemplate.getForObject(buildUri(symbol), String.class);

    List<Candle> stockCandles = parseCandles(stockCandlesString, from, to);

    return stockCandles;
  }


  private List<Candle> parseCandles(String stockCandlesString, LocalDate from, LocalDate to)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    AlphavantageDailyResponse alphavantageDailyResponse =
        objectMapper.readValue(stockCandlesString, AlphavantageDailyResponse.class);

    return alphavantageDailyResponse.getCandles().entrySet().stream()
        .filter((entry) -> (entry.getKey().isAfter(from) || entry.getKey().equals(from)))
        .filter((entry) -> (entry.getKey().isBefore(to) || entry.getKey().equals(to)))
        .peek((entry) -> entry.getValue().setDate(entry.getKey()))
        .map((entry) -> entry.getValue())
        .sorted((candle1, candle2) -> candle1.getDate().compareTo(candle2.getDate()))
        .collect(Collectors.toList());
  }



  // CHECKSTYLE:ON
  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  // 1. Write a method to create appropriate url to call Alphavantage service. The method should
  // be using configurations provided in the {@link @application.properties}.
  // 2. Use this method in #getStockQuote.

  protected String buildUri(String symbol) {
    String uriTemplate =
        "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=" + symbol
            + "&outputsize=full&apikey=" + apiKey;
    return uriTemplate;
  }

  // public static void main(String[] args) throws JsonProcessingException {
  //   RestTemplate restTemplate = new RestTemplate();
  //   AlphavantageService service = new AlphavantageService(restTemplate);
  //   System.out.println(service.buildUri("AAPL"));
  //   service.getStockQuote("AAPL", LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-05"))
  //   .forEach(candle -> System.out.println(candle.getOpen()));
  // }
}

// https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=IBM&outputsize=full&apikey=9DNICBKJGHJ3TVL0
