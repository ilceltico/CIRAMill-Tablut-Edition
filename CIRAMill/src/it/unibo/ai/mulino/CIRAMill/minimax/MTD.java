package it.unibo.ai.mulino.CIRAMill.minimax;

import java.util.List;

public class MTD implements IMinimax{
	
	private int expandedStates = 0;
	private int times = 0;
	private long elapsedTime;
	private ITieChecker tieChecker;
	private int firstGuess;
	
	public MTD(ITieChecker tieChecker) {
		this.tieChecker = tieChecker;
		this.firstGuess = 0;
	}

	@Override
	public ValuedAction minimaxDecision(IState state, int maxDepth) {
		elapsedTime = System.currentTimeMillis();
		ValuedAction valuedAction = mtdf(state, firstGuess, maxDepth);
		elapsedTime = System.currentTimeMillis() - elapsedTime;
		System.out.println("MTD-f:");
		System.out.println("Elapsed time: " + elapsedTime);
		System.out.println("Expanded states: " + expandedStates);
		System.out.println("Number of Alpha-Beta iterations: " + times);
		System.out.println("First guess: " + firstGuess);
		System.out.println("Selected action is: " + valuedAction);
		expandedStates = 0;
		times = 0;
		firstGuess = valuedAction.getValue();
		return valuedAction;
	}
	
	private ValuedAction mtdf(IState state, int firstGuess, int maxDepth) {
		ValuedAction result = new ValuedAction(null, firstGuess);
		int beta;
		int upperBound = Integer.MAX_VALUE;
		int lowerBound = Integer.MIN_VALUE;
		do {
			times++;
			
			if(result.getValue() == lowerBound)
				beta = result.getValue() + 1;
			else
				beta = result.getValue();
			result = max(state, maxDepth, beta-1, beta);
			if(result.getValue() < beta)
				upperBound = result.getValue();
			else
				lowerBound = result.getValue();
			
		} while(lowerBound < upperBound);
		
		return result;
	}
	/*	AlfaBeta	*/
	private ValuedAction max(IState state, int maxDepth, int alpha, int beta) {
		List<IAction> actions = state.getFollowingMoves();
		ValuedAction result = new ValuedAction(null, Integer.MIN_VALUE);
		ValuedAction temp = new ValuedAction();

		for (IAction a : actions) {
			expandedStates++;
			state.move(a);
//			BitBoardState newState = (BitBoardState) state.applyMove(a);
			if (state.isWinningState()) {
				result.set(a, Integer.MAX_VALUE-1);
				state.unmove(a);
				break;
			} else if (tieChecker.isTie(state)) {
				temp.set(a, 0);
			} else if (maxDepth > 1) {
//				state.move(a);
				temp = min(state, maxDepth - 1, alpha, beta);
//				state.unmove(a);
			} else {
				temp.set(a, -state.getHeuristicEvaluation());
			}

			if (temp.getValue() > result.getValue()) {
				result.set(a, temp.getValue());
			}
			if (result.getValue() >= beta) {
				state.unmove(a);
				return result;
			}
			if(result.getValue() >= alpha) {
				alpha = result.getValue();
			}

			state.unmove(a);
		}

		return result;
	}

	private ValuedAction min(IState state, int maxDepth, int alpha, int beta) {
		List<IAction> actions = state.getFollowingMoves();
		ValuedAction result = new ValuedAction(null, Integer.MAX_VALUE);
		ValuedAction temp = new ValuedAction();

		for (IAction a : actions) {
			expandedStates++;
			state.move(a);
//			BitBoardState newState = (BitBoardState) state.applyMove(a);
			if (state.isWinningState()) {
				result.set(a, Integer.MIN_VALUE+1);
				state.unmove(a);
				break;
			} else if (tieChecker.isTie(state)) {
				temp.set(a, 0);
			} else if (maxDepth > 1) {
//				state.move(a);
				temp = max(state, maxDepth - 1, alpha, beta);
//				state.unmove(a);
			} else {
				temp.set(a, state.getHeuristicEvaluation());
			}
			if (temp.getValue() < result.getValue()) {
				result.set(a, temp.getValue());
			}
			if (result.getValue() <= alpha) {
				state.unmove(a);
				return result;
			}
			if(result.getValue() <= beta) {
				beta = result.getValue();
			}

			state.unmove(a);
		}

		return result;
	}
}
