package com.msscbeerorderservices.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msscbeerorderservices.bootstrap.BeerOrderBootStrap;
import com.msscbeerorderservices.domain.Customer;
import com.msscbeerorderservices.repositories.BeerOrderRepository;
import com.msscbeerorderservices.repositories.CustomerRepository;
import com.msscbeerorderservices.services.inventory.CheckInventoryStockedBeerClient;
import com.msscbeerorderservices.web.model.BeerOrderDto;
import com.msscbeerorderservices.web.model.BeerOrderLineDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class TastingRoomService {

    private final CustomerRepository customerRepository;
    private final BeerOrderService beerOrderService;

    private final CheckInventoryStockedBeerClient checkInventoryStockedBeerClient;
    private final BeerOrderRepository beerOrderRepository;
    private final List<String> beerUpcs = new ArrayList<>(3);

    private final ObjectMapper objectMapper;

    public TastingRoomService(CustomerRepository customerRepository, BeerOrderService beerOrderService,
                              CheckInventoryStockedBeerClient checkInventoryStockedBeerClient, BeerOrderRepository beerOrderRepository, ObjectMapper objectMapper) {
        this.customerRepository = customerRepository;
        this.beerOrderService = beerOrderService;
        this.checkInventoryStockedBeerClient = checkInventoryStockedBeerClient;
        this.beerOrderRepository = beerOrderRepository;

        beerUpcs.add(BeerOrderBootStrap.BEER_1_UPC);
        beerUpcs.add(BeerOrderBootStrap.BEER_2_UPC);
        beerUpcs.add(BeerOrderBootStrap.BEER_3_UPC);
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Scheduled(fixedRate = 1240000) //run every 2 seconds 2000ms
    public void placeTastingRoomOrder(){

        List<Customer> customerList = customerRepository.findAllByCustomerNameLike(BeerOrderBootStrap.TASTING_ROOM);

        if (customerList.size() == 1){ //should be just one
            doPlaceOrder(customerList.get(0));
        } else {
            log.error("Too many or too few tasting room customers found");

            customerList.forEach(customer -> log.debug(customer.toString()));
        }
    }

    private BeerOrderDto doPlaceOrder(Customer customer) {
        String beerToOrder = getRandomBeerUpc();

        BeerOrderLineDto beerOrderLine = BeerOrderLineDto.builder()
                .upc(beerToOrder)
                .orderQuantity(new Random().nextInt(6)) //todo externalize value to property
                .build();

       Integer inventoryBeerSum = checkInventoryStockedBeerClient.getBeerInventoryStocked(beerToOrder);

        if (beerOrderLine.getOrderQuantity() > inventoryBeerSum) {

            log.info("the quantity is more than stock...!");
            return null;
        }

        List<BeerOrderLineDto> beerOrderLineSet = new ArrayList<>();
        beerOrderLineSet.add(beerOrderLine);

        BeerOrderDto beerOrder = BeerOrderDto.builder()
                .customerId(customer.getId())
                .customerRef(UUID.randomUUID().toString())
                .beerOrderLines(beerOrderLineSet)
                .build();
        try {
            String beerOrderToJson = objectMapper.writeValueAsString(beerOrder);
            System.out.println(beerOrderToJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }



        BeerOrderDto savedOrder = beerOrderService.placeOrder(customer.getId(), beerOrder);

        return savedOrder;

    }

    private String getRandomBeerUpc() {
        return beerUpcs.get(new Random().nextInt(beerUpcs.size() -0));
    }
}
