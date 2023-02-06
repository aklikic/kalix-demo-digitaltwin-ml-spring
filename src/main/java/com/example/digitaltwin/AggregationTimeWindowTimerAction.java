package com.example.digitaltwin;

import kalix.javasdk.action.Action;
import kalix.springsdk.KalixClient;
import kalix.springsdk.annotations.Subscribe;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletionStage;

@Subscribe.EventSourcedEntity(value = DigitalTwinService.class,ignoreUnknown = true)
public class AggregationTimeWindowTimerAction extends Action {

    private KalixClient kalixClient;

    public AggregationTimeWindowTimerAction(KalixClient kalixClient) {
        this.kalixClient = kalixClient;
    }

    private String getTimerName (String dtId){
        return  "TIME-WINDOW-TIMER/" + dtId;
    }
    private CompletionStage<String> createTimer(String dtId, String aggregationId, Integer aggregationTimeWindowSeconds){
        return timers().cancel(getTimerName(dtId))
                .thenCompose(done -> {
                    var deferredCall = kalixClient.post("/dt/"+dtId+"/aggregation-time-window-timer-trigger",new DigitalTwinModel.AggregationTimeWindowDoneRequest(aggregationId),String.class);
                    return timers().startSingleTimer(getTimerName(dtId), Duration.of(aggregationTimeWindowSeconds, ChronoUnit.SECONDS),deferredCall);
                })
                .thenApply(done -> DigitalTwinModel.OK_RESPONSE);
    }
    public Effect<String> onCreatedEvent(DigitalTwinModel.CreatedEvent event){
        var timerCreate = createTimer(event.dtId, event.nextAggregationId, event.aggregationTimeWindowSeconds);
        return effects().asyncReply(timerCreate);
    }

    public Effect<String> onAggregationFinishedEvent(DigitalTwinModel.AggregationFinishedEvent event){
        if(!event.maintenanceRequired) {
            var timerCreate = createTimer(event.dtId, event.nextAggregationId, event.aggregationTimeWindowSeconds);
            return effects().asyncReply(timerCreate);
        } else {
            return effects().reply(DigitalTwinModel.OK_RESPONSE);
        }
    }

    public Effect<String> onAggregationFinishedEvent(DigitalTwinModel.MaintenancePerformedEvent event){
        var timerCreate = createTimer(event.dtId, event.nextAggregationId, event.aggregationTimeWindowSeconds);
        return effects().asyncReply(timerCreate);
    }


}
