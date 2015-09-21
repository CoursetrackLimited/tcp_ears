package com.ordint.tcpears.util.prediction;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.util.MeasuredShape;
import com.ordint.tcpears.util.PredictionUtil;

public class RacePositionCalculator {
	private MeasuredShape track;
	TrackBasedPredictor tbp ;
	public RacePositionCalculator(Path2D trackPath) {
		track = new MeasuredShape(trackPath);
		 tbp = new TrackBasedPredictor(trackPath);
		
	}
	
	public Map<String, RacePosition> calculate(Collection<Position> currentPositions) {
		Map<String, RacePosition> results = new HashMap<>();
		List<PositionDistance> distances = new ArrayList<>();
		for(Position p : currentPositions) {
			Point2D currentPoint = PredictionUtil.toPoint(p);
			Point2D trackPoint = track.getClosestPoint(currentPoint);
			distances.add(new PositionDistance(p.getClientId(), track.getPointDistance(trackPoint)));
		}
		distances.sort((PositionDistance p1, PositionDistance p2) -> Double.compare(p2.distance, p1.distance));
		for(int i = 0; i < distances.size(); i++) {
			RacePosition rp = new RacePosition(i + 1, -1, distances.get(i).distance, distances.get(i).clientId);
			results.put(rp.getRunnerId(), rp);
		}
		
		return results;
	}
	

	
	private static class PositionDistance {
		final String clientId;
		final double distance;
		public PositionDistance(String clientId, double distance) {
			this.clientId = clientId;
			this.distance = distance;
		}
	}
}
