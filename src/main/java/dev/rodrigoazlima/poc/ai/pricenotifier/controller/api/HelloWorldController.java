package dev.rodrigoazlima.poc.ai.pricenotifier.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(
        name = "Hello World",
        description = "Simple hello world endpoint"
)
public class HelloWorldController {

    @GetMapping("/hello")
    @Operation(
            summary = "Get a greeting message",
            description = "Returns a simple hello world message")
    public String hello() {
        return "Hello, World!";
    }

}