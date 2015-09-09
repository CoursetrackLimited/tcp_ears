package com.ordint.tcpears.domain;

import static java.lang.Double.parseDouble;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.jws.HandlerChain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ordint.tcpears.domain.json.JsonLocalDateTimeDeserializer;
import com.ordint.tcpears.domain.json.JsonLocalDateTimeSerialiser;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties({"groupId","clientId","currentLag", "gpstimestamp","speedValue"})
public class Position {
	private static final DateTimeFormatter GPS_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("Hmmss.SS");
	@NonFinal
	@JsonSerialize(using=JsonLocalDateTimeSerialiser.class)
	@JsonDeserialize(using=JsonLocalDateTimeDeserializer.class)
	private LocalDateTime timestamp;
	private String lat;
	private String lon;
	private String speed;
	@NonFinal
	private String altitude;
	private String heading;
	private String horizontalAccuracy;
	private String verticalAccuracy;
	private String status;
	private ClientDetails clientDetails;
	@JsonSerialize(using=JsonLocalDateTimeSerialiser.class)
	@JsonDeserialize(using=JsonLocalDateTimeDeserializer.class)
	private LocalDateTime timeCreated;
	@NonFinal
	private Long lag;
	@JsonCreator
	public Position() {}

	public String getGroupId() {
		return clientDetails.getGroupId();
	}

	public String getClientId() {
		return clientDetails.getClientId();
	}

	public long getLag() {
		if (lag == null) {
			lag = getCurrentLag();
		}
		return lag.longValue();
	}

	public long getCurrentLag() {
		return ChronoUnit.MILLIS.between(timestamp, timeCreated);
	}

	public String getGPSTimestamp() {
		return timestamp.toString();
	}

	public Position smoothAltitude(Position previous) {
		if (StringUtils.equals(altitude, "-1")) {
			altitude = previous.getAltitude();
		}
		return this;

	}

	public double getSpeedValue() {
		return parseDouble(speed);
	}

	public static class PositionBuilder {

		private LocalDateTime timestamp;
		private String lat;
		private String lon;
		private String speed;
		private String altitude;
		private String heading;
		private String horizontalAccuracy;
		private String verticalAccuracy;
		private String status;
		private ClientDetails clientDetails;
		private LocalDateTime timeCreated;
		private Long lag;
		private Clock clock;

		PositionBuilder(Clock clock) {
			this.clock = clock;
		}

		public PositionBuilder timestampFromTime(final String time) {
			
			this.timestamp = LocalDateTime.of(LocalDate.now(clock), LocalTime.parse(time, GPS_TIMESTAMP_FORMAT));
			return this;
		}
		public PositionBuilder timestampFromDateTime(final LocalDateTime timestamp) {			
			this.timestamp = timestamp;
			return this;
		}
		public PositionBuilder lat(final String lat) {
			this.lat = lat;
			return this;
		}

		public PositionBuilder lon(final String lon) {
			this.lon = lon;
			return this;
		}

		public PositionBuilder speed(final String speed) {
			this.speed = speed;
			return this;
		}

		public PositionBuilder altitude(final String altitude) {
			this.altitude = altitude;
			return this;
		}

		public PositionBuilder heading(final String heading) {
			this.heading = heading;
			return this;
		}

		public PositionBuilder horizontalAccuracy(final String horizontalAccuracy) {
			this.horizontalAccuracy = horizontalAccuracy;
			return this;
		}

		public PositionBuilder verticalAccuracy(final String verticalAccuracy) {
			this.verticalAccuracy = verticalAccuracy;
			return this;
		}

		public PositionBuilder status(final String status) {
			this.status = status;
			return this;
		}

		public PositionBuilder clientDetails(final ClientDetails clientDetails) {
			this.clientDetails = clientDetails;
			return this;
		}

		public PositionBuilder timeCreated(final LocalDateTime timeCreated) {
			this.timeCreated = timeCreated;
			return this;
		}
		public PositionBuilder lag(final long lag) {
			this.lag = lag;
			return this;
		}
		public Position build() {
			return new Position(timestamp, lat, lon, speed, altitude, heading, horizontalAccuracy, verticalAccuracy,
					status, clientDetails, timeCreated, lag);
		}

		@Override
		public String toString() {
			return "Position.PositionBuilder(timestamp=" + this.timestamp + ", lat=" + this.lat + ", lon=" + this.lon
					+ ", speed=" + this.speed + ", altitude=" + this.altitude + ", heading=" + this.heading
					+ ", horizontalAccuracy=" + this.horizontalAccuracy + ", verticalAccuracy=" + this.verticalAccuracy
					+ ", status=" + this.status + ", clientDetails=" + this.clientDetails + ", timeCreated="
					+ this.timeCreated + ", lag="+ lag + ")";
		}
	}

	public static PositionBuilder builder() {
		return new PositionBuilder(Clock.systemUTC());
	}
	protected static PositionBuilder builder(Clock clock) {
		return new PositionBuilder(clock);
	}
}
