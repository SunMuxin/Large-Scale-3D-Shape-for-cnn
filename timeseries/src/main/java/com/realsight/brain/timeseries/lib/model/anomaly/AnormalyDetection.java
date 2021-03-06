package com.realsight.brain.timeseries.lib.model.anomaly;

import java.util.ArrayList;
import java.util.List;

import com.realsight.brain.timeseries.lib.model.htm.AnormalyHierarchy;
import com.realsight.brain.timeseries.lib.model.segment.AnormalySegment;
import com.realsight.brain.timeseries.lib.series.DoubleSeries;
import com.realsight.brain.timeseries.lib.series.TimeSeries.Entry;
import com.realsight.brain.timeseries.lib.util.Pair;

public class AnormalyDetection {
	private final int windowsLen = 50;
	private AnormalyHierarchy anormalyHTM = null;
	private AnormalySegment anormalySegment = null;
	DoubleSeries anormalys = new DoubleSeries("anormalys");
	
	private AnormalyDetection() {}
	
	public Entry<Double> detection(double value, Long timestamp) {
		double score = anormalyHTM.detectorAnomaly(value, timestamp);
		score += anormalySegment.detectorAnomaly(value, timestamp)/2;
		return (new Entry<Double>(score, timestamp));
	}
	
	public DoubleSeries detectorSeries(DoubleSeries nSeries) {
		DoubleSeries scores = new DoubleSeries("anormalys");
		for ( int i = 0; i < nSeries.size(); i++ ) {
			double value = nSeries.get(i).getItem();
			Long timestamp = nSeries.get(i).getInstant();
			scores.add(detection(value, timestamp));
		}
		return scores;
	}
	
	public List<Pair<Long, Long>> detectorSeriesAnomalySegment(DoubleSeries nSeries) {
		return detectorSeriesAnomalySegment(nSeries, 0.81);
	}
	
	public List<Pair<Long, Long>> detectorSeriesAnomalySegment(DoubleSeries nSeries, double baseThreshold) {
		if ( baseThreshold <= 0.3 )
			baseThreshold = 0.3;
		List<Pair<Long, Long>> res = new ArrayList<Pair<Long, Long>>();
		DoubleSeries seriesAnomalys = detectorSeries(nSeries);
		Long segmentLeftTimestamp = -1L;
		Long segmentRightTimestamp = -1L;
		for(int i = 0; i < seriesAnomalys.size(); i++) {
			double anormaly = seriesAnomalys.get(i).getItem();
			Long timestamp = seriesAnomalys.get(i).getInstant();
			if ( anormaly >= baseThreshold ) {
				if (segmentRightTimestamp == -1){
					segmentLeftTimestamp = timestamp;
					segmentRightTimestamp = timestamp;
				}
				else if ( seriesAnomalys.subSeries(i-windowsLen, i).max() >= baseThreshold) {
					segmentRightTimestamp = timestamp;
				}
				else {
					res.add(new Pair<Long, Long>(segmentLeftTimestamp, segmentRightTimestamp));
					segmentLeftTimestamp = timestamp;
					segmentRightTimestamp = timestamp;
				}
			}
		}
		if (segmentRightTimestamp != -1) {
			res.add(new Pair<Long, Long>(segmentLeftTimestamp, segmentRightTimestamp));
		}
		return res;
	}
	
	public static AnormalyDetection build(DoubleSeries nSeries, double minValue, double maxValue) {
		AnormalyDetection AD = new AnormalyDetection();
		AD.anormalyHTM = AnormalyHierarchy.build(nSeries, minValue, maxValue);
		AD.anormalySegment = AnormalySegment.build(nSeries, minValue, maxValue);
		return AD;
	}
}
