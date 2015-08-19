package com.ordint.tcpears.service.impl;

import static com.ordint.tcpears.domain.PositionUtil.createPosition;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

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
		 clientManager = new ClientManagerImpl(clock);
	}
	

	
	@Test
	public void shouldGroupLatestPositionsByGroupId() throws Exception {

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
		
		
		Map<String, List<Position>> actual = clientManager.groupClientsByGroup();

		assertThat(actual.get("groupId"), Matchers.containsInAnyOrder(p5,p2));
		
		assertThat(actual.get("groupId2"), Matchers.contains(p3));
		
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
		
		
		
		//clientManager.publishTracks();
		
		ConcurrentMap<String, ConcurrentMap<String, String>> allTracks = clientManager.getGroupTracks();
		
		assertThat(allTracks.get("groupId"), Matchers.allOf(
				hasEntry("clientId1", "111,99,-1 88,77,-1 22,11,-1 ")));
				//hasEntry("clientId2", "44,33,-1 ")));
		
		//Map<String, List<Position>> clientsByGroup = clientManager.groupClientsByTrackedGroup();
		
		//assertThat(clientsByGroup.get("groupId"), containsInAnyOrder(p5,p2));
		
		//assertThat(clientsByGroup.get("groupId2"), is(nullValue()));		
		

		
	}
	
	@Test
	public void stopTrackingGroupShouldNotPublishTrack() throws Exception {
		
		
		clientManager.trackGroup("groupId");
		
		Position p1 = createPosition("11","22", "-1", "groupId", "clientId1");
		Position p2 = createPosition("33","44", "-1", "groupId", "clientId2");	
		
		clientManager.updatePostion(p1);
		clientManager.updatePostion(p2);
		
		clientManager.stopTrackingGroup("groupId");
		
		ConcurrentMap<String, ConcurrentMap<String, String>> allTracks = clientManager.getGroupTracks();
				
		assertThat(allTracks.get("groupId"), is(nullValue()));	
		
	}
	@Test
	public void clearTrackShouldRemoveTrack() throws Exception {
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
		
		
		
		//clientManager.publishTracks();
		
		
		
		clientManager.clearTrack("groupId");
		
		ConcurrentMap<String, ConcurrentMap<String, String>> allTracks = clientManager.getGroupTracks();
		
		assertThat(allTracks.get("groupId"),is(nullValue()));		
		
	}
	@Test
	public void oldClientsShouldNotBePublished() throws Exception {
		clientManager.trackGroup("groupId");
		Position p1 = Position.builder().clientDetails(new ClientDetails("groupId", "oldClient"))
			.altitude("2") 
			.lat("22.2")
			.lon("33.2")
			.timestamp("105413.15")
			.timeCreated(LocalDateTime.now(clock).minusSeconds(300))
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
		//clientManager.publishPositions();
		ConcurrentMap<String, ConcurrentMap<String, String>> actual = clientManager.getGroupTracks();

		assertThat(actual.get("groupId"), Matchers.allOf(
				hasEntry("newClient", "323.2,122.2,32 ")));
		
		assertThat(actual.get("groupId").size(), equalTo(1));

		
		
	}
	
}
