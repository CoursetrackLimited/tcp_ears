package com.ordint.tcpears.track;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.ordint.tcpears.domain.Sector;
import com.ordint.tcpears.domain.SectorTime;

public class SectorTimeCalculator {

    private Clock clock = Clock.systemUTC();
    private Instant start;
    private List<Sector> sectors;
    private ConcurrentMap<String, Deque<SectorTime>> clientSectorTimes;

    public SectorTimeCalculator(Clock clock, List<Sector> sectors) {
        this.sectors = sectors;
        this.clock = clock;
    }

    public void start() {
        start = clock.instant();
    }


    public void checkSector(String clientId, double distanceFromFinish) {

        Deque<SectorTime> clientSectors = clientSectorTimes.getOrDefault(clientId, new ArrayDeque<>());
        int sectorIndex = clientSectors.size();

        if (sectorIndex  < sectors.size()) {
            Sector sector = sectors.get(clientSectors.size());

            if (distanceFromFinish > sector.getSectorDistance()) {
                clientSectors.push(SectorTime.builder().time(clock.instant().toEpochMilli() - start.toEpochMilli()).sector(sector).build());
            }
        }

    }

    public List<SectorTime> getSectorTimes(String clientId){
        return Collections.unmodifiableList(new ArrayList<>(clientSectorTimes.getOrDefault(clientId, new ArrayDeque<>())));
    }



}
