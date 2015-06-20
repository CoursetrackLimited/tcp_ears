package com.ordint.tcpears.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.ordint.tcpears.domain.ClientDetails;
import com.ordint.tcpears.domain.Position;

public class ConversionUtilTest {



	@Test
	public void testPosToDec() throws Exception {
        String latIn = "5101.63261";
        String lonIn = "-00000.49920";
        
        String latOut = ConversionUtil.posToDec("5101.63261");
        String lonOut = ConversionUtil.posToDec("-00000.49920");
        //5101.63261N=51.027210166667 00000.49920W=-0.00832
       
        assertThat(ConversionUtil.posToDec("5101.63261"), equalTo("51.027210166667"));
        assertThat(ConversionUtil.posToDec("0511.63261"), equalTo("5.193876833333"));
        assertThat(ConversionUtil.posToDec("511.63261"), equalTo("5.193876833333"));
        assertThat(ConversionUtil.posToDec("-11.63261"), equalTo("-0.193876833333"));
        assertThat(ConversionUtil.posToDec("-05101.63261"), equalTo("-51.027210166667"));
        assertThat(ConversionUtil.posToDec("-00000.49920"), equalTo("-0.00832"));
        assertThat(ConversionUtil.posToDec("-0.49920"), equalTo("-0.00832"));
        assertThat(ConversionUtil.posToDec("0.49920"), equalTo("0.00832"));
        assertThat(ConversionUtil.posToDec("00.49920"), equalTo("0.00832"));
        assertThat(ConversionUtil.posToDec("-.49920"), equalTo("-0.00832"));
        
	}

	@Test
	public void testFormatDouble() throws Exception {
	
		assertThat(ConversionUtil.formatDouble(3.3333333333333333333333), equalTo("3.333333333333"));
		assertThat(ConversionUtil.formatDouble(3.3333333333337333333333), equalTo("3.333333333334"));
	}

}
