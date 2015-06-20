package com.ordint.tcpears.domain;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

public class DefaultOutputWriterTest {

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
				.clientDetails(new ClientDetails("groupId", "clientId"))
				.speed("99")
				.horizontalAccuracy("11")
				.lat("50.1")
				.lon("34.32")
				.timestamp("010330.30")
				.status("A")
				.build();
		DefaultOutputWriter out = new DefaultOutputWriter();
		String actual = out.write(p);
		
		String cells[] =StringUtils.splitByWholeSeparatorPreserveAllTokens(actual, ",");
		
		assertThat(cells[cells.length-1], equalTo("34.32"));
		assertThat(cells[cells.length-2], equalTo("50.1"));
		assertThat(cells[cells.length-12], equalTo("11"));
		assertThat(cells[cells.length-6], equalTo("99"));
		assertThat(cells[cells.length-13], equalTo("A"));
		assertThat(cells[cells.length-19], equalTo("010330.30"));
		
		
	}

}
