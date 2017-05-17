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
import java.util.List;

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

import com.ordint.tcpears.domain.lombok.ClientDetails;
import com.ordint.tcpears.domain.lombok.RaceDetail;
import com.ordint.tcpears.domain.lombok.RaceDetail.RaceStatus;
import com.ordint.tcpears.domain.lombok.TrackConfig;
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
		given(jdbcTemplate.queryForObject(anyString(), any(RaceRowMapper.class), anyLong()))
			.willReturn(RaceDetail.builder().status(RaceStatus.STARTED).build());
		
		defaultRaceService.startRace(100);
	}
	@Test(expected = IllegalStateException.class)
	public void startRaceShouldThrowIllegalStateException2() throws Exception {
		given(jdbcTemplate.queryForObject(anyString(), any(RaceRowMapper.class), anyLong()))
			.willReturn(RaceDetail.builder().status(RaceStatus.FINISHED).build());
		
		defaultRaceService.startRace(100);
	}
	
	@Test
	public void startRaceShouldUpdateClientDetails() throws Exception {
		given(jdbcTemplate.queryForObject(eq(DefaultRaceService.RACE_DETAIL_SQL), any(RaceRowMapper.class), anyLong()))
			.willReturn(RaceDetail.builder()
					.groupId(1l)
					.scheduledStartTime(LocalDateTime.now())
					.status(RaceStatus.NOT_STARTED).build());
		ClientDetails cd1 = new ClientDetails();
		ClientDetails cd2 = new ClientDetails();
		given(jdbcTemplate.query(eq(DefaultRaceService.CLIENT_DETAILS_FOR_RACE_SQL), (RowMapper<ClientDetails>)anyObject(), anyLong()))
			.willReturn(Arrays.asList(cd1,cd2));
		given(jdbcTemplate.queryForObject(eq(DefaultRaceService.TRACK_CONFIG_SQL), any(TrackConfigRowMapper.class), 
				anyLong())).willReturn(TrackConfig.builder().kml("kml").finishLine("finishLine").build());
	
		defaultRaceService.startRace(100);
	
		ArgumentCaptor<List> cap = ArgumentCaptor.forClass(List.class);
		
		Mockito.verify(clientDetailsResolver).updateClientDetails(cap.capture());
		
		
	}
	@Test
	public void testFinishRace() throws Exception {
		given(jdbcTemplate.queryForObject(anyString(), any(RaceRowMapper.class), anyLong()))
			.willReturn(RaceDetail.builder().status(RaceStatus.STARTED).build());
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

		ClientDetails cd1 = new ClientDetails();
		ClientDetails cd2 = new ClientDetails();
		given(jdbcTemplate.query(eq(DefaultRaceService.CLIENT_DETAILS_FOR_RACE_SQL), (RowMapper<ClientDetails>)anyObject(), anyLong()))
			.willReturn(Arrays.asList(cd1,cd2));

		givenAFinishedRace(100);

		given(jdbcTemplate.queryForObject(eq(DefaultRaceService.TRACK_CONFIG_SQL), any(TrackConfigRowMapper.class), 
				anyLong())).willReturn(TrackConfig.builder().kml("kml").finishLine("finishLine").build());

		
		String raceName = defaultRaceService.replayRace(100);
		
		then(clientManager).should().clearSnake("100");
		then(clientManager).should().trackGroup("100");
		then(replayService).should().replayFrom(any(LocalDateTime.class), eq(60), eq(true),eq("1-Some race-2015-11-16T22:32:38-60"));
		then(jdbcTemplate).should().update(eq("update races set status ='REPLAYING' where race_id=?"), eq(100l));
		
		assertThat(raceName, equalTo("Some race"));
		assertThat(defaultRaceService.getCurrentReplayRaceId(1), equalTo(100l));		
	}
	
	private RaceDetail givenAFinishedRace(long id) {
		RaceDetail race = RaceDetail.builder()
				.groupId(id)
				.id(id)
				.venueId(1l)
				.status(RaceStatus.FINISHED)
				.actualStartTime(LocalDateTime.parse("2015-11-16 22:32:38", DefaultRaceService.MYSQL_DATETIME_FORMATTER))
				.finishTime(LocalDateTime.parse("2015-11-16 22:33:38", DefaultRaceService.MYSQL_DATETIME_FORMATTER))
				.name("Some race")
				.scheduledStartTime(LocalDateTime.now()).build();
		
		given(jdbcTemplate.queryForObject(eq(DefaultRaceService.RACE_DETAIL_SQL), any(RaceRowMapper.class), eq(id)))
			.willReturn(race);	
		return race;
	}
	
	@Test(expected = RaceServiceException.class)
	public void shouldThrowRaceExceptionIfAReplayIsAlreadyRunningForThatVenue() throws Exception {
		givenAFinishedRace(100);
		ClientDetails cd1 = new ClientDetails();
		given(jdbcTemplate.query(eq(DefaultRaceService.CLIENT_DETAILS_FOR_RACE_SQL), (RowMapper<ClientDetails>)anyObject(), anyLong()))
			.willReturn(Arrays.asList(cd1));

		given(jdbcTemplate.queryForObject(eq(DefaultRaceService.RACE_DETAIL_SQL), any(RaceRowMapper.class), eq(200l)))
			.willReturn(RaceDetail.builder()
					.venueId(1l)
					.status(RaceStatus.FINISHED).build());
		
		given(jdbcTemplate.queryForObject(eq(DefaultRaceService.TRACK_CONFIG_SQL), any(TrackConfigRowMapper.class), 
				anyLong())).willReturn(TrackConfig.builder().kml("kml").finishLine("finishLine").build());
		
		defaultRaceService.replayRace(100);
		
		defaultRaceService.replayRace(200);
		
		
		
		
	
	}

}
