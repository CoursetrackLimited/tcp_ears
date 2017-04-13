package com.ordint.tcpears.track;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.Sector;
import com.ordint.tcpears.domain.SectorTime;

public class SectorTimeCalculator {
	
	private static final Logger log = LoggerFactory.getLogger(SectorTimeCalculator.class);
    private Instant start;
    private List<Sector> sectors;
    private ConcurrentMap<String, List<SectorTime>> clientSectorTimes = new ConcurrentHashMap<>();

    public SectorTimeCalculator(List<Sector> sectors) {
        this.sectors = sectors;
        
    }
    
    public void checkSector(Position p,  double distanceFromStart) {
    	if (p != null) {
	        List<SectorTime> clientSectors = clientSectorTimes.getOrDefault(p.getClientId(), new ArrayList<>());
	        int sectorIndex = clientSectors.size();
	       
	        if (sectorIndex  < sectors.size()) {
	            Sector sector = sectors.get(sectorIndex);
	            if (sector != null) {
	            	if (distanceFromStart  >= sector.getSectorDistance()) {
		                clientSectors.add(SectorTime.builder().timestamp(p.getTimestamp()).sector(sector).build());
		                clientSectorTimes.put(p.getClientId(), clientSectors);
		            }
	            }
	        }
    	}
    }

    public List<SectorTime> getSectorTimes(String clientId){
        return Collections.unmodifiableList(clientSectorTimes.getOrDefault(clientId, new ArrayList<>()));
    }
    
    public List<Sector> getSectors() {
    	return Collections.unmodifiableList(sectors);
    }



}
