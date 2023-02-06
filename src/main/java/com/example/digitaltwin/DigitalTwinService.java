package com.example.digitaltwin;

import com.example.digitaltwin.ml.MLScoringService;
import io.grpc.Status;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import kalix.springsdk.annotations.EntityKey;
import kalix.springsdk.annotations.EntityType;
import kalix.springsdk.annotations.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@EntityKey("dtId")
@EntityType("digitaltwin")
@RequestMapping("/dt/{dtId}")
public class DigitalTwinService extends EventSourcedEntity<DigitalTwinModel.State> {

    private final String dtId;

    private final MLScoringService mlScoringService;

    @Autowired
    public DigitalTwinService(EventSourcedEntityContext context, MLScoringService mlScoringService) {
        this.dtId = context.entityId();
        this.mlScoringService = mlScoringService;
    }

    @Override
    public DigitalTwinModel.State emptyState() {
        return DigitalTwinModel.State.empty();
    }


    @PostMapping("/create")
    public Effect<String> create(@RequestBody DigitalTwinModel.CreateRequest request){
        if(this.currentState().isEmpty()){
            var nextAggregationId = UUID.randomUUID().toString();
            var event =
                    new DigitalTwinModel.CreatedEvent(
                            dtId,
                            request.name,
                            request.aggregationLimit,
                            request.aggregationTimeWindowSeconds,
                            nextAggregationId,
                            Instant.now());
            return effects().emitEvent(event).thenReply(newState -> DigitalTwinModel.OK_RESPONSE);
        } else {
            return effects().reply(DigitalTwinModel.OK_RESPONSE);
        }
    }

    @PostMapping("/metric")
    public Effect<String> metric(@RequestBody DigitalTwinModel.MetricRequest request) {
        if (currentState().isEmpty()) {
            //if digital twin was not created so not recognized
            return effects().error("Digital twin not created!", Status.Code.NOT_FOUND);
        }else if(currentState().isDuplicate(request.requestId)){
            //check duplicate requests
            return effects().reply(DigitalTwinModel.OK_RESPONSE);
        }else if(currentState().maintenanceRequired){
            //if maintenanceRequired is triggered all metrics are ignored until maintenance perform is triggered
            return effects().reply(DigitalTwinModel.OK_RESPONSE);
        }else{
            var metricAggregateEvent =
                    new DigitalTwinModel.MetricAggregatedEvent(
                            dtId,
                            currentState().aggregationId,
                            request.requestId,
                            request.raw1,
                            request.raw2,
                            Instant.now());
            var tmpNewState = currentState().onMetricAggregatedEvent(metricAggregateEvent);
            if(tmpNewState.isAggregationFinished()) {
                //if aggregation is finished, trigger ML scoring/serving
                var event = triggerMlScoring(tmpNewState);
                return effects().emitEvent(event).thenReply(newState -> DigitalTwinModel.OK_RESPONSE);
            }else{
                //if aggregation is NOT finished, aggregate
                return effects().emitEvent(metricAggregateEvent).thenReply(newState -> DigitalTwinModel.OK_RESPONSE);
            }
        }
    }

    private DigitalTwinModel.AggregationFinishedEvent triggerMlScoring(DigitalTwinModel.State state){
        var dataList = state.aggregation.stream().map(md -> new MLScoringService.Data(md.raw1, md.raw2)).collect(Collectors.toList());
        var maintenanceRequired = mlScoringService.scoreAndReturnIfMaintenanceRequired(dataList);
        var nextAggregationId = UUID.randomUUID().toString();
        return new DigitalTwinModel.AggregationFinishedEvent(dtId, currentState().aggregationTimeWindowSeconds, nextAggregationId, maintenanceRequired, Instant.now());
    }



    @PostMapping("/aggregation-time-window-timer-trigger")
    public Effect<String> aggregationTimeWindowTimerTrigger(@RequestBody DigitalTwinModel.AggregationTimeWindowDoneRequest request){
        if(currentState().isEmpty()){
            return effects().reply(DigitalTwinModel.OK_RESPONSE);
        }else if (currentState().aggregationId.equals(request.aggregationId) && currentState().isAggregationFinished()){
            //if aggregation is finished, trigger ML scoring/serving
            var event = triggerMlScoring(currentState());
            return effects().emitEvent(event).thenReply(newState -> DigitalTwinModel.OK_RESPONSE);
        } else {
            return effects().reply(DigitalTwinModel.OK_RESPONSE);
        }
    }

    @PostMapping("/set-maintenance-performed")
    public Effect<String> setMaintenancePerformed(){
        if(currentState().isEmpty()){
            return effects().error("Not found", Status.Code.NOT_FOUND);
        }else if (currentState().maintenanceRequired){
            var nextAggregationId = UUID.randomUUID().toString();
            var event = new DigitalTwinModel.MaintenancePerformedEvent(dtId, currentState().aggregationTimeWindowSeconds, nextAggregationId, Instant.now());
            return effects().emitEvent(event).thenReply(newState -> DigitalTwinModel.OK_RESPONSE);
        } else {
            return effects().reply(DigitalTwinModel.OK_RESPONSE);
        }
    }

    @GetMapping
    public Effect<DigitalTwinModel.GetResponse> get(){
        if(currentState().isEmpty()){
            return effects().error("Not found", Status.Code.NOT_FOUND);
        } else {
            return effects().reply(new DigitalTwinModel.GetResponse(currentState().name, currentState().aggregation.size(), currentState().maintenanceRequired));
        }
    }

    @EventHandler
    public DigitalTwinModel.State onCreatedEvent (DigitalTwinModel.CreatedEvent event){
        return this.currentState().onCreatedEvent(event);
    }
    @EventHandler
    public DigitalTwinModel.State onMetricAggregateEvent (DigitalTwinModel.MetricAggregatedEvent event){
        return this.currentState().onMetricAggregatedEvent(event);
    }
    @EventHandler
    public DigitalTwinModel.State onAggregationFinishedEvent (DigitalTwinModel.AggregationFinishedEvent event){
        return this.currentState().onAggregationFinishedEvent(event);
    }
    @EventHandler
    public DigitalTwinModel.State onMaintenancePerformedEvent (DigitalTwinModel.MaintenancePerformedEvent event){
        return this.currentState().onMaintenancePerformedEvent(event);
    }
}
