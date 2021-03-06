package com.realsight.brain.timeseries.example;


import java.io.File;
//import java.nio.file.Paths;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;

import com.realsight.brain.timeseries.lib.csv.CsvReader;
import com.realsight.brain.timeseries.lib.model.anomaly.AnormalyDetection;
import com.realsight.brain.timeseries.lib.series.DoubleSeries;
import com.realsight.brain.timeseries.lib.series.TimeSeries;
import com.realsight.brain.timeseries.lib.util.Pair;

/**
 * @author Sun Muxin
 * 
 */ 
public class OfflineAnormalySegmentExample {
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		String root = new File(System.getProperty("user.dir")).getPath();
		String localDir = Paths.get(root, "target", "data", "Health.csv").toString();
		char delimiter = ',';
		Charset charset = Charset.forName("ISO-8859-1");
		DoubleSeries nSeries = new DoubleSeries("series name");
		try {
			CsvReader cr = new CsvReader(localDir, delimiter, charset);
			cr.readHeaders();
			if(cr.getIndex("timestamp") == -1)
				throw new IOException("File not exists timestamp.");
			while(cr.readRecord()){
				String time = cr.get("timestamp");
				Long timestamp = Long.parseLong(time);
				double value = Double.parseDouble(cr.get("value"));
				nSeries.add(new TimeSeries.Entry<Double>(value, timestamp));
			}
			cr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		int n = nSeries.size();
		double minValue = nSeries.min();
		double maxValue = nSeries.max();
		AnormalyDetection anormalySegment = AnormalyDetection.build(nSeries.subSeries(0, n/10), minValue, maxValue);
		List<Pair<Long, Long>> anomalySegments = anormalySegment.detectorSeriesAnomalySegment(nSeries, 0.3);
		for ( int i = 0; i < anomalySegments.size(); i++) {
			System.out.println(anomalySegments.get(i).toString());
		}
	}
}
