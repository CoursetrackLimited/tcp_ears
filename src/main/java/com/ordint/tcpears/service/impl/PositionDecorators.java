package com.ordint.tcpears.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.service.PositionDecorator;

@Component
public class PositionDecorators {
	private final static Logger log = LoggerFactory.getLogger(PositionDecorators.class);
	private Map<String, List<PositionDecorator>> positionDecorators = new HashMap<>();
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public PositionDecorators() {
		// TODO Auto-generated constructor stub
	}
	
	
	public List<Position> applyDecorators(String groupId, List<Position> positions) {
		List<PositionDecorator> decorators = positionDecorators.get(groupId);
		if (decorators == null) {
			return positions;
		} else {
			return applyDecorators(positions, 0, decorators);
		}
		
	}
	
	public void addReplayDecorators(String groupId, String trackConfigId) {
		log.info("Adding replay decorators for groupId {}", groupId);
		List<PositionDecorator> pd = Arrays.asList(new RacePositionDecorator());
		positionDecorators.put(groupId, pd);
	} 
	
	public void addRaceDecorators(String groupId, String trackConfigId) {
		log.info("Adding race decorators for groupId {}", groupId);
		List<PositionDecorator> pd = Arrays.asList(new RacePositionDecorator());
		positionDecorators.put(groupId, pd);		
	}
	
	public void clearDecorator(String groupId) {
		log.info("Clearing decorators"); 
		positionDecorators.clear();
	}
	
	private List<Position> applyDecorators(List<Position> positions, int index, List<PositionDecorator> decorators) {
		if (index == decorators.size())
			return positions;
		else
			return decorators.get(index).decorate(applyDecorators(positions, index +1, decorators));
	}

}
