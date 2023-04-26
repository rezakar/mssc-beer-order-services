package com.msscbeerorderservices.web.mappers;

import com.msscbeerorderservices.domain.BeerOrderLine;
import com.msscbeerorderservices.services.beer.BeerService;
import com.msscbeerorderservices.web.model.BeerDto;
import com.msscbeerorderservices.web.model.BeerOrderLineDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Optional;

public abstract class BeerOrderLineMapperDecorator implements BeerOrderLineMapper {

    private BeerService beerService;
    private BeerOrderLineMapper beerOrderLineMapper;

    @Autowired
    public void setBeerService(BeerService beerService) {
        this.beerService = beerService;
    }

    @Autowired
    @Qualifier("delegate")
    public void setBeerOrderMapper(BeerOrderLineMapper beerOrderLineMapper) {
        this.beerOrderLineMapper = beerOrderLineMapper;
    }


    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
        BeerOrderLineDto beerOrderLineDto = beerOrderLineMapper.beerOrderLineToDto(line);
        Optional<BeerDto> beerDtoOptional = beerService.getBeerByUpc(line.getUpc());

        beerDtoOptional.ifPresent(beerDto -> {
            beerOrderLineDto.setBeerName(beerDto.getBeerName());
            beerOrderLineDto.setBeerStyle(beerDto.getBeerStyle());
//            beerOrderLineDto.setUpc(beerDto.getUpc());
            beerOrderLineDto.setPrice(beerDto.getPrice());
        });
        return beerOrderLineDto;
    }
}
