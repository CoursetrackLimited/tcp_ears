package com.ordint.tcpears.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;



@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SectorTime {

    private float time;
    private Sector sector;
}
