package com.ordint.tcpears.domain;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.time.Clock;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.ordint.tcpears.util.time.Timestamper;

public class DefaultOutputWriterTest {
	
	private Timestamper timestamper = Timestamper.fixedTimestamper();
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void writeShouldReturnCorrectFormat() throws Exception {
        /* from map.php
		$clientId=$clientDetails[1];
        $clon=$clientDetails[sizeof($clientDetails)-1]; //last two elements
        $clat=$clientDetails[sizeof($clientDetails)-2];
        $accuracyH=$clientDetails[sizeof($clientDetails)-12];
        $currentSpeed=$clientDetails[sizeof($clientDetails)-6];
        $altitude = $clientDetails[8];
        //if the message says -1 for the speed use the last speed value;
        if ($currentSpeed!="-1"){
            $speed=$currentSpeed;
        }
        $status=$clientDetails[sizeof($clientDetails)-13];
        $time=$clientDetails[sizeof($clientDetails)-19];
        */
		
		Position p = Position.builder()
				.altitude("10")
				.clientDetails(new ClientDetails("groupId", "clientId", "kmpO1", "Onion Terror", "16:40 Redbridge Hndicap","5"))
				.speed("99")
				.horizontalAccuracy("11")
				.heading("300.1")
				.lat("50.1")
				.lon("34.32")
				.timestampFromTime("10330.30")
				.timeCreated(timestamper.now())
				.lag(100)
				.status("A")
				.standing(4)
				.distinceInfo(new PositionDistanceInfo("clientId", 1d, 300d, 0d))
				.build();
		DefaultOutputWriter out = new DefaultOutputWriter();
		String actual = out.write(p);
		
		String cells[] =StringUtils.splitByWholeSeparatorPreserveAllTokens(actual, ",");
		LocalDate t = LocalDate.now(Clock.systemUTC());
		assertThat(cells[1], equalTo("clientId")); 
		assertThat(cells[3], equalTo(t + "T01:03:30.300"));
		assertThat(cells[4], equalTo("300.1"));
		assertThat(cells[5], equalTo("5"));
		assertThat(cells[8], equalTo("10"));
		assertThat(cells[9], equalTo("A"));
		assertThat(cells[10], equalTo("11"));
		assertThat(cells[11], equalTo("4"));
		assertThat(cells[12], equalTo("300.0"));
		assertThat(cells[16], equalTo("99"));
		assertThat(cells[19], equalTo("100"));
		assertThat(cells[20], equalTo("50.1"));
		assertThat(cells[21], equalTo("34.32"));
		
		
	}

	@Test
	public void writeShouldReturnCorrectFormatWith2DigitHourInput() throws Exception {

		
		Position p = Position.builder()
				.altitude("10")
				.clientDetails(new ClientDetails("groupId", "clientId"))
				.speed("99")
				.horizontalAccuracy("11")
				.lat("50.1")
				.lon("34.32")
				.timestampFromTime("110330.30")
				.timeCreated(timestamper.now())
				.status("A")
				.standing(4)
				.build();
		DefaultOutputWriter out = new DefaultOutputWriter();
		String actual = out.write(p);
		
		String cells[] =StringUtils.splitByWholeSeparatorPreserveAllTokens(actual, ",");
		LocalDate t = LocalDate.now(Clock.systemUTC());
		
		
		
		
		
		assertThat(cells[1], equalTo("clientId")); 
		assertThat(cells[3], equalTo(t + "T11:03:30.300"));
		assertThat(cells[4], equalTo(""));
		assertThat(cells[9], equalTo("A"));
		assertThat(cells[10], equalTo("11"));
		assertThat(cells[11], equalTo("4"));
		assertThat(cells[16], equalTo("99"));
		assertThat(cells[20], equalTo("50.1"));
		assertThat(cells[21], equalTo("34.32"));
	}
}
