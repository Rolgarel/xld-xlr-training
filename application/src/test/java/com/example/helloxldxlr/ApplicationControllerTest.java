package com.example.helloxldxlr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.version}")
    private String version;

    @Test
    void shouldReturnApplicationInformation() {

        var response = restTemplate.getForObject(
                "http://localhost:" + port + "/api/info",
                String.class
        );

        assertThat(response).contains("hello-xld-xlr");
        assertThat(response).contains(version);
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