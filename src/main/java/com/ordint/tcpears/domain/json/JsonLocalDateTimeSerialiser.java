package com.ordint.tcpears.domain.json;

import java.io.IOException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonSerializer;

public class JsonLocalDateTimeSerialiser extends JsonSerializer<LocalDateTime> {

	@Override
	public void serialize(LocalDateTime value, com.fasterxml.jackson.core.JsonGenerator gen,
			com.fasterxml.jackson.databind.SerializerProvider serializers) throws IOException,
			com.fasterxml.jackson.core.JsonProcessingException {
		gen.writeString(value.toString());
		
	}






 

}
