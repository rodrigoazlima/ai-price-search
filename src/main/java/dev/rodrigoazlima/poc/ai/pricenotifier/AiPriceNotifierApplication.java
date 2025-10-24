package dev.rodrigoazlima.poc.ai.pricenotifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AiPriceNotifierApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiPriceNotifierApplication.class, args);
    }

}
