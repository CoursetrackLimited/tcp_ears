package com.ordint.tcpears.track;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ordint.tcpears.domain.Sector;
import com.ordint.tcpears.domain.SectorTime;

public class SectorTimeCalculator {
	
	private static final Logger log = LoggerFactory.getLogger(SectorTimeCalculator.class);
    private Clock clock = Clock.systemUTC();
    private Instant start;
    private List<Sector> sectors;
    private ConcurrentMap<String, List<SectorTime>> clientSectorTimes = new ConcurrentHashMap<>();

    public SectorTimeCalculator(Clock clock, List<Sector> sectors) {
        this.sectors = sectors;
        this.clock = clock;
    }

    public void start() {
        start = clock.instant();
    }


    public void checkSector(String clientId, double distanceFromStart) {

        List<SectorTime> clientSectors = clientSectorTimes.getOrDefault(clientId, new ArrayList<>());
        int sectorIndex = clientSectors.size();

        if (sectorIndex  < sectors.size()) {
            Sector sector = sectors.get(sectorIndex);
            log.info("Sectors {} > {}", distanceFromStart, sector.getSectorDistance());
            if (distanceFromStart > sector.getSectorDistance()) {
                clientSectors.add(SectorTime.builder().time(clock.instant().toEpochMilli() - start.toEpochMilli()).sector(sector).build());
            }
        }

    }

    public List<SectorTime> getSectorTimes(String clientId){
        return Collections.unmodifiableList(clientSectorTimes.getOrDefault(clientId, new ArrayList<>()));
    }



}
