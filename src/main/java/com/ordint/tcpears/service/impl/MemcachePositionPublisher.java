package com.ordint.tcpears.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.domain.DefaultOutputWriter;
import com.ordint.tcpears.domain.OutputWriter;
import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.service.PositionDataProvider;
import com.ordint.tcpears.service.PositionPublisher;

@Component
public class MemcachePositionPublisher implements PositionPublisher {
	
	private final static Logger log = LoggerFactory.getLogger(MemcachePositionPublisher.class);
	private final static String TRACK_PREFIX = "/ggps/tracks/%s";
	private final static String LOCATION_PREFIX = "/ggps/locations/%s";
	private final static String PREDICTIONS_KEY = "/ggps/predictions/";
	private OutputWriter outputBuilder = new DefaultOutputWriter();

	private ConcurrentMap<String, String> predictions = new ConcurrentHashMap<>();
	
	@Autowired
	private MemcacheHelper memcacheHelper;
	@Autowired
	@Qualifier("clientManager")
	private PositionDataProvider dataProvider;
	@Autowired
	private PredictionServiceImpl predictionService;

	
	/* (non-Javadoc)
	 * @see com.ordint.tcpears.service.impl.PositionPublisher#publishPositions()
	 */
	@Override
	public void publishPositions()  throws IOException {
		//A map of lists of postitions keyed on groupId
		ConcurrentMap<String, List<Position>> positionGroups = dataProvider.groupClientsByGroup();
		//build memecache objects for each of the groups of positions	
		for(String groupId : positionGroups.keySet()) {
			List<Position> positions = positionGroups.get(groupId);
			//predictions.putAll(predictionService.predictPositions(groupId, positions));
			//transform the postions here??
			if (positions != null) {
				ConcurrentMap<String, String> postionMap = positions
						.stream()
						.collect(Collectors.toConcurrentMap(Position::getClientId, p -> outputBuilder.write(p)));
	
				//save to memcache
				String groupKeyName = String.format(LOCATION_PREFIX, groupId);
				
				memcacheHelper.set(groupKeyName, groupKeyName, postionMap, 5);
				
			}
		}	
		
	}
	
	/* (non-Javadoc)
	 * @see com.ordint.tcpears.service.impl.PositionPublisher#publishTracks()
	 */
	@Override
	public void publishTracks() throws Exception {
		ConcurrentMap<String, ConcurrentMap<String, String>> groupTracks = dataProvider.getGroupTracks();

		boolean atLeastOne = false;
		for(String groupId : groupTracks.keySet()) {
			String groupKeyName = String.format(TRACK_PREFIX, groupId);		
			memcacheHelper.set(groupKeyName, groupKeyName, groupTracks.get(groupId));
			atLeastOne = true;			
		}
		if (atLeastOne) {
			memcacheHelper.set(PREDICTIONS_KEY, PREDICTIONS_KEY, predictions);
		}
		
 	}
	
	/* (non-Javadoc)
	 * @see com.ordint.tcpears.service.impl.PositionPublisher#clearTrack(java.lang.String)
	 */
	@Override
	public void clearTrack(String groupId) {
		String groupKeyName = String.format(TRACK_PREFIX,groupId);
		memcacheHelper.clear(groupKeyName, groupKeyName);		
	}
	
	/* (non-Javadoc)
	 * @see com.ordint.tcpears.service.impl.PositionPublisher#clearPositions(java.lang.String)
	 */
	@Override
	public void clearPositions(String groupId) {
		String groupKeyName = String.format(LOCATION_PREFIX, groupId);
		memcacheHelper.clear(groupKeyName, groupKeyName);			
	}

	@Override
	public void clearAllTracks() {
		ConcurrentMap<String, ConcurrentMap<String, String>> groupTracks = dataProvider.getGroupTracks();
		groupTracks.keySet().forEach(groupId -> clearTrack(groupId));
		
	}
	


}
