package com.ordint.tcpears.track;

import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ordint.tcpears.domain.lombok.Position;
import com.ordint.tcpears.domain.lombok.PositionDistanceInfo;
import com.ordint.tcpears.domain.lombok.Sector;
import com.ordint.tcpears.domain.lombok.TrackConfig;
import com.ordint.tcpears.track.geom.MeasuredShape;
import com.ordint.tcpears.track.geom.PositionToPointConverter;
import com.ordint.tcpears.track.geom.TrackGeomFactory;

public class Track2 {
	
	private static final double FURLONG_IN_METERS = 201.168;

	private static final Logger log = LoggerFactory.getLogger(Track.class);

	private double officialRaceDistance;
	private MeasuredShape trackShape;
	private PositionToPointConverter positionToPointConverter;
	private final double startDistance;


	public Track2(String kmlPoints, String finishLatLong, double raceDistanceMeters) {
		this(kmlPoints, finishLatLong, new TrackGeomFactory(), raceDistanceMeters);
		
	}
	public Track2(String kmlPoints, String finishLatLong, TrackGeomFactory geomFactory, double raceDistanceMeters) {
		positionToPointConverter = geomFactory.createPositionToPointConverter(kmlPoints);
		this.trackShape = geomFactory.createTrackShape(kmlPoints, positionToPointConverter);
		this.officialRaceDistance = raceDistanceMeters;
		Point2D temp = positionToPointConverter.toPoint(finishLatLong);
		double[] info = trackShape.getDistanceAlongTrackLastThreeSegments(temp);

		startDistance =  info[2] - officialRaceDistance;

		System.out.println(positionToPointConverter.metersToLat(info[1]) + " " + positionToPointConverter.metersToLon(info[0]));
		
	}
	

	public Track2(TrackConfig trackConfig, TrackGeomFactory geomFactory,  double raceDistanceMeters) {
		this(trackConfig.getKml(), trackConfig.getFinishLine(), geomFactory, raceDistanceMeters);
	}




    private double distanceFromStartOfTrack(Point2D currentPoint, double distanceFromStartOfTrack) {
        double[] info = trackShape.getDistanceAlongTrack(currentPoint, distanceFromStartOfTrack);
        if (info != null) {
            return info[2];
        } else {
            log.warn("Could not cacluclate distance info for {}", currentPoint);
            return -1;
        }
    }
    private double distanceFromStartOfTrack(Position position) {
        Point2D currentPoint = positionToPointConverter.toPoint(position);
        return distanceFromStartOfTrack(currentPoint, position.getDistanceInfo() != null ? 
        		trackShape.getOriginalDistance() - position.getDistanceInfo().getDistanceFromEndOfTrack() : 
        			startDistance); 
    }


    public PositionDistanceInfo calculateDistanceInfo(Position position) {
		double distance = distanceFromStartOfTrack(position);
		if (distance  >  0) {
		    return new PositionDistanceInfo(position.getClientId(),  distance - startDistance , trackShape.getOriginalDistance() - distance , 0);
		} else {
			log.warn("Could not cacluclate distance info for {}", position);
			return  new PositionDistanceInfo(position.getClientId(), -1, -1, 0);
		}		
	}

	public double getFinishDistance() {

	    return officialRaceDistance;
	}
	
	public List<Sector> getSectors() {
		ArrayList<Sector> sectors = new ArrayList<>();
		int sectorCount = (int)(officialRaceDistance/ FURLONG_IN_METERS);
		for (int i =0; i < sectorCount; i++) {
			if ( i < sectorCount -1) {
				sectors.add(Sector.builder().name("Furlong "+ (i + 1)).sectorDistance(FURLONG_IN_METERS * (i + 1)).build());
			} else {
				sectors.add(Sector.builder().name("Finish").sectorDistance(officialRaceDistance).build());
			}
		}
		return sectors;
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
