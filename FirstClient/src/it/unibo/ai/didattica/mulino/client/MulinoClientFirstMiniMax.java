package it.unibo.ai.didattica.mulino.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;

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
import it.unibo.ai.didattica.mulino.domain.ValuedAction;

public class MulinoClientFirstMiniMax extends MulinoClient {

	public MulinoClientFirstMiniMax(Checker player) throws UnknownHostException, IOException {
		super(player);
	}
	
	public static Checker player;
	private static int expandedStates;
	private static long elapsedTime;
	public static final int MAXDEPTH = 1;
//	public static final int THREAD_NUMBER = 4;
	public static void main(String[] args) throws Exception {
		MulinoClient mulinoClient = null;
		if (args[0].toLowerCase().equals("white"))
			mulinoClient = new MulinoClientFirstMiniMax(Checker.WHITE);
		else if (args[0].toLowerCase().equals("black"))
			mulinoClient = new MulinoClientFirstMiniMax(Checker.BLACK);
		else
			System.exit(-2);
		player = mulinoClient.getPlayer();
		State currentState = null;
		
		
		do {
			expandedStates = 0;
			
			if (player == Checker.WHITE) {
				currentState = mulinoClient.read();
				System.out.println("Current state is:");
				System.out.println(currentState.toString());
				
				Action a = minimaxDecision(currentState, MAXDEPTH);
				
				mulinoClient.write(a);
				
				currentState = mulinoClient.read();
				System.out.println("New state is:");
				System.out.println(currentState.toString());
				
				System.out.println("\nWaiting for opponent move...");
				
				
			} else if (player == Checker.BLACK) {
				currentState = mulinoClient.read();
				System.out.println("New state is:");
				System.out.println(currentState.toString());
				
				System.out.println("\nWaiting for opponent move...");
				
				currentState = mulinoClient.read();
				System.out.println("Current state is:");
				System.out.println(currentState.toString());
				
				Action a = minimaxDecision(currentState, MAXDEPTH);
				
				mulinoClient.write(a);
			} else {
				System.out.println("Wrong checker");
				System.exit(-1);
			}
			
			
		} while(currentState.getBlackCheckers() > 3 && currentState.getWhiteCheckers() > 3);
		
	}
	
	public static Action minimaxDecision(State state, int maxDepth) throws Exception {
		elapsedTime = System.currentTimeMillis();
		ValuedAction a = max(state, maxDepth);
		elapsedTime = System.currentTimeMillis() - elapsedTime;
		System.out.println("Elapsed time: " + elapsedTime);
		System.out.println("Expanded states: " + expandedStates);
		System.out.println("Selected action is: " + a);
		return a.getAction();
	}
	
	public static ValuedAction max (State state, int maxDepth) throws Exception {
		HashMap<Action, State> successors = successors(state, player);
		ValuedAction result = new ValuedAction(null, Integer.MIN_VALUE);
		ValuedAction temp;
		State newState;
		
		for (Action a : successors.keySet()) {
			newState = successors.get(a);
			if (isWinningState(newState, player)) {
				result = new ValuedAction(a, Integer.MAX_VALUE);
				return result;
			}
			if (maxDepth > 1)
				temp = min(newState, maxDepth-1);
			else {
				switch(state.getCurrentPhase()) {
				case FIRST: Phase1Action action1 = (Phase1Action) a; 
							temp = new ValuedAction(a, heuristic(newState, action1.getPutPosition(), player)); 
							break;
				case SECOND: Phase2Action action2 = (Phase2Action) a; 
							temp = new ValuedAction(a, heuristic(newState, action2.getTo(), player)); 
							break;
				case FINAL: PhaseFinalAction actionFinal = (PhaseFinalAction) a; 
							temp = new ValuedAction(a, heuristic(newState, actionFinal.getTo(), player)); 
							break;
				default: throw new Exception("Illegal Phase");
				}
			}
			if (temp.getValue() > result.getValue()) {
				result = new ValuedAction(a, temp.getValue());
			}
		}
		return result;
	}
	
	public static ValuedAction min (State state, int maxDepth) throws Exception {
		Checker minPlayer = player==Checker.BLACK ? Checker.WHITE : Checker.BLACK;
		HashMap<Action, State> successors = successors(state, minPlayer);
		ValuedAction result = new ValuedAction(null, Integer.MAX_VALUE);
		ValuedAction temp;
		State newState;
		
		for (Action a : successors.keySet()) {
			newState = successors.get(a);
			if (isWinningState(newState, minPlayer)) {
				result = new ValuedAction(a, Integer.MIN_VALUE);
				return result;
			}
			if (maxDepth > 1) {
				temp = max(newState, maxDepth-1);
			}
			else {
				switch(state.getCurrentPhase()) {
				case FIRST: Phase1Action action1 = (Phase1Action) a; 
							temp = new ValuedAction(a, heuristic(newState, action1.getPutPosition(), player)); 
							break;
				case SECOND: Phase2Action action2 = (Phase2Action) a; 
							temp = new ValuedAction(a, heuristic(newState, action2.getTo(), player)); 
							break;
				case FINAL: PhaseFinalAction actionFinal = (PhaseFinalAction) a; 
							temp = new ValuedAction(a, heuristic(newState, actionFinal.getTo(), player)); 
							break;
				default: throw new Exception("Illegal Phase");
				}
			}
			if (temp.getValue() < result.getValue()) {
				result = new ValuedAction(a, temp.getValue());
			}
		}
		return result;
	}
		
	public static HashMap<Action, State> successors(State state, Checker p) throws Exception {
		switch(state.getCurrentPhase()) {
		case FIRST: return successorsFirst(state, p); 
		case SECOND: return successorsSecond(state, p);
		case FINAL: return successorsFinalOrSecond(state, p);
		default: throw new Exception("Illegal Phase");
		}
	}
	
	public static HashMap<Action, State> successorsFirst(State state, Checker p) {
		HashMap<Action, State> result = new HashMap<Action, State>();
		Phase1Action temp;
		State newState;
		HashMap<String, Checker> board = state.getBoard();
		State.Checker otherChecker = p==Checker.WHITE ? Checker.BLACK : Checker.WHITE;
		
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
							if (board.get(otherPosition) == otherChecker && !Util.hasCompletedTriple(newState, otherPosition, otherChecker)) {
								temp.setRemoveOpponentChecker(otherPosition);
								newState = Phase1.applyMove(state, temp, p);
								result.put(temp, newState);
								expandedStates++;
								foundRemovableChecker = true;
							}
						}
						if (!foundRemovableChecker) {
							for (String otherPosition : state.positions) {
								if (board.get(otherPosition) == otherChecker && Util.hasCompletedTriple(newState, otherPosition, otherChecker)) {
									temp.setRemoveOpponentChecker(otherPosition);
									newState = Phase1.applyMove(state, temp, p);
									result.put(temp, newState);
									expandedStates++;
								}
							}
						}
					} else {
						newState = Phase1.applyMove(state, temp, p);
						result.put(temp, newState);
						expandedStates++;
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
	
	public static HashMap<Action, State> successorsSecond(State state, Checker p) {
		HashMap<Action, State> result = new HashMap<Action, State>();
		Phase2Action temp;
		State newState;
		HashMap<String, Checker> board = state.getBoard();
		State.Checker otherChecker = p==Checker.WHITE ? Checker.BLACK : Checker.WHITE;
		
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
										if (board.get(otherPosition) == otherChecker && !Util.hasCompletedTriple(newState, otherPosition, otherChecker)) {
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
											expandedStates++;
											foundRemovableChecker = true;
										}
									}
									if (!foundRemovableChecker) {
										for (String otherPosition : state.positions) {
											if (board.get(otherPosition) == otherChecker && Util.hasCompletedTriple(newState, otherPosition, otherChecker)) {
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
												expandedStates++;
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
									expandedStates++;
								}
							} catch (WrongPhaseException | PositionNotEmptyException | NullCheckerException
									| WrongPositionException | TryingToRemoveOwnCheckerException
									| TryingToRemoveEmptyCheckerException | NullStateException
									| TryingToRemoveCheckerInTripleException | NullActionException | TryingToMoveOpponentCheckerException | FromAndToAreEqualsException | FromAndToAreNotConnectedException e) {
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

	public static HashMap<Action, State> successorsFinalOrSecond(State state, Checker p) {
		if (p == Checker.WHITE) {
			if (state.getWhiteCheckersOnBoard() > 3)
				return successorsSecond(state, p);
			else {
				return successorsFinal(state, p);
			}
		}
		//Player is BLACK
		else {
			if (state.getBlackCheckersOnBoard() > 3)
				return successorsSecond(state, p);
			else {
				return successorsFinal(state, p);
			}
		}
	}
	
	public static HashMap<Action, State> successorsFinal(State state, Checker p) {
		HashMap<Action, State> result = new HashMap<Action, State>();
		PhaseFinalAction temp;
		State newState;
		HashMap<String, Checker> board = state.getBoard();
		State.Checker otherChecker = p==Checker.WHITE ? Checker.BLACK : Checker.WHITE;
		
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
									if (board.get(otherPosition) == otherChecker && !Util.hasCompletedTriple(newState, otherPosition, otherChecker)) {
										temp.setRemoveOpponentChecker(otherPosition);
										newState = PhaseFinal.applyMove(state, temp, p);
										result.put(temp, newState);
										expandedStates++;
										foundRemovableChecker = true;
									}
								}
								if (!foundRemovableChecker) {
									for (String otherPosition : state.positions) {
										if (board.get(otherPosition) == otherChecker && Util.hasCompletedTriple(newState, otherPosition, otherChecker)) {
											temp.setRemoveOpponentChecker(otherPosition);
											newState = PhaseFinal.applyMove(state, temp, p);
											result.put(temp, newState);
											expandedStates++;
										}
									}
								}
							} else {
								newState = PhaseFinal.applyMove(state, temp, p);
								result.put(temp, newState);
								expandedStates++;
							}
						} catch (WrongPhaseException | PositionNotEmptyException | NullCheckerException
								| WrongPositionException | TryingToRemoveOwnCheckerException
								| TryingToRemoveEmptyCheckerException | NullStateException
								| TryingToRemoveCheckerInTripleException | NullActionException | TryingToMoveOpponentCheckerException | FromAndToAreEqualsException | FromAndToAreNotConnectedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				}
			}
		}
		
		return result;
	}
	
	public static int heuristic (State state, String position, Checker p) throws Exception {
		switch(state.getCurrentPhase()) {
		case FIRST: return heuristicPhase1(state, position, p);
		case SECOND: return heuristicPhase2(state, position, p);
		case FINAL: return heuristicPhaseFinalOr2(state, position, p);
		default: throw new Exception("Illegal Phase");
		}
	}

	private static int heuristicPhaseFinalOr2(State state, String position, Checker p) {
		if (p == Checker.WHITE) {
			if (state.getWhiteCheckersOnBoard() > 3)
				return heuristicPhase2(state, position, p);
			else {
				return heuristicPhaseFinal(state, position, p);
			}
		}
		//Player is BLACK
		else {
			if (state.getBlackCheckersOnBoard() > 3)
				return heuristicPhase2(state, position, p);
			else {
				return heuristicPhaseFinal(state, position, p);
			}
		}
	}

	private static int heuristicPhaseFinal(State state, String position, Checker p) {
		int result = 0;
		Checker otherPlayer = player==Checker.WHITE ? Checker.BLACK : Checker.WHITE;
		
		// number of 2 pieces configuration
				int twoPiecesConfigurationPlayer = 0;
				int twoPiecesConfigurationOtherPlayer = 0;
				for(String pos : state.positions) {
					if(state.getBoard().get(pos) == player) {
						int emptyPosH = 0;
						int occupiedPosH = 0;

						String[] hSet = Util.getHSet(pos);
						
						for(String hPos : hSet) {
							if(state.getBoard().get(hPos) == player)
								occupiedPosH++;
							if(state.getBoard().get(hPos) == Checker.EMPTY)
								emptyPosH++;
						}
						
						if(emptyPosH == 1 && occupiedPosH == 2)
							twoPiecesConfigurationPlayer++;
						
						int emptyPosV = 0;
						int occupiedPosV = 0;
						
						String[] vSet = Util.getVSet(pos);
						
						for(String vPos : vSet) {
							if(state.getBoard().get(vPos) == player)
								occupiedPosV++;
							if(state.getBoard().get(vPos) == Checker.EMPTY)
								emptyPosV++;
						}
						
						if(emptyPosV == 1 && occupiedPosV == 2)
							twoPiecesConfigurationPlayer++;
					}
					
					if(state.getBoard().get(pos) == otherPlayer) {
						int emptyPosH = 0;
						int occupiedPosH = 0;

						String[] hSet = Util.getHSet(pos);
						
						for(String hPos : hSet) {
							if(state.getBoard().get(hPos) == otherPlayer)
								occupiedPosH++;
							if(state.getBoard().get(hPos) == Checker.EMPTY)
								emptyPosH++;
						}
						
						if(emptyPosH == 1 && occupiedPosH == 2)
							twoPiecesConfigurationOtherPlayer++;
						
						int emptyPosV = 0;
						int occupiedPosV = 0;
						
						String[] vSet = Util.getVSet(pos);
						
						for(String vPos : vSet) {
							if(state.getBoard().get(vPos) == otherPlayer)
								occupiedPosV++;
							if(state.getBoard().get(vPos) == Checker.EMPTY)
								emptyPosV++;
						}
						
						if(emptyPosV == 1 && occupiedPosV == 2)
							twoPiecesConfigurationOtherPlayer++;
					}
				}
				result += 10*(twoPiecesConfigurationPlayer/2);
				result -= 10*(twoPiecesConfigurationOtherPlayer/2); 
				
				// number of 3 pieces configuration
				int threePiecesConfigurationPlayer = 0;
				int threePiecesConfigurationOtherPlayer = 0;
				for(String pos : state.positions) {
					if(state.getBoard().get(pos) == player) {
						int emptyPosH = 0;
						int occupiedPosH = 0;

						String[] hSet = Util.getHSet(pos);
						
						for(String hPos : hSet) {
							if(state.getBoard().get(hPos) == player)
								occupiedPosH++;
							if(state.getBoard().get(hPos) == Checker.EMPTY)
								emptyPosH++;
						}
						
						int emptyPosV = 0;
						int occupiedPosV = 0;
						
						String[] vSet = Util.getVSet(pos);
						
						for(String vPos : vSet) {
							if(state.getBoard().get(vPos) == player)
								occupiedPosV++;
							if(state.getBoard().get(vPos) == Checker.EMPTY)
								emptyPosV++;
						}
						
						if(emptyPosH == 1 && occupiedPosH == 2 && emptyPosV == 1 && occupiedPosV == 2)
							threePiecesConfigurationPlayer++;
					}
					
					if(state.getBoard().get(pos) == otherPlayer) {
						int emptyPosH = 0;
						int occupiedPosH = 0;

						String[] hSet = Util.getHSet(pos);
						
						for(String hPos : hSet) {
							if(state.getBoard().get(hPos) == otherPlayer)
								occupiedPosH++;
							if(state.getBoard().get(hPos) == Checker.EMPTY)
								emptyPosH++;
						}
						
						int emptyPosV = 0;
						int occupiedPosV = 0;
						
						String[] vSet = Util.getVSet(pos);
						
						for(String vPos : vSet) {
							if(state.getBoard().get(vPos) == otherPlayer)
								occupiedPosV++;
							if(state.getBoard().get(vPos) == Checker.EMPTY)
								emptyPosV++;
						}
						
						if(emptyPosH == 1 && occupiedPosH == 2 && emptyPosV == 1 && occupiedPosV == 2)
							threePiecesConfigurationOtherPlayer++;
					}
				}
				result += threePiecesConfigurationPlayer;
				result -= threePiecesConfigurationOtherPlayer;

		
		// closed morris
		if(p == player) {
			if (Util.hasCompletedTriple(state, position, p)) {
				result += 16;
			}
		} else { // other player
			if (Util.hasCompletedTriple(state, position, p)) {
			result -= 16;
			}
		}
		
		
		return result;
	}

	private static int heuristicPhase2(State state, String position, Checker p) {
		int result = 0;
		Checker otherPlayer = player==Checker.WHITE ? Checker.BLACK : Checker.WHITE;
		
		// closed morris
		if(p == player) {
			if (Util.hasCompletedTriple(state, position, p)) {
				result += 14;
			}
		} else { // other player
			if (Util.hasCompletedTriple(state, position, p)) {
			result -= 14;
			}
		}
		
		// morrises number
		int morrisClosed3player = 0;
		int morrisClosed3OtherPlayer = 0;
		for (String pos : state.positions) {
				if (state.getBoard().get(pos) == player) {
				if (Util.hasCompletedTriple(state, pos, player)) {
					morrisClosed3player++;
				}
			}
			
			if (state.getBoard().get(pos) == otherPlayer) {
				if (Util.hasCompletedTriple(state, pos, otherPlayer)) {
					morrisClosed3OtherPlayer++;
				}
			}
		}
		result += 43*(morrisClosed3player/3);
		result -= 43*(morrisClosed3OtherPlayer/3);
		
		// number of blocked oppenent pieces
		int blockedPiecesPlayer = 0;
		int blockedPiecesOtherPlayer = 0;
		for (String pos : state.positions) {
			if (state.getBoard().get(pos) == otherPlayer) {
				boolean isBlocked = true;
				try {
					for (String adjPos : Util.getAdiacentTiles(pos)) {
						if (state.getBoard().get(adjPos) == Checker.EMPTY) {
							isBlocked = false;
						}
					}
				} catch (WrongPositionException e) {
					e.printStackTrace();
				}
				if (isBlocked) {
					blockedPiecesPlayer++;
				}
			}
			
			if (state.getBoard().get(pos) == player) {
				boolean isBlocked = true;
				try {
					for (String adjPos : Util.getAdiacentTiles(pos)) {
						if (state.getBoard().get(adjPos) == Checker.EMPTY) {
							isBlocked = false;
						}
					}
				} catch (WrongPositionException e) {
					e.printStackTrace();
				}
				if (isBlocked) {
					blockedPiecesOtherPlayer++;
				}
			}
		}
		result += 10*blockedPiecesPlayer;
		result -= 10*blockedPiecesOtherPlayer;
		
		// pieces number
		if (p == Checker.WHITE)
			result += 8*(state.getWhiteCheckersOnBoard() - state.getBlackCheckersOnBoard());
		else 
			result += 8*(state.getBlackCheckersOnBoard() - state.getBlackCheckersOnBoard());

		// opened morris (che cazzo �???)
			
		// double morris
		int doubleMorrisPlayer = 0;
		int doubleMorrisOtherPlayer = 0;
		for(String pos : state.positions) {
			if(state.getBoard().get(pos) == player) {
				if(Util.isInHTriple(state, pos) && Util.isInVTriple(state, pos))
					doubleMorrisPlayer++;
			}
			
			if(state.getBoard().get(pos) == otherPlayer) {
				if(Util.isInHTriple(state, pos) && Util.isInVTriple(state, pos))
					doubleMorrisOtherPlayer++;
				
			}
		}
		result += 42*doubleMorrisPlayer;
		result -= 42*doubleMorrisOtherPlayer;
		
		return result;
	}

	private static int heuristicPhase1(State state, String position, Checker p) {
		int result=0;
		Checker otherPlayer = player==Checker.WHITE ? Checker.BLACK : Checker.WHITE;
		
		// closed morris
		if(p == player) {
			if (Util.hasCompletedTriple(state, position, p)) {
				result += 18;
			}
		} else { // other player
			if (Util.hasCompletedTriple(state, position, p)) {
				result -= 18;
			}
		}
		
		// morrises number
		int morrisClosed3player = 0;
		int morrisClosed3OtherPlayer = 0;
		for (String pos : state.positions) {
			if (state.getBoard().get(pos) == player) {
				if (Util.hasCompletedTriple(state, pos, player)) {
					morrisClosed3player++;
				}
			}
			
			if (state.getBoard().get(pos) == otherPlayer) {
				if (Util.hasCompletedTriple(state, pos, otherPlayer)) {
					morrisClosed3OtherPlayer++;
				}
			}
		}
		result += 26*(morrisClosed3player/3);
		result -= 26*(morrisClosed3OtherPlayer/3);
		
		// number of blocked oppenent pieces
		int blockedPiecesPlayer = 0;
		int blockedPiecesOtherPlayer = 0;
		for (String pos : state.positions) {
			if (state.getBoard().get(pos) == otherPlayer) {
				boolean isBlocked = true;
				try {
					for (String adjPos : Util.getAdiacentTiles(pos)) {
						if (state.getBoard().get(adjPos) == Checker.EMPTY) {
							isBlocked = false;
						}
					}
				} catch (WrongPositionException e) {
					e.printStackTrace();
				}
				if (isBlocked) {
					blockedPiecesPlayer++;
				}
			}
			
			if (state.getBoard().get(pos) == player) {
				boolean isBlocked = true;
				try {
					for (String adjPos : Util.getAdiacentTiles(pos)) {
						if (state.getBoard().get(adjPos) == Checker.EMPTY) {
							isBlocked = false;
						}
					}
				} catch (WrongPositionException e) {
					e.printStackTrace();
				}
				if (isBlocked) {
					blockedPiecesOtherPlayer++;
				}
			}
		}
		result += blockedPiecesPlayer;
		result -= blockedPiecesOtherPlayer;
		
		// pieces number
		if (p == Checker.WHITE)
			result += 6*(state.getWhiteCheckersOnBoard() - state.getBlackCheckersOnBoard());
		else 
			result += 6*(state.getBlackCheckersOnBoard() - state.getBlackCheckersOnBoard());
		
		
		// number of 2 pieces configuration
		int twoPiecesConfigurationPlayer = 0;
		int twoPiecesConfigurationOtherPlayer = 0;
		for(String pos : state.positions) {
			if(state.getBoard().get(pos) == player) {
				int emptyPosH = 0;
				int occupiedPosH = 0;

				String[] hSet = Util.getHSet(pos);
				
				for(String hPos : hSet) {
					if(state.getBoard().get(hPos) == player)
						occupiedPosH++;
					if(state.getBoard().get(hPos) == Checker.EMPTY)
						emptyPosH++;
				}
				
				if(emptyPosH == 1 && occupiedPosH == 2)
					twoPiecesConfigurationPlayer++;
				
				int emptyPosV = 0;
				int occupiedPosV = 0;
				
				String[] vSet = Util.getVSet(pos);
				
				for(String vPos : vSet) {
					if(state.getBoard().get(vPos) == player)
						occupiedPosV++;
					if(state.getBoard().get(vPos) == Checker.EMPTY)
						emptyPosV++;
				}
				
				if(emptyPosV == 1 && occupiedPosV == 2)
					twoPiecesConfigurationPlayer++;
			}
			
			if(state.getBoard().get(pos) == otherPlayer) {
				int emptyPosH = 0;
				int occupiedPosH = 0;

				String[] hSet = Util.getHSet(pos);
				
				for(String hPos : hSet) {
					if(state.getBoard().get(hPos) == otherPlayer)
						occupiedPosH++;
					if(state.getBoard().get(hPos) == Checker.EMPTY)
						emptyPosH++;
				}
				
				if(emptyPosH == 1 && occupiedPosH == 2)
					twoPiecesConfigurationOtherPlayer++;
				
				int emptyPosV = 0;
				int occupiedPosV = 0;
				
				String[] vSet = Util.getVSet(pos);
				
				for(String vPos : vSet) {
					if(state.getBoard().get(vPos) == otherPlayer)
						occupiedPosV++;
					if(state.getBoard().get(vPos) == Checker.EMPTY)
						emptyPosV++;
				}
				
				if(emptyPosV == 1 && occupiedPosV == 2)
					twoPiecesConfigurationOtherPlayer++;
			}
		}
		result += 12*(twoPiecesConfigurationPlayer/2);
		result -= 12*(twoPiecesConfigurationOtherPlayer/2); 
		
		// number of 3 pieces configuration
		int threePiecesConfigurationPlayer = 0;
		int threePiecesConfigurationOtherPlayer = 0;
		for(String pos : state.positions) {
			if(state.getBoard().get(pos) == player) {
				int emptyPosH = 0;
				int occupiedPosH = 0;

				String[] hSet = Util.getHSet(pos);
				
				for(String hPos : hSet) {
					if(state.getBoard().get(hPos) == player)
						occupiedPosH++;
					if(state.getBoard().get(hPos) == Checker.EMPTY)
						emptyPosH++;
				}
				
				int emptyPosV = 0;
				int occupiedPosV = 0;
				
				String[] vSet = Util.getVSet(pos);
				
				for(String vPos : vSet) {
					if(state.getBoard().get(vPos) == player)
						occupiedPosV++;
					if(state.getBoard().get(vPos) == Checker.EMPTY)
						emptyPosV++;
				}
				
				if(emptyPosH == 1 && occupiedPosH == 2 && emptyPosV == 1 && occupiedPosV == 2)
					threePiecesConfigurationPlayer++;
			}
			
			if(state.getBoard().get(pos) == otherPlayer) {
				int emptyPosH = 0;
				int occupiedPosH = 0;

				String[] hSet = Util.getHSet(pos);
				
				for(String hPos : hSet) {
					if(state.getBoard().get(hPos) == otherPlayer)
						occupiedPosH++;
					if(state.getBoard().get(hPos) == Checker.EMPTY)
						emptyPosH++;
				}
				
				int emptyPosV = 0;
				int occupiedPosV = 0;
				
				String[] vSet = Util.getVSet(pos);
				
				for(String vPos : vSet) {
					if(state.getBoard().get(vPos) == otherPlayer)
						occupiedPosV++;
					if(state.getBoard().get(vPos) == Checker.EMPTY)
						emptyPosV++;
				}
				
				if(emptyPosH == 1 && occupiedPosH == 2 && emptyPosV == 1 && occupiedPosV == 2)
					threePiecesConfigurationOtherPlayer++;
			}
		}
		result += 7*threePiecesConfigurationPlayer;
		result -= 7*threePiecesConfigurationOtherPlayer;
			
		return result;
	}
	
	private static boolean isWinningState(State state, Checker p) {
		if (state.getCurrentPhase() == State.Phase.FIRST)
			return false;
		if (p == Checker.WHITE && state.getBlackCheckersOnBoard() < 3)
			return true;
		else if (p == Checker.BLACK && state.getWhiteCheckersOnBoard() < 3)
			return true;
		
		Checker otherPlayer = player==Checker.WHITE ? Checker.BLACK : Checker.WHITE;
		for (String position : state.positions) {
			if (state.getBoard().get(position) == otherPlayer) {
				boolean isBlocked = true;
				try {
					for (String adjPos : Util.getAdiacentTiles(position)) {
						if (state.getBoard().get(adjPos) == Checker.EMPTY) {
							isBlocked = false;
							break;
						}
					}
				} catch (WrongPositionException e) {
					e.printStackTrace();
				}
				if (!isBlocked) {
					return false;
				}
			}
		}
		return true;
	}

}