package it.unibo.ai.mulino.CIRAMill.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import it.unibo.ai.didattica.mulino.actions.Action;
import it.unibo.ai.didattica.mulino.actions.FromAndToAreEqualsException;
import it.unibo.ai.didattica.mulino.actions.FromAndToAreNotConnectedException;
import it.unibo.ai.didattica.mulino.actions.NoMoreCheckersAvailableException;
import it.unibo.ai.didattica.mulino.actions.NullActionException;
import it.unibo.ai.didattica.mulino.actions.NullCheckerException;
import it.unibo.ai.didattica.mulino.actions.NullStateException;
import it.unibo.ai.didattica.mulino.actions.Phase1;
import it.unibo.ai.didattica.mulino.actions.Phase1Action;
import it.unibo.ai.didattica.mulino.actions.Phase2;
import it.unibo.ai.didattica.mulino.actions.Phase2Action;
import it.unibo.ai.didattica.mulino.actions.PhaseFinal;
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction;
import it.unibo.ai.didattica.mulino.actions.PositionNotEmptyException;
import it.unibo.ai.didattica.mulino.actions.TryingToMoveOpponentCheckerException;
import it.unibo.ai.didattica.mulino.actions.TryingToRemoveCheckerInTripleException;
import it.unibo.ai.didattica.mulino.actions.TryingToRemoveEmptyCheckerException;
import it.unibo.ai.didattica.mulino.actions.TryingToRemoveOwnCheckerException;
import it.unibo.ai.didattica.mulino.actions.Util;
import it.unibo.ai.didattica.mulino.actions.WrongPhaseException;
import it.unibo.ai.didattica.mulino.actions.WrongPositionException;
import it.unibo.ai.didattica.mulino.domain.State;
import it.unibo.ai.didattica.mulino.domain.State.Checker;
import it.unibo.ai.didattica.mulino.domain.State.Phase;
import it.unibo.ai.mulino.CIRAMill.minimax.IAction;
import it.unibo.ai.mulino.CIRAMill.minimax.IState;
import it.unibo.ai.mulino.CIRAMill.minimax.ITieChecker;

public class BitBoardState implements IState {

	public static final byte DEFAULT_INITIAL_CHECKERS = 9;

	public static final byte WHITE = 0;
	public static final byte BLACK = 1;

	public static final byte MIDGAME = 0;

	private static final byte A7 = 0;
	private static final byte D7 = 1;
	private static final byte G7 = 2;
	private static final byte G4 = 3;
	private static final byte G1 = 4;
	private static final byte D1 = 5;
	private static final byte A1 = 6;
	private static final byte A4 = 7;

	private static final byte B6 = 8;
	private static final byte D6 = 9;
	private static final byte F6 = 10;
	private static final byte F4 = 11;
	private static final byte F2 = 12;
	private static final byte D2 = 13;
	private static final byte B2 = 14;
	private static final byte B4 = 15;

	private static final byte C5 = 16;
	private static final byte D5 = 17;
	private static final byte E5 = 18;
	private static final byte E4 = 19;
	private static final byte E3 = 20;
	private static final byte D3 = 21;
	private static final byte C3 = 22;
	private static final byte C4 = 23;

	private static final int MILL_0_1_2 = (1 << A7) | (1 << D7) | (1 << G7);
	private static final int MILL_2_3_4 = (1 << G7) | (1 << G4) | (1 << G1);
	private static final int MILL_4_5_6 = (1 << G1) | (1 << D1) | (1 << A1);
	private static final int MILL_6_7_0 = (1 << A1) | (1 << A4) | (1 << A7);

	private static final int MILL_8_9_10 = (1 << B6) | (1 << D6) | (1 << F6);
	private static final int MILL_10_11_12 = (1 << F6) | (1 << F4) | (1 << F2);
	private static final int MILL_12_13_14 = (1 << F2) | (1 << D2) | (1 << B2);
	private static final int MILL_14_15_8 = (1 << B2) | (1 << B4) | (1 << B6);

	private static final int MILL_16_17_18 = (1 << C5) | (1 << D5) | (1 << E5);
	private static final int MILL_18_19_20 = (1 << E5) | (1 << E4) | (1 << E3);
	private static final int MILL_20_21_22 = (1 << E3) | (1 << D3) | (1 << C3);
	private static final int MILL_22_23_16 = (1 << C3) | (1 << C4) | (1 << C5);

	private static final int MILL_1_9_17 = (1 << D7) | (1 << D6) | (1 << D5);
	private static final int MILL_3_11_19 = (1 << G4) | (1 << F4) | (1 << E4);
	private static final int MILL_5_13_21 = (1 << D1) | (1 << D2) | (1 << D3);
	private static final int MILL_7_15_23 = (1 << A4) | (1 << B4) | (1 << C4);

	private static final int[] MILLS = { MILL_0_1_2, MILL_2_3_4, MILL_4_5_6, MILL_6_7_0, MILL_8_9_10, MILL_10_11_12,
			MILL_12_13_14, MILL_14_15_8, MILL_16_17_18, MILL_18_19_20, MILL_20_21_22, MILL_22_23_16, MILL_1_9_17,
			MILL_5_13_21, MILL_3_11_19, MILL_7_15_23 };

	private static final int[][] POSITION_MILLS = {
			{MILL_0_1_2, MILL_6_7_0},
			{MILL_0_1_2, MILL_1_9_17},
			{MILL_0_1_2, MILL_2_3_4},
			{MILL_2_3_4, MILL_3_11_19},
			{MILL_2_3_4, MILL_4_5_6},
			{MILL_4_5_6, MILL_5_13_21},
			{MILL_4_5_6, MILL_6_7_0},
			{MILL_6_7_0, MILL_7_15_23},
			
			{MILL_8_9_10, MILL_14_15_8},
			{MILL_8_9_10, MILL_1_9_17},
			{MILL_8_9_10, MILL_10_11_12},
			{MILL_10_11_12, MILL_3_11_19},
			{MILL_10_11_12, MILL_12_13_14},
			{MILL_12_13_14, MILL_5_13_21},
			{MILL_12_13_14, MILL_14_15_8},
			{MILL_14_15_8, MILL_7_15_23},
			
			{MILL_16_17_18, MILL_22_23_16},
			{MILL_16_17_18, MILL_1_9_17},
			{MILL_16_17_18, MILL_18_19_20},
			{MILL_18_19_20, MILL_3_11_19},
			{MILL_18_19_20, MILL_20_21_22},
			{MILL_20_21_22, MILL_5_13_21},
			{MILL_20_21_22, MILL_22_23_16},
			{MILL_22_23_16, MILL_7_15_23}
	};

	private static final int[][] ADJACENT_POSITIONS = {
			{D7, A4},
			{A7, G7, D6},
			{D7, G4},
			{G7, G1, F4},
			{G4, D1},
			{G1, A1, D2},
			{D1, A4},
			{A1, A7, B4},
			
			{D6, B4},
			{D7, B6, F6, D5},
			{D6, F4},
			{G4, F6, F2, E4},
			{F4, D2},
			{D1, F2, B2, D3},
			{D2, B4},
			{A4, B6, B2, C4},
			
			{D5, C4},
			{D6, C5, E5},
			{D5, E4},
			{F4, E5, E3},
			{E4, D3},
			{D2, E3, C3},
			{D3, C4},
			{B4, C3, C5}			
	};

//	private static final int INITIALPHASE_HAS_COMPLETED_MORRIS = 18;
	private static final int INITIALPHASE_CLOSED_MORRIS = 26;
	private static final int INITIALPHASE_BLOCKED_PIECES = 1;
	private static final int INITIALPHASE_TWO_PIECES_CONFIGURATION = 12;
	private static final int INITIALPHASE_THREE_PIECES_CONFIGURATION = 7;
	private static final int INITIALPHASE_PIECES_NUMBER = 6;
	
//	private static final int MIDGAME_HAS_COMPLETED_MORRIS = 14;
	private static final int MIDGAME_CLOSED_MORRIS = 43;
	private static final int MIDGAME_BLOCKED_PIECES = 10;
	private static final int MIDGAME_DOUBLE_MORRIS = 42;
	private static final int MIDGAME_PIECES_NUMBER = 6;
	private static final int MIDGAME_OPENED_MORRIS = 7;
	private static final int MIDGAME_OPENED_MORRIS_NEAR_OPPONENT = -2;
	private static final int MIDGAME_UNBLOCKABLE_DOUBLE_MORRIS = 42;
	
	private static final int ENDGAME_TWO_PIECES_CONFIGURATION = 10;
	private static final int ENDGAME_THREE_PIECES_CONFIGURATION = 1;
//	private static final int ENDGAME_HAS_COMPLETED_MORRIS = 16;
	private static final int ENDGAME_CLOSED_MORRIS = 16;
	
	public static final byte COLOR_INVERSION = 0b10000;
	public static final byte INSIDE_OUT = 0b01000;
	public static final byte VERTICAL_FLIP = 0b00100;
	public static final byte ROTATION_90 = 0b00001;
	public static final byte ROTATION_180 = 0b00010;
	public static final byte ROTATION_270 = 0b00011;
	
	private int[] board = new int[2];
	private byte[] checkersToPut = new byte[2];
	private byte[] checkersOnBoard = new byte[2];
	public byte playerToMove;
	private byte gamePhase;
	private ITieChecker tieChecker;

	public BitBoardState(byte initialWhiteCheckers, byte initialBlackCheckers, ITieChecker tieChecker) {
		if (initialWhiteCheckers < 1 || initialBlackCheckers < 1)
			throw new IllegalArgumentException("Initial checkers must be positive");
		board[WHITE] = 0;
		board[BLACK] = 0;

		checkersToPut[WHITE] = initialWhiteCheckers;
		checkersToPut[BLACK] = initialBlackCheckers;

		checkersOnBoard[WHITE] = 0;
		checkersOnBoard[BLACK] = 0;

		playerToMove = WHITE;

		gamePhase = ~MIDGAME;
		
		this.tieChecker = tieChecker;
	}

	public BitBoardState(int initialWhiteCheckers, int initialBlackCheckers, ITieChecker tieChecker) {
		this((byte) checkIntToByte(initialWhiteCheckers), (byte) checkIntToByte(initialBlackCheckers), tieChecker);
	}

	public BitBoardState(int whiteCheckersToPut, int blackCheckersToPut, int whiteBitBoard, int blackBitBoard, byte playerToMove, ITieChecker tieChecker) {
		this(tieChecker);
		
		checkersToPut[WHITE] = (byte) whiteCheckersToPut;
		checkersToPut[BLACK] = (byte) blackCheckersToPut;

		board[WHITE] = whiteBitBoard;
		board[BLACK] = blackBitBoard;
		
		checkersOnBoard[WHITE] = (byte) Integer.bitCount(board[WHITE]);
		checkersOnBoard[BLACK] = (byte) Integer.bitCount(board[BLACK]);
		
		gamePhase = (byte) (checkersToPut[WHITE] | checkersToPut[BLACK]);
		
		if(playerToMove != WHITE && playerToMove != BLACK)
			throw new IllegalArgumentException("Player must be white or black");
		
		this.playerToMove = playerToMove;
	}

	public BitBoardState(ITieChecker tieChecker) {
		this(DEFAULT_INITIAL_CHECKERS, DEFAULT_INITIAL_CHECKERS, tieChecker);
	}
	
	public void setTieChecker(ITieChecker tieChecker) {
		this.tieChecker = tieChecker;
	}
	
	public byte getCheckersOnBoard(int player) {
		if (player != WHITE && player != BLACK)
			throw new IllegalArgumentException();
		return checkersOnBoard[player];
	}

	public static int checkIntToByte(int value) {
		if (value > Byte.MAX_VALUE)
			throw new IllegalArgumentException("Initial checkers must be less or equal to  " + Byte.MAX_VALUE);
		return value;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("7 ");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("a7")) != 0 ? "W"	: (board[BLACK] & BitBoardUtils.boardFromPosition("a7")) != 0 ? "B" : "O");
		result.append("--------");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("d7")) != 0 ? "W"	: (board[BLACK] & BitBoardUtils.boardFromPosition("d7")) != 0 ? "B" : "O");
		result.append("--------");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("g7")) != 0 ? "W"	: (board[BLACK] & BitBoardUtils.boardFromPosition("g7")) != 0 ? "B" : "O");
		result.append("\n");

		result.append("6 |--");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("b6")) != 0 ? "W"	: (board[BLACK] & BitBoardUtils.boardFromPosition("b6")) != 0 ? "B" : "O");
		result.append("-----");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("d6")) != 0 ? "W"	: (board[BLACK] & BitBoardUtils.boardFromPosition("d6")) != 0 ? "B" : "O");
		result.append("-----");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("f6")) != 0 ? "W"	: (board[BLACK] & BitBoardUtils.boardFromPosition("f6")) != 0 ? "B" : "O");
		result.append("--|\n");

		result.append("5 |--|--");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("c5")) != 0 ? "W"	: (board[BLACK] & BitBoardUtils.boardFromPosition("c5")) != 0 ? "B" : "O");
		result.append("--");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("d5")) != 0 ? "W"	: (board[BLACK] & BitBoardUtils.boardFromPosition("d5")) != 0 ? "B" : "O");
		result.append("--");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("e5")) != 0 ? "W"	: (board[BLACK] & BitBoardUtils.boardFromPosition("e5")) != 0 ? "B" : "O");
		result.append("--|--|\n");

		result.append("4 ");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("a4")) != 0 ? "W"	: (board[BLACK] & BitBoardUtils.boardFromPosition("a4")) != 0 ? "B" : "O");
		result.append("--");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("b4")) != 0 ? "W"	: (board[BLACK] & BitBoardUtils.boardFromPosition("b4")) != 0 ? "B" : "O");
		result.append("--");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("c4")) != 0 ? "W" : (board[BLACK] & BitBoardUtils.boardFromPosition("c4")) != 0 ? "B" : "O");
		result.append("     ");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("e4")) != 0 ? "W"	: (board[BLACK] & BitBoardUtils.boardFromPosition("e4")) != 0 ? "B" : "O");
		result.append("--");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("f4")) != 0 ? "W"	: (board[BLACK] & BitBoardUtils.boardFromPosition("f4")) != 0 ? "B" : "O");
		result.append("--");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("g4")) != 0 ? "W"	: (board[BLACK] & BitBoardUtils.boardFromPosition("g4")) != 0 ? "B" : "O");
		result.append("\n");

		result.append("3 |--|--");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("c3")) != 0 ? "W"	: (board[BLACK] & BitBoardUtils.boardFromPosition("c3")) != 0 ? "B" : "O");
		result.append("--");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("d3")) != 0 ? "W"	: (board[BLACK] & BitBoardUtils.boardFromPosition("d3")) != 0 ? "B" : "O");
		result.append("--");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("e3")) != 0 ? "W"	: (board[BLACK] & BitBoardUtils.boardFromPosition("e3")) != 0 ? "B" : "O");
		result.append("--|--|\n");

		result.append("2 |--");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("b2")) != 0 ? "W" : (board[BLACK] & BitBoardUtils.boardFromPosition("b2")) != 0 ? "B" : "O");
		result.append("-----");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("d2")) != 0 ? "W"	: (board[BLACK] & BitBoardUtils.boardFromPosition("d2")) != 0 ? "B" : "O");
		result.append("-----");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("f2")) != 0 ? "W" : (board[BLACK] & BitBoardUtils.boardFromPosition("f2")) != 0 ? "B" : "O");
		result.append("--|\n");

		result.append("1 ");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("a1")) != 0 ? "W" : (board[BLACK] & BitBoardUtils.boardFromPosition("a1")) != 0 ? "B" : "O");
		result.append("--------");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("d1")) != 0 ? "W" : (board[BLACK] & BitBoardUtils.boardFromPosition("d1")) != 0 ? "B" : "O");
		result.append("--------");
		result.append((board[WHITE] & BitBoardUtils.boardFromPosition("g1")) != 0 ? "W" : (board[BLACK] & BitBoardUtils.boardFromPosition("g1")) != 0 ? "B" : "O");
		result.append("\n");

		result.append("  a  b  c  d  e  f  g\n");
		result.append("Player: " + (playerToMove == WHITE ? "WHITE" : "BLACK") + ";\n");
		result.append("Phase: " + (gamePhase == MIDGAME ? "MIDGAME" : "INITIALPHASE") + ";\n");
		result.append("White Checkers: " + checkersToPut[WHITE] + ";\n");
		result.append("Black Checkers: " + checkersToPut[BLACK] + ";\n");
		result.append("White Checkers On Board: " + checkersOnBoard[WHITE] + ";\n");
		result.append("Black Checkers On Board: " + checkersOnBoard[BLACK] + ";\n");

		return result.toString();
	}

	public static BitBoardState fromStateToBitBoard(State state, byte playerToMove, ITieChecker tieChecker) throws Exception {
		BitBoardState result = new BitBoardState(tieChecker);

		HashMap<String, State.Checker> boardMap = state.getBoard();
		for (String position : boardMap.keySet()) {
			if (boardMap.get(position) == State.Checker.WHITE)
				result.board[WHITE] |= BitBoardUtils.boardFromPosition(position);
			else if (boardMap.get(position) == State.Checker.BLACK)
				result.board[BLACK] |= BitBoardUtils.boardFromPosition(position);
		}

		if (state.getWhiteCheckers() > Byte.MAX_VALUE || state.getWhiteCheckers() < 0)
			throw new Exception("Wrong number of White checkers to put");
		result.checkersToPut[WHITE] = (byte) state.getWhiteCheckers();

		if (state.getBlackCheckers() > Byte.MAX_VALUE || state.getBlackCheckers() < 0)
			throw new Exception("Wrong number of Black checkers to put");
		result.checkersToPut[BLACK] = (byte) state.getBlackCheckers();

		if (state.getWhiteCheckersOnBoard() > Byte.MAX_VALUE || state.getWhiteCheckersOnBoard() < 0)
			throw new Exception("Wrong number of White checkers on board");
		result.checkersOnBoard[WHITE] = (byte) state.getWhiteCheckersOnBoard();

		if (state.getBlackCheckersOnBoard() > Byte.MAX_VALUE || state.getBlackCheckersOnBoard() < 0)
			throw new Exception("Wrong number of Black checkers on board");
		result.checkersOnBoard[BLACK] = (byte) state.getBlackCheckersOnBoard();

		if (playerToMove > BitBoardState.BLACK)
			throw new Exception("Wrong player");
		result.playerToMove = playerToMove;

		switch (state.getCurrentPhase()) {
		case FIRST:
			result.gamePhase = ~MIDGAME;
			break;
		case SECOND:
			result.gamePhase = BitBoardState.MIDGAME;
			break;
		case FINAL:
			result.gamePhase = BitBoardState.MIDGAME;
			break;
		default:
			throw new Exception("Wrong Phase");
		}

		return result;
	}
	
	public State fromBitBoardToState() {
		State result = new State();
		if (gamePhase != MIDGAME)
			result.setCurrentPhase(Phase.FIRST);
		else
			if (checkersOnBoard[WHITE] <= 3 || checkersOnBoard[BLACK] <= 3)
				result.setCurrentPhase(Phase.FINAL);
			else
				result.setCurrentPhase(Phase.SECOND);
		result.setBlackCheckers(this.checkersToPut[BLACK]);
		result.setWhiteCheckers(this.checkersToPut[WHITE]);
		result.setBlackCheckersOnBoard(this.checkersOnBoard[BLACK]);
		result.setWhiteCheckersOnBoard(this.checkersOnBoard[WHITE]);
		
		int position;
		for (int i=0; i<24; i++) {
			position = 1 << i;
			if ((this.board[WHITE] & position) != 0)
				result.getBoard().put(BitBoardUtils.positionFromBoard(position), Checker.WHITE);
			if ((this.board[BLACK] & position) != 0)
				result.getBoard().put(BitBoardUtils.positionFromBoard(position), Checker.BLACK);
		}
		
		return result;
	}
	
	public static LinkedHashMap<Action, State> successors(State state, Checker p) throws Exception {
		switch (state.getCurrentPhase()) {
		case FIRST:
			return successorsFirst(state, p);
		case SECOND:
			return successorsSecond(state, p);
		case FINAL:
			return successorsFinalOrSecond(state, p);
		default:
			throw new Exception("Illegal Phase");
		}
	}

	public static LinkedHashMap<Action, State> successorsFirst(State state, Checker p) {
		LinkedHashMap<Action, State> result = new LinkedHashMap<Action, State>();
		Phase1Action temp;
		State newState;
		LinkedHashMap<String, Checker> board = new LinkedHashMap<String, Checker>(state.getBoard());
		State.Checker otherChecker = p == Checker.WHITE ? Checker.BLACK : Checker.WHITE;

		for (String position : state.positions) {
			if (board.get(position) == State.Checker.EMPTY) {
				temp = new Phase1Action();
				temp.setPutPosition(position);
				newState = state.clone();
				newState.getBoard().put(position, p);
				try {
					if (Util.hasCompletedTriple(newState, position, p)) {
						boolean foundRemovableChecker = false;
						for (String otherPosition : state.positions) {
							if (board.get(otherPosition) == otherChecker
									&& !Util.hasCompletedTriple(newState, otherPosition, otherChecker)) {
								temp.setRemoveOpponentChecker(otherPosition);
								newState = Phase1.applyMove(state, temp, p);
								result.put(temp, newState);
								temp = new Phase1Action();
								temp.setPutPosition(position);
								newState = state.clone();
								newState.getBoard().put(position, p);
								foundRemovableChecker = true;
							}
						}
						if (!foundRemovableChecker) {
							for (String otherPosition : state.positions) {
								if (board.get(otherPosition) == otherChecker
										&& Util.hasCompletedTriple(newState, otherPosition, otherChecker)) {
									temp.setRemoveOpponentChecker(otherPosition);
									newState = Phase1.applyMove(state, temp, p);
									result.put(temp, newState);
									temp = new Phase1Action();
									temp.setPutPosition(position);
									newState = state.clone();
									newState.getBoard().put(position, p);
								}
							}
						}
					} else {
						newState = Phase1.applyMove(state, temp, p);
						result.put(temp, newState);
						temp = new Phase1Action();
						temp.setPutPosition(position);
						newState = state.clone();
						newState.getBoard().put(position, p);
					}
				} catch (WrongPhaseException | PositionNotEmptyException | NullCheckerException
						| NoMoreCheckersAvailableException | WrongPositionException | TryingToRemoveOwnCheckerException
						| TryingToRemoveEmptyCheckerException | NullStateException
						| TryingToRemoveCheckerInTripleException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	public static LinkedHashMap<Action, State> successorsSecond(State state, Checker p) {
		LinkedHashMap<Action, State> result = new LinkedHashMap<Action, State>();
		Phase2Action temp;
		State newState;
		LinkedHashMap<String, Checker> board = new LinkedHashMap<String, Checker>(state.getBoard());
		State.Checker otherChecker = p == Checker.WHITE ? Checker.BLACK : Checker.WHITE;

		for (String position : state.positions) {
			if (board.get(position) == p) {
				temp = new Phase2Action();
				temp.setFrom(position);
				try {
					for (String adjPos : Util.getAdiacentTiles(position)) {
						if (board.get(adjPos) == Checker.EMPTY) {

							temp.setTo(adjPos);
							newState = state.clone();
							newState.getBoard().put(adjPos, p);
							newState.getBoard().put(position, Checker.EMPTY);
							try {
								if (Util.hasCompletedTriple(newState, adjPos, p)) {
									boolean foundRemovableChecker = false;
									for (String otherPosition : state.positions) {
										if (board.get(otherPosition) == otherChecker
												&& !Util.hasCompletedTriple(newState, otherPosition, otherChecker)) {
											temp.setRemoveOpponentChecker(otherPosition);
											if (state.getCurrentPhase() == Phase.SECOND)
												newState = Phase2.applyMove(state, temp, p);
											else {
												PhaseFinalAction finalAction = new PhaseFinalAction();
												finalAction.setFrom(temp.getFrom());
												finalAction.setTo(temp.getTo());
												finalAction.setRemoveOpponentChecker(temp.getRemoveOpponentChecker());
												newState = PhaseFinal.applyMove(state, finalAction, p);
											}
											result.put(temp, newState);
											temp = new Phase2Action();
											temp.setFrom(position);
											temp.setTo(adjPos);
											foundRemovableChecker = true;
											newState = state.clone();
											newState.getBoard().put(adjPos, p);
											newState.getBoard().put(position, Checker.EMPTY);
										}
									}
									if (!foundRemovableChecker) {
										for (String otherPosition : state.positions) {
											if (board.get(otherPosition) == otherChecker
													&& Util.hasCompletedTriple(newState, otherPosition, otherChecker)
													) {
												temp.setRemoveOpponentChecker(otherPosition);
												if (state.getCurrentPhase() == Phase.SECOND)
													newState = Phase2.applyMove(state, temp, p);
												else {
													PhaseFinalAction finalAction = new PhaseFinalAction();
													finalAction.setFrom(temp.getFrom());
													finalAction.setTo(temp.getTo());
													finalAction
															.setRemoveOpponentChecker(temp.getRemoveOpponentChecker());
													newState = PhaseFinal.applyMove(state, finalAction, p);
												}
												result.put(temp, newState);
												temp = new Phase2Action();
												temp.setFrom(position);
												temp.setTo(adjPos);
												newState = state.clone();
												newState.getBoard().put(adjPos, p);
												newState.getBoard().put(position, Checker.EMPTY);
											}
										}
									}
								} else {
									if (state.getCurrentPhase() == Phase.SECOND)
										newState = Phase2.applyMove(state, temp, p);
									else {
										PhaseFinalAction finalAction = new PhaseFinalAction();
										finalAction.setFrom(temp.getFrom());
										finalAction.setTo(temp.getTo());
										finalAction.setRemoveOpponentChecker(temp.getRemoveOpponentChecker());
										newState = PhaseFinal.applyMove(state, finalAction, p);
									}
									result.put(temp, newState);
									temp = new Phase2Action();
									temp.setFrom(position);
									newState = state.clone();
									newState.getBoard().put(adjPos, p);
									newState.getBoard().put(position, Checker.EMPTY);
								}
							} catch (WrongPhaseException | PositionNotEmptyException | NullCheckerException
									| WrongPositionException | TryingToRemoveOwnCheckerException
									| TryingToRemoveEmptyCheckerException | NullStateException
									| TryingToRemoveCheckerInTripleException | NullActionException
									| TryingToMoveOpponentCheckerException | FromAndToAreEqualsException
									| FromAndToAreNotConnectedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}
				} catch (WrongPositionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	public static LinkedHashMap<Action, State> successorsFinalOrSecond(State state, Checker p) {
		if (p == Checker.WHITE) {
			if (state.getWhiteCheckersOnBoard() > 3) {
				LinkedHashMap<Action, State> resultMap = new LinkedHashMap<>();
				successorsSecond(state, p).forEach((k, v) -> {
					Phase2Action action = (Phase2Action) k;
					PhaseFinalAction result = new PhaseFinalAction();
					result.setFrom(action.getFrom());
					result.setTo(action.getTo());
					result.setRemoveOpponentChecker(action.getRemoveOpponentChecker());
					resultMap.put(result, v);
				});
				return resultMap;
			} else {
				return successorsFinal(state, p);
			}
		}
		// Player is BLACK
		else {
			if (state.getBlackCheckersOnBoard() > 3) {
				LinkedHashMap<Action, State> resultMap = new LinkedHashMap<>();
				successorsSecond(state, p).forEach((k, v) -> {
					Phase2Action action = (Phase2Action) k;
					PhaseFinalAction result = new PhaseFinalAction();
					result.setFrom(action.getFrom());
					result.setTo(action.getTo());
					result.setRemoveOpponentChecker(action.getRemoveOpponentChecker());
					resultMap.put(result, v);
				});
				return resultMap;
			} else {
				return successorsFinal(state, p);
			}
		}
	}

	public static LinkedHashMap<Action, State> successorsFinal(State state, Checker p) {
		LinkedHashMap<Action, State> result = new LinkedHashMap<Action, State>();
		PhaseFinalAction temp;
		State newState;
		LinkedHashMap<String, Checker> board = new LinkedHashMap<String, Checker>(state.getBoard());
		State.Checker otherChecker = p == Checker.WHITE ? Checker.BLACK : Checker.WHITE;

		for (String position : state.positions) {
			if (board.get(position) == p) {
				temp = new PhaseFinalAction();
				temp.setFrom(position);
				for (String toPos : state.positions) {
					if (board.get(toPos) == Checker.EMPTY) {

						temp.setTo(toPos);
						newState = state.clone();
						newState.getBoard().put(toPos, p);
						newState.getBoard().put(position, Checker.EMPTY);
						try {
							if (Util.hasCompletedTriple(newState, toPos, p)) {
								boolean foundRemovableChecker = false;
								for (String otherPosition : state.positions) {
									if (board.get(otherPosition) == otherChecker
											&& !Util.hasCompletedTriple(newState, otherPosition, otherChecker)) {
										temp.setRemoveOpponentChecker(otherPosition);
										newState = PhaseFinal.applyMove(state, temp, p);
										result.put(temp, newState);
										temp = new PhaseFinalAction();
										temp.setFrom(position);
										temp.setTo(toPos);
										foundRemovableChecker = true;
										newState = state.clone();
										newState.getBoard().put(toPos, p);
										newState.getBoard().put(position, Checker.EMPTY);
									}
								}
								if (!foundRemovableChecker) {
									for (String otherPosition : state.positions) {
										if (board.get(otherPosition) == otherChecker
												&& Util.hasCompletedTriple(newState, otherPosition, otherChecker)
												) {
											temp.setRemoveOpponentChecker(otherPosition);
											newState = PhaseFinal.applyMove(state, temp, p);
											result.put(temp, newState);
											temp = new PhaseFinalAction();
											temp.setFrom(position);
											temp.setTo(toPos);
											newState = state.clone();
											newState.getBoard().put(toPos, p);
											newState.getBoard().put(position, Checker.EMPTY);
										}
									}
								}
							} else {
								newState = PhaseFinal.applyMove(state, temp, p);
								result.put(temp, newState);
								temp = new PhaseFinalAction();
								temp.setFrom(position);
								newState = state.clone();
								newState.getBoard().put(toPos, p);
								newState.getBoard().put(position, Checker.EMPTY);
							}
						} catch (WrongPhaseException | PositionNotEmptyException | NullCheckerException
								| WrongPositionException | TryingToRemoveOwnCheckerException
								| TryingToRemoveEmptyCheckerException | NullStateException
								| TryingToRemoveCheckerInTripleException | NullActionException
								| TryingToMoveOpponentCheckerException | FromAndToAreEqualsException
								| FromAndToAreNotConnectedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
			}
		}

		return result;
	}

	private static Action stringToAction(String actionString, Phase fase) {
		if (fase == Phase.FIRST) { // prima fase
			Phase1Action action;
			action = new Phase1Action();
			action.setPutPosition(actionString.substring(0, 2));
			if (actionString.length() == 4)
				action.setRemoveOpponentChecker(actionString.substring(2, 4));
			else
				action.setRemoveOpponentChecker(null);
			return action;
		} else if (fase == Phase.SECOND) { // seconda fase
			Phase2Action action;
			action = new Phase2Action();
			action.setFrom(actionString.substring(0, 2));
			action.setTo(actionString.substring(2, 4));
			if (actionString.length() == 6)
				action.setRemoveOpponentChecker(actionString.substring(4, 6));
			else
				action.setRemoveOpponentChecker(null);
			return action;
		} else { // ultima fase
			PhaseFinalAction action;
			action = new PhaseFinalAction();
			action.setFrom(actionString.substring(0, 2));
			action.setTo(actionString.substring(2, 4));
			if (actionString.length() == 6)
				action.setRemoveOpponentChecker(actionString.substring(4, 6));
			else
				action.setRemoveOpponentChecker(null);
			return action;
		}
	}
	
	@Override
	public List<IAction> getFollowingMoves() {
		List<IAction> result = new ArrayList<>();

		if(gamePhase == MIDGAME) {
			if (checkersOnBoard[playerToMove] > 3) {
				result = getFollowingMovesMidGame();
			} else
				result = getFollowingMovesEndGame();
					
//			} else if (checkersOnBoard[playerToMove] == 3){
//				result = getFollowingMovesEndGame();
//			} else
//				throw new IllegalArgumentException("Qualcosa non quadra");
		} else {
			result = getFollowingMovesInitialPhase();
		}
		
//		try {
//			HashMap<Action, State> oldList = successors(fromBitBoardToState(), playerToMove == WHITE ? Checker.WHITE : Checker.BLACK);
////			if (oldList.keySet().size() != result.size())
////				throw new IllegalArgumentException("Dimensioni diverse");
//			for (IAction bitAction : result) {
//				boolean found = false;
//				for (Action action : oldList.keySet()) {
//					String key = action.toString();
//					if (key.equals(bitAction.toString())) {
//						found = true;
//						if (!oldList.get(action).equals(((BitBoardState) applyMove(bitAction)).fromBitBoardToState())) {
//							System.out.println("bitAction " + ((BitBoardAction) bitAction));
//							System.out.println("action " + action);
//							System.out.println("current state\n" + this);
//							System.out.println("next bit state\n" + ((BitBoardState)this.applyMove(bitAction)));
//							System.out.println("next chesani state\n" + oldList.get(action));
//							throw new IllegalArgumentException("Stati successori non uguali");
//						}
//						break;
//					}
//				}
//				if (!found)
//					throw new IllegalArgumentException();						
//			}
//			
//			for (Action action : oldList.keySet()) {
//				boolean found = false;
//				for (IAction bitAction : result) {
//					String key = bitAction.toString();
//					if (key.equals(action.toString())) {
//						found = true;
//						if (!oldList.get(action).equals(((BitBoardState) applyMove(bitAction)).fromBitBoardToState())) {
//							System.out.println("bitAction " + bitAction);
//							System.out.println("action " + action);
//							System.out.println("current state\n" + this);
//							System.out.println("next bit state\n" + this.applyMove(bitAction));
//							System.out.println("next chesani state\n" + oldList.get(action));
//							throw new IllegalArgumentException("Stati successori non uguali2");
//						}
//						break;
//					}
//				}
//				if (!found)
//					throw new IllegalArgumentException();	
//			}
//			
//		} catch (Exception e) {
////			e.printStackTrace();
//			throw new IllegalArgumentException(e);
//		}
//		
		
		return result;
	}

	private List<IAction> getFollowingMovesInitialPhase() {
		List<IAction> result = new ArrayList<>();
		BitBoardAction temp;
		int to;
		int remove;
		byte opponentPlayer = playerToMove == BitBoardState.WHITE ? BitBoardState.BLACK : BitBoardState.WHITE;
		
//		if (this.equals(new BitBoardState(tieChecker))) {
//			result.add(new BitBoardAction(0, 1, 0));
//			result.add(new BitBoardAction(0, 1 << 1, 0));
//			result.add(new BitBoardAction(0, 1 << 8, 0));
//			result.add(new BitBoardAction(0, 1 << 9, 0));
//			return result;
//		}
		
		for (int i = 0; i < 24; i++) {
			to = 1 << i;
			
			// empty position
			if (((board[WHITE] | board[BLACK]) & to) == 0) {				

				if (willCompleteMorris(0, i, playerToMove)) {
					boolean foundRemovableChecker = false;

					for (int j = 0; j < 24; j++) {
						remove = 1 << j;
						
						// opponent checker
						if ((board[opponentPlayer] & remove) != 0 && !willCompleteMorris(0, j, opponentPlayer)) {
							temp = new BitBoardAction(0, to, remove);
							result.add(temp);
							foundRemovableChecker = true;
						}
					}
					if (!foundRemovableChecker) {
						for (int j = 0; j < 24; j++) {
							remove = 1 << j;
							
							// opponent checker
							if ((board[opponentPlayer] & remove) != 0) {								
								temp = new BitBoardAction(0, to, remove);
								result.add(temp);
							}
						}
					}
				} else {
					temp = new BitBoardAction(0, to, 0);
					result.add(temp);
				}
			}
		}

		return result;
	}

	private List<IAction> getFollowingMovesMidGame() {
		List<IAction> result = new ArrayList<>();
		BitBoardAction temp;
		int from;
		int to;
		int remove;
		byte opponentPlayer = playerToMove == BitBoardState.WHITE ? BitBoardState.BLACK : BitBoardState.WHITE;

		for (int i = 0; i < 24; i++) {
			from = 1 << i;
			
			// player checker
			if ((board[playerToMove] & from) != 0) {				

				for (Integer adjacentPosition : ADJACENT_POSITIONS[i]) {
					to = 1 << adjacentPosition;
					
					// empty pos
					if (((board[WHITE] | board[BLACK]) & to) == 0) {

						if (willCompleteMorris(from, adjacentPosition, playerToMove)) {
							boolean foundRemovableChecker = false;

							for (int j = 0; j < 24; j++) {
								remove = 1 << j;
								
								// opponent checker
								if ((board[opponentPlayer] & remove) != 0 && !willCompleteMorris(0, j, opponentPlayer)) {
									temp = new BitBoardAction(from, to, remove);
									result.add(temp);
									foundRemovableChecker = true;
								}
							}

							if (!foundRemovableChecker) {
								for (int j = 0; j < 24; j++) {
									remove = 1 << j;
									
									// opponent checker
									if ((board[opponentPlayer] & remove) != 0 && willCompleteMorris(0, j, opponentPlayer)) {
										temp = new BitBoardAction(from, to, remove);
										result.add(temp);
									}
								}
							}
						} else {
							temp = new BitBoardAction(from, to, 0);
							result.add(temp);
						}
					}
				}
			}
		}

		return result;
	}

	private List<IAction> getFollowingMovesEndGame() {
		List<IAction> result = new ArrayList<>();
		BitBoardAction temp = new BitBoardAction();
		int from;
		int to;
		int remove;
		byte opponentPlayer = playerToMove == BitBoardState.WHITE ? BitBoardState.BLACK : BitBoardState.WHITE;

		for (int i = 0; i < 24; i++) {
			from = 1 << i;
			
			// player checker
			if ((board[playerToMove] & from) != 0) {

				for (int j = 0; j < 24; j++) {
					to = 1 << j;
					
					// empty position
					if (((board[WHITE] | board[BLACK]) & to) == 0) {

						if (willCompleteMorris(from, j, playerToMove)) {
							boolean foundRemovableChecker = false;

							for (int k = 0; k < 24; k++) {
								remove = 1 << k;
								
								// opponent checker
								if ((board[opponentPlayer] & remove) != 0 && !willCompleteMorris(0, k, opponentPlayer)) {									
									temp = new BitBoardAction(from, to, remove);
									result.add(temp);
									foundRemovableChecker = true;
								}
							}

							if (!foundRemovableChecker) {
								for (int k = 0; k < 24; k++) {
									remove = 1 << k;
									
									// opponent checker
									if ((board[opponentPlayer] & remove) != 0 && willCompleteMorris(0, k, opponentPlayer)) {
										temp = new BitBoardAction(from, to, remove);
										result.add(temp);
									}
								}
							}
						} else {
							temp = new BitBoardAction(from, to, 0);
							result.add(temp);
						}
					}
				}
			}
		}

		return result;
	}

	public boolean willCompleteMorris(int bitFrom, int intTo, byte player) {
		int tempBoard = (board[player] | (1 << intTo)) ^ bitFrom;
		
		for(Integer mill : POSITION_MILLS[intTo]) {
			if((tempBoard & mill) == mill)
				return true;
		}
		
		return false;
	}

	@Override
	public void move(IAction action) {
		
		byte opponentPlayer = (playerToMove == WHITE) ? BLACK : WHITE;
		
		board[playerToMove] ^= ((BitBoardAction) action).getFrom();
		board[playerToMove] |= ((BitBoardAction) action).getTo();
		board[opponentPlayer] ^= ((BitBoardAction) action).getRemove();
		
		if(gamePhase != MIDGAME) {
			checkersToPut[playerToMove]--;
			checkersOnBoard[playerToMove]++;
		}
		
		if(((BitBoardAction) action).getRemove() != 0)
			checkersOnBoard[opponentPlayer]--;

		if((checkersToPut[WHITE] | checkersToPut[BLACK]) == 0)
			gamePhase = MIDGAME;
		
		playerToMove = opponentPlayer;
		
		((BitBoardTieChecker) tieChecker).addState(clone());
	}

	@Override
	public void unmove(IAction action) {
		((BitBoardTieChecker) tieChecker).removeState(this);
		
		
		byte opponentPlayer = playerToMove;
		playerToMove = (opponentPlayer == WHITE) ? BLACK : WHITE;
		
		board[playerToMove] |= ((BitBoardAction) action).getFrom();
		board[playerToMove] ^= ((BitBoardAction) action).getTo();
		board[opponentPlayer] |= ((BitBoardAction) action).getRemove();
		
		if(((BitBoardAction) action).getFrom() == 0) {
			gamePhase = ~MIDGAME;
			checkersToPut[playerToMove]++;
			checkersOnBoard[playerToMove]--;
		}
		
		if(((BitBoardAction) action).getRemove() != 0)
			checkersOnBoard[opponentPlayer]++;
	}

	public IState applyMove(IAction action) {
//		byte opponentPlayer = playerToMove == WHITE ? BLACK : WHITE;
//		int put = checkersToPut[playerToMove];
//		
//		if(gamePhase != MIDGAME)
//			put--;
//		
//		if(playerToMove == WHITE) {
//			return new BitBoardState(put, checkersToPut[opponentPlayer], (board[WHITE] ^ ((BitBoardAction) action).getFrom()) | ((BitBoardAction) action).getTo(), board[BLACK] ^ ((BitBoardAction) action).getRemove(), opponentPlayer, this.tieChecker);
//		} else {
//			return new BitBoardState(checkersToPut[opponentPlayer], put, board[WHITE] ^ ((BitBoardAction) action).getRemove(), (board[BLACK] ^ ((BitBoardAction) action).getFrom()) | ((BitBoardAction) action).getTo(), opponentPlayer, this.tieChecker);
//		}
		
		BitBoardState result = (BitBoardState) this.clone();
		byte opponentPlayer = (playerToMove == WHITE) ? BLACK : WHITE;
		
		result.board[playerToMove] ^= ((BitBoardAction) action).getFrom();
		result.board[playerToMove] |= ((BitBoardAction) action).getTo();
		result.board[opponentPlayer] ^= ((BitBoardAction) action).getRemove();
		
		if(result.gamePhase != MIDGAME) {
			result.checkersToPut[playerToMove]--;
			result.checkersOnBoard[playerToMove]++;
		}
		
		if(((BitBoardAction) action).getRemove() != 0)
			result.checkersOnBoard[opponentPlayer]--;

		if((result.checkersToPut[WHITE] | result.checkersToPut[BLACK]) == 0)
			result.gamePhase = MIDGAME;
		
		result.playerToMove = opponentPlayer;
		
		return result;
	}

	@Override
	public IState clone() {		
		return new BitBoardState(checkersToPut[WHITE], checkersToPut[BLACK], board[WHITE], board[BLACK], playerToMove, this.tieChecker);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj.getClass() != this.getClass())
			return false;
		
		BitBoardState state = (BitBoardState) obj;
		if(this.board[WHITE] == state.board[WHITE] &&
				this.board[BLACK] == state.board[BLACK] &&
				this.checkersToPut[WHITE] == state.checkersToPut[WHITE] &&
				this.checkersToPut[BLACK] == state.checkersToPut[BLACK] 
//				&& this.checkersOnBoard[WHITE] == state.checkersOnBoard[WHITE] &&
//				this.checkersOnBoard[BLACK] == state.checkersOnBoard[BLACK]
//						&&
//				this.playerToMove == state.playerToMove &&
//				((this.gamePhase == MIDGAME && state.gamePhase == MIDGAME) || (this.gamePhase != MIDGAME && state.gamePhase != MIDGAME))
				)
			return true;
		
//		if(this.board[WHITE] == ((BitBoardState) state).board[WHITE] &&
//				this.board[BLACK] == ((BitBoardState) state).board[BLACK] 
////						&&
////				this.gamePhase == MIDGAME &&
////				((BitBoardState) state).gamePhase == MIDGAME
//				)
//			return true;
		
		return false;
	}

	@Override
	public int getHeuristicEvaluation() {
//		int result;
//		
//		if(gamePhase == MIDGAME) {
//			if (checkersOnBoard[playerToMove] > 3) {
//				result = getHeuristicEvaluationMidGame();
//			} else {
//				result = getHeuristicEvaluationEndGame();
//			}
//		} else {
//			result = getHeuristicEvaluationInitialPhase();
//		}
//		
//		return result;
		
//		byte originalPlayer = playerToMove;
//		byte opponentPlayer = playerToMove == WHITE ? BLACK : WHITE;
//		int result;
//		
//		playerToMove = opponentPlayer;
//		opponentPlayer = opponentPlayer == WHITE ? BLACK : WHITE;
//		
//		result = newHeuristic();
//		
//		playerToMove = originalPlayer;
//		
//		return -result;
		
		int result;
		byte opponentPlayer = playerToMove == WHITE ? BLACK : WHITE;
		
		if(gamePhase == MIDGAME) {
			if (checkersOnBoard[playerToMove] > 3 && checkersOnBoard[opponentPlayer] == 3) {
				result = getHeuristicEvaluationPlayerMidGameOpponentEndGame();
			} else if(checkersOnBoard[playerToMove] == 3 && checkersOnBoard[opponentPlayer] > 3) {
				result = getHeuristicEvaluationPlayerEndGameOpponentMidGame();
			} else if(checkersOnBoard[playerToMove] == 3 && checkersOnBoard[opponentPlayer] == 3) {
				result = getHeuristicEvaluationEndGame();
			} else {
				result = getHeuristicEvaluationMidGame();
			}
			
		} else {
			result = getHeuristicEvaluationInitialPhase();
		}
		
		return result;

	}
	
	private int getHeuristicEvaluationInitialPhase() {
		int result = 0;
		byte opponentPlayer = playerToMove == WHITE ? BLACK : WHITE;
		
		//HAS_COMPLETED_MORRIS
		
		//CLOSED_MORRIS
		int closedMorrisPlayer = 0;
		int closedMorrisOpponent = 0;
		
		//TWO_PIECES_CONFIGURATION
		int twoPiecesConfigurationPlayer = 0;
		int twoPiecesConfigurationOpponent = 0;
		
		int playerToMoveMill;
		int opponentPlayerMill;
		
		for(int mill : MILLS) {
			playerToMoveMill = board[playerToMove] & mill;
			opponentPlayerMill = board[opponentPlayer] & mill;
			
			//CLOSED_MORRIS
			if(playerToMoveMill == mill) {
				closedMorrisPlayer++;
			} else if(opponentPlayerMill == mill) {
				closedMorrisOpponent++;
			}
			
			//TWO_PIECES_CONFIGURATION
			if(opponentPlayerMill == 0 && Integer.bitCount(playerToMoveMill) == 2) {
				twoPiecesConfigurationPlayer++;
			} else if(playerToMoveMill == 0 && Integer.bitCount(opponentPlayerMill) == 2) {
				twoPiecesConfigurationOpponent++;
			}
		}
		
		//BLOCKED_PIECES
		int blockedPiecesPlayer = 0;
		int blockedPiecesOpponent = 0;
		
		//THREE_PIECES_CONFIGURATION
		int threePiecesConfigurationPlayer = 0;
		int threePiecesConfigurationOpponent = 0;
		
		int temp;
		int fullBoard = board[playerToMove] | board[opponentPlayer];
		int playerToMovePiece;
		int opponentPlayerPiece;
		
		for(int i=0; i<24; i++) {
			temp = 1 << i;			
			playerToMovePiece = board[playerToMove] & temp;
			opponentPlayerPiece = board[opponentPlayer] & temp;			
			
			//BLOCKED_PIECES
			if(opponentPlayerPiece != 0) {
				boolean isBlocked = true;
				
				for(int adjacentPosition : ADJACENT_POSITIONS[i]) {
					if((fullBoard & (1 << adjacentPosition)) == 0) {
						isBlocked = false;
						break;
					}
				}
				if(isBlocked)
					blockedPiecesPlayer++;
				
			} else if(playerToMovePiece != 0) {
				boolean isBlocked = true;
				
				for(int adjacentPosition : ADJACENT_POSITIONS[i]) {
					if((fullBoard & (1 << adjacentPosition)) == 0) {
						isBlocked = false;
						break;
					}
				}
				if(isBlocked)
					blockedPiecesOpponent++;
			}
			
			//THREE_PIECES_CONFIGURATION
			if(playerToMovePiece != 0) {
				boolean check = true;
				for(int mill : POSITION_MILLS[i]) {
					if(!((board[opponentPlayer] & mill) == 0 && Integer.bitCount(board[playerToMove] & mill) == 2)) {
						check = false;
						break;
					}
				}
				if(check) {
					threePiecesConfigurationPlayer++;
				}
				
			}else if(opponentPlayerPiece != 0) {
				boolean check = true;
				for(int mill : POSITION_MILLS[i]) {
					if(!((board[playerToMove] & mill) == 0 && Integer.bitCount(board[opponentPlayer] & mill) == 2)) {
						check = false;
						break;
					}
				}
				if(check) {
					threePiecesConfigurationOpponent++;
				}
			}
			
		}
		
		result += INITIALPHASE_CLOSED_MORRIS * closedMorrisPlayer;
		result -= INITIALPHASE_CLOSED_MORRIS * closedMorrisOpponent;
		
		result += INITIALPHASE_BLOCKED_PIECES * blockedPiecesPlayer;
		result -= INITIALPHASE_BLOCKED_PIECES * blockedPiecesOpponent;
		
		result += INITIALPHASE_TWO_PIECES_CONFIGURATION * twoPiecesConfigurationPlayer;
		result -= INITIALPHASE_TWO_PIECES_CONFIGURATION * twoPiecesConfigurationOpponent;
		
		result += INITIALPHASE_THREE_PIECES_CONFIGURATION * threePiecesConfigurationPlayer;
		result -= INITIALPHASE_THREE_PIECES_CONFIGURATION * threePiecesConfigurationOpponent;
		
		result += INITIALPHASE_PIECES_NUMBER * (checkersOnBoard[playerToMove] - checkersOnBoard[opponentPlayer]);
		
		return result;
	}
	
	private int getHeuristicEvaluationMidGame() {
		int result = 0;
		byte opponentPlayer = playerToMove == WHITE ? BLACK : WHITE;
		
		//CLOSED_MORRIS
		int closedMorrisPlayer = 0;
		int closedMorrisOpponent = 0;
		
		//OPENED_MORRIS
		int openedMorrisPlayer = 0;
		int openedMorrisOpponent = 0;
		
		//OPENED_MORRIS_NEAR_OPPONENT
		int openedMorrisNearOpponentPlayer = 0;
		int openedMorrisNearOpponentOpponent = 0;
		
		//UNBLOCKABLE_DOUBLE_MORRIS
		int unblockableDoubleMorrisPlayer = 0;
		int unblockableDoubleMorrisOpponent = 0;
		
		int playerToMoveMill;
		int opponentPlayerMill;
		
		for(int mill : MILLS) {
			playerToMoveMill = board[playerToMove] & mill;
			opponentPlayerMill = board[opponentPlayer] & mill;
			
			//CLOSED_MORRIS
			if(playerToMoveMill == mill) {
				closedMorrisPlayer++;
			} else if(opponentPlayerMill == mill) {
				closedMorrisOpponent++;
			}
			
			//OPENED_MORRIS
			boolean foundPlayer = false;
			boolean foundOpponent = false;
			
			if(opponentPlayerMill == 0 && Integer.bitCount(playerToMoveMill) == 2) {
				for(int adjacentPosition : ADJACENT_POSITIONS[Integer.numberOfTrailingZeros(playerToMoveMill ^ mill)]) {
					if((board[playerToMove] & (1 << adjacentPosition)) != 0) {
						foundPlayer = true;
					} else if((board[opponentPlayer] & (1 << adjacentPosition)) != 0) {
						foundOpponent = true;
					}
				}
				
				if(foundPlayer) {
					openedMorrisPlayer++;
					if(foundOpponent)
						openedMorrisNearOpponentPlayer++;
				}
				
			} else if(playerToMoveMill == 0 && Integer.bitCount(opponentPlayerMill) == 2) {
				for(int adjacentPosition : ADJACENT_POSITIONS[Integer.numberOfTrailingZeros(opponentPlayerMill ^ mill)]) {
					if((board[opponentPlayer] & (1 << adjacentPosition)) != 0) {
						foundOpponent = true;
					} else if((board[playerToMove] & (1 << adjacentPosition)) != 0) {
						foundPlayer = true;
					}
				}
				
				if(foundOpponent) {
					openedMorrisOpponent++;
					if(foundPlayer)
						openedMorrisNearOpponentOpponent++;
				}
				
			}
			
			//UNBLOCKABLE_DOUBLE_MORRIS
			// ho un mill
			if((board[playerToMove] & mill) == mill) {
				for(int i=0; i<24; i++) {
					// per ogni pedina del mill
					if((mill & (1 << i)) != 0) {
						// per ogni pedina adiacente
						for(int adjacentPosition : ADJACENT_POSITIONS[i]) {
							// pedine adiacenti non appartenenti al mill
							if((mill & (1 << adjacentPosition)) == 0) {
								unblockableDoubleMorrisPlayer += is2PiecesConfigurationNotInMill(playerToMove, adjacentPosition, mill, false);
							}
						}
					}
				}
			}
			// ho un mill
			else if((board[opponentPlayer] & mill) == mill) {
				for(int i=0; i<24; i++) {
					// per ogni pedina del mill
					if((mill & (1 << i)) != 0) {
						// per ogni pedina adiacente
						for(int adjacentPosition : ADJACENT_POSITIONS[i]) {
							// pedine adiacenti non appartenenti al mill
							if((mill & (1 << adjacentPosition)) == 0) {
								unblockableDoubleMorrisOpponent += is2PiecesConfigurationNotInMill(opponentPlayer, adjacentPosition, mill, true);
							}
						}
					}
				}
			}
		}
		
		//DOUBLE_MORRIS
//		int doubleMorrisPlayer = 0;
//		int doubleMorrisOpponent = 0;
		
		//BLOCKED_PIECES
		int blockedPiecesPlayer = 0;
		int blockedPiecesOpponent = 0;
		
		int temp;
		int fullBoard = board[playerToMove] | board[opponentPlayer];
		
		for(int i=0; i<24; i++) {
			temp = 1 << i;			
			
//			//DOUBLE_MORRIS
//			boolean check = true;
//			for(int mill : POSITION_MILLS[i]) {
//				if((board[playerToMove] & mill) != mill) {
//					check = false;
//					break;
//				}
//			}
//			if(check) {
//				doubleMorrisPlayer++;
//			}
//			
//			check = true;
//			for(int mill : POSITION_MILLS[i]) {
//				if((board[opponentPlayer] & mill) != mill) {
//					check = false;
//					break;
//				}
//			}
//			if(check) {
//				doubleMorrisOpponent++;
//			}
			
//			boolean checkPlayer = true;
//			boolean checkOpponent = true;
//			for(int mill : POSITION_MILLS[i]) {
//				if((board[playerToMove] & mill) != mill) {
//					checkPlayer = false;
//				}
//				
//				if((board[opponentPlayer] & mill) != mill) {
//					checkOpponent = false;
//				}
//			}
//			
//			if(checkPlayer) {
//				doubleMorrisPlayer++;
//			}
//			
//			if(checkOpponent) {
//				doubleMorrisOpponent++;
//			}
			
			//BLOCKED_PIECES
			if((board[opponentPlayer] & temp) != 0) {
				boolean isBlocked = true;
				
				for(int adjacentPosition : ADJACENT_POSITIONS[i]) {
					if((fullBoard & (1 << adjacentPosition)) == 0) {
						isBlocked = false;
						break;
					}
				}
				if(isBlocked) {
					blockedPiecesPlayer++;
				}
				
			} else if((board[playerToMove] & temp) != 0) {
				boolean isBlocked = true;
				
				for(int adjacentPosition : ADJACENT_POSITIONS[i]) {
					if((fullBoard & (1 << adjacentPosition)) == 0) {
						isBlocked = false;
						break;
					}
				}
				if(isBlocked) {
					blockedPiecesOpponent++;
				}
			}
		}
		
		result += MIDGAME_CLOSED_MORRIS * closedMorrisPlayer;
		result -= MIDGAME_CLOSED_MORRIS * closedMorrisOpponent;
		
		result += MIDGAME_OPENED_MORRIS * openedMorrisPlayer;
		result -= MIDGAME_OPENED_MORRIS * openedMorrisOpponent;
		
		result += MIDGAME_OPENED_MORRIS_NEAR_OPPONENT * openedMorrisNearOpponentPlayer;
		result -= MIDGAME_OPENED_MORRIS_NEAR_OPPONENT * openedMorrisNearOpponentOpponent;
		
		result += MIDGAME_UNBLOCKABLE_DOUBLE_MORRIS * unblockableDoubleMorrisPlayer;
		result -= MIDGAME_UNBLOCKABLE_DOUBLE_MORRIS * unblockableDoubleMorrisOpponent;
		
//		result += MIDGAME_DOUBLE_MORRIS * doubleMorrisPlayer;
//		result -= MIDGAME_DOUBLE_MORRIS * doubleMorrisOpponent;
		
		result += MIDGAME_BLOCKED_PIECES * blockedPiecesPlayer;
		result -= MIDGAME_BLOCKED_PIECES * blockedPiecesOpponent;
		
		result += MIDGAME_PIECES_NUMBER * (checkersOnBoard[playerToMove] - checkersOnBoard[opponentPlayer]);
		
		return result;
	}
	
	private int getHeuristicEvaluationEndGame() {
		int result = 0;
		byte opponentPlayer = playerToMove == WHITE ? BLACK : WHITE;
		
		//CLOSED_MORRIS
		int closedMorrisPlayer = 0;
		int closedMorrisOpponent = 0;
		
		//TWO_PIECES_CONFIGURATION
		int twoPiecesConfigurationPlayer = 0;
		int twoPiecesConfigurationOpponent = 0;
		
		int playerToMoveMill;
		int opponentPlayerMill;
		
		for(int mill : MILLS) {
			playerToMoveMill = board[playerToMove] & mill;
			opponentPlayerMill = board[opponentPlayer] & mill;
			
			//CLOSED_MORRIS
			if(playerToMoveMill == mill) {
				closedMorrisPlayer++;
			} else if(opponentPlayerMill == mill) {
				closedMorrisOpponent++;
			}
			
			//TWO_PIECES_CONFIGURATION
			if(opponentPlayerMill == 0 && Integer.bitCount(playerToMoveMill) == 2) {
				twoPiecesConfigurationPlayer++;
			} else if(playerToMoveMill == 0 && Integer.bitCount(opponentPlayerMill) == 2) {
				twoPiecesConfigurationOpponent++;
			}
		}

		//THREE_PIECES_CONFIGURATION
		int threePiecesConfigurationPlayer = 0;
		int threePiecesConfigurationOpponent = 0;
		
		int temp;
		
		for(int i=0; i<24; i++) {
			temp = 1 << i;
			
			//THREE_PIECES_CONFIGURATION
			if((board[playerToMove] & temp) != 0) {
				boolean check = true;
				for(int mill : POSITION_MILLS[i]) {
					if(!((board[opponentPlayer] & mill) == 0 && Integer.bitCount(board[playerToMove] & mill) == 2)) {
						check = false;
						break;
					}
				}
				if(check) {
					threePiecesConfigurationPlayer++;
				}
				
			} else if((board[opponentPlayer] & temp) != 0) {
				boolean check = true;
				for(int mill : POSITION_MILLS[i]) {
					if(!((board[playerToMove] & mill) == 0 && Integer.bitCount(board[opponentPlayer] & mill) == 2)) {
						check = false;
						break;
					}
				}
				if(check) {
					threePiecesConfigurationOpponent++;
				}
			}

		}
		
		result += ENDGAME_CLOSED_MORRIS * closedMorrisPlayer;
		result -= ENDGAME_CLOSED_MORRIS * closedMorrisOpponent;
		
		result += ENDGAME_TWO_PIECES_CONFIGURATION * twoPiecesConfigurationPlayer;
		result -= ENDGAME_TWO_PIECES_CONFIGURATION * twoPiecesConfigurationOpponent;
		
		result += ENDGAME_THREE_PIECES_CONFIGURATION * threePiecesConfigurationPlayer;
		result -= ENDGAME_THREE_PIECES_CONFIGURATION * threePiecesConfigurationOpponent;
		
		return result;
	}
	
	private int getHeuristicEvaluationPlayerMidGameOpponentEndGame() {
		int result = 0;
		byte opponentPlayer = playerToMove == WHITE ? BLACK : WHITE;
		
		/*
		 * player MIDGAME
		 */
		
		//CLOSED_MORRIS
		int closedMorrisPlayer = 0;

		//OPENED_MORRIS
		int openedMorrisPlayer = 0;

		//OPENED_MORRIS_NEAR_OPPONENT
//		int openedMorrisNearOpponentPlayer = 0;
		
		//UNBLOCKABLE_DOUBLE_MORRIS
		int unblockableDoubleMorrisPlayer = 0;

		int playerToMoveMill;
		int opponentPlayerMill;

		for(int mill : MILLS) {
			playerToMoveMill = board[playerToMove] & mill;
			opponentPlayerMill = board[opponentPlayer] & mill;

			//CLOSED_MORRIS
			if(playerToMoveMill == mill) {
				closedMorrisPlayer++;
			}

			//OPENED_MORRIS
			boolean foundPlayer = false;
//			boolean foundOpponent = false;

			if(opponentPlayerMill == 0 && Integer.bitCount(playerToMoveMill) == 2) {
				for(int adjacentPosition : ADJACENT_POSITIONS[Integer.numberOfTrailingZeros(playerToMoveMill ^ mill)]) {
					if((board[playerToMove] & (1 << adjacentPosition)) != 0) {
						foundPlayer = true;
					}
//					else if((board[opponentPlayer] & adjacentPosition) != 0) {
//						foundOpponent = true;
//					}
				}

				if(foundPlayer) {
					openedMorrisPlayer++;
//					if(foundOpponent)
//						openedMorrisNearOpponentPlayer++;
				}

			}
			
			//UNBLOCKABLE_DOUBLE_MORRIS
			// ho un mill
			if((board[playerToMove] & mill) == mill) {
				for(int i=0; i<24; i++) {
					// per ogni pedina del mill
					if((mill & (1 << i)) != 0) {
						// per ogni pedina adiacente
						for(int adjacentPosition : ADJACENT_POSITIONS[i]) {
							// pedine adiacenti non appartenenti al mill
							if((mill & (1 << adjacentPosition)) == 0) {
								unblockableDoubleMorrisPlayer += is2PiecesConfigurationNotInMill(playerToMove, adjacentPosition, mill, false);
							}
						}
					}
				}
			}
			
		}

		//DOUBLE_MORRIS
//		int doubleMorrisPlayer = 0;

		//BLOCKED_PIECES
//		int blockedPiecesPlayer = 0;

		int temp;
//		int fullBoard = board[playerToMove] | board[opponentPlayer];

//		for(int i=0; i<24; i++) {
//			temp = 1 << i;			
//
////			//DOUBLE_MORRIS
////			boolean check = true;
////			for(int mill : POSITION_MILLS[i]) {
////				if((board[playerToMove] & mill) != mill) {
////					check = false;
////					break;
////				}
////			}
////			if(check) {
////				doubleMorrisPlayer++;
////			}
//
//			//BLOCKED_PIECES
////			if((board[opponentPlayer] & temp) != 0) {
////				boolean isBlocked = true;
////
////				for(int adjacentPosition : ADJACENT_POSITIONS[i]) {
////					if((fullBoard & (1 << adjacentPosition)) == 0) {
////						isBlocked = false;
////						break;
////					}
////				}
////				if(isBlocked) {
////					blockedPiecesPlayer++;
////				}
////
////			}
//		}

		result += MIDGAME_CLOSED_MORRIS * closedMorrisPlayer;

//		result += MIDGAME_OPENED_MORRIS * openedMorrisPlayer;
//
//		result += MIDGAME_OPENED_MORRIS_NEAR_OPPONENT * openedMorrisNearOpponentPlayer;
		
		// ogni opened morris puo' essere chiuso
		result += MIDGAME_OPENED_MORRIS_NEAR_OPPONENT * openedMorrisPlayer;
		
//		result += MIDGAME_DOUBLE_MORRIS * doubleMorrisPlayer;
		
		result += MIDGAME_UNBLOCKABLE_DOUBLE_MORRIS * unblockableDoubleMorrisPlayer;
		
		// nessuno pezzo puo' essere bloccato
//		result += MIDGAME_BLOCKED_PIECES * blockedPiecesPlayer;

		result += MIDGAME_PIECES_NUMBER * (checkersOnBoard[playerToMove] - checkersOnBoard[opponentPlayer]);
				
		/*
		 * opponent ENDGAME
		 */
		
		//CLOSED_MORRIS
		int closedMorrisOpponent = 0;

		//TWO_PIECES_CONFIGURATION
		int twoPiecesConfigurationOpponent = 0;

		for(int mill : MILLS) {
			playerToMoveMill = board[playerToMove] & mill;
			opponentPlayerMill = board[opponentPlayer] & mill;

			//CLOSED_MORRIS
			if(opponentPlayerMill == mill) {
				closedMorrisOpponent++;
			}

			//TWO_PIECES_CONFIGURATION
			if(playerToMoveMill == 0 && Integer.bitCount(opponentPlayerMill) == 2) {
				twoPiecesConfigurationOpponent++;
			}
		}

		//THREE_PIECES_CONFIGURATION
		int threePiecesConfigurationOpponent = 0;

		for(int i=0; i<24; i++) {
			temp = 1 << i;
			
			if((board[opponentPlayer] & temp) != 0) {
				boolean check = true;
				for(int mill : POSITION_MILLS[i]) {
					if(!((board[playerToMove] & mill) == 0 && Integer.bitCount(board[opponentPlayer] & mill) == 2)) {
						check = false;
						break;
					}
				}
				if(check) {
					threePiecesConfigurationOpponent++;
				}
			}

		}

		result -= ENDGAME_CLOSED_MORRIS * closedMorrisOpponent;

		result -= ENDGAME_TWO_PIECES_CONFIGURATION * twoPiecesConfigurationOpponent;

		result -= ENDGAME_THREE_PIECES_CONFIGURATION * threePiecesConfigurationOpponent;
		
		return result;
	}
	
	private int getHeuristicEvaluationPlayerEndGameOpponentMidGame() {
		int result = 0;
		byte opponentPlayer = playerToMove == WHITE ? BLACK : WHITE;
		
		/*
		 * player ENDGAME
		 */
		
		//CLOSED_MORRIS
		int closedMorrisPlayer = 0;

		//TWO_PIECES_CONFIGURATION
		int twoPiecesConfigurationPlayer = 0;

		int playerToMoveMill;
		int opponentPlayerMill;

		for(int mill : MILLS) {
			playerToMoveMill = board[playerToMove] & mill;
			opponentPlayerMill = board[opponentPlayer] & mill;

			//CLOSED_MORRIS
			if(playerToMoveMill == mill) {
				closedMorrisPlayer++;
			}

			//TWO_PIECES_CONFIGURATION
			if(opponentPlayerMill == 0 && Integer.bitCount(playerToMoveMill) == 2) {
				twoPiecesConfigurationPlayer++;
			}
		}

		//THREE_PIECES_CONFIGURATION
		int threePiecesConfigurationPlayer = 0;

		int temp;

		for(int i=0; i<24; i++) {
			temp = 1 << i;

			//THREE_PIECES_CONFIGURATION
			if((board[playerToMove] & temp) != 0) {
				boolean check = true;
				for(int mill : POSITION_MILLS[i]) {
					if(!((board[opponentPlayer] & mill) == 0 && Integer.bitCount(board[playerToMove] & mill) == 2)) {
						check = false;
						break;
					}
				}
				if(check) {
					threePiecesConfigurationPlayer++;
				}

			}

		}

		result += ENDGAME_CLOSED_MORRIS * closedMorrisPlayer;

		result += ENDGAME_TWO_PIECES_CONFIGURATION * twoPiecesConfigurationPlayer;

		result += ENDGAME_THREE_PIECES_CONFIGURATION * threePiecesConfigurationPlayer;
		
		/*
		 * opponent MIDGAME
		 */
		
		//CLOSED_MORRIS
		int closedMorrisOpponent = 0;

		//OPENED_MORRIS
		int openedMorrisOpponent = 0;
		
		//UNBLOCKABLE_DOUBLE_MORRIS
		int unblockableDoubleMorrisOpponent = 0;

		//OPENED_MORRIS_NEAR_OPPONENT
//		int openedMorrisNearOpponentOpponent = 0;

		for(int mill : MILLS) {
			playerToMoveMill = board[playerToMove] & mill;
			opponentPlayerMill = board[opponentPlayer] & mill;

			//CLOSED_MORRIS
			if(opponentPlayerMill == mill) {
				closedMorrisOpponent++;
			}

			//OPENED_MORRIS
//			boolean foundPlayer = false;
			boolean foundOpponent = false;

			if(playerToMoveMill == 0 && Integer.bitCount(opponentPlayerMill) == 2) {
				for(int adjacentPosition : ADJACENT_POSITIONS[Integer.numberOfTrailingZeros(opponentPlayerMill ^ mill)]) {
					if((board[opponentPlayer] & (1 << adjacentPosition)) != 0) {
						foundOpponent = true;
					}
//					else if((board[playerToMove] & adjacentPosition) != 0) {
//						foundPlayer = true;
//					}
				}

				if(foundOpponent) {
					openedMorrisOpponent++;
//					if(foundPlayer)
//						openedMorrisNearOpponentOpponent++;
				}

			}
			
			//UNBLOCKABLE_DOUBLE_MORRIS
			if((board[opponentPlayer] & mill) == mill) {
				for(int i=0; i<24; i++) {
					// per ogni pedina del mill
					if((mill & (1 << i)) != 0) {
						// per ogni pedina adiacente
						for(int adjacentPosition : ADJACENT_POSITIONS[i]) {
							// pedine adiacenti non appartenenti al mill
							if((mill & (1 << adjacentPosition)) == 0) {
								unblockableDoubleMorrisOpponent += is2PiecesConfigurationNotInMill(opponentPlayer, adjacentPosition, mill, true);
							}
						}
					}
				}
			}
			
		}

		//DOUBLE_MORRIS
//		int doubleMorrisOpponent = 0;

		//BLOCKED_PIECES
//		int blockedPiecesOpponent = 0;

//		int fullBoard = board[playerToMove] | board[opponentPlayer];

//		for(int i=0; i<24; i++) {
//			temp = 1 << i;			
//
////			//DOUBLE_MORRIS
////			boolean check = true;
////			for(int mill : POSITION_MILLS[i]) {
////				if((board[opponentPlayer] & mill) != mill) {
////					check = false;
////					break;
////				}
////			}
////			if(check) {
////				doubleMorrisOpponent++;
////			}
//
//			//BLOCKED_PIECES
////			if((board[playerToMove] & temp) != 0) {
////				boolean isBlocked = true;
////
////				for(int adjacentPosition : ADJACENT_POSITIONS[i]) {
////					if((fullBoard & (1 << adjacentPosition)) == 0) {
////						isBlocked = false;
////						break;
////					}
////				}
////				if(isBlocked) {
////					blockedPiecesOpponent++;
////				}
////			}
//		}

		result -= MIDGAME_CLOSED_MORRIS * closedMorrisOpponent;

//		result -= MIDGAME_OPENED_MORRIS * openedMorrisOpponent;
//
//		result -= MIDGAME_OPENED_MORRIS_NEAR_OPPONENT * openedMorrisNearOpponentOpponent;

		// ogni opened morris puo' essere chiuso
		result -= MIDGAME_OPENED_MORRIS_NEAR_OPPONENT * openedMorrisOpponent;
		
//		result -= MIDGAME_DOUBLE_MORRIS * doubleMorrisOpponent;

		result -= MIDGAME_UNBLOCKABLE_DOUBLE_MORRIS * unblockableDoubleMorrisOpponent;

		// nessuno pezzo puo' essere bloccato
//		result -= MIDGAME_BLOCKED_PIECES * blockedPiecesOpponent;

		result += MIDGAME_PIECES_NUMBER * (checkersOnBoard[playerToMove] - checkersOnBoard[opponentPlayer]);
		
		return result;
	}
	
//	public int fivePiecesConfiguration() {
//		int result = 0;
//		
//		// per ogni mill, prendo le sue pedine, prendo quelle adiacenti non
//		// appartenenti alla riga dell mill, se la pedina appartiene ad un
//		// 2piecesConfiguration e le altre due pedine non appartengono alla riga
//		// del morris -> 1
//		// se c'e' un avversario adiacente alla posizione appartenente al 2piecesConfig -> 2
//		for(int mill : MILLS) {
//			if((board[playerToMove] & mill) == mill) {
//				for(int i=0; i<24; i++) {
//					// per ogni pedina del mill
//					if((mill & (1 << i)) != 0) {
//						// per ogni pedina adiacente
//						for(int adjacentPosition : ADJACENT_POSITIONS[i]) {
//							// pedine adiacenti non appartenenti al mill
//							if((mill & (1 << adjacentPosition)) == 0) {
//								result += is2PiecesConfigurationNotInMill(playerToMove, adjacentPosition, mill, true);
//							}
//						}
//					}
//				}
//			}
//		}
//		
//		return result;
//	}
	
	private int is2PiecesConfigurationNotInMill(int player, int position, int millToAvoid, boolean lookForOpponentBlock) {
		byte opponentPlayer = player == WHITE ? BLACK : WHITE;
		int result = 0;
		// mill passanti per position
		for(int mill : POSITION_MILLS[position]) {
				// se il mill non e' al millToAvoid
				// se e' un 2piecesConfig			
				if((mill & millToAvoid) == 0 && Integer.bitCount(board[opponentPlayer] & mill) == 0 && Integer.bitCount(board[player] & mill) == 2) {
					// se posizione vuota e' la position
					if((board[player] & (1 << position)) == 0) {
					    result++;
						
					    if(lookForOpponentBlock) {
						    // se c'e' un avversario adiacente alla posizione libera del 2piecesConfig
							for(int adjacentPosition : ADJACENT_POSITIONS[Integer.numberOfTrailingZeros((board[player] & mill) ^ mill)]) {
								if((board[opponentPlayer] & (1 << adjacentPosition)) != 0) {
									/*
									 * OK ma avversario
									 */
									result--;
									// forse posso fare return
								}
							}
					    }
					}
				}
//			}
		}
		
		return result;
	}
	
	@Override
	public boolean isWinningState() {
		if(gamePhase != MIDGAME)
			return false;
		
		if(checkersOnBoard[playerToMove] < 3)
			return true;
		else if(checkersOnBoard[playerToMove] > 3) {
			
			for(int i=0; i<24; i++) {				
				if((board[playerToMove] & (1 << i)) != 0) {					
					for(int position : ADJACENT_POSITIONS[i]) {
						if(((board[WHITE] | board[BLACK]) & (1 << position)) == 0)
							return false;
					}
				}
			}
			return true;
		}
		
		return false;
	}
	
	public boolean isQuiescent() {
		byte opponentPlayer = playerToMove;
		playerToMove = opponentPlayer == WHITE ? BLACK : WHITE;
		
//		byte opponentPlayer = playerToMove == WHITE ? BLACK : WHITE;

		int playerToMoveMill;
		int opponentPlayerMill;
		
		boolean foundPlayer;
		boolean foundOpponent;
		
		for(int mill : MILLS) {
			playerToMoveMill = board[playerToMove] & mill;
			opponentPlayerMill = board[opponentPlayer] & mill;
			
			// l'avversario pu� bloccare un morris
			
			if(gamePhase != MIDGAME || checkersOnBoard[playerToMove] == 3) {
				if((opponentPlayerMill) == 0 && Integer.bitCount(playerToMoveMill) == 2) {
					for(int adjacentPosition : ADJACENT_POSITIONS[Integer.numberOfTrailingZeros(playerToMoveMill ^ mill)]) {
						if((board[opponentPlayer] & adjacentPosition) != 0) {
							playerToMove = playerToMove == WHITE ? BLACK : WHITE;
							return false;
						}
					}
				}
			} else {
				foundPlayer = false;
				foundOpponent = false;
				
				if((opponentPlayerMill) == 0 && Integer.bitCount(playerToMoveMill) == 2) {
					for(int adjacentPosition : ADJACENT_POSITIONS[Integer.numberOfTrailingZeros(playerToMoveMill ^ mill)]) {
						if((board[playerToMove] & adjacentPosition) != 0) {
							foundPlayer = true;
						}
						if((board[opponentPlayer] & adjacentPosition) != 0) {
							foundOpponent = true;
						}
					}
				}
				
				if(foundPlayer && foundOpponent) {
					playerToMove = playerToMove == WHITE ? BLACK : WHITE;
					return false;
				}
			}
			
			// l'avversario pu� chiudere un morris
//			if(playerToMoveMill == 0 && Integer.bitCount(opponentPlayerMill) == 2) {
//				for(int adjacentPosition : ADJACENT_POSITIONS[Integer.numberOfTrailingZeros(opponentPlayerMill ^ mill)]) {
//					if((board[opponentPlayer] & adjacentPosition) != 0) {
//						return false;
//					}
//				}
//			}
		}
		
		playerToMove = playerToMove == WHITE ? BLACK : WHITE;
		return true;
		
//		if(gamePhase == MIDGAME) {
//			if (checkersOnBoard[playerToMove] > 3) {
//				return true;
//			} else {
//				return true;
//			}
//		} else {
//			return true;
//		}
	}
	
	public byte getGamePhase() {
		return this.gamePhase;
	}
	
	public boolean isLegalMove(IAction action) {
		
		if(getFollowingMoves().contains(action))
			return true;
		else
			return false;
		
//		BitBoardAction bAction = (BitBoardAction) action;
//		byte opponentPlayer = playerToMove==WHITE?BLACK:WHITE;
//		
//		//Initial Phase
//		if (this.gamePhase != MIDGAME) {
//			if (bAction.getFrom() != 0)
//				return false;
////			if ( (bAction.getTo() & board[WHITE]) != 0 || (bAction.getTo() & board[BLACK]) != 0)
//			if(((board[WHITE] | board[BLACK]) & bAction.getTo()) != 0)
//				return false;
//			if (bAction.getRemove() != 0) {
////				int to;
////				for (to=0; to<24; to++) {
////					if (bAction.getTo() >> to == 1)
////						break;
////				}
////				if ( !willCompleteMorris(bAction.getFrom(), to, this.playerToMove) )
//				if ( !willCompleteMorris(bAction.getFrom(), Integer.numberOfTrailingZeros(bAction.getTo()), this.playerToMove) )
//					return false;
//				if ( (board[opponentPlayer] & bAction.getRemove()) == 0)
//					return false;
//				
//				if(checkersOnBoard[opponentPlayer] > 3) {
//				
//					boolean isRemoveInMill = false;				
//					for(int mill : POSITION_MILLS[Integer.numberOfTrailingZeros(bAction.getRemove())]) {
//						if((board[opponentPlayer] & mill) == mill) {
//							isRemoveInMill = true;
//							break;
//						}
//					}
//					
//					boolean checkerFree = false;
//					if(isRemoveInMill) {
//						for(int i=0; i<24; i++) {
//							if((board[opponentPlayer] & (1 << i)) != 0) {
//								boolean checkerInMill = false;
//								for(int mill : POSITION_MILLS[i]) {
//									if((board[opponentPlayer] & mill) == mill) {
//										checkerInMill = true;
//										break;
//									}
//								}
//								
//								if(!checkerInMill) {
//									checkerFree = true;
//									break;
//								}
//							}
//						}
//					}
//					
//					if(checkerFree && isRemoveInMill)
//						return false;
//				}
//			}
//		}
//		
//		else {
//			
//			//Second Phase
////			if (this.checkersOnBoard[playerToMove] >= 3) {
//			if (this.checkersOnBoard[playerToMove] > 3) {
//				if (bAction.getFrom() == 0)
//					return false;
////				if ( (bAction.getTo() & board[WHITE]) != 0 || (bAction.getTo() & board[BLACK]) != 0)
//				if(((board[WHITE] | board[BLACK]) & bAction.getTo()) != 0)
//					return false;
////				int from;
////				for (from=0; from<24; from++) {
////					if (bAction.getFrom() >> from == 1)
////						break;
////				}
//				boolean found = false;
////				for (Integer adjacentPosition : ADJACENT_POSITIONS[from]) {
//				for (Integer adjacentPosition : ADJACENT_POSITIONS[Integer.numberOfTrailingZeros(bAction.getFrom())]) {
//					if (bAction.getTo() == (1 << adjacentPosition)) {
//						found = true;
//						break;
//					}
//				}
//				if (!found)
//					return false;
//				if (bAction.getRemove() != 0) {
////					int to;
////					for (to=0; to<24; to++) {
////						if (bAction.getTo() >> to == 1)
////							break;
////					}
////					if ( !willCompleteMorris(bAction.getFrom(), to, this.playerToMove) )
//					if ( !willCompleteMorris(bAction.getFrom(), Integer.numberOfTrailingZeros(bAction.getTo()), this.playerToMove) )
//						return false;
//					if ( (board[opponentPlayer] & bAction.getRemove()) == 0)
//						return false;
//					
//					if(checkersOnBoard[opponentPlayer] > 3) {
//					
//						boolean isRemoveInMill = false;				
//						for(int mill : POSITION_MILLS[Integer.numberOfTrailingZeros(bAction.getRemove())]) {
//							if((board[opponentPlayer] & mill) == mill) {
//								isRemoveInMill = true;
//								break;
//							}
//						}
//						
//						boolean checkerFree = false;
//						if(isRemoveInMill) {
//							for(int i=0; i<24; i++) {
//								if((board[opponentPlayer] & (1 << i)) != 0) {
//									boolean checkerInMill = false;
//									for(int mill : POSITION_MILLS[i]) {
//										if((board[opponentPlayer] & mill) == mill) {
//											checkerInMill = true;
//											break;
//										}
//									}
//									
//									if(!checkerInMill) {
//										checkerFree = true;
//										break;
//									}
//								}
//							}
//						}
//						
//						if(checkerFree && isRemoveInMill)
//							return false;
//					}
//				}
//			}
//			
//			//Third Phase
//			else {
//				if (bAction.getFrom() == 0)
//					return false;
////				if ( (bAction.getTo() & board[WHITE]) != 0 || (bAction.getTo() & board[BLACK]) != 0)
//				if(((board[WHITE] | board[BLACK]) & bAction.getTo()) != 0)
//					return false;
//				if (bAction.getRemove() != 0) {
////					int to;
////					for (to=0; to<24; to++) {
////						if (bAction.getTo() >> to == 1)
////							break;
////					}
////					if ( !willCompleteMorris(bAction.getFrom(), to, this.playerToMove) )
//					if ( !willCompleteMorris(bAction.getFrom(), Integer.numberOfTrailingZeros(bAction.getTo()), this.playerToMove) )
//						return false;
//					if ( (board[opponentPlayer] & bAction.getRemove()) == 0)
//						return false;
//					
//					if(checkersOnBoard[opponentPlayer] > 3) {
//					
//						boolean isRemoveInMill = false;				
//						for(int mill : POSITION_MILLS[Integer.numberOfTrailingZeros(bAction.getRemove())]) {
//							if((board[opponentPlayer] & mill) == mill) {
//								isRemoveInMill = true;
//								break;
//							}
//						}
//						
//						boolean checkerFree = false;
//						if(isRemoveInMill) {
//							for(int i=0; i<24; i++) {
//								if((board[opponentPlayer] & (1 << i)) != 0) {
//									boolean checkerInMill = false;
//									for(int mill : POSITION_MILLS[i]) {
//										if((board[opponentPlayer] & mill) == mill) {
//											checkerInMill = true;
//											break;
//										}
//									}
//									
//									if(!checkerInMill) {
//										checkerFree = true;
//										break;
//									}
//								}
//							}
//						}
//						
//						if(checkerFree && isRemoveInMill)
//							return false;
//					}
//				}
//			}
//			
//		}
//
//		return true;		
	}
	
	public int hashCode() {
		return this.board[WHITE] + 31*this.board[BLACK] + 31*31*this.checkersToPut[WHITE] + 31*31*31*this.checkersToPut[BLACK];
	}
	
	public BitBoardHash getHash() {
		byte symms = 0;
		long hash;
		long tempHash;
		byte opponentPlayer = playerToMove == WHITE ? BLACK : WHITE;
		
		// WHITE = 0 ---> faccio le simmetrie senza colore
		if(playerToMove == WHITE) {
			// current
			hash = board[WHITE];
			hash |= ((long) board[BLACK]) << 24;
//			hash |= ((long) checkersToPut[WHITE]) << 48;
//			hash |= ((long) checkersToPut[BLACK]) << 52;
//			hash |= ((long) playerToMove) << 56;		
			
			// rotation 90
			tempHash = BitBoardUtils.rotationClockwise90(board[WHITE]);
			tempHash |= ((long) BitBoardUtils.rotationClockwise90(board[BLACK])) << 24;
//			tempHash |= ((long) checkersToPut[WHITE]) << 48;
//			tempHash |= ((long) checkersToPut[BLACK]) << 52;
//			tempHash |= ((long) playerToMove) << 56;
			
			if(tempHash < hash) {
				hash = tempHash;
				symms = ROTATION_90;
			}
			
			// rotation 180
			tempHash = BitBoardUtils.rotationClockwise180(board[WHITE]);
			tempHash |= ((long) BitBoardUtils.rotationClockwise180(board[BLACK])) << 24;
//			tempHash |= ((long) checkersToPut[WHITE]) << 48;
//			tempHash |= ((long) checkersToPut[BLACK]) << 52;
//			tempHash |= ((long) playerToMove) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = ROTATION_180;
			}

			// rotation 270
			tempHash = BitBoardUtils.rotationAnticlockwise90(board[WHITE]);
			tempHash |= ((long) BitBoardUtils.rotationAnticlockwise90(board[BLACK])) << 24;
//			tempHash |= ((long) checkersToPut[WHITE]) << 48;
//			tempHash |= ((long) checkersToPut[BLACK]) << 52;
//			tempHash |= ((long) playerToMove) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = ROTATION_270;
			}
			
			// vertical flip
			tempHash = BitBoardUtils.verticalFlip(board[WHITE]);
			tempHash |= ((long) BitBoardUtils.verticalFlip(board[BLACK])) << 24;
//			tempHash |= ((long) checkersToPut[WHITE]) << 48;
//			tempHash |= ((long) checkersToPut[BLACK]) << 52;
//			tempHash |= ((long) playerToMove) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = VERTICAL_FLIP;
			}
			
			// vertical flip - rotation 90
			tempHash = BitBoardUtils.verticalFlip(BitBoardUtils.rotationClockwise90(board[WHITE]));
			tempHash |= ((long) BitBoardUtils.verticalFlip(BitBoardUtils.rotationClockwise90(board[BLACK]))) << 24;
//			tempHash |= ((long) checkersToPut[WHITE]) << 48;
//			tempHash |= ((long) checkersToPut[BLACK]) << 52;
//			tempHash |= ((long) playerToMove) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = VERTICAL_FLIP;
				symms |= ROTATION_90;
			}
			
			// vertical flip - rotation 180
			tempHash = BitBoardUtils.verticalFlip(BitBoardUtils.rotationClockwise180(board[WHITE]));
			tempHash |= ((long) BitBoardUtils.verticalFlip(BitBoardUtils.rotationClockwise180(board[BLACK]))) << 24;
//			tempHash |= ((long) checkersToPut[WHITE]) << 48;
//			tempHash |= ((long) checkersToPut[BLACK]) << 52;
//			tempHash |= ((long) playerToMove) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = VERTICAL_FLIP;
				symms |= ROTATION_180;
			}

			// vertical flip - rotation 270
			tempHash = BitBoardUtils.verticalFlip(BitBoardUtils.rotationAnticlockwise90(board[WHITE]));
			tempHash |= ((long) BitBoardUtils.verticalFlip(BitBoardUtils.rotationAnticlockwise90(board[BLACK]))) << 24;
//			tempHash |= ((long) checkersToPut[WHITE]) << 48;
//			tempHash |= ((long) checkersToPut[BLACK]) << 52;
//			tempHash |= ((long) playerToMove) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = VERTICAL_FLIP;
				symms |= ROTATION_270;
			}
			
			// inside out
			tempHash = BitBoardUtils.insideOut(board[WHITE]);
			tempHash |= ((long) BitBoardUtils.insideOut(board[BLACK])) << 24;
//			tempHash |= ((long) checkersToPut[WHITE]) << 48;
//			tempHash |= ((long) checkersToPut[BLACK]) << 52;
//			tempHash |= ((long) playerToMove) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = INSIDE_OUT;
			}
			
			// inside out - rotation 90
			tempHash = BitBoardUtils.insideOut(BitBoardUtils.rotationClockwise90(board[WHITE]));
			tempHash |= ((long) BitBoardUtils.insideOut(BitBoardUtils.rotationClockwise90(board[BLACK]))) << 24;
//			tempHash |= ((long) checkersToPut[WHITE]) << 48;
//			tempHash |= ((long) checkersToPut[BLACK]) << 52;
//			tempHash |= ((long) playerToMove) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = INSIDE_OUT;
				symms |= ROTATION_90;
			}
			
			// inside out - rotation 180
			tempHash = BitBoardUtils.insideOut(BitBoardUtils.rotationClockwise180(board[WHITE]));
			tempHash |= ((long) BitBoardUtils.insideOut(BitBoardUtils.rotationClockwise180(board[BLACK]))) << 24;
//			tempHash |= ((long) checkersToPut[WHITE]) << 48;
//			tempHash |= ((long) checkersToPut[BLACK]) << 52;
//			tempHash |= ((long) playerToMove) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = INSIDE_OUT;
				symms |= ROTATION_180;
			}
			
			// inside out - rotation 270
			tempHash = BitBoardUtils.insideOut(BitBoardUtils.rotationAnticlockwise90(board[WHITE]));
			tempHash |= ((long) BitBoardUtils.insideOut(BitBoardUtils.rotationAnticlockwise90(board[BLACK]))) << 24;
//			tempHash |= ((long) checkersToPut[WHITE]) << 48;
//			tempHash |= ((long) checkersToPut[BLACK]) << 52;
//			tempHash |= ((long) playerToMove) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = INSIDE_OUT;
				symms |= ROTATION_270;
			}
			
			// inside out - vertical flip
			tempHash = BitBoardUtils.insideOut(BitBoardUtils.verticalFlip(board[WHITE]));
			tempHash |= ((long) BitBoardUtils.insideOut(BitBoardUtils.verticalFlip(board[BLACK]))) << 24;
//			tempHash |= ((long) checkersToPut[WHITE]) << 48;
//			tempHash |= ((long) checkersToPut[BLACK]) << 52;
//			tempHash |= ((long) playerToMove) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = INSIDE_OUT;
				symms |= VERTICAL_FLIP;
			}
			
			// inside out - vertical flip - rotation 90
			tempHash = BitBoardUtils.insideOut(BitBoardUtils.verticalFlip(BitBoardUtils.rotationClockwise90(board[WHITE])));
			tempHash |= ((long) BitBoardUtils.insideOut(BitBoardUtils.verticalFlip(BitBoardUtils.rotationClockwise90(board[BLACK])))) << 24;
//			tempHash |= ((long) checkersToPut[WHITE]) << 48;
//			tempHash |= ((long) checkersToPut[BLACK]) << 52;
//			tempHash |= ((long) playerToMove) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = INSIDE_OUT;
				symms |= VERTICAL_FLIP;
				symms |= ROTATION_90;
			}
			
			// inside out - vertical flip - rotation 180
			tempHash = BitBoardUtils.insideOut(BitBoardUtils.verticalFlip(BitBoardUtils.rotationClockwise180(board[WHITE])));
			tempHash |= ((long) BitBoardUtils.insideOut(BitBoardUtils.verticalFlip(BitBoardUtils.rotationClockwise180(board[BLACK])))) << 24;
//			tempHash |= ((long) checkersToPut[WHITE]) << 48;
//			tempHash |= ((long) checkersToPut[BLACK]) << 52;
//			tempHash |= ((long) playerToMove) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = INSIDE_OUT;
				symms |= VERTICAL_FLIP;
				symms |= ROTATION_180;
			}
			
			// inside out - vertical flip - rotation 270
			tempHash = BitBoardUtils.insideOut(BitBoardUtils.verticalFlip(BitBoardUtils.rotationAnticlockwise90(board[WHITE])));
			tempHash |= ((long) BitBoardUtils.insideOut(BitBoardUtils.verticalFlip(BitBoardUtils.rotationAnticlockwise90(board[BLACK])))) << 24;
//			tempHash |= ((long) checkersToPut[WHITE]) << 48;
//			tempHash |= ((long) checkersToPut[BLACK]) << 52;
//			tempHash |= ((long) playerToMove) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = INSIDE_OUT;
				symms |= VERTICAL_FLIP;
				symms |= ROTATION_270;
			}
			
			hash |= ((long) checkersToPut[WHITE]) << 48;
			hash |= ((long) checkersToPut[BLACK]) << 52;
			hash |= ((long) playerToMove) << 56;
		}
		// BLACK = 1 ---> faccio le simmetrie con colore
		else {
			// color inversion
			hash = board[BLACK];
			hash |= ((long) board[WHITE]) << 24;
//			hash |= ((long) checkersToPut[BLACK]) << 48;
//			hash |= ((long) checkersToPut[WHITE]) << 52;
//			hash |= ((long) opponentPlayer) << 56;

//			if(tempHash < hash) {
//				hash = tempHash;
				symms = COLOR_INVERSION;
//			}
			
			// color inversion - rotation 90
			tempHash = BitBoardUtils.rotationClockwise90(board[BLACK]);
			tempHash |= ((long) BitBoardUtils.rotationClockwise90(board[WHITE])) << 24;
//			tempHash |= ((long) checkersToPut[BLACK]) << 48;
//			tempHash |= ((long) checkersToPut[WHITE]) << 52;
//			tempHash |= ((long) opponentPlayer) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = ROTATION_90;
				symms |= COLOR_INVERSION;
			}

			// color inversion - rotation 180
			tempHash = BitBoardUtils.rotationClockwise180(board[BLACK]);
			tempHash |= ((long) BitBoardUtils.rotationClockwise180(board[WHITE])) << 24;
//			tempHash |= ((long) checkersToPut[BLACK]) << 48;
//			tempHash |= ((long) checkersToPut[WHITE]) << 52;
//			tempHash |= ((long) opponentPlayer) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = ROTATION_180;
				symms |= COLOR_INVERSION;
			}

			// color inversion - rotation 270
			tempHash = BitBoardUtils.rotationAnticlockwise90(board[BLACK]);
			tempHash |= ((long) BitBoardUtils.rotationAnticlockwise90(board[WHITE])) << 24;
//			tempHash |= ((long) checkersToPut[BLACK]) << 48;
//			tempHash |= ((long) checkersToPut[WHITE]) << 52;
//			tempHash |= ((long) opponentPlayer) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = ROTATION_270;
				symms |= COLOR_INVERSION;
			}
			
			// color inversion - vertical flip
			tempHash = BitBoardUtils.verticalFlip(board[BLACK]);
			tempHash |= ((long) BitBoardUtils.verticalFlip(board[WHITE])) << 24;
//			tempHash |= ((long) checkersToPut[BLACK]) << 48;
//			tempHash |= ((long) checkersToPut[WHITE]) << 52;
//			tempHash |= ((long) opponentPlayer) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = VERTICAL_FLIP;
				symms |= COLOR_INVERSION;
			}

			 //color inversion - vertical flip - rotation 90
			tempHash = BitBoardUtils.verticalFlip(BitBoardUtils.rotationClockwise90(board[BLACK]));
			tempHash |= ((long) BitBoardUtils.verticalFlip(BitBoardUtils.rotationClockwise90(board[WHITE]))) << 24;
//			tempHash |= ((long) checkersToPut[BLACK]) << 48;
//			tempHash |= ((long) checkersToPut[WHITE]) << 52;
//			tempHash |= ((long) opponentPlayer) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = VERTICAL_FLIP;
				symms |= ROTATION_90;
				symms |= COLOR_INVERSION;
			}

			// color inversion - vertical flip - rotation 180
			tempHash = BitBoardUtils.verticalFlip(BitBoardUtils.rotationClockwise180(board[BLACK]));
			tempHash |= ((long) BitBoardUtils.verticalFlip(BitBoardUtils.rotationClockwise180(board[WHITE]))) << 24;
//			tempHash |= ((long) checkersToPut[BLACK]) << 48;
//			tempHash |= ((long) checkersToPut[WHITE]) << 52;
//			tempHash |= ((long) opponentPlayer) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = VERTICAL_FLIP;
				symms |= ROTATION_180;
				symms |= COLOR_INVERSION;
			}

			// color inversion - vertical flip - rotation 270
			tempHash = BitBoardUtils.verticalFlip(BitBoardUtils.rotationAnticlockwise90(board[BLACK]));
			tempHash |= ((long) BitBoardUtils.verticalFlip(BitBoardUtils.rotationAnticlockwise90(board[WHITE]))) << 24;
//			tempHash |= ((long) checkersToPut[BLACK]) << 48;
//			tempHash |= ((long) checkersToPut[WHITE]) << 52;
//			tempHash |= ((long) opponentPlayer) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = VERTICAL_FLIP;
				symms |= ROTATION_270;
				symms |= COLOR_INVERSION;
			}
			
			// color inversion - inside out
			tempHash = BitBoardUtils.insideOut(board[BLACK]);
			tempHash |= ((long) BitBoardUtils.insideOut(board[WHITE])) << 24;
//			tempHash |= ((long) checkersToPut[BLACK]) << 48;
//			tempHash |= ((long) checkersToPut[WHITE]) << 52;
//			tempHash |= ((long) opponentPlayer) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = INSIDE_OUT;
				symms |= COLOR_INVERSION;
			}

			// color inversion - inside out - rotation 90
			tempHash = BitBoardUtils.insideOut(BitBoardUtils.rotationClockwise90(board[BLACK]));
			tempHash |= ((long) BitBoardUtils.insideOut(BitBoardUtils.rotationClockwise90(board[WHITE]))) << 24;
//			tempHash |= ((long) checkersToPut[BLACK]) << 48;
//			tempHash |= ((long) checkersToPut[WHITE]) << 52;
//			tempHash |= ((long) opponentPlayer) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = INSIDE_OUT;
				symms |= ROTATION_90;
				symms |= COLOR_INVERSION;
			}

			// color inversion - inside out - rotation 180
			tempHash = BitBoardUtils.insideOut(BitBoardUtils.rotationClockwise180(board[BLACK]));
			tempHash |= ((long) BitBoardUtils.insideOut(BitBoardUtils.rotationClockwise180(board[WHITE]))) << 24;
//			tempHash |= ((long) checkersToPut[BLACK]) << 48;
//			tempHash |= ((long) checkersToPut[WHITE]) << 52;
//			tempHash |= ((long) opponentPlayer) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = INSIDE_OUT;
				symms |= ROTATION_180;
				symms |= COLOR_INVERSION;
			}

			// coor inversion - inside out - rotation 270
			tempHash = BitBoardUtils.insideOut(BitBoardUtils.rotationAnticlockwise90(board[BLACK]));
			tempHash |= ((long) BitBoardUtils.insideOut(BitBoardUtils.rotationAnticlockwise90(board[WHITE]))) << 24;
//			tempHash |= ((long) checkersToPut[BLACK]) << 48;
//			tempHash |= ((long) checkersToPut[WHITE]) << 52;
//			tempHash |= ((long) opponentPlayer) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = INSIDE_OUT;
				symms |= ROTATION_270;
				symms |= COLOR_INVERSION;
			}
			
			// color inversion - inside out - vertical flip
			tempHash = BitBoardUtils.insideOut(BitBoardUtils.verticalFlip(board[BLACK]));
			tempHash |= ((long) BitBoardUtils.insideOut(BitBoardUtils.verticalFlip(board[WHITE]))) << 24;
//			tempHash |= ((long) checkersToPut[BLACK]) << 48;
//			tempHash |= ((long) checkersToPut[WHITE]) << 52;
//			tempHash |= ((long) opponentPlayer) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = INSIDE_OUT;
				symms |= VERTICAL_FLIP;
				symms |= COLOR_INVERSION;
			}

			// color inversion - inside out - vertical flip - rotation 90
			tempHash = BitBoardUtils.insideOut(BitBoardUtils.verticalFlip(BitBoardUtils.rotationClockwise90(board[BLACK])));
			tempHash |= ((long) BitBoardUtils.insideOut(BitBoardUtils.verticalFlip(BitBoardUtils.rotationClockwise90(board[WHITE])))) << 24;
//			tempHash |= ((long) checkersToPut[BLACK]) << 48;
//			tempHash |= ((long) checkersToPut[WHITE]) << 52;
//			tempHash |= ((long) opponentPlayer) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = INSIDE_OUT;
				symms |= VERTICAL_FLIP;
				symms |= ROTATION_90;
				symms |= COLOR_INVERSION;
			}

			// color inversion - inside out - vertical flip - rotation 180
			tempHash = BitBoardUtils.insideOut(BitBoardUtils.verticalFlip(BitBoardUtils.rotationClockwise180(board[BLACK])));
			tempHash |= ((long) BitBoardUtils.insideOut(BitBoardUtils.verticalFlip(BitBoardUtils.rotationClockwise180(board[WHITE])))) << 24;
//			tempHash |= ((long) checkersToPut[BLACK]) << 48;
//			tempHash |= ((long) checkersToPut[WHITE]) << 52;
//			tempHash |= ((long) opponentPlayer) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = INSIDE_OUT;
				symms |= VERTICAL_FLIP;
				symms |= ROTATION_180;
				symms |= COLOR_INVERSION;
			}

			// color inversion - inside out - vertical flip - rotation 270
			tempHash = BitBoardUtils.insideOut(BitBoardUtils.verticalFlip(BitBoardUtils.rotationAnticlockwise90(board[BLACK])));
			tempHash |= ((long) BitBoardUtils.insideOut(BitBoardUtils.verticalFlip(BitBoardUtils.rotationAnticlockwise90(board[WHITE])))) << 24;
//			tempHash |= ((long) checkersToPut[BLACK]) << 48;
//			tempHash |= ((long) checkersToPut[WHITE]) << 52;
//			tempHash |= ((long) opponentPlayer) << 56;

			if(tempHash < hash) {
				hash = tempHash;
				symms = INSIDE_OUT;
				symms |= VERTICAL_FLIP;
				symms |= ROTATION_270;
				symms |= COLOR_INVERSION;
			}
			
			hash |= ((long) checkersToPut[BLACK]) << 48;
			hash |= ((long) checkersToPut[WHITE]) << 52;
			hash |= ((long) opponentPlayer) << 56;
		}
		
		return new BitBoardHash(hash, symms);
	}

	public class BitBoardHash {
		private long hash;
		private byte symms;
		
		public BitBoardHash(long hash, byte symms) {
			this.hash = hash;
			this.symms = symms;
		}
		
		public long getHash() {
			return hash;
		}
		
		public byte getSymms() {
			return symms;
		}
	}
	
	//------------------------------------
	
    static final int[] THREE_WAY_INTERSECTIONS = { 1, 3, 5, 7, 17, 19, 21, 23 };
    static final int THREE_WAY_INTERSECTIONS_BB = (1 << 1) | (1 << 3) | (1 << 5) | (1 << 7) | (1 << 17) | (1 << 19) | (1 << 21) | (1 << 23);
    static final int[] FOUR_WAY_INTERSECTIONS = { 9, 11, 13, 15 };
    static final int FOUR_WAY_INTERSECTIONS_BB = (1 << 9) | (1 << 11) | (1 << 13) | (1 << 15);
    
    static final int FOUR_WAY_INTERSECTION_VALUE = 64;
    static final int THREE_WAY_INTERSECTION_VALUE = 16;
    static final int SPOKE_EXCLUSIVITY_VALUE = 24;
    static final int MIDDLE_RING_EXCLUSIVITY_VALUE = 16;
    
    static final int MILL_VALUE = 32;
    static final int ROW_EXCLUSIVITY_VALUE = 64;
    static final int POTENTIAL_MILL_VALUE = 160;
    static final int POTENTIAL_MILL_NEXT_TURN_VALUE = 128;
    
    static final int PIECE_ADVANTAGE_EXPONENT = 8;
    static final int MOBILITY_ADVANTAGE_EXPONENT = 6;
    static final int MILL_MOBILITY_EXPONENT = 3;
    static final int SINGLE_JUMPER_EXPONENT = 3;
    
    public static final int OUTER_RING_IDX = 0;
    public static final int MIDDLE_RING_IDX = 4;
    public static final int INNER_RING_IDX = 8;
    public static final int SPOKES_IDX = 12;
	
	private int newHeuristic() {
		int result = 0;
		byte opponentPlayer = playerToMove == WHITE ? BLACK : WHITE;
		
		result += (checkersOnBoard[playerToMove] - checkersOnBoard[opponentPlayer]) << PIECE_ADVANTAGE_EXPONENT;
		
		if(gamePhase != MIDGAME || (gamePhase == MIDGAME && checkersOnBoard[playerToMove] > 3))  {
			// initialPhase e midGame
			result += mobility() << MOBILITY_ADVANTAGE_EXPONENT;
			result += intersectionAdvantage();
			result += rowAdvantage();
		} else {
			//endgame
			result <<= SINGLE_JUMPER_EXPONENT;
			result += intersectionAdvantage();
			result += rowAdvantage();
		}
		
		return result;
	}
	
	private int mobility() {
		int result = 0;
		byte opponentPlayer = playerToMove == WHITE ? BLACK : WHITE;
		
		for(int i=0; i<24; i++) {
			if((board[playerToMove] & (i << 1)) != 0) {
				for(int adjacentPosition : ADJACENT_POSITIONS[i]) {
					if(((board[WHITE] | board[BLACK]) & (1 << adjacentPosition)) == 0) {
						result++;
					}
				}
			}
		}
		
		for(int i=0; i<24; i++) {
			if((board[opponentPlayer] & (i << 1)) != 0) {
				for(int adjacentPosition : ADJACENT_POSITIONS[i]) {
					if(((board[WHITE] | board[BLACK]) & (1 << adjacentPosition)) == 0) {
						result--;
					}
				}
			}
		}
		
		return result;
	}
	
	private int intersectionAdvantage() {
		int result = 0;
		byte opponentPlayer = playerToMove == WHITE ? BLACK : WHITE;
		
		result += Integer.bitCount(board[playerToMove] & THREE_WAY_INTERSECTIONS_BB) * THREE_WAY_INTERSECTION_VALUE +
				Integer.bitCount(board[playerToMove] & FOUR_WAY_INTERSECTIONS_BB) * FOUR_WAY_INTERSECTION_VALUE;
		
//		result -= Integer.bitCount(board[opponentPlayer] & THREE_WAY_INTERSECTIONS_BB) * THREE_WAY_INTERSECTION_VALUE +
//				Integer.bitCount(board[opponentPlayer] & FOUR_WAY_INTERSECTIONS_BB) * FOUR_WAY_INTERSECTION_VALUE;
		
		return result;
	}
	
	private int rowAdvantage() {
		int result = 0;
		byte opponentPlayer = playerToMove == WHITE ? BLACK : WHITE;
		int playerRow;
		int opponentRow;
		/*
		 * 
		 * 
		 */
		int rowFreePositions;
		
		int mill;
		for(int i=0; i<MILLS.length; i++) {
			mill = MILLS[i];
			playerRow = board[playerToMove] & mill;
			opponentRow = board[opponentPlayer] & mill;
			
			if(playerRow > 0 && opponentRow == 0) {
				rowFreePositions = mill & ~playerRow;
				
				if(i >= SPOKES_IDX) {
					result += SPOKE_EXCLUSIVITY_VALUE;
				} else if(i >= MIDDLE_RING_IDX && i < INNER_RING_IDX) {
					result += MIDDLE_RING_EXCLUSIVITY_VALUE;
				}
				
				switch(Integer.bitCount(playerRow)) {
				case 3:
					result += MILL_VALUE;
					int mobility = 0;
					
					for(int j=0; j<24; j++) {
						if((playerRow & (1 << i)) != 0) {
							for(int adjacentPosition : ADJACENT_POSITIONS[j]) {
								if(((board[WHITE] | board[BLACK]) & (1 << adjacentPosition)) == 0) {
									mobility++;
								}
							}
						}
					}
					
					result += mobility << MILL_MOBILITY_EXPONENT;
					break;
				case 2:
					if(checkersToPut[playerToMove] > 0 || canSlideIntoRow(mill, rowFreePositions, playerRow, playerToMove)) {
						/*
						 * 
						 * 
						 */
						result += POTENTIAL_MILL_VALUE;
					}
//					break;
				case 1:
					result += ROW_EXCLUSIVITY_VALUE;
					break;
				}
				
			} else if(opponentRow > 0 && playerRow == 0) {
				rowFreePositions = mill & ~opponentRow;
				
				if(i >= SPOKES_IDX) {
					result -= SPOKE_EXCLUSIVITY_VALUE;
				} else if(i >= MIDDLE_RING_IDX && i < INNER_RING_IDX) {
					result -= MIDDLE_RING_EXCLUSIVITY_VALUE;
				}
				
				switch(Integer.bitCount(opponentRow)) {
				case 3:
					result -= MILL_VALUE;
					int mobility = 0;
					
					for(int j=0; j<24; j++) {
						if((opponentRow & (1 << i)) != 0) {
							for(int adjacentPosition : ADJACENT_POSITIONS[j]) {
								if(((board[WHITE] | board[BLACK]) & (1 << adjacentPosition)) == 0) {
									mobility++;
								}
							}
						}
					}
					
					result -= mobility << MILL_MOBILITY_EXPONENT;
					break;
				case 2:
					if(checkersToPut[opponentPlayer] > 0 || canSlideIntoRow(mill, rowFreePositions, opponentRow, opponentPlayer)) {
						/*
						 * 
						 * 
						 */
						result -= POTENTIAL_MILL_NEXT_TURN_VALUE;
					}
//					break;
				case 1:
					result -= ROW_EXCLUSIVITY_VALUE;
					break;
				}
			}
		}
		
		return result;
	}
	
	private boolean canSlideIntoRow(int mill, int rowFreePositions, int row, int player) {
		byte opponentPlayer = playerToMove == WHITE ? BLACK : WHITE;
		/*
		 * 
		 * 
		 */
		int notInRowPositions = ~row;
		
		for(int i=0; i<24; i++) {
			if((rowFreePositions & (1 << i)) == 0) {
				for(int adjacentPosition : ADJACENT_POSITIONS[i]) {
					if((board[player] & ((1 << adjacentPosition) & notInRowPositions)) != 0) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
}
