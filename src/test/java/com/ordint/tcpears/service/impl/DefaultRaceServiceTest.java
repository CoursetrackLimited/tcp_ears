package com.ordint.tcpears.service.impl;

import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;

import java.util.Arrays;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.ordint.tcpears.domain.ClientDetails;
import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.ClientManager;

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
	
	@InjectMocks
	private DefaultRaceService defaultRaceService;
	

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
		given(jdbcTemplate.query(anyString(), (RowMapper<ClientDetails>)anyObject(), anyLong()))
			.willReturn(Arrays.asList(cd1,cd2));
		defaultRaceService.startRace(100);
		ArgumentCaptor<ClientDetails> cap = ArgumentCaptor.forClass(ClientDetails.class);
		Mockito.verify(clientDetailsResolver, times(2)).updateClientDetails(cap.capture());
		
		assertThat(cap.getAllValues(), Matchers.contains(cd1, cd2));
	}
	@Test
	public void testFinishRace() throws Exception {
		given(jdbcTemplate.queryForObject("select status from races where race_id=100", String.class)).willReturn("STARTED");
		ClientDetails cd1 = new ClientDetails();
		ClientDetails cd2 = new ClientDetails();
		given(jdbcTemplate.query(anyString(), (RowMapper<ClientDetails>)anyObject(), anyLong()))
			.willReturn(Arrays.asList(cd1,cd2));
		defaultRaceService.finishRace(100);
		ArgumentCaptor<ClientDetails> cap = ArgumentCaptor.forClass(ClientDetails.class);
		Mockito.verify(clientDetailsResolver, times(2)).updateClientDetails(cap.capture());
		
		assertThat(cap.getAllValues(), Matchers.contains(cd1, cd2));
	}

	@Test
	public void testReplayRace() throws Exception {
		given(jdbcTemplate.queryForObject("select status from races where race_id=100", String.class)).willReturn("FINISHED");
		
		//defaultRaceService.replayRace(raceId);
	}

}
