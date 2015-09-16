package com.ordint.tcpears.service.impl;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.ordint.tcpears.server.Config;
@RunWith(SpringJUnit4ClassRunner.class)
//ApplicationContext will be loaded from the static inner ContextConfiguration class
@Ignore
@ContextConfiguration(loader=AnnotationConfigContextLoader.class, classes= {Config.class})
public class MySqlReplayServiceTest {
	

	@Autowired
	private MySqlReplayService replayService;
	
	@Before
	public void setUp() throws Exception {
		//clientManager = Mockito.mock(ClientManager.class);
		MockitoAnnotations.initMocks(this);
		replayService = new MySqlReplayService();
	}
	
	@Test
	public void test() throws Exception {
		
		replayService.replayFrom(LocalDateTime.parse("2015-08-12T18:38:03.261"), 330, true);
	}

}
