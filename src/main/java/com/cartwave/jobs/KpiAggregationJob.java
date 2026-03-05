package com.cartwave.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class KpiAggregationJob {
    private static final Logger log = LoggerFactory.getLogger(KpiAggregationJob.class);

    @Scheduled(cron = "${cartwave.jobs.kpi-aggregation-cron:0 */30 * * * *}")
    public void run() {
        log.info("KPI aggregation cycle executed");
    }
}
