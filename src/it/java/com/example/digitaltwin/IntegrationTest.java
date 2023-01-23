package com.example.digitaltwin;

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

        var dtId = UUID.randomUUID().toString();
        var aggregationLimit = 2;
        var aggregationTimeWindowSeconds = 10;

        //create
        ResponseEntity<DigitalTwinModel.EmptyResponse> emptyRes =
        webClient.post()
                .uri("/dt/"+dtId+"/create")
                .bodyValue(new DigitalTwinModel.CreateRequest("name",aggregationLimit,aggregationTimeWindowSeconds))
                .retrieve()
                .toEntity(DigitalTwinModel.EmptyResponse.class)
                .block(timeout);

        assertEquals(HttpStatus.OK,emptyRes.getStatusCode());

        DigitalTwinModel.GetResponse getRes =
        webClient.get()
                .uri("/dt/"+dtId)
                .retrieve()
                .bodyToMono(DigitalTwinModel.GetResponse.class)
                .block(timeout);

        assertFalse(getRes.isMaintenanceRequired());
        assertEquals(0,getRes.getAggregationSize());

        //send first OK metric
        emptyRes =
        webClient.post()
                .uri("/dt/"+dtId+"/metric")
                .bodyValue(MLScoringServiceMock.metricOKRequest)
                .retrieve()
                .toEntity(DigitalTwinModel.EmptyResponse.class)
                .block(timeout);

        assertEquals(HttpStatus.OK,emptyRes.getStatusCode());

        getRes =
        webClient.get()
                .uri("/dt/"+dtId)
                .retrieve()
                .bodyToMono(DigitalTwinModel.GetResponse.class)
                .block(timeout);

        assertFalse(getRes.isMaintenanceRequired());
        assertEquals(1,getRes.getAggregationSize());

        //send second OK metric - aggregation is done and scoring initiated. Scoring result indicated that maintenance is NOT required
        emptyRes =
        webClient.post()
                .uri("/dt/"+dtId+"/metric")
                .bodyValue(MLScoringServiceMock.metricOKRequest)
                .retrieve()
                .toEntity(DigitalTwinModel.EmptyResponse.class)
                .block(timeout);

        assertEquals(HttpStatus.OK,emptyRes.getStatusCode());

        getRes =
        webClient.get()
                .uri("/dt/"+dtId)
                .retrieve()
                .bodyToMono(DigitalTwinModel.GetResponse.class)
                .block(timeout);

        assertFalse(getRes.isMaintenanceRequired());
        assertEquals(0,getRes.getAggregationSize());

        //send first FAIL metric
        emptyRes =
        webClient.post()
                .uri("/dt/"+dtId+"/metric")
                .bodyValue(MLScoringServiceMock.metricFailRequest)
                .retrieve()
                .toEntity(DigitalTwinModel.EmptyResponse.class)
                .block(timeout);

        assertEquals(HttpStatus.OK,emptyRes.getStatusCode());

        getRes =
        webClient.get()
                .uri("/dt/"+dtId)
                .retrieve()
                .bodyToMono(DigitalTwinModel.GetResponse.class)
                .block(timeout);

        assertFalse(getRes.isMaintenanceRequired());
        assertEquals(1,getRes.getAggregationSize());

        //send second FAIL metric - aggregation is done and scoring initiated. Scoring result indicated that maintenance is required
        emptyRes =
        webClient.post()
                .uri("/dt/"+dtId+"/metric")
                .bodyValue(MLScoringServiceMock.metricFailRequest)
                .retrieve()
                .toEntity(DigitalTwinModel.EmptyResponse.class)
                .block(timeout);

        assertEquals(HttpStatus.OK,emptyRes.getStatusCode());

        getRes =
        webClient.get()
                .uri("/dt/"+dtId)
                .retrieve()
                .bodyToMono(DigitalTwinModel.GetResponse.class)
                .block(timeout);

        assertTrue(getRes.isMaintenanceRequired());
        assertEquals(0,getRes.getAggregationSize());

        //mark maintenance as performed
        emptyRes =
        webClient.post()
                .uri("/dt/"+dtId+"/set-maintenance-performed")
                .retrieve()
                .toEntity(DigitalTwinModel.EmptyResponse.class)
                .block(timeout);

        assertEquals(HttpStatus.OK,emptyRes.getStatusCode());

        getRes =
        webClient.get()
                .uri("/dt/"+dtId)
                .retrieve()
                .bodyToMono(DigitalTwinModel.GetResponse.class)
                .block(timeout);

        assertFalse(getRes.isMaintenanceRequired());
        assertEquals(0,getRes.getAggregationSize());

    }

    @Test
    public void testAggregationTimeWindowTimer() throws Exception {
        var dtId = UUID.randomUUID().toString();
        var aggregationLimit = 2;
        var aggregationTimeWindowSeconds = 2;

        //create
        ResponseEntity<DigitalTwinModel.EmptyResponse> emptyRes =
        webClient.post()
                .uri("/dt/"+dtId+"/create")
                .bodyValue(new DigitalTwinModel.CreateRequest("name",aggregationLimit,aggregationTimeWindowSeconds))
                .retrieve()
                .toEntity(DigitalTwinModel.EmptyResponse.class)
                .block(timeout);

        assertEquals(HttpStatus.OK,emptyRes.getStatusCode());

        //send one FAIL metric
        emptyRes =
        webClient.post()
                .uri("/dt/"+dtId+"/metric")
                .bodyValue(MLScoringServiceMock.metricFailRequest)
                .retrieve()
                .toEntity(DigitalTwinModel.EmptyResponse.class)
                .block(timeout);

        assertEquals(HttpStatus.OK,emptyRes.getStatusCode());

        //wait for timer to be triggered
       Thread.sleep(3000);

        DigitalTwinModel.GetResponse getRes =
        webClient.get()
                .uri("/dt/"+dtId)
                .retrieve()
                .bodyToMono(DigitalTwinModel.GetResponse.class)
                .block(timeout);

        assertTrue(getRes.isMaintenanceRequired());
        assertEquals(0,getRes.getAggregationSize());


    }

}
