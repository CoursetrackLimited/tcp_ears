package com.ordint.tcpears.service.position;

import static com.ordint.tcpears.domain.PositionUtil.createPosition;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.ordint.tcpears.domain.DefaultOutputWriter;
import com.ordint.tcpears.domain.lombok.Position;
import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.service.PositionDataProvider;

@RunWith(MockitoJUnitRunner.class)
public class MemcachePositionPublisherTest {
	@Mock
	private PositionDataProvider dataProvider;

	@Mock
	private MemcacheHelper memcacheHelper;

	@InjectMocks
	private MemcachePositionPublisher memcachePositionPublisher;

	@Before
	public void setUp() throws Exception {
	}
	@Captor
	private ArgumentCaptor<Map<String, String>> mapCaptor;
	@Test
	public void publishPositionsShouldSavePostions() throws Exception {

		
		Position p2 = createPosition("33","44", "-1", "groupId", "clientId2");		
		Position p3 = createPosition("55","66", "-1", "groupId2", "clientId11");
		Position p5 = createPosition("99","111", "-1", "groupId", "clientId1");
		ConcurrentHashMap<String, List<Position>> clients = new ConcurrentHashMap<>();
		clients.put("groupId", Arrays.asList(p5, p2));
		clients.put("groupId2", Arrays.asList(p3));
		
		given(dataProvider.groupClientsByGroup()).willReturn(clients);
	
		ArgumentCaptor<Map> captor1 = ArgumentCaptor.forClass(Map.class);
		ArgumentCaptor<Map> captor2 = ArgumentCaptor.forClass(Map.class);
		
		memcachePositionPublisher.publishPositions();
		
		verify(memcacheHelper).set(Mockito.eq("/ggps/locations/groupId"), Mockito.eq("/ggps/locations/groupId"), 
				captor1.capture(), Mockito.eq(5));
		verify(memcacheHelper).set(Mockito.eq("/ggps/locations/groupId2"), Mockito.eq("/ggps/locations/groupId2"), 
				captor2.capture(), Mockito.eq(5));	
		
		
		
		
		Map<String, String> actual = captor1.getValue();
		DefaultOutputWriter out = new DefaultOutputWriter();
		assertThat(actual, Matchers.allOf(Matchers.hasEntry(p2.getClientId(), out.write(p2)),
				Matchers.hasEntry(p5.getClientId(), out.write(p5))));
		actual = captor2.getValue();
		assertThat(actual, Matchers.allOf(Matchers.hasEntry(p3.getClientId(),out.write(p3))));
		
	}
	@Test
	public void publishTracksShouldSaveTracks() throws Exception {
		
		ConcurrentHashMap<String, ConcurrentMap<String,String>> groupTracks = new ConcurrentHashMap<>();
		ConcurrentHashMap<String, String> trackMap = new ConcurrentHashMap<>();
		trackMap.put("clientId1", "111,99,-1 88,77,-1 22,11,-1 ");
		groupTracks.put("groupId", trackMap);

		given(dataProvider.getSnakes()).willReturn(groupTracks);
		
		memcachePositionPublisher.publishSnakes();
		
		verify(memcacheHelper).set(Mockito.eq("/ggps/tracks/groupId"), Mockito.eq("/ggps/tracks/groupId"), 
				mapCaptor.capture());	
		
		verify(memcacheHelper).set(Mockito.eq("/ggps/predictions/"), Mockito.eq("/ggps/predictions/"), Mockito.anyMap());

		assertThat(mapCaptor.getValue(), anyOf(hasEntry("clientId1", "111,99,-1 88,77,-1 22,11,-1 ")));	
		
	}


	@Test
		public void testClearSnake() throws Exception {
			memcachePositionPublisher.clearSnake("groupId");
			verify(memcacheHelper).clear(Mockito.eq("/ggps/tracks/groupId"), Mockito.eq("/ggps/tracks/groupId"));
		}

	@Test
	public void testClearPositions() throws Exception {
		memcachePositionPublisher.clearPositions("groupId");
		verify(memcacheHelper).clear(Mockito.eq("/ggps/locations/groupId"), Mockito.eq("/ggps/locations/groupId"));
	}

}
