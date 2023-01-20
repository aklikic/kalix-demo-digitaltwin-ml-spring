package com.example.digitaltwin.ml;

import com.example.digitaltwin.DigitalTwinModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MLScoringServiceMock implements MLScoringService {

    public static final DigitalTwinModel.MetricRequest metricOKRequest = new DigitalTwinModel.MetricRequest(0.92d,0.328501935d);
    public static final DigitalTwinModel.MetricRequest metricFailRequest = new DigitalTwinModel.MetricRequest(0.72d,0.002764904d);

    @Override
    public boolean scoreIfMaintenanceRequired(List<Data> dataList) {
        return !dataList.stream().filter(d -> d.raw1 == metricOKRequest.raw1.doubleValue() && d.raw2 == metricOKRequest.raw2.doubleValue()).findAny().isPresent();
    }
}
