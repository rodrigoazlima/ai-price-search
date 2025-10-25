package dev.rodrigoazlima.poc.ai.pricenotifier.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ActuatorHealthEndpointTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    HealthContributorAutoConfiguration.class,
                    HealthEndpointAutoConfiguration.class
            ));

    @Test
    void healthEndpoint_shouldBeUpByDefault() {
        contextRunner.run(context -> {
            HealthEndpoint healthEndpoint = context.getBean(HealthEndpoint.class);
            HealthComponent health = healthEndpoint.health();
            assertThat(health.getStatus()).isEqualTo(Status.UP);
        });
    }
}
