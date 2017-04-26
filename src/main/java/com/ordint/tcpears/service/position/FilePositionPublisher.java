package com.ordint.tcpears.service.position;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.ordint.tcpears.domain.lombok.Position;
import com.ordint.tcpears.service.PositionDataProvider;
import com.ordint.tcpears.service.PositionPublisher;


public class FilePositionPublisher implements PositionPublisher {
	
	private static final String KML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml><Document>";
	private static final String KML_FOOTER = "</Document></kml>";
	private static final String KML_POINT ="<Placemark><name>%s</name><Point><coordinates>%s,%s,0</coordinates></Point></Placemark>";
	private File output = new File("c:/dev/points.kml");
	@Autowired
	@Qualifier("clientManager")
	private PositionDataProvider dataProvider;
	
	public FilePositionPublisher() throws IOException {
		if (!output.exists()) {
			output.createNewFile();
		}
		
	}
	
	@Override
	public void publishPositions()  throws IOException {
		//A map of lists of postitions keyed on groupId
		ConcurrentMap<String, List<Position>> positionGroups = dataProvider.groupClientsByGroup();
		//build memecache objects for each of the groups of positions	
		PrintWriter writer = new PrintWriter(output);
		writer.println(KML_HEADER);
		for(String groupId : positionGroups.keySet()) {
			
			List<Position> positions = positionGroups.get(groupId);
			//predictions.putAll(predictionService.predictPositions(groupId, positions));
			//transform the postions here??
			
			if (positions != null) {
	
				positions.forEach(p -> writer.print(String.format(KML_POINT, p.getClientDetails().getRunnerIdent() + getDistance(p), p.getLon(), p.getLat())));
				
				//save to memcache
			}
			
		}	
		writer.print(KML_FOOTER);
		
		writer.flush();
		writer.close();
	}
	private static String getDistance(Position p) {
		if (p.getDistanceInfo() == null) return "";
		return String.format(" (%.2f)", p.getDistanceInfo().getDistanceFromStart());
	}
	
	@Override
	public void publishSnakes() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearSnake(String groupId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearAllSnakes() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearPositions(String groupId) {
		// TODO Auto-generated method stub

	}

}
