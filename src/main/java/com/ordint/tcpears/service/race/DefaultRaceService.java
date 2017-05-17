package com.ordint.tcpears.service.race;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.ordint.tcpears.domain.lombok.ClientDetails;
import com.ordint.tcpears.domain.lombok.RaceDetail;
import com.ordint.tcpears.domain.lombok.RaceDetail.RaceStatus;
import com.ordint.tcpears.domain.lombok.SectorTime;
import com.ordint.tcpears.domain.lombok.TrackConfig;
import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.ClientManager;
import com.ordint.tcpears.service.PositionPublisher;
import com.ordint.tcpears.service.RaceService;
import com.ordint.tcpears.service.ReplayService;
import com.ordint.tcpears.service.position.PositionEnhancers;
import com.ordint.tcpears.util.DateUtil;


public class DefaultRaceService implements RaceService {
	
	private static final String CURRENT_RACE_DETAILS_MEMCACHE_KEY = "/ggps/CurrentRaceDetails";
	private static final String RACE_DETAILS_MEMCACHE_KEY = "/ggps/RaceDetails/%s";
	private static final Logger log = LoggerFactory.getLogger(DefaultRaceService.class);
	

    private static final String KML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml><Document>";
    private static final String KML_FOOTER = "</Document></kml>";
    private static final String KML_LINESTRING = "<Placemark><LineString><tessellate>1</tessellate><coordinates>";
    private static final String KML_LINESTRING_END = "</coordinates></LineString></Placemark>";
    private static final String KML_POINT ="<Placemark><name>%s</name><Point><coordinates>%s</coordinates></Point></Placemark>";

	
	static final String RACE_DETAIL_SQL = "SELECT races.race_id, group_id, races.name as race_name, scheduledStartTime, actualStartTime, "
			+ "finishTime, races.venue_id, venues.name AS venue_name, status, track_config_id, raceDistanceMetres FROM races INNER JOIN venues "
			+ "ON races.venue_id = venues.venue_id WHERE races.race_id=?";
	
	static final String CLIENT_DETAILS_FOR_RACE_SQL = 
			"SELECT clients.client_ident as clientId, friendly_name as fixedName, "
			+ "groups.group_id as groupId, groups.name as groupName, runners.name as tempName, runners.ident as runner_ident "
			+ "FROM clients "
			+ "INNER JOIN runners ON clients.client_id = runners.client_id "
			+ "INNER JOIN races ON races.race_id = runners.race_id "
			+ "INNER JOIN groups ON races.group_id = groups.group_id "
			+ "WHERE races.race_id = ? and runners.non_runner=0";
	
	static final String RESET_CLIENT_DETAILS_SQL =
			"SELECT clients.client_ident AS clientId, clients.friendly_name AS fixedName, "
			+ "groups.group_id AS groupId, groups.name AS groupName, NULL AS tempName, NULL as runner_ident "
			+ "FROM clients "
			+ "INNER JOIN groups ON groups.group_id = clients.group_id "
			+ "INNER JOIN runners ON clients.client_id = runners.client_id "
			+ "WHERE runners.race_id =? and runners.non_runner=0";
	
	static final String TRACK_CONFIG_SQL = "select * from trackconfig where track_config_id = ?";
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ClientDetailsResolver clientDetailsResolver;
	@Autowired
	private ReplayService replayService;
	@Autowired
	private ClientManager clientManager;
	@Autowired
	private MemcacheHelper memcacheHelper;
	@Autowired
	private PositionPublisher positionPublisher;
	@Autowired
	private PositionEnhancers positionEnhancers;
	@Autowired
	private File sectorDirectory;
	
	private RaceObserver currentRaceObserver;
	
	private static final RaceRowMapper RACE_ROW_MAPPER = new RaceRowMapper();
	private static final TrackConfigRowMapper TRACK_CONFIG_ROW_MAPPER = new TrackConfigRowMapper();
	
	private Map<Long, Long> currentReplayRaces = new HashMap<Long, Long>();
	

	
	public static final DateTimeFormatter MYSQL_DATETIME_FORMATTER =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	@Override
	public void startRace(long raceId) throws RaceServiceException {
		//update race status in db
		RaceDetail race = findRace(raceId, RaceStatus.NOT_STARTED);
		jdbcTemplate.update("update races set status ='STARTED' where race_id=?", raceId);
		
		//update the clientDetailsResolver with runner details
		List<ClientDetails> clientDetails = updateClientDetailsResolver(CLIENT_DETAILS_FOR_RACE_SQL, raceId);
		
		configureRaceObserver(race, clientDetails);
		
		publishRaceDetails(race, clientDetails);
		
		
	}
	
	
	private void publishRaceDetails(RaceDetail race, List<ClientDetails> clientDetails) throws RaceServiceException {
	
		HashMap<String, Object> raceDetailsMap = buildRaceDetailsMap(race, clientDetails);
		
		String raceMemcaheKey = String.format(RACE_DETAILS_MEMCACHE_KEY, race.getGroupId().toString());
		try {
			memcacheHelper.set(CURRENT_RACE_DETAILS_MEMCACHE_KEY, CURRENT_RACE_DETAILS_MEMCACHE_KEY, raceDetailsMap);
			memcacheHelper.set(raceMemcaheKey, raceMemcaheKey, raceDetailsMap);
		} catch (IOException e) {
			log.error("Error publishing race details for race with id {}", race.getId(), e);
			throw new RaceServiceException(e);
		}
	}


	private HashMap<String, Object> buildRaceDetailsMap(RaceDetail race, List<ClientDetails> clientDetails) {
		HashMap<String, Object> raceDetailsMap = new HashMap<>();
		raceDetailsMap.put("race_group_id", race.getGroupId().toString());
		raceDetailsMap.put("race_name", clientDetails.get(0).getCurrentGroupName());
		raceDetailsMap.put("client_details", clientDetails);
		raceDetailsMap.put("venueName", race.getVenueName());
		raceDetailsMap.put("raceStartTime", race.getStartTime());
		return raceDetailsMap;
	}


	private RaceDetail findRace(long raceId, RaceStatus requirdStatus) {
		RaceDetail race = findRace(raceId);
		if (race.getStatus() != requirdStatus) {
			throw new IllegalStateException(String.format("Race with race_id %s was in %s state, needed to be %s",
					raceId, race.getStatus(), requirdStatus));
		}
		return race;
	}
	
	private RaceDetail findRace(long raceId) {
        RaceDetail race = jdbcTemplate.queryForObject(RACE_DETAIL_SQL,RACE_ROW_MAPPER, raceId);
        Objects.requireNonNull(race, "no race with race_id " + raceId);
        return race;
	}
	
	
	private List<ClientDetails> updateClientDetailsResolver(String sql, long raceId) {
		 List<ClientDetails> clientDetails =jdbcTemplate.query(sql, new RowMapper<ClientDetails>() {
			@Override
			public ClientDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new ClientDetails(rs.getString("groupId"), rs.getString("clientId"),
						 rs.getString("fixedName"), rs.getString("tempName"), rs.getString("groupName"),
						 rs.getString("runner_ident"));
			}}, raceId);
		 	
		 	clientDetailsResolver.updateClientDetails(clientDetails);
		 	return clientDetails;
		
	}

	@Override
	public void finishRace(long raceId) {
		RaceDetail race = findRace(raceId, RaceStatus.STARTED);
		jdbcTemplate.update("update races set status ='FINISHED', finishTime = NOW() where race_id=?", raceId);
		
		try {
			writeRaceReport("race_", raceId);
		} catch (IOException e) {
			log.error("Failed to write out sector times", e);
		}
		//update the clientDetailsResolver with default details
		updateClientDetailsResolver(RESET_CLIENT_DETAILS_SQL, raceId);
		positionEnhancers.clearEnhancers("");
	}
	
	private void writeRaceReport(String fileName, long raceId) throws IOException {
		if (currentRaceObserver == null) {
			log.info("No sector times aviablae to write");
		} else {
			
		    RaceDetail race = findRace(raceId);
		    log.info("Writing race report for race id {} the {}", race.getId(), race.getName());
		    //actual start time is in fact a zoned time, so convert to utc like timestamps
		    Optional<LocalDateTime> startOpt = getStartTime(race);
		    
			Map<String, List<SectorTime>> sectorTimes = currentRaceObserver.getSectorTimes();
			if (!sectorTimes.isEmpty()) {
				File file = new File(sectorDirectory, fileName  + race.getId() + ".csv");
				if (!file.exists()) {
					file.createNewFile();
				}
				FileOutputStream os = new FileOutputStream(file);
				PrintWriter out = new PrintWriter(os);
	
				CSVPrinter printer = CSVFormat.newFormat(',')
						.withHeader(currentRaceObserver.getReportHeader())
						.withQuoteMode(QuoteMode.ALL)
						.withRecordSeparator("\r\n").print(out);
				
				for (Entry<String, List<SectorTime>> runner : sectorTimes.entrySet()) {
					List<SectorTime> times = runner.getValue();
					List<Object> row = new ArrayList<>();
					row.add(runner.getKey());
					for(int i = 0; i < times.size(); i ++) {
						row.add("" + getValueForSector(times.get(i).getTimestamp(), startOpt));
					}
					printer.printRecord(row);
					
				}
				printer.flush();
				printer.close();
				
			} else {
				log.info("No sector times for race with id {}", raceId);
			}
		}
	}
	
	private String getValueForSector(LocalDateTime sectorTimestamp, Optional<LocalDateTime> startOp) {
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.CEILING);
		if (sectorTimestamp == null) return "";
		if (startOp.isPresent()) {
			return df.format(ChronoUnit.MILLIS.between(startOp.get(), sectorTimestamp) / 1000d);
		} else {
			return sectorTimestamp.toString();
		}
	}
	
	private Optional<LocalDateTime> getStartTime(RaceDetail race) {
		try {
		    return Optional.of(DateUtil.ukLocalDateTimeToUTC(race.getActualStartTime()));			
		} catch (Exception e) {
			
			log.warn("Could not parese actualStartTime for race with id " + race.getId(), e);
			return Optional.empty();
		}
	}
	
	@Override
	public String replayRace(long raceId) throws RaceServiceException {
		RaceDetail race = findRace(raceId, RaceStatus.FINISHED);

		Long currentRaceReplay = currentReplayRaces.get(race.getVenueId());
		if (currentRaceReplay != null) {
			throw new RaceServiceException(String.format("Race with id %s is already being replayed at venue with id %s", currentRaceReplay, race.getVenueId()));
		}
		
		int timeInSecs = (int) race.getActualStartTime().until(race.getFinishTime(), ChronoUnit.SECONDS);
		//String groupId = row.get("group_id").toString();
		
		jdbcTemplate.update("update races set status ='REPLAYING' where race_id=?", raceId);
		
		List<ClientDetails> clientDetails = updateClientDetailsResolver(CLIENT_DETAILS_FOR_RACE_SQL, raceId);
		
		clearSnake(race);
		
		configureRaceObserver(race, clientDetails);
		
		publishRaceDetails(race, clientDetails);
		LocalDateTime startTime = getStartTime(race).orElseThrow(() -> new IllegalArgumentException("No start date for race"));
		String replayId = race.getVenueId() + "-" + race.getName() + "-" + startTime + "-" + timeInSecs;
		replayService.replayFrom(startTime, timeInSecs, true, replayId);
		
		currentReplayRaces.put(race.getVenueId(), raceId);
		
		return race.getName();
	}


	private void clearSnake(RaceDetail race) {
		positionPublisher.clearSnake(race.getGroupId().toString());
		clientManager.clearSnake(race.getGroupId().toString());
	}
	
	private void configureRaceObserver(RaceDetail race, List<ClientDetails> runners) {
		clientManager.trackGroup(race.getGroupId().toString());
		TrackConfig trackConfig = jdbcTemplate.queryForObject(TRACK_CONFIG_SQL, TRACK_CONFIG_ROW_MAPPER, race.getTrackConfigId());
		writeOutTrackKml(trackConfig);
		currentRaceObserver = positionEnhancers.addRacePositionEnhancers(race, trackConfig, runners);	
	}
	
	private void writeOutTrackKml(TrackConfig trackConfig) {
        try {
    	    File file = new File(sectorDirectory, "track_config.kml");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream os = new FileOutputStream(file);
            PrintWriter out = new PrintWriter(os);
            
            out.write(KML_HEADER + KML_LINESTRING + trackConfig.getKml() + KML_LINESTRING_END );
            out.write(String.format(KML_POINT, "Finish", trackConfig.getFinishLine()));
            out.write(KML_FOOTER);
            out.flush();
            out.close();
        } catch (IOException e) {
            log.warn("Could not write out replay track kml", e);
        }
    }


    public void replayEnded(String replayId) {
		
		Long raceId = currentReplayRaces.remove(Long.parseLong(StringUtils.substringBefore(replayId, "-")));
		try {
			writeRaceReport("replay_race_",  raceId);
		} catch (IOException e) {
			log.error("Failed to write out sector times for replay", e);
		}
		positionEnhancers.clearEnhancers("");
		if (raceId != null) {
			jdbcTemplate.update("update races set status ='FINISHED' where race_id=?", raceId);	
		}
	}
	
	
	public Long getCurrentReplayRaceId(long venueId) {
		return currentReplayRaces.get(venueId);
	}


	@Override
	public Map<String, List<SectorTime>> getSectorTimes() {
		try {
			writeRaceReport("currentRace_", currentRaceObserver.getRaceId());
		} catch (IOException e) {
			log.error("Failed to write current race report", e);
		}
		return currentRaceObserver.getSectorTimes();
	}


}
