package Probabilistic_Reasoning;

import java.util.ArrayList;
import java.util.Random;

public class SensorModel {

	// ==========================================================
  	// Private Properties
  	// ========================================================== 
	
	private int ROWS, COLS, HEAD, s;
	
	// O's
	private Matrix O;
	
	// probabilities
	private static double TrueLocationProbability = 0.10;
	private static double SurroundingFieldsProbability = 0.05;
	private static double SecondarySurroundingFieldsProbability = 0.025;
		
    // ==========================================================
  	// Constructor
  	// ========================================================== 

	public SensorModel( int rows, int cols) {
		ROWS = rows;
		COLS = cols;
		HEAD = 4;
		s = ROWS * COLS * 4;
		
		// generate O's
		generateOs();
	}	
	
	// ==========================================================
  	// Public methods
  	// ========================================================== 
	
	public double getOrXY(int rX, int rY, int x, int y, int h) {
		return O.getElementAt(readingIndex(rX,rY), stateIndex(x,y,h));
	}
	
	public Position currentReading(Position Pos) {
		
		// get possible next Positions
		ArrayList<Position> positions = new ArrayList<Position>();
		
		int stateIndex = stateIndex(Pos);

		for (int reading = 0; reading < ROWS * COLS + 1; reading ++) {
			
			double chance = O.getElementAt(reading,stateIndex);
			
			if(chance > 0.001) {
				int percentage = Math.round((int)(chance * 100));
				for (int i = 0; i < percentage; i++) 
					positions.add(readingPosition(reading));			
			}
		}
		
		// choose the next position randomly
		return positions.get( new Random().nextInt(positions.size()));		
	}
	
	public Matrix getO(Position reading) {
		
		int readingIndex = readingIndex(reading.getX(), reading.getY());
		double[] diag = new double[s];
		
		for (int i = 0; i < s; i++)
				diag[i] = O.getElementAt(readingIndex,i);
		
		return new Matrix(diag);
	}
	
	// ==========================================================
  	// Private methods
  	// ========================================================== 
	
	private void generateOs() {
		O = new Matrix(ROWS*COLS+1,s);
		
		for (int reading = 0; reading < O.getRows(); reading++) {
			for (int state = 0; state < O.getCols(); state++) {
				Position statePos = statePosition(state);
				Position readingPos = readingPosition(reading);
				O.setElementAt(reading,state,
						generateOrXY(readingPos.getX(), readingPos.getY(), statePos.getX(), statePos.getY(), statePos.getH()));
			}
		}		
	}
	
	private double generateOrXY(int rX, int rY, int x, int y, int h) {
		// Probability that sensor reading r = (rX,rY) has been caused by respective state (x,y,h)
		
		// reading is equal
		if (isReadingCorrect(rX,rY,x,y)) return TrueLocationProbability;
		
		// reading is surrounding field
		if (isSurroundingField(rX,rY,x,y)) return SurroundingFieldsProbability;
		
		// reading is secondary surrounding field 
		if (isSecondarySurroundingField(rX,rY,x,y)) return SecondarySurroundingFieldsProbability;
		
		// reading is nothing
		if (isReadingNothing(rX,rY))
			return 1.0 -
					TrueLocationProbability - 
					numberOfSurroundingFields(x,y) * SurroundingFieldsProbability - 
					numberOfSecondarySurroundingFields(x,y) * SecondarySurroundingFieldsProbability;
		return 0.0;
	}
	
	private int stateIndex(int x, int y, int h) {
		return h + x * HEAD + y * HEAD * ROWS;
	}
	
	private int stateIndex(Position pos) {
		return pos.getH() + pos.getX() * HEAD + pos.getY() * HEAD * ROWS;
	}
	
	private Position statePosition(int index) {
		int y = index / (HEAD * ROWS);
	    index = index % (HEAD * ROWS);
	    int x = index / HEAD;
	    int h = index % HEAD;
	    return new Position(x,y,h);
		
	}
	
	private int readingIndex(int rX, int rY) {
		if (rX == -1 && rY == -1) return ROWS * COLS;
		return rX + rY*ROWS;
	}
	
	private Position readingPosition(int index) {
		if (index == ROWS * COLS) return new Position(-1,-1);
		
		int rY = index / ROWS;
		int rX = index % ROWS;
		return new Position(rX,rY);			
		
	}
	
	private boolean isReadingNothing(int rX, int rY) {
		return (rX == -1 && rY == -1);
	}
		
	private boolean isReadingCorrect(int rX, int rY, int x, int y ) {
		return (rX == x && rY == y);
	}
	
	private boolean isSurroundingField(int rX, int rY, int x, int y) {
		if (isReadingNothing(rX,rY)) return false;
		int verticalDistance = Math.abs(rX-x);
		int horizontalDistance = Math.abs(rY-y);
		if (verticalDistance == 1 && horizontalDistance == 0) return true;
		if (verticalDistance == 1 && horizontalDistance == 1) return true;
		if (verticalDistance == 0 && horizontalDistance == 1) return true;
		return false;
	}
	
	private boolean isSecondarySurroundingField(int rX, int rY, int x, int y) {
		if (isReadingNothing(rX,rY)) return false;
		int verticalDistance = Math.abs(rX - x);
		int horizontalDistance = Math.abs(rY - y);
		if (verticalDistance == 2 && horizontalDistance == 2) return true;
		if (verticalDistance == 2 && horizontalDistance == 1) return true;		
		if (verticalDistance == 2 && horizontalDistance == 0) return true;
		if (verticalDistance == 1 && horizontalDistance == 2) return true;
		if (verticalDistance == 0 && horizontalDistance == 2) return true;
		return false;
	}
	
	private int numberOfSurroundingFields(int x, int y) {
		if (isInCorner(x,y)) return 3;
		if (isNextToWall(x,y)) return 5;
		return 8;
	}

	private int numberOfSecondarySurroundingFields(int x, int y) {
		if (isInCorner(x,y)) return 5;
		if (isNextToCorner(x,y)) return 6;
		if (isDiagonalToCorner(x,y)) return 7;
		if (isTwoAwayFromCorner(x,y)) return 9;
		if (isTwoAndOneAwayFromWall(x,y)) return 11;
		return 16;
	}
	
	private boolean isInCorner(int x, int y) {
		if (x == 0 && y == 0) return true;
		if (x == 0 && y == COLS-1) return true;
		if (x == ROWS-1 && y == 0 ) return true;
		if (x == ROWS-1 && y == COLS-1) return true;
		return false;
	}
	
	private boolean isNextToWall(int x, int y) {
		if (x == 0 	|| x == ROWS - 1	) return true;
		if (y == 0 	|| y == COLS - 1	) return true;
		return false;
	}
	
	private boolean isNextToCorner(int x, int y) {
		if (isNextToWall(x,y) && (x == 1 || x == ROWS - 2)) return true;
		if (isNextToWall(x,y) && (y == 1 || y == COLS - 2)) return true;
		return false;
	}
	
	private boolean isDiagonalToCorner(int x, int y) {
		if (x == 1 && y == 1) return true;
		if (x == 1 && y == COLS - 2) return true;
		if (x == ROWS -2 &&  y == 1) return true;
		if (x == ROWS -2 && y == COLS - 2) return true;
		return false;
	}
	
	private boolean isTwoAwayFromCorner(int x, int y) {
		if (isNextToWall(x,y) && (x == 2 || x == ROWS - 3)) return true;
		if (isNextToWall(x,y) && (y == 2 || y == COLS - 3)) return true;
		return false;
	}
	
	private boolean isTwoAndOneAwayFromWall(int x, int y) {
		if (x == 1 && (y == 2 || y == COLS - 3)) return true;
		if (x == 2 && (y == 1 || y == COLS - 2)) return true;
		if (x == ROWS - 3 && (y == 1 || y == COLS - 2)) return true;
		if (x == ROWS - 2 && (y == 2 || y == COLS - 3)) return true;
		return false;
	}
		
}
