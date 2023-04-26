package com.msscbeerorderservices.services.inventory;


import com.msscbeerorderservices.web.model.BeerInventoryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Slf4j
@ConfigurationProperties(prefix = "stock.inventory", ignoreUnknownFields = true)
@Component
public class CheckInventoryStockedBeerClientImpl implements CheckInventoryStockedBeerClient {

    public static final String INVENTORY_PATH_UPC = "/api/v1/beer/{upc}/upc";

    private final RestTemplate restTemplate;

    private String beerInventoryClientHost;

    public void setBeerInventoryClientHost(String beerInventoryClientHost) {
        this.beerInventoryClientHost = beerInventoryClientHost;
    }

    public CheckInventoryStockedBeerClientImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public Integer getBeerInventoryStocked(String upc) {
        log.debug("Calling Inventory Service by Upc... ");

        ResponseEntity<List<BeerInventoryDto>> responseEntity = restTemplate
                .exchange(beerInventoryClientHost + INVENTORY_PATH_UPC, HttpMethod.GET, null,
                        new ParameterizedTypeReference<List<BeerInventoryDto>>(){} , (Object) upc);

        Integer OnStock = Objects.requireNonNull(responseEntity.getBody())
                .stream()
                .mapToInt(BeerInventoryDto::getQuantityOnHand)
                .sum();

        return OnStock;
    }
}
