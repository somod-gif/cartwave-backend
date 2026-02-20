package com.cartwave.billing.mapper;

import com.cartwave.billing.dto.BillingTransactionDTO;
import com.cartwave.billing.entity.BillingTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface BillingTransactionMapper {

    BillingTransactionDTO toBillingTransactionDTO(BillingTransaction billingTransaction);

    @Mapping(target = "storeId", ignore = true)
    @Mapping(target = "transactionDetails", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    BillingTransaction toBillingTransaction(BillingTransactionDTO billingTransactionDTO);

}
