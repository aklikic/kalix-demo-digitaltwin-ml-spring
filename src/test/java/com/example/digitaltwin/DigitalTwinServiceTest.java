package com.example.digitaltwin;

import com.example.digitaltwin.ml.MLScoringService;
import com.example.digitaltwin.ml.MLScoringServiceMock;
import kalix.javasdk.testkit.EventSourcedResult;
import kalix.springsdk.testkit.EventSourcedTestKit;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DigitalTwinServiceTest {


    @Test
    public void happyPathMock(){
        happyPath(new MLScoringServiceMock());
    }

//    @Test
//    public void happyPathH20()throws Exception{
//        happyPath(new MLScoringServiceH20());
//    }


    private void happyPath(MLScoringService mlScoringService){

        var dtId = UUID.randomUUID().toString();
        var aggregationLimit = 2;
        var aggregationTimeWindowSeconds = 10;

        EventSourcedTestKit<DigitalTwinModel.State, DigitalTwinService> testKit = EventSourcedTestKit.of(dtId, context -> new DigitalTwinService(context,mlScoringService));

        //create
        var createRequest = new DigitalTwinModel.CreateRequest("name",aggregationLimit,aggregationTimeWindowSeconds);
        EventSourcedResult<DigitalTwinModel.EmptyResponse> createResult = testKit.call(service -> service.create(createRequest));
        createResult.getNextEventOfType(DigitalTwinModel.CreatedEvent.class);

        var updatedState = (DigitalTwinModel.State)createResult.getUpdatedState();
        assertFalse(updatedState.maintenanceRequired);
        assertEquals(0,updatedState.aggregation.size());


        var metricOKRequest = MLScoringServiceMock.metricOKRequest;
        var metricFailRequest = MLScoringServiceMock.metricFailRequest;

        //send first OK metric
        var metricResult = testKit.call(service -> service.metric(metricOKRequest));
        metricResult.getNextEventOfType(DigitalTwinModel.MetricAggregateEvent.class);
        updatedState = (DigitalTwinModel.State)metricResult.getUpdatedState();
        assertFalse(updatedState.maintenanceRequired);
        assertEquals(1,updatedState.aggregation.size());

        //send second OK metric - aggregation is done and scoring initiated. Scoring result indicated that maintenance is NOT required
        metricResult = testKit.call(service -> service.metric(metricOKRequest));
        metricResult.getNextEventOfType(DigitalTwinModel.AggregationFinishedEvent.class);
        updatedState = (DigitalTwinModel.State)metricResult.getUpdatedState();
        assertFalse(updatedState.maintenanceRequired);
        assertEquals(0,updatedState.aggregation.size());


        //send first FAIL metric
        metricResult = testKit.call(service -> service.metric(metricFailRequest));
        metricResult.getNextEventOfType(DigitalTwinModel.MetricAggregateEvent.class);
        updatedState = (DigitalTwinModel.State)metricResult.getUpdatedState();
        assertFalse(updatedState.maintenanceRequired);
        assertEquals(1,updatedState.aggregation.size());

        //send second FAIL metric - aggregation is done and scoring initiated. Scoring result indicated that maintenance is required
        metricResult = testKit.call(service -> service.metric(metricFailRequest));
        metricResult.getNextEventOfType(DigitalTwinModel.AggregationFinishedEvent.class);
        updatedState = (DigitalTwinModel.State)metricResult.getUpdatedState();
        assertTrue(updatedState.maintenanceRequired);
        assertEquals(0,updatedState.aggregation.size());

        //mark maintenance as performed
        var maintenancePerformedResult = testKit.call(service -> service.setMaintenancePerformed());
        maintenancePerformedResult.getNextEventOfType(DigitalTwinModel.MaintenancePerformedEvent.class);
        updatedState = (DigitalTwinModel.State)maintenancePerformedResult.getUpdatedState();
        assertFalse(updatedState.maintenanceRequired);
        assertEquals(0,updatedState.aggregation.size());

    }

}
