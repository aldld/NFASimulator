package ca.ericbannatyne.nfa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A state of an NFA, including the transitions from this state to other states
 * of the associated NFA. A state may only have transitions via letters in the
 * associated NFA's alphabet and epsilon-transitions, and all transitions must
 * be to other states within the associated NFA.
 */
public class State {

	private NFA nfa;
	private String name;
	private Map<Character, Set<State>> transitions;

	/**
	 * Constructs a state associated with the given NFA with a specified
	 * transition function.
	 * 
	 * @param nfa
	 *            the associated NFA
	 * @param name
	 *            name of the state
	 * @param transitions
	 *            the transition function
	 */
	State(NFA nfa, String name, Map<Character, Set<State>> transitions) {
		this.nfa = nfa;
		this.name = name;
		this.transitions = new HashMap<Character, Set<State>>(transitions);
	}

	/**
	 * Constructs a state associated with the given NFA with a default empty
	 * transition function.
	 * 
	 * @param nfa
	 *            the associated NFA
	 * @param name
	 *            name of the state
	 */
	State(NFA nfa, String name) {
		this(nfa, name, new HashMap<Character, Set<State>>());
	}

	/**
	 * Gets a reference to the associated NFA.
	 * 
	 * @return reference to the associated NFA
	 */
	public NFA getNFA() {
		return nfa;
	}
	
	/**
	 * Gets the name of this state.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this state.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the entire transition function of this state.
	 * 
	 * @return the transition function
	 */
	public Map<Character, Set<State>> getTransitions() {
		return new HashMap<Character, Set<State>>(transitions);
	}

	/**
	 * Gets the set of resulting states accessible from this state via a single
	 * occurrence of the given letter.
	 * 
	 * @param letter
	 *            the letter
	 * @return the set of resulting states
	 */
	public Set<State> transition(Character letter) {
		return transitions.get(letter);
	}

	/**
	 * Sets the transition function of this state.
	 * 
	 * @param transitions
	 *            the transition function
	 */
	public void setTransitions(Map<Character, Set<State>> transitions) {
		// TODO: Check that all strings and states are valid for this nfa
		this.transitions = new HashMap<Character, Set<State>>(transitions);
	}

	/**
	 * Adds a transition from this state to another.
	 * 
	 * @param letter
	 *            the letter consumed by the transition
	 * @param next
	 *            the state reached by the transition
	 */
	public void addTransition(Character letter, State next) {
		// TODO: Check if letter is in the alphabet, and State is in the nfa
		if (!nfa.hasState(next))
			throw new UnknownStateException(); // TODO: Better description
			
		if (transitions.containsKey(letter)) {
			transitions.get(letter).add(next);
		} else {
			Set<State> nextStates = new HashSet<State>();
			nextStates.add(next);
			transitions.put(letter, nextStates);
		}
	}

	/**
	 * Removes the given transition from this state's transition function.
	 * 
	 * @param letter
	 *            the letter consumed by the transition
	 * @param next
	 *            the state reached by the transition
	 * @return true if the set of transitions from this state has been modified
	 *         as a result of this operation
	 */
	public boolean removeTransition(Character letter, State next) {
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
		visited.add(this);
		Set<State> closure = new HashSet<State>();
		closure.add(this);
		
		Set<State> epsilonTransitionStates = transition(NFA.EMPTY_STR);
		if (epsilonTransitionStates != null) {
			for (State state : epsilonTransitionStates) {
				if (!visited.contains(state)) {
					closure.addAll(state.epsilonClosureSearch(visited));
				}
			}
		}
		
		return closure;
	}

	/**
	 * Checks whether this state is a start state of the associated NFA.
	 * 
	 * @return true if this state is a start state
	 */
	public boolean isStartState() {
		return nfa.isStartState(this);
	}

	/**
	 * Set whether or not this state should be a start state of the associated
	 * NFA.
	 * 
	 * @param startState
	 *            true to set this as start state
	 * @return true if the associated NFA's start state set changed as a result
	 *         of this operation
	 */
	public boolean setStartState(boolean startState) {
		if (startState) {
			return nfa.addStartState(this);
		} else {
			return nfa.removeStartState(this);
		}
	}

	/**
	 * Checks whether this state is a final state of the associated NFA.
	 * 
	 * @return true if this state is a final state
	 */
	public boolean isFinalState() {
		return nfa.isFinalState(this);
	}

	/**
	 * Set whether or not this state should be a final state of the associated
	 * NFA.
	 * 
	 * @param startState
	 *            true to set this as final state
	 * @return true if the associated NFA's final state set changed as a result
	 *         of this operation
	 */
	public boolean setFinalState(boolean finalState) {
		if (finalState) {
			return nfa.addFinalState(this);
		} else {
			return nfa.removeFinalState(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

}
