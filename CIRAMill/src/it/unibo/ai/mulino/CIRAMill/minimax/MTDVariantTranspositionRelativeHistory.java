package it.unibo.ai.mulino.CIRAMill.minimax;

import java.util.Comparator;
import java.util.List;

public class MTDVariantTranspositionRelativeHistory implements IMinimax{
	private int expandedStates = 0;
	private int times = 0;
	private long elapsedTime;	
	private int firstGuess[];
	private int ttHits = 0;
	
	private ITieChecker tieChecker;	
	private ITranspositionTable transpositionTable;
	private IHistoryTable historyTable;
	private IHistoryTable butterflyTable;
	
	public MTDVariantTranspositionRelativeHistory(ITieChecker tieChecker, ITranspositionTable transpositionTable, IHistoryTable historyTable, IHistoryTable butterflyTable) {
		this.tieChecker = tieChecker;
		this.firstGuess = new int[]{0,0};
		this.transpositionTable = transpositionTable;
		this.historyTable = historyTable;
		this.butterflyTable = butterflyTable;
	}

	@Override
	public ValuedAction minimaxDecision(IState state, int maxDepth) {
		elapsedTime = System.currentTimeMillis();
		ValuedAction valuedAction = mtdf(state, firstGuess[0], maxDepth);
		elapsedTime = System.currentTimeMillis() - elapsedTime;
		System.out.println("MTD-f Variant Transposition Relative History:");
		System.out.println("Elapsed time: " + elapsedTime);
		System.out.println("Expanded states: " + expandedStates);
		System.out.println("Number of Alpha-Beta iterations: " + times);
		System.out.println("First guess: " + firstGuess[0]);
		System.out.println("TT Hits: " + ttHits);
		System.out.println("Selected action is: " + valuedAction);
		expandedStates = 0;
		times = 0;
		ttHits = 0;
		firstGuess[0] = firstGuess[1];
		firstGuess[1] = valuedAction.getValue();
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
	/*	AlfaBeta Transposition Relative History	*/
	private ValuedAction max(IState state, int maxDepth, int alpha, int beta) {
		ValuedAction result = new ValuedAction(null, Integer.MIN_VALUE);
		ValuedAction temp = new ValuedAction();
		
		//Tranposition Handling
		IAction[] transpActions = transpositionTable.getActions(state);
		IAction action;
		if (transpActions.length > 0)
			ttHits++;
		for (int i=0; i<transpActions.length; i++) {
			expandedStates++;
			action = transpActions[i];
			state.move(action);
//			BitBoardState newState = (BitBoardState) state.applyMove(a);
			if (state.isWinningState()) {
				result.set(action, Integer.MAX_VALUE-1);
				state.unmove(action);
				transpositionTable.putAction(state, action, maxDepth);
				historyTable.incrementValue(action, maxDepth);
				return result;
			} else if (tieChecker.isTie(state)) {
				temp.set(action, 0);
			} else if (maxDepth > 1) {
//				state.move(a);
				temp = min(state, maxDepth - 1, alpha, beta);
//				state.unmove(a);
			} else {
				temp.set(action, -state.getHeuristicEvaluation());
			}
			
			if (temp.getValue() > result.getValue()) {
				result.set(action, temp.getValue());
			}
			if (result.getValue() >= beta) {
				state.unmove(action);
				transpositionTable.putAction(state, action, maxDepth);
				historyTable.incrementValue(result.getAction(), maxDepth);
				return result;
			} else {
				butterflyTable.incrementValue(result.getAction(), maxDepth);
			}
			if(result.getValue() >= alpha) {
				alpha = result.getValue();
			}
			
			state.unmove(action);
		}

		List<IAction> actions = state.getFollowingMoves();
		for (int i=0; i<transpActions.length; i++) {
			actions.remove(transpActions[i]);
		}
		
		actions.sort(new ActionComparator());
		
		for (IAction a : actions) {
			expandedStates++;
			state.move(a);
//			BitBoardState newState = (BitBoardState) state.applyMove(a);
			if (state.isWinningState()) {
				result.set(a, Integer.MAX_VALUE-1);
				state.unmove(a);
				transpositionTable.putAction(state, a, maxDepth);
				historyTable.incrementValue(a, maxDepth);
				return result;
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
				transpositionTable.putAction(state, a, maxDepth);
				historyTable.incrementValue(result.getAction(), maxDepth);
				return result;
			} else {
				butterflyTable.incrementValue(result.getAction(), maxDepth);
			}
			if(result.getValue() >= alpha) {
				alpha = result.getValue();
			}
			
			state.unmove(a);
		}
		
		if (result.getAction() != null) {
			transpositionTable.putAction(state, result.getAction(), maxDepth);
			historyTable.incrementValue(result.getAction(), maxDepth);
		}
		return result;
	}
	
	private ValuedAction min(IState state, int maxDepth, int alpha, int beta) {
		ValuedAction result = new ValuedAction(null, Integer.MAX_VALUE);
		ValuedAction temp = new ValuedAction();

		//Tranposition Handling
		IAction[] transpActions = transpositionTable.getActions(state);
		IAction action;
		if (transpActions.length > 0)
			ttHits++;
		for (int i=0; i<transpActions.length; i++) {
			expandedStates++;
			action = transpActions[i];
			state.move(action);
//			BitBoardState newState = (BitBoardState) state.applyMove(a);
			if (state.isWinningState()) {
				result.set(action, Integer.MIN_VALUE+1);
				state.unmove(action);
				transpositionTable.putAction(state, action, maxDepth);
				historyTable.incrementValue(action, maxDepth);
				return result;
			} else if (tieChecker.isTie(state)) {
				temp.set(action, 0);
			} else if (maxDepth > 1) {
//				state.move(a);
				temp = max(state, maxDepth - 1, alpha, beta);
//				state.unmove(a);
			} else {
				temp.set(action, state.getHeuristicEvaluation());
			}
			if (temp.getValue() < result.getValue()) {
				result.set(action, temp.getValue());
			}
			if (result.getValue() <= alpha) {
				state.unmove(action);
				transpositionTable.putAction(state, action, maxDepth);
				historyTable.incrementValue(result.getAction(), maxDepth);
				return result;
			} else {
				butterflyTable.incrementValue(result.getAction(), maxDepth);
			}
			if(result.getValue() <= beta) {
				beta = result.getValue();
			}
			
			state.unmove(action);
		}
		
		List<IAction> actions = state.getFollowingMoves();
		for (int i=0; i<transpActions.length; i++) {
			actions.remove(transpActions[i]);
		}
		
		actions.sort(new ActionComparator());
		
		for (IAction a : actions) {
			expandedStates++;
			state.move(a);
//			BitBoardState newState = (BitBoardState) state.applyMove(a);
			if (state.isWinningState()) {
				result.set(a, Integer.MIN_VALUE+1);
				state.unmove(a);
				transpositionTable.putAction(state, a, maxDepth);
				historyTable.incrementValue(a, maxDepth);
				return result;
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
				transpositionTable.putAction(state, a, maxDepth);
				historyTable.incrementValue(result.getAction(), maxDepth);
				return result;
			} else {
				butterflyTable.incrementValue(result.getAction(), maxDepth);
			}
			if(result.getValue() <= beta) {
				beta = result.getValue();
			}
			
			state.unmove(a);
		}
		
		if (result.getAction() != null) {
			transpositionTable.putAction(state, result.getAction(), maxDepth);
			historyTable.incrementValue(result.getAction(), maxDepth);
		}
		return result;
	}
	
	class ActionComparator implements Comparator<IAction> {
		
		@Override
		public int compare(IAction arg0, IAction arg1) {			
			return historyTable.getValue(arg0) / butterflyTable.getValue(arg0) == historyTable.getValue(arg1) / butterflyTable.getValue(arg1) ? 0 : historyTable.getValue(arg0) / butterflyTable.getValue(arg0) > historyTable.getValue(arg1) / butterflyTable.getValue(arg1) ? -1 : 1;
		}
		
	}
}
