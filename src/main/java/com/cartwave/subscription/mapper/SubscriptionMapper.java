package com.cartwave.subscription.mapper;

import com.cartwave.subscription.dto.SubscriptionDTO;
import com.cartwave.subscription.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface SubscriptionMapper {

    SubscriptionDTO toSubscriptionDTO(Subscription subscription);

    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "renewalDate", ignore = true)
    @Mapping(target = "features", ignore = true)
    Subscription toSubscription(SubscriptionDTO subscriptionDTO);

}
