package com.ordint.tcpears.util.prediction;

import com.ordint.tcpears.domain.Position;

public interface PositionPredictor<T> {

	String predict(Position currentPosition, int predictionTime, int numberOfPoints, T data); 
}