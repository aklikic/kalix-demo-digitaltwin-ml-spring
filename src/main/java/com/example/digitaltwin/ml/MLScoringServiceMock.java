package com.example.digitaltwin.ml;

import com.example.digitaltwin.DigitalTwinModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MLScoringServiceMock implements MLScoringService {

//    private static final DigitalTwinModel.MetricRequest metricOKRequest = new DigitalTwinModel.MetricRequest(UUID.randomUUID().toString(),0.92d,0.328501935d);
//    private static final DigitalTwinModel.MetricRequest metricFailRequest = new DigitalTwinModel.MetricRequest(UUID.randomUUID().toString(),0.72d,0.002764904d);

    private static final double [] ok = {0.92d,0.328501935d};
    private static final double [] fail = {0.72d,0.002764904d};

    public static final DigitalTwinModel.MetricRequest metricOKRequest () {
        return new DigitalTwinModel.MetricRequest(UUID.randomUUID().toString(),ok[0],ok[1]);
    }
    public static final DigitalTwinModel.MetricRequest metricFailRequest () {
        return new DigitalTwinModel.MetricRequest(UUID.randomUUID().toString(),fail[0],fail[1]);
    };


    @Override
    public boolean scoreAndReturnIfMaintenanceRequired(List<Data> dataList) {
        return !dataList.stream().filter(d -> d.raw1 == ok[0] && d.raw2 == ok[1]).findAny().isPresent();
    }
}
