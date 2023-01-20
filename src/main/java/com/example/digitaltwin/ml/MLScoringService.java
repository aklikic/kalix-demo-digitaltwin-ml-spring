package com.example.digitaltwin.ml;

import java.util.List;

public interface MLScoringService {

    class Data{
        public final double raw1;
        public final double raw2;

        public Data(double raw1, double raw2) {
            this.raw1 = raw1;
            this.raw2 = raw2;
        }
    }

    boolean scoreIfMaintenanceRequired(List<Data> dataList);

}
