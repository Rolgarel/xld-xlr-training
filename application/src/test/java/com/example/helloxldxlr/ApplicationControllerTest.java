package com.example.helloxldxlr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnApplicationInformation() {

        var response = restTemplate.getForObject(
                "http://localhost:" + port + "/api/info",
                String.class
        );

        assertThat(response).contains("hello-xld-xlr");
        assertThat(response).contains("1.0.0");
    }

    @Test
    void shouldReturnHealthStatus() {

        var response = restTemplate.getForObject(
                "http://localhost:" + port + "/health",
                String.class
        );

        assertThat(response).contains("UP");
    }

}