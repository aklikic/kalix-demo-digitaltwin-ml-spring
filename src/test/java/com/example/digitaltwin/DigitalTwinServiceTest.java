package com.example.digitaltwin;

import com.example.digitaltwin.api.DigitalTwinApi;
import com.example.digitaltwin.api.DigitalTwinService;
import com.example.digitaltwin.domain.DigitalTwinEvent;
import com.example.digitaltwin.domain.DigitalTwinState;
import com.example.digitaltwin.ml.MLScoringService;
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


    private void happyPath(MLScoringService mlScoringService){

        var dtId = UUID.randomUUID().toString();

        EventSourcedTestKit<DigitalTwinState, DigitalTwinService> testKit = EventSourcedTestKit.of(dtId,context -> new DigitalTwinService(context));

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
}
