package com.example.helloWorld.atStart;

import com.example.helloWorld.service.BowlingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Order(1)
@AllArgsConstructor
public class StartUpRunner implements ApplicationRunner {

    private BowlingService bowlingService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Hello world");

        bowlingService.startBowling();
    }
}
