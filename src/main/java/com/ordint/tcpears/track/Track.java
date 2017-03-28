package com.ordint.tcpears.track;

import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.PositionDistanceInfo;
import com.ordint.tcpears.domain.TrackConfig;
import com.ordint.tcpears.track.geom.MeasuredShape;
import com.ordint.tcpears.track.geom.PositionToPointConverter;
import com.ordint.tcpears.track.geom.TrackGeomFactory;

public class Track {
	
	private static final Logger log = LoggerFactory.getLogger(Track.class);

	private double finishLineDistanceFromStartOfTrackShape;
	private double officialRaceDistance;
	private MeasuredShape trackShape;
	private PositionToPointConverter positionToPointConverter;



	public Track(String kmlPoints, String finishLatLong, double raceDistanceMeters) {
		this(kmlPoints, finishLatLong, new TrackGeomFactory(), raceDistanceMeters);
		
	}
	public Track(String kmlPoints, String finishLatLong, TrackGeomFactory geomFactory, double raceDistanceMeters) {
		positionToPointConverter = geomFactory.createPositionToPointConverter(kmlPoints);
		this.trackShape = geomFactory.createTrackShape(kmlPoints, positionToPointConverter);
		this.officialRaceDistance = raceDistanceMeters;
		Point2D temp = positionToPointConverter.toPoint(finishLatLong);
		double[] info = trackShape.getDistanceAlongTrack(temp);
		finishLineDistanceFromStartOfTrackShape = info[2];

		System.out.println(positionToPointConverter.metersToLat(info[1]) + " " + positionToPointConverter.metersToLon(info[0]));
		
	}
	

	public Track(TrackConfig trackConfig, TrackGeomFactory geomFactory,  double raceDistanceMeters) {
		this(trackConfig.getKml(), trackConfig.getFinishLine(), geomFactory, raceDistanceMeters);
	}

/*	private void calculateRaceDistance(){
	           numberOfLaps = (int) (officialRaceDistance / trackShape.getClosedDistance());
	    double lastLapDistance = officialRaceDistance - (numberOfLaps * trackShape.getClosedDistance());

        if (lastLapDistance > finishLineDistanceFromStartOfTrackShape) {
            // startLine is before the start of the track shape
            startLineDistanceFromStartOfTrackShape = trackShape.getClosedDistance() - (lastLapDistance - finishLineDistanceFromStartOfTrackShape);

        } else {
            startLineDistanceFromStartOfTrackShape = finishLineDistanceFromStartOfTrackShape - lastLapDistance;
        }
        startPoint = trackShape.getPoint(startLineDistanceFromStartOfTrackShape);

	}*/

    private double calculateDistance(Point2D currentPoint, double distanceFromFinish) {
        double[] info = trackShape.getDistanceAlongTrack(currentPoint);
        if (info != null) {
            double distanceFromStartOfShape = info[2];
            double distancetToFinish = delta(distanceFromStartOfShape);
            int lapsToGo = 0;
            if (distanceFromFinish > trackShape.getClosedDistance()) {
                double wholeNumberOfLapsPart = ((int) (distanceFromFinish / trackShape.getClosedDistance())) * trackShape.getClosedDistance();
                lapsToGo = (int)((wholeNumberOfLapsPart + distancetToFinish) / trackShape.getClosedDistance());
            }


            distancetToFinish = distancetToFinish + (lapsToGo * trackShape.getClosedDistance());


            return distancetToFinish;
        } else {
            log.warn("Could not cacluclate distance info for {}", currentPoint);
            return -1;
        }
    }
    private double calculateDistance2(Point2D currentPoint, double distanceFromFinish) {
        double[] info = trackShape.getDistanceAlongTrack(currentPoint, finishLineDistanceFromStartOfTrackShape - distanceFromFinish);
        if (info != null) {
            double distanceFromStartOfShape = info[2];
            return finishLineDistanceFromStartOfTrackShape - distanceFromStartOfShape;
        } else {
            log.warn("Could not cacluclate distance info for {}", currentPoint);
            return -1;
        }
    }
    private double calculateDistance(Position position) {
        Point2D currentPoint = positionToPointConverter.toPoint(position);
        return calculateDistance2(currentPoint, position.getDistanceInfo() != null ? position.getDistanceInfo().getDistanceFromFinish() : 0);
    }





    public PositionDistanceInfo calculateDistanceInfo(Position position) {
		double distance = calculateDistance(position);
		if (distance  >  0) {
		    return new PositionDistanceInfo(position.getClientId(),  officialRaceDistance - distance , distance, 0);
		} else {
			log.warn("Could not cacluclate distance info for {}", position);
			return  new PositionDistanceInfo(position.getClientId(), -1, -1, 0);
		}		
	}

	public double getFinishDistance() {

	    return officialRaceDistance;
	}




	private double delta(double horseDistance) {
	    if (horseDistance > finishLineDistanceFromStartOfTrackShape) {
	        return (trackShape.getClosedDistance() - horseDistance + finishLineDistanceFromStartOfTrackShape);
	    } else {
	        return finishLineDistanceFromStartOfTrackShape - horseDistance;
	    }

	}


	public Position predict(Position p, double timeInMillis) {

		double distance = p.getSpeedValue() * (timeInMillis / 1000);
		Point2D extra = trackShape.getOffTrackPoint(positionToPointConverter.toPoint(p), distance);

	    return Position.builder().position(p)
				.timeCreated(LocalDateTime.now())
				.timestampFromDateTime(p.getTimestamp().plus((long) timeInMillis, ChronoUnit.MILLIS))
				.lat(Double.toString(positionToPointConverter.metersToLat(extra.getY())))
				.lon(Double.toString(positionToPointConverter.metersToLon(extra.getX()))).build();
	}		
		
	
}
