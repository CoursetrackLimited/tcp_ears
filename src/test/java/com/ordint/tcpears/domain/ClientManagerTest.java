package com.ordint.tcpears.domain;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Map;

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

import com.ordint.tcpears.memcache.DevMemcachedClient;
import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.server.Position;
import com.ordint.tcpears.service.ClientDetails;

@RunWith(MockitoJUnitRunner.class)
public class ClientManagerTest {
	@Mock
	private MemcacheHelper memcacheHelper;
	
	@Captor
	private ArgumentCaptor<Map<String, String>> mapCaptor;
	
	@InjectMocks
	private ClientManager clientManager;
	
	@Before
	public void setUp() throws Exception {
	}
	
	private Position2 createPosition(String lat, String lon, String alt, String group, String clientId) {
		 return Position2.builder().clientDetails(new ClientDetails("clientId1", group, clientId))
			.altitude(alt)
			.lat(lat)
			.lon(lon).build();
	}
	
	@Test
	public void publishPositionsShouldSavePostions() throws Exception {

		Position2 p1 = createPosition("50.3453454","1.111", "10", "groupId", "clientId1");
		Position2 p2 = createPosition("22.332312","1.11133", "10", "groupId", "clientId2");		
		Position2 p3 = createPosition("22.332312","12.23", "10", "groupId2", "clientId11");

		Position2 p4 =createPosition("40.3453454","12.111", "10", "groupId", "clientId1");
		Position2 p5 = createPosition("10.3453454","13.111", "10", "groupId", "clientId1");
		clientManager.updatePostion(p1);
		clientManager.updatePostion(p2);
		clientManager.updatePostion(p3);
		clientManager.updatePostion(p4);
		clientManager.updatePostion(p5);
		
		clientManager.publishPositions();
		ArgumentCaptor<Map> captor1 = ArgumentCaptor.forClass(Map.class);
		ArgumentCaptor<Map> captor2 = ArgumentCaptor.forClass(Map.class);
		
		Mockito.verify(memcacheHelper).set(Mockito.eq("/ggps/locations/groupId"), Mockito.eq("/ggps/locations/groupId"), 
				captor1.capture());
		Mockito.verify(memcacheHelper).set(Mockito.eq("/ggps/locations/groupId2"), Mockito.eq("/ggps/locations/groupId2"), 
				captor2.capture());	
		
		
		
		Map<String, String> actual = captor1.getValue();
		
		assertThat(actual, Matchers.allOf(Matchers.hasEntry(p2.getClientId(), p2.asMessage()),
				Matchers.hasEntry(p5.getClientId(), p5.asMessage())));
		actual = captor2.getValue();
		assertThat(actual, Matchers.allOf(Matchers.hasEntry(p3.getClientId(), p3.asMessage())));
		
	}
	
	@Test
	public void publishPositionsShouldSaveTracks() throws Exception {
		
		clientManager.trackGroup("groupId");
		
		Position2 p1 = createPosition("50.3453454","1.111", "10", "groupId", "clientId1");
		Position2 p2 = createPosition("22.332312","1.11133", "10", "groupId", "clientId2");		
		Position2 p3 = createPosition("22.332312","12.23", "10", "groupId2", "clientId3");

		Position2 p4 =createPosition("40.3453454","12.111", "10", "groupId", "clientId1");
		Position2 p5 = createPosition("10.3453454","13.111", "10", "groupId", "clientId1");
		clientManager.updatePostion(p1);
		clientManager.updatePostion(p2);
		clientManager.updatePostion(p3);
		clientManager.updatePostion(p4);
		clientManager.updatePostion(p5);
		
		
		
		clientManager.publishPositions();
		
		
		Mockito.verify(memcacheHelper).set(Mockito.eq("/ggps/tracks/groupId"), Mockito.eq("/ggps/tracks/groupId"), 
				mapCaptor.capture());	
		String expected =  p5.concatTrack(p4.concatTrack(p1.concatTrack("")));
		assertThat(mapCaptor.getValue(), anyOf(hasEntry(p1.getClientId(), expected)));
		
		
		
	}
	
	
	
}
