package com.cartwave.billing.service;

import com.cartwave.billing.dto.BillingTransactionDTO;
import com.cartwave.billing.mapper.BillingTransactionMapper;
import com.cartwave.billing.repository.BillingTransactionRepository;
import com.cartwave.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BillingService {

    private final BillingTransactionRepository billingTransactionRepository;
    private final BillingTransactionMapper billingTransactionMapper;

    @Transactional(readOnly = true)
    public Page<BillingTransactionDTO> getTransactionsForStore(Pageable pageable) {
        log.info("Fetching billing transactions for store");
        var storeId = TenantContext.getTenantId();
        
        Page<com.cartwave.billing.entity.BillingTransaction> transactions = 
            billingTransactionRepository.findByStoreId(storeId, pageable);
        
        return transactions.map(billingTransactionMapper::toBillingTransactionDTO);
    }

}
