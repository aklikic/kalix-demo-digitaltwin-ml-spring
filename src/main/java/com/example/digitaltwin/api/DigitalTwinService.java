package com.example.digitaltwin.api;

import com.example.digitaltwin.domain.DigitalTwinEvent;
import com.example.digitaltwin.domain.DigitalTwinState;
import com.example.digitaltwin.ml.MLScoringService;
import com.example.digitaltwin.ml.MLScoringServiceMock;
import io.grpc.Status;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import kalix.springsdk.annotations.Entity;
import kalix.springsdk.annotations.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;

@Entity(entityKey = "dtId", entityType = "digitaltwin")
@RequestMapping("/dt/{dtId}")
public class DigitalTwinService extends EventSourcedEntity<DigitalTwinState> {

    private final String dtId;

    private final MLScoringService mlScoringService;

    @Autowired
    public DigitalTwinService(EventSourcedEntityContext context,MLScoringService mlScoringService) {
        this.dtId = context.entityId();
        this.mlScoringService = mlScoringService;
    }

    @Override
    public DigitalTwinState emptyState() {
        return DigitalTwinState.empty();
    }


    @PostMapping("/create")
    public Effect<DigitalTwinApi.EmptyResponse> create(@RequestBody DigitalTwinApi.CreateRequest request){
        if(this.currentState().isEmpty()){
            DigitalTwinEvent.Created event = new DigitalTwinEvent.Created(dtId, request.getName(),Instant.now());
            return effects().emitEvent(event).thenReply(newState -> DigitalTwinApi.EmptyResponse.of());
        } else {
            return effects().reply(DigitalTwinApi.EmptyResponse.of());
        }
    }

    @PostMapping("/metric")
    public Effect<DigitalTwinApi.EmptyResponse> metric(@RequestBody DigitalTwinApi.MetricRequest request){
        if(currentState().isEmpty()){
            return effects().error("Not found", Status.Code.NOT_FOUND);
        }else if (!currentState().isMaintenanceRequired()){
            boolean maintenanceRequired = mlScoringService.scoreIfMaintenanceRequired(request.getRaw1(), request.getRaw2());
            if(maintenanceRequired){
                DigitalTwinEvent.MaintenanceRequired event = new DigitalTwinEvent.MaintenanceRequired(dtId,Instant.now());
                return effects().emitEvent(event).thenReply(newState -> DigitalTwinApi.EmptyResponse.of());
            } else {
                return effects().reply(DigitalTwinApi.EmptyResponse.of());
            }
        } else {
            return effects().reply(DigitalTwinApi.EmptyResponse.of());
        }
    }

    @PostMapping("/set-maintenance-performed")
    public Effect<DigitalTwinApi.EmptyResponse> setMaintenancePerformed(){
        if(currentState().isEmpty()){
            return effects().error("Not found", Status.Code.NOT_FOUND);
        }else if (currentState().isMaintenanceRequired()){
            DigitalTwinEvent.MaintenancePerformed event = new DigitalTwinEvent.MaintenancePerformed(dtId,Instant.now());
            return effects().emitEvent(event).thenReply(newState -> DigitalTwinApi.EmptyResponse.of());
        } else {
            return effects().reply(DigitalTwinApi.EmptyResponse.of());
        }
    }

    @GetMapping
    public Effect<DigitalTwinApi.GetResponse> get(){
        if(currentState().isEmpty()){
            return effects().error("Not found", Status.Code.NOT_FOUND);
        } else {
            return effects().reply(new DigitalTwinApi.GetResponse(this.currentState().getName(),this.currentState().isMaintenanceRequired(),this.currentState().getLastUpdated()));
        }
    }

    @EventHandler
    public DigitalTwinState onCreated (DigitalTwinEvent.Created event){
        return this.currentState().onCreated(event);
    }
    @EventHandler
    public DigitalTwinState onMaintenanceRequired (DigitalTwinEvent.MaintenanceRequired event){
        return this.currentState().onMaintenanceRequired(event);
    }
    @EventHandler
    public DigitalTwinState onMaintenancePerformed (DigitalTwinEvent.MaintenancePerformed event){
        return this.currentState().onMaintenancePerformed(event);
    }
}
