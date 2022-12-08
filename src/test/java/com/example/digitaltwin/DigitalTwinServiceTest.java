package com.example.digitaltwin;

import com.example.digitaltwin.model.DigitalTwinApi;
import com.example.digitaltwin.api.DigitalTwinController;
import com.example.digitaltwin.model.DigitalTwinEvent;
import com.example.digitaltwin.model.DigitalTwinState;
import com.example.digitaltwin.ml.MLScoringService;
import com.example.digitaltwin.ml.MLScoringServiceH20;
import com.example.digitaltwin.ml.MLScoringServiceMock;
import kalix.javasdk.testkit.EventSourcedResult;
import kalix.springsdk.testkit.EventSourcedTestKit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.UUID;

public class DigitalTwinServiceTest {


    @Test
    public void happyPathMock(){
        happyPath(new MLScoringServiceMock());
    }

    @Test
    public void happyPathH20()throws Exception{
        happyPath(new MLScoringServiceH20());
    }

    @Test
    public void happyPathWithAggregationMock(){
        happyPathWithAggregation(new MLScoringServiceMock());
    }
    @Test
    public void happyPathWithAggregationH20()throws Exception{
        happyPathWithAggregation(new MLScoringServiceH20());
    }

    private void happyPath(MLScoringService mlScoringService){

        var dtId = UUID.randomUUID().toString();

        EventSourcedTestKit<DigitalTwinState, DigitalTwinController> testKit = EventSourcedTestKit.of(dtId, context -> new DigitalTwinController(context,mlScoringService));

        var createRequest = new DigitalTwinApi.CreateRequest("name");
        EventSourcedResult<DigitalTwinApi.EmptyResponse> createResult = testKit.call(service -> service.create(createRequest));
        DigitalTwinEvent.Created createdEvent = createResult.getNextEventOfType(DigitalTwinEvent.Created.class);
        assertEquals(dtId,createdEvent.getDtId());

        DigitalTwinState updatedState = (DigitalTwinState)createResult.getUpdatedState();
        assertFalse(updatedState.isMaintenanceRequired());

        var metricOKRequest = MLScoringServiceMock.metricOKRequest;
        var metricFailRequest = MLScoringServiceMock.metricFailRequest;

        EventSourcedResult<DigitalTwinApi.EmptyResponse> metricResult = testKit.call(service -> service.metric(metricOKRequest));
        assertFalse(metricResult.didEmitEvents());

        metricResult = testKit.call(service -> service.metric(metricFailRequest));
        DigitalTwinEvent.MaintenanceRequired mrEvent = metricResult.getNextEventOfType(DigitalTwinEvent.MaintenanceRequired.class);
        assertEquals(dtId,mrEvent.getDtId());

        metricResult = testKit.call(service -> service.metric(metricFailRequest));
        assertFalse(metricResult.didEmitEvents());

        updatedState = (DigitalTwinState)metricResult.getUpdatedState();
        assertTrue(updatedState.isMaintenanceRequired());


        EventSourcedResult<DigitalTwinApi.EmptyResponse> setMaintenancePerformedResult = testKit.call(service -> service.setMaintenancePerformed());
        DigitalTwinEvent.MaintenancePerformed mpEvent = setMaintenancePerformedResult.getNextEventOfType(DigitalTwinEvent.MaintenancePerformed.class);
        assertEquals(dtId,mpEvent.getDtId());

        updatedState = (DigitalTwinState)createResult.getUpdatedState();
        assertFalse(updatedState.isMaintenanceRequired());

    }
    private void happyPathWithAggregation(MLScoringService mlScoringService){

        var dtId = UUID.randomUUID().toString();

        EventSourcedTestKit<DigitalTwinState, DigitalTwinController> testKit = EventSourcedTestKit.of(dtId, context -> new DigitalTwinController(context,mlScoringService));

        var createRequest = new DigitalTwinApi.CreateRequest("name");
        EventSourcedResult<DigitalTwinApi.EmptyResponse> createResult = testKit.call(service -> service.create(createRequest));
        DigitalTwinEvent.Created createdEvent = createResult.getNextEventOfType(DigitalTwinEvent.Created.class);
        assertEquals(dtId,createdEvent.getDtId());

        DigitalTwinState updatedState = (DigitalTwinState)createResult.getUpdatedState();
        assertFalse(updatedState.isMaintenanceRequired());


        //raw1 received and then raw2 received -> raw2 triggers maintenance check - metrics OK
        EventSourcedResult<DigitalTwinApi.EmptyResponse> metricRaw1Result = testKit.call(service -> service.metricRaw1(MLScoringServiceMock.metricRaw1OKRequest));
        DigitalTwinEvent.MetricRaw1Received raw1Received = metricRaw1Result.getNextEventOfType(DigitalTwinEvent.MetricRaw1Received.class);
        assertEquals(dtId,raw1Received.getDtId());
        updatedState = (DigitalTwinState)metricRaw1Result.getUpdatedState();
        assertTrue(updatedState.isRaw1Received());

        EventSourcedResult<DigitalTwinApi.EmptyResponse> metricRaw2Result = testKit.call(service -> service.metricRaw2(MLScoringServiceMock.metricRaw2OKRequest));
        DigitalTwinEvent.MaintenanceNotRequired maintenanceNotRequired = metricRaw2Result.getNextEventOfType(DigitalTwinEvent.MaintenanceNotRequired.class);
        assertEquals(dtId,maintenanceNotRequired.getDtId());
        updatedState = (DigitalTwinState)metricRaw2Result.getUpdatedState();
        assertFalse(updatedState.isMaintenanceRequired());
        assertFalse(updatedState.isRaw1Received());
        assertFalse(updatedState.isRaw2Received());


        //raw2 received and then raw1 received -> raw1 triggers maintenance check - metrics OK
        metricRaw2Result = testKit.call(service -> service.metricRaw2(MLScoringServiceMock.metricRaw2OKRequest));
        DigitalTwinEvent.MetricRaw2Received raw2Received = metricRaw2Result.getNextEventOfType(DigitalTwinEvent.MetricRaw2Received.class);
        assertEquals(dtId,raw2Received.getDtId());
        updatedState = (DigitalTwinState)metricRaw2Result.getUpdatedState();
        assertTrue(updatedState.isRaw2Received());

        metricRaw1Result = testKit.call(service -> service.metricRaw1(MLScoringServiceMock.metricRaw1OKRequest));
        maintenanceNotRequired = metricRaw1Result.getNextEventOfType(DigitalTwinEvent.MaintenanceNotRequired.class);
        assertEquals(dtId,maintenanceNotRequired.getDtId());
        updatedState = (DigitalTwinState)metricRaw1Result.getUpdatedState();
        assertFalse(updatedState.isMaintenanceRequired());
        assertFalse(updatedState.isRaw1Received());
        assertFalse(updatedState.isRaw2Received());

        //raw1 received and then raw2 received -> raw2 triggers maintenance check - metrics NOT OK - triggering maintenance
        metricRaw1Result = testKit.call(service -> service.metricRaw1(MLScoringServiceMock.metricRaw1FailRequest));
        raw1Received = metricRaw1Result.getNextEventOfType(DigitalTwinEvent.MetricRaw1Received.class);
        assertEquals(dtId,raw1Received.getDtId());

        metricRaw2Result = testKit.call(service -> service.metricRaw2(MLScoringServiceMock.metricRaw2FailRequest));
        DigitalTwinEvent.MaintenanceRequired maintenanceRequired = metricRaw2Result.getNextEventOfType(DigitalTwinEvent.MaintenanceRequired.class);
        assertEquals(dtId,maintenanceRequired.getDtId());
        updatedState = (DigitalTwinState)metricRaw2Result.getUpdatedState();
        assertTrue(updatedState.isMaintenanceRequired());
        assertTrue(updatedState.isRaw1Received());
        assertTrue(updatedState.isRaw2Received());

        EventSourcedResult<DigitalTwinApi.EmptyResponse> performMaintenanceResult = testKit.call(service -> service.setMaintenancePerformed());
        DigitalTwinEvent.MaintenancePerformed maintenancePerformed = performMaintenanceResult.getNextEventOfType(DigitalTwinEvent.MaintenancePerformed.class);
        assertEquals(dtId,maintenancePerformed.getDtId());
        updatedState = (DigitalTwinState)performMaintenanceResult.getUpdatedState();
        assertFalse(updatedState.isMaintenanceRequired());
        assertFalse(updatedState.isRaw1Received());
        assertFalse(updatedState.isRaw2Received());


    }
}
