package com.ordint.tcpears.service.impl;

import static com.ordint.tcpears.domain.PositionUtil.createPosition;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ordint.tcpears.domain.ClientDetails;
import com.ordint.tcpears.domain.DefaultOutputWriter;
import com.ordint.tcpears.domain.DefaultTrackWriter;
import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.memcache.MemcacheHelper;
@SuppressWarnings("all")
@RunWith(MockitoJUnitRunner.class)
public class ClientManagerImplTest {
	@Mock
	private MemcacheHelper memcacheHelper;
	
	@Mock
	private JdbcTemplate jdbcTemplate;
	
	@Captor
	private ArgumentCaptor<Map<String, String>> mapCaptor;
	
	
	private ClientManagerImpl clientManager;
	
	private Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
	@Before
	public void setUp() throws Exception {
		 clientManager = new ClientManagerImpl(memcacheHelper, clock, jdbcTemplate);
	}
	

	
	@Test
	public void publishPositionsShouldSavePostions() throws Exception {

		Position p1 = createPosition("11","22", "-1", "groupId", "clientId1");
		Position p2 = createPosition("33","44", "-1", "groupId", "clientId2");		
		Position p3 = createPosition("55","66", "-1", "groupId2", "clientId11");

		Position p4 = createPosition("77","88", "-1", "groupId", "clientId1");
		Position p5 = createPosition("99","111", "-1", "groupId", "clientId1");
		clientManager.updatePostion(p1);
		clientManager.updatePostion(p2);
		clientManager.updatePostion(p3);
		clientManager.updatePostion(p4);
		clientManager.updatePostion(p5);
		
		clientManager.publishPositions();
		ArgumentCaptor<Map> captor1 = ArgumentCaptor.forClass(Map.class);
		ArgumentCaptor<Map> captor2 = ArgumentCaptor.forClass(Map.class);
		
		verify(memcacheHelper).set(Mockito.eq("/ggps/locations/groupId"), Mockito.eq("/ggps/locations/groupId"), 
				captor1.capture());
		verify(memcacheHelper).set(Mockito.eq("/ggps/locations/groupId2"), Mockito.eq("/ggps/locations/groupId2"), 
				captor2.capture());	
		
		
		
		
		Map<String, String> actual = captor1.getValue();
		DefaultOutputWriter out = new DefaultOutputWriter();
		assertThat(actual, Matchers.allOf(Matchers.hasEntry(p2.getClientId(), out.write(p2)),
				Matchers.hasEntry(p5.getClientId(), out.write(p5))));
		actual = captor2.getValue();
		assertThat(actual, Matchers.allOf(Matchers.hasEntry(p3.getClientId(),out.write(p3))));
		
	}
	
	@Test
	public void publishTracksShouldSaveTracks() throws Exception {
		
		clientManager.trackGroup("groupId");
		
		Position p1 = createPosition("11","22", "-1", "groupId", "clientId1");
		Position p2 = createPosition("33","44", "-1", "groupId", "clientId2");		
		Position p3 = createPosition("55","66", "-1", "groupId2", "clientId11");

		Position p4 =createPosition("77","88", "-1", "groupId", "clientId1");
		Position p5 = createPosition("99","111", "-1", "groupId", "clientId1");
		clientManager.updatePostion(p1);
		clientManager.updatePostion(p2);
		clientManager.updatePostion(p3);
		clientManager.updatePostion(p4);
		clientManager.updatePostion(p5);
		
		
		
		clientManager.publishTracks();
		
		DefaultTrackWriter out = new DefaultTrackWriter();
		verify(memcacheHelper).set(Mockito.eq("/ggps/tracks/groupId"), Mockito.eq("/ggps/tracks/groupId"), 
				mapCaptor.capture());	
		String expected =  out.write(p5, out.write(p4, out.write(p1,"")));
		assertThat(mapCaptor.getValue(), anyOf(hasEntry(p1.getClientId(), expected)));	
		
	}
	
	@Test
	public void stopTrackingGroupShouldNotPublishTrack() throws Exception {
		
		
		clientManager.trackGroup("groupId");
		
		Position p1 = createPosition("11","22", "-1", "groupId", "clientId1");
		Position p2 = createPosition("33","44", "-1", "groupId", "clientId2");	
		
		clientManager.updatePostion(p1);
		clientManager.updatePostion(p2);
		
		clientManager.stopTrackingGroup("groupId");
		
		clientManager.publishTracks();
				
		verifyZeroInteractions(memcacheHelper); 
		
	}
	@Test
	public void clearTrackShouldClearMemcache() throws Exception {
		clientManager.clearTrack("groupId");
		verify(memcacheHelper).clear(Mockito.eq("/ggps/tracks/groupId"), Mockito.eq("/ggps/tracks/groupId"));
		
	}
	@Test
	public void oldClientsShouldNotBePublished() throws Exception {
		Position p1 = Position.builder().clientDetails(new ClientDetails("groupId", "oldClient"))
			.altitude("2")
			.lat("22.2")
			.lon("33.2")
			.timestamp("105413.15")
			.timeCreated(LocalDateTime.now(clock).minusSeconds(301))
			.build();
		Position p2 = Position.builder().clientDetails(new ClientDetails("groupId", "newClient"))
			.altitude("32")
			.lat("122.2")
			.lon("323.2")
			.timestamp("105413.15")
			.timeCreated(LocalDateTime.now(clock))
			.build();
		clientManager.updatePostion(p1);
		clientManager.updatePostion(p2);
		clientManager.removeStaleClients();
		clientManager.publishPositions();
		verify(memcacheHelper).set(Mockito.eq("/ggps/locations/groupId"), Mockito.eq("/ggps/locations/groupId"), 
				mapCaptor.capture());
		
		assertThat(mapCaptor.getValue(), not(hasKey("oldClient")));
		
		
	}
	
}
