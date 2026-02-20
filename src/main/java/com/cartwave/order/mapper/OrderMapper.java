package com.cartwave.order.mapper;

import com.cartwave.order.dto.OrderDTO;
import com.cartwave.order.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderDTO toOrderDTO(Order order);

    @Mapping(target = "storeId", ignore = true)
    @Mapping(target = "customerPhoneNumber", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    Order toOrder(OrderDTO orderDTO);

}
