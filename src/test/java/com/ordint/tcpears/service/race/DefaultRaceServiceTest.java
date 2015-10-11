package com.ordint.tcpears.service.race;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.ordint.tcpears.domain.ClientDetails;
import com.ordint.tcpears.domain.RaceDetail;
import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.ClientManager;
import com.ordint.tcpears.service.PositionPublisher;
import com.ordint.tcpears.service.ReplayService;
import com.ordint.tcpears.service.position.PositionEnhancers;

@RunWith(MockitoJUnitRunner.class)
public class DefaultRaceServiceTest {
	@Mock
	private ClientDetailsResolver clientDetailsResolver;
	
	@Mock
	private ClientManager clientManager;
	
	@Mock
	private JdbcTemplate jdbcTemplate;
	
	@Mock
	private MemcacheHelper memcacheHelper;
	
	@Mock
	private ReplayService replayService;
	
	@InjectMocks
	private DefaultRaceService defaultRaceService;
	
	@Mock
	private PositionPublisher positionPublisher;
	
	@Mock
	private PositionEnhancers positionEnhancers;
	
	@Before
	public void setUp() throws Exception {
	}

	@Test(expected = IllegalStateException.class)
	public void startRaceShouldThrowIllegalStateException() throws Exception {
		given(jdbcTemplate.queryForObject("select status from races where race_id=100", String.class)).willReturn("STARTED");
		
		defaultRaceService.startRace(100);
	}
	@Test(expected = IllegalStateException.class)
	public void startRaceShouldThrowIllegalStateException2() throws Exception {
		given(jdbcTemplate.queryForObject("select status from races where race_id=100", String.class)).willReturn("FINISHED");
		
		defaultRaceService.startRace(100);
	}
	
	@Test
	public void startRaceShouldUpdateClientDetails() throws Exception {
		given(jdbcTemplate.queryForObject("select status from races where race_id=100", String.class)).willReturn("NOT_STARTED");
		ClientDetails cd1 = new ClientDetails();
		ClientDetails cd2 = new ClientDetails();
		given(jdbcTemplate.query(eq(DefaultRaceService.CLIENT_DETAILS_FOR_RACE_SQL), (RowMapper<ClientDetails>)anyObject(), anyLong()))
			.willReturn(Arrays.asList(cd1,cd2));
		given(jdbcTemplate.queryForObject(eq(DefaultRaceService.RACE_DETAIL_SQL), any(RaceRowMapper.class), anyLong()))
			.willReturn(RaceDetail.builder().scheduledStartTime(LocalDateTime.now()).build());
		defaultRaceService.startRace(100);
		ArgumentCaptor<List> cap = ArgumentCaptor.forClass(List.class);
		Mockito.verify(clientDetailsResolver).updateClientDetails(cap.capture());
		
		
	}
	@Test
	public void testFinishRace() throws Exception {
		given(jdbcTemplate.queryForObject("select status from races where race_id=100", String.class)).willReturn("STARTED");
		ClientDetails cd1 = new ClientDetails();
		ClientDetails cd2 = new ClientDetails();
		given(jdbcTemplate.query(eq(DefaultRaceService.RESET_CLIENT_DETAILS_SQL), (RowMapper<ClientDetails>)anyObject(), anyLong()))
			.willReturn(Arrays.asList(cd1,cd2));
		defaultRaceService.finishRace(100);
		ArgumentCaptor<List> cap = ArgumentCaptor.forClass(List.class);
		Mockito.verify(clientDetailsResolver).updateClientDetails(cap.capture());
		
		
	}

	@Test
	public void shouldReplayRace() throws Exception {
		given(jdbcTemplate.queryForObject("select status from races where race_id=100", String.class)).willReturn("FINISHED");
		ClientDetails cd1 = new ClientDetails();
		ClientDetails cd2 = new ClientDetails();
		given(jdbcTemplate.query(eq(DefaultRaceService.CLIENT_DETAILS_FOR_RACE_SQL), (RowMapper<ClientDetails>)anyObject(), anyLong()))
			.willReturn(Arrays.asList(cd1,cd2));
		given(jdbcTemplate.queryForObject(eq(DefaultRaceService.RACE_DETAIL_SQL), any(RaceRowMapper.class), anyLong()))
			.willReturn(RaceDetail.builder().scheduledStartTime(LocalDateTime.now()).build());
		
		Map<String,Object> row = new HashMap<>();
		row.put("group_id", 1l);
		row.put("actualStartTime", "2015-09-16 22:32:38");
		row.put("finishTime", "2015-09-16 22:33:38");
		row.put("name", "Some race");
		row.put("venue_id", 1l);
		
		given(jdbcTemplate.queryForMap(anyString(), anyLong())).willReturn(row); 
		String raceName = defaultRaceService.replayRace(100);
		then(clientManager).should().clearTrack("1");

		then(replayService).should().replayFrom(any(LocalDateTime.class), eq(60), eq(true),eq("1-Some race-2015-09-16T22:32:38-60"));
		then(jdbcTemplate).should().update(eq("update races set status ='REPLAYING' where race_id=?"), eq(100l));
		
		assertThat(raceName, equalTo("Some race"));
		assertThat(defaultRaceService.getCurrentReplayRaceId(1), equalTo(100l));		
	}
	
	@Test(expected = RaceServiceException.class)
	public void shouldThrowRaceExceptionIfAReplayIsAlreadyRunningForThatVenue() throws Exception {
		given(jdbcTemplate.queryForObject(anyString(), eq(String.class))).willReturn("FINISHED");	
		ClientDetails cd1 = new ClientDetails();
		given(jdbcTemplate.query(eq(DefaultRaceService.CLIENT_DETAILS_FOR_RACE_SQL), (RowMapper<ClientDetails>)anyObject(), anyLong()))
			.willReturn(Arrays.asList(cd1));
		given(jdbcTemplate.queryForObject(eq(DefaultRaceService.RACE_DETAIL_SQL), any(RaceRowMapper.class), anyLong()))
			.willReturn(RaceDetail.builder().scheduledStartTime(LocalDateTime.now()).build());
		Map<String,Object> row = new HashMap<>();
		row.put("group_id", 2l);
		row.put("actualStartTime", "2015-09-16 22:32:38");
		row.put("finishTime", "2015-09-16 22:33:38");
		row.put("name", "Some Other race");
		row.put("venue_id", 1l);
		
		given(jdbcTemplate.queryForMap(anyString(), anyLong())).willReturn(row);
		defaultRaceService.replayRace(100);
		
		defaultRaceService.replayRace(200);
		
		
		
		
	
	}

}
