
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.ObjectInputStream.GetField;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {


  private RestTemplate restTemplate;
  private String apiKey = "c304a89b7a742d1caf9d52b7f40e2797ada137cb";


  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }


  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  // Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  // ./gradlew test --tests TiingoServiceTest

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {

    String stockCandlesString =
        restTemplate.getForObject(buildUri(symbol, from, to), String.class);

    Candle[] stockCandles = parseCandles(stockCandlesString);
    return Arrays.asList(stockCandles);
  }

  // CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  // Write a method to create appropriate url to call the Tiingo API.

  private Candle[] parseCandles(String stockCandlesString) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    Candle[] stockCandles = objectMapper.readValue(stockCandlesString, TiingoCandle[].class);
    return stockCandles;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate="
        + startDate.toString() + "&endDate=" + endDate.toString() + "&token=" + apiKey;
    return uriTemplate;
  }




  // public static void main(String[] args) throws JsonProcessingException {
  //   RestTemplate restTemplate = new RestTemplate();
  //   TiingoService service = new TiingoService(restTemplate);
  //   System.out.println(service.buildUri("AAPL", LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-05")));
  //   service.getStockQuote("AAPL", LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-05"))
  //   .forEach(candle -> System.out.println(candle.getDate()));
  // }

}
