package com.ordint.tcpears.service;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.ordint.tcpears.domain.Position;

public interface PositionDataProvider {

	ConcurrentMap<String, ConcurrentMap<String, String>> getSnakes();

	ConcurrentMap<String, List<Position>> groupClientsByGroup();

}