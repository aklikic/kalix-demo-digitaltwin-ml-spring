package com.example.digitaltwin;

import com.example.digitaltwin.api.DigitalTwinApi;
import com.example.digitaltwin.ml.MLScoringServiceMock;
import kalix.springsdk.testkit.KalixIntegrationTestKitSupport;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class IntegrationTest extends KalixIntegrationTestKitSupport {
    private static Logger logger = LoggerFactory.getLogger(IntegrationTest.class);
    @Autowired
    private WebClient webClient;

    private Duration timeout = Duration.of(5, ChronoUnit.SECONDS);

    @Test
    public void test() throws Exception {
        String dtId = UUID.randomUUID().toString();

        ResponseEntity<DigitalTwinApi.EmptyResponse> emptyRes =
        webClient.post()
                .uri("/dt/"+dtId+"/create")
                .bodyValue(new DigitalTwinApi.CreateRequest("name"))
                .retrieve()
                .toEntity(DigitalTwinApi.EmptyResponse.class)
                .block(timeout);

        assertEquals(HttpStatus.OK,emptyRes.getStatusCode());

        DigitalTwinApi.GetResponse getRes =
        webClient.post()
                .uri("/dt/"+dtId)
                .retrieve()
                .bodyToMono(DigitalTwinApi.GetResponse.class)
                .block(timeout);

        assertFalse(getRes.isMaintenanceRequired());

        emptyRes =
        webClient.post()
                .uri("/dt/"+dtId+"/metric")
                .bodyValue(MLScoringServiceMock.metricOKRequest)
                .retrieve()
                .toEntity(DigitalTwinApi.EmptyResponse.class)
                .block(timeout);

        assertEquals(HttpStatus.OK,emptyRes.getStatusCode());

        getRes =
        webClient.post()
                .uri("/dt/"+dtId)
                .retrieve()
                .bodyToMono(DigitalTwinApi.GetResponse.class)
                .block(timeout);

        assertFalse(getRes.isMaintenanceRequired());

        emptyRes =
        webClient.post()
                .uri("/dt/"+dtId+"/metric")
                .bodyValue(MLScoringServiceMock.metricFailRequest)
                .retrieve()
                .toEntity(DigitalTwinApi.EmptyResponse.class)
                .block(timeout);

        assertEquals(HttpStatus.OK,emptyRes.getStatusCode());

        getRes =
        webClient.post()
                .uri("/dt/"+dtId)
                .retrieve()
                .bodyToMono(DigitalTwinApi.GetResponse.class)
                .block(timeout);

        assertTrue(getRes.isMaintenanceRequired());

    }
}
