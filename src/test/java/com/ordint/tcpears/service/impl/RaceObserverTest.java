package com.ordint.tcpears.service.impl;

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.PositionDistanceInfo;
import com.ordint.tcpears.domain.PositionUtil;
import com.ordint.tcpears.domain.Track;
import com.ordint.tcpears.service.impl.RaceObserver.RaceStatus;

@RunWith(MockitoJUnitRunner.class)
public class RaceObserverTest {
	@Mock
	private Track track;
	@InjectMocks
	private RaceObserver raceObserver;
	
	private RaceStatusListener statusListener;
	
	@Before
	public void setUp() throws Exception {
		statusListener = new StatusListener();
		raceObserver.addRaceStatusListener(statusListener);
	}

	@Test
	public void testDecorate() throws Exception {
		
	}

	public void raceStatusShouldChangeToUnderStartrsOrdersWhenAllRunnersArsStill() {
			
	}
	
	public void raceStatusShouldChangeToStartedWhenRunnersStartMoving() {
		
	}
	
	public void raceStatusShouldChangeToFinishedWhenAllRunnersCrossFinishLine() {
		
	}
	
	public void positionsShouldHaveCorrectStandingsDuringRace() throws Exception {
		Position p1 = PositionUtil.createPosition("1", "2", "1", "group", "client1");
		Position p2 = PositionUtil.createPosition("1", "2", "1", "group", "client2");
		Position p3 = PositionUtil.createPosition("1", "2", "1", "group", "client3");
		
		
		BDDMockito.given(track.calculateDistanceInfo(p1)).willReturn(new PositionDistanceInfo(p1.getClientId(), 100, 300, -1));
		BDDMockito.given(track.calculateDistanceInfo(p2)).willReturn(new PositionDistanceInfo(p2.getClientId(), 125, 275, -1));
		BDDMockito.given(track.calculateDistanceInfo(p3)).willReturn(new PositionDistanceInfo(p3.getClientId(), 150, 250, -1));
		
		raceObserver.decorate(p1);
		raceObserver.decorate(p2);
		raceObserver.decorate(p3);
		
		List<Position> updated = raceObserver.decorate(Arrays.asList(p1,p2,p3));
		
		assertThat(updated, Matchers.contains(Position.builder().position(p1).standing(3).build(),
				Position.builder().position(p2).standing(2).build(), Position.builder().position(p3).standing(1).build()));
		
		
	}
	
	public void standingsShouldNotChangeOnceFinishIsPassed() throws Exception {
		Position p1 = PositionUtil.createPosition("1", "2", "1", "group", "client1");
		Position p2 = PositionUtil.createPosition("1", "2", "1", "group", "client2");
		Position p3 = PositionUtil.createPosition("1", "2", "1", "group", "client3");
		
		
		BDDMockito.given(track.calculateDistanceInfo(p1)).willReturn(new PositionDistanceInfo(p1.getClientId(), 100, 300, -1));
		BDDMockito.given(track.calculateDistanceInfo(p2)).willReturn(new PositionDistanceInfo(p2.getClientId(), 125, 275, -1),new PositionDistanceInfo(p2.getClientId(), 500, -100, -1));
		BDDMockito.given(track.calculateDistanceInfo(p3)).willReturn(new PositionDistanceInfo(p3.getClientId(), 150, 250, -1),new PositionDistanceInfo(p3.getClientId(), 450, -50, -1));
		
		raceObserver.decorate(p1);
		raceObserver.decorate(p2);
		raceObserver.decorate(p3);
		raceObserver.decorate(Arrays.asList(p1,p2,p3));
		//this will put p3 over the finsih line
		raceObserver.decorate(p3);
		
		//raceObserver.decorate(Arrays.asList(p1,p2,p3));
		//p2 is now in front of p3, but standings should stay the same
		raceObserver.decorate(p2);
		
		List<Position> updated = raceObserver.decorate(Arrays.asList(p1,p2,p3));
		
		assertThat(updated, Matchers.contains(Position.builder().position(p1).standing(3).build(),
				Position.builder().position(p2).standing(2).build(), Position.builder().position(p3).standing(1).build()));
		
		
	}
}

class StatusListener implements RaceStatusListener{	
	private RaceStatus status;
	@Override
	public void onStatusChange(RaceStatus status) {
		this.status = status;
	}
	public RaceStatus getStatus() {
		return status;
	}
	
	
}
