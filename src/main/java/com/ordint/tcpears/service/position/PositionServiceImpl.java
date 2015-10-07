package com.ordint.tcpears.service.position;



import com.ordint.tcpears.domain.DefaultInputParser;
import com.ordint.tcpears.domain.InputParser;
import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.ClientManager;
import com.ordint.tcpears.service.PositionService;
import com.ordint.tcpears.util.time.Timestamper;


public class PositionServiceImpl implements PositionService {
	
	private ClientDetailsResolver clientDetailsResolver;;
	private ClientManager clientManager;
	private InputParser inputParser ;
	private PositionLogger positionLogger;
	
	public PositionServiceImpl() {}
	
	public PositionServiceImpl(ClientManager clientManager, ClientDetailsResolver clientDetailsResolve,
			PositionLogger positionLogger) {
		this.clientDetailsResolver = clientDetailsResolve;
		this.clientManager = clientManager;
		this.positionLogger = positionLogger;
		this.inputParser = new DefaultInputParser(clientDetailsResolver, Timestamper.nanoTimestamper());
	}
	
	@Override
	public void update(String positionInfo) {
		
		Position p = inputParser.parse(positionInfo);
		positionLogger.log(p, "horse", "boxes");
		clientManager.updatePostion(p);	

	}
	
	public void update(Position p) {
		positionLogger.log(p, "horse", "replay");
		clientManager.updatePostion(p);			
	}



	
}