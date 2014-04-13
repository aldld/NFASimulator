package ca.ericbannatyne.nfa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 */
public class State {

	private NFA nfa;
	private String name;
	private Map<Character, Set<State>> transitions;

	/**
	 * 
	 * @param nfa
	 * @param name
	 * @param transitions
	 * @param isFinal
	 */
	public State(NFA nfa, String name, Map<Character, Set<State>> transitions,
			boolean isFinal) {
		this.nfa = nfa;
		this.name = name;
		this.transitions = new HashMap<Character, Set<State>>(transitions); // Deep
																			// copy
		// TODO: Check if transitions are valid
		// OR: enforce adding/removing individual transitions one at a time?
	}

	/**
	 * 
	 * @param nfa
	 * @param name
	 * @param isFinal
	 */
	public State(NFA nfa, String name, boolean isFinal) {
		this(nfa, name, new HashMap<Character, Set<State>>(), isFinal);
	}

	/**
	 * 
	 * @return
	 */
	public NFA getNFA() {
		return nfa;
	}

	/**
	 * 
	 * @param nfa
	 */
	public void setNFA(NFA nfa) {
		this.nfa = nfa;
	}

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(String name) {
		// TODO: Check that no other state in nfa has the same name
		this.name = name;
	}

	/**
	 * 
	 * @return
	 */
	public Map<Character, Set<State>> getTransitions() {
		return new HashMap<Character, Set<State>>(transitions);
	}

	/**
	 * 
	 * @param letter
	 * @return
	 */
	public Set<State> transition(Character letter) {
		return transitions.get(letter);
	}

	/**
	 * 
	 * @param transitions
	 */
	public void setTransitions(Map<Character, Set<State>> transitions) {
		// TODO: Check that all strings and states are valid for this nfa
		this.transitions = new HashMap<Character, Set<State>>(transitions); // Deep
																			// copy
	}

	/**
	 * 
	 * @param letter
	 * @param next
	 */
	public void addTransition(Character letter, State next) {
		// TODO: Check if letter is in the alphabet, and State is in the nfa
		if (transitions.containsKey(letter)) {
			transitions.get(letter).add(next);
		} else {
			Set<State> nextStates = new HashSet<State>();
			nextStates.add(next);
			transitions.put(letter, nextStates);
		}
	}

	/**
	 * 
	 * @param letter
	 * @param next
	 * @return true if the set of transitions from this state has been modified
	 *         as a result of this operation
	 */
	public boolean removeTransition(String letter, State next) {
		if (transitions.containsKey(letter)) {
			Set<State> nextStates = transitions.get(letter);
			boolean removed = nextStates.remove(next);

			if (nextStates.isEmpty()) {
				transitions.remove(letter);
			}

			return removed;
		} else {
			return false;
		}
	}

	/**
	 * The epsilon-closure of a state is the set of all states reachable from
	 * that state using only epsilon-transitions, including the state itself.
	 * 
	 * @return the epsilon-closure of this state
	 */
	public Set<State> epsilonClosure() {
		// TODO: Generate the epsilon closure of this state. Be sure to keep
		// track of state already visited
		// e.g. a private helper method that works recursively, with a set to
		// keep track of states already visited (BFS or DFS)
		return epsilonClosureSearch(new HashSet<State>());
	}

	/**
	 * Helper method for searching the NFA graph to find the epsilon-closure of
	 * a state.
	 * 
	 * @param visited
	 *            set of states visited so far in the search
	 * @return the epsilon-closure of this state
	 */
	private Set<State> epsilonClosureSearch(Set<State> visited) {
		// TODO
		return null;
	}

}
