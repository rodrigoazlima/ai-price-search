package dev.rodrigoazlima.poc.ai.pricenotifier;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class AiPriceNotifierApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertNotNull(context, "Spring application context should have been started");
    }

    @Test
    void applicationBeanPresent() {
        assertNotNull(context.getBean(AiPriceNotifierApplication.class),
                "AiPriceNotifierApplication bean should be present");
    }

    @Test
    void mainDoesNotThrow() {
        assertDoesNotThrow(() -> AiPriceNotifierApplication.main(new String[]{"--spring.profiles.active=test"}));
    }
}