package com.ordint.tcpears.domain;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class SectorTime {

    private long time;
    private Sector sector;
}
