package ca.ericbannatyne.nfa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * This class implements methods for building and running a nondeterministic
 * finite automaton. An NFA may be in either of two states: running or not
 * running (a.k.a. building). If a method that is intended to have side effects
 * on the state of this NFA is called incorrectly, for example if a running-mode
 * method is called while the NFA is not running, or if the set of states is
 * modified while the NFA is running, the method will throw an
 * IllegalStatException.
 */
public class NFA {

	/**
	 * Representation of the empty string, for example, for use with
	 * epsilon-transitions.
	 */
	public static final char EMPTY_STR = '\0';

	private static final String NFA_RUNNING = "NFA is currently running";
	private static final String NFA_NOT_RUNNING = "NFA is not running";
	private static final String INVALID_START_STATE = "Invalid start state";
	private static final String INVALID_FINAL_STATE = "Invalid final state";

	private Set<Character> alphabet;

	private Set<State> states;
	private Set<State> startStates;
	private Set<State> finalStates;

	private boolean running = false;
	private String string = null;
	private int position = -1;
	private Set<State> currentStates = null;

	/**
	 * Constructs a new empty NFA whose language is over the specified alphabet.
	 * 
	 * @param alphabet
	 *            the alphabet
	 */
	public NFA(Set<Character> alphabet) {
		this(alphabet, new HashSet<State>());
	}

	/**
	 * Constructs a new NFA over the specified alphabet, with the given set of
	 * states, without specifying which states are starting states or final
	 * states.
	 * 
	 * @param alphabet
	 *            the alphabet
	 * @param states
	 *            states of the NFA
	 */
	public NFA(Set<Character> alphabet, Set<State> states) {
		this(alphabet, states, new HashSet<State>());
	}

	/**
	 * Constructs a new NFA over the specified alphabet, with the given set of
	 * states, and the specified starting states, but with no specified final
	 * states. If no final states are specified after this constructor is
	 * called, then this NFA accepts exactly the empty language.
	 * 
	 * @param alphabet
	 *            the alphabet
	 * @param states
	 *            states of the NFA
	 * @param startStates
	 *            subset of states that are starting states
	 */
	public NFA(Set<Character> alphabet, Set<State> states,
			Set<State> startStates) {
		this(alphabet, states, startStates, new HashSet<State>());
	}

	/**
	 * Constructs a new NFA over the specified alphabet, with the given set of
	 * states, as well as the specified starting and final states.
	 * 
	 * @param alphabet
	 *            the alphabet
	 * @param states
	 *            states of the NFA
	 * @param startStates
	 *            subset of the states that are starting states
	 * @param finalStates
	 *            subset of the states that are final states
	 */
	public NFA(Set<Character> alphabet, Set<State> states,
			Set<State> startStates, Set<State> finalStates) {
		this.alphabet = new HashSet<Character>(alphabet);

		checkStateSetIsValid(states);
		this.states = new HashSet<State>(states);

		if (!this.states.containsAll(startStates))
			throw new InvalidStartOrFinalStateException(INVALID_START_STATE);
		if (!this.states.containsAll(finalStates))
			throw new InvalidStartOrFinalStateException(INVALID_FINAL_STATE);

		this.startStates = new HashSet<State>(startStates);
		this.finalStates = new HashSet<State>(finalStates);
	}

	/**
	 * Constructs a new NFA over the same alphabet as the original NFA, with the
	 * same states, starting states and final states as the original. This
	 * produces a deep copy of the original NFA.
	 * 
	 * @param nfa
	 *            the original NFA
	 */
	public NFA(NFA nfa) {
		this(nfa.alphabet, nfa.states, nfa.startStates, nfa.finalStates);
	}

	/**
	 * Checks whether a given letter is in the alphabet of this NFA.
	 * 
	 * @param letter
	 *            the letter to check
	 * @return true if the given letter is in the alphabet
	 */
	public boolean letterInAlphabet(Character letter) {
		return alphabet.contains(letter);
	}

	/**
	 * Returns a deep copy of the states of this NFA.
	 * 
	 * @return the states
	 */
	public Set<State> getStates() {
		return new HashSet<State>(states);
	}

	/**
	 * Resets the set of states of this NFA to contain the same elements of the
	 * given set of states, while resetting the start and final states to be
	 * empty.
	 * 
	 * @param states
	 *            the states to set
	 */
	public void setStates(Set<State> states) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);

		checkStateSetIsValid(states);

		this.states = new HashSet<State>(states);
		this.startStates = new HashSet<State>();
		this.finalStates = new HashSet<State>();
	}

	/**
	 * Checks whether the given state is in this NFA.
	 * 
	 * @param state
	 * @return true if the state is a possible state of this NFA
	 */
	public boolean hasState(State state) {
		return states.contains(state);
	}

	/**
	 * Checks whether the given state's transitions are only to other states in
	 * the specified set of states, and that all transitions (other than
	 * epsilon-transitions) are done via letters in this NFA's alphabet.
	 * 
	 * @param state
	 *            the state to check
	 * @throws NoSuchElementException
	 *             if the state is not valid
	 */
	private void checkStateIsValid(State state, Set<State> stateSet)
			throws NoSuchElementException {
		Map<Character, Set<State>> transitions = state.getTransitions();

		for (char letter : transitions.keySet()) {
			if (letter != EMPTY_STR && !alphabet.contains(letter))
				throw new NoSuchElementException("Attempted to add state \""
						+ state.getName() + "\" with transition via nonempty "
						+ "character '" + letter + "' not in alphabet.");

			if (!stateSet.containsAll(transitions.get(letter)))
				throw new NoSuchElementException("Attempted to add state with "
						+ "transition to unknown state from \""
						+ state.getName() + "\" via letter '" + letter + "'.");
		}
	}

	/**
	 * Checks if every state in the given set of states is valid with respect to
	 * the given state itself.
	 * 
	 * @param stateSet
	 *            the set of states to check
	 * @throws NoSuchElementException
	 *             if any state in the given set is not valid
	 */
	private void checkStateSetIsValid(Set<State> stateSet)
			throws NoSuchElementException {
		for (State state : stateSet)
			checkStateIsValid(state, stateSet);
	}

	/**
	 * Adds the specified state to the NFA.
	 * 
	 * @param state
	 *            the state to add
	 * @return true if the given state was not already in this NFA
	 */
	private boolean addState(State state) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);
		checkStateIsValid(state, states);

		return states.add(state);
	}

	/**
	 * 
	 * @param name
	 * @param transitions
	 * @return
	 */
	public State newState(String name, Map<Character, Set<State>> transitions) {
		State state = new State(this, name, transitions);
		addState(state);
		return state;
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public State newState(String name) {
		return newState(name, new HashMap<Character, Set<State>>());
	}

	/**
	 * Remove the specified state from the NFA, along with any transitions from
	 * other states to the specified state.
	 * 
	 * @param state
	 *            the state to remove
	 * @return true if the set of states of this NFA was changed as a result of
	 *         this operation
	 */
	public boolean removeState(State state) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);

		// TODO: Remove any transitions going to this state

		return states.remove(state);
	}

	/**
	 * Returns a deep copy of the starting states of this NFA.
	 * 
	 * @return the starting states
	 */
	public Set<State> getStartStates() {
		return new HashSet<State>(startStates);
	}

	/**
	 * Sets the starting states of this NFA to be the specified subset of this
	 * NFA's states.
	 * 
	 * @param startStates
	 *            subset of states to be set as starting states
	 */
	public void setStartStates(Set<State> startStates) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);

		if (!states.containsAll(startStates))
			throw new InvalidStartOrFinalStateException(INVALID_START_STATE);

		this.startStates = new HashSet<State>(startStates);
	}

	/**
	 * Checks whether a given state is a starting state or not.
	 * 
	 * @param state
	 *            the state to check
	 * @return true if the given state is a starting state
	 */
	public boolean isStartState(State state) {
		return states.contains(state);
	}

	/**
	 * Sets the given state to be a starting state.
	 * 
	 * @param state
	 *            the state to be set as a starting state
	 */
	boolean addStartState(State state) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);

		// TODO: Check that this is in states
		return false;
	}

	/**
	 * Sets the specified state to no longer be a starting state.
	 * 
	 * @param state
	 *            the state to no longer be a starting state
	 * @return true if the set of starting states was changed as a result of
	 *         this operation
	 */
	boolean removeStartState(State state) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);

		// TODO
		return false;
	}

	/**
	 * Returns a deep copy of the set of final states of this NFA.
	 * 
	 * @return the final states of this NFA
	 */
	public Set<State> getFinalStates() {
		return new HashSet<State>(finalStates);
	}

	/**
	 * Sets the final states of this NFA to be the specified subset of this
	 * NFA's states.
	 * 
	 * @param finalStates
	 *            subset of states to be set as final states
	 */
	public void setFinalStates(Set<State> finalStates) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);
		
		if (!states.containsAll(finalStates))
			throw new InvalidStartOrFinalStateException(INVALID_FINAL_STATE);

		this.finalStates = new HashSet<State>(finalStates);
	}

	/**
	 * Checks whether or not a given state is a final state.
	 * 
	 * @param state
	 *            the state to check
	 * @return true if the specified state is a final state
	 */
	public boolean isFinalState(State state) {
		return false;
	}

	/**
	 * Adds the given state to the set of final states of this NFA.
	 * 
	 * @param state
	 */
	boolean addFinalState(State state) {
		return false;
	}

	/**
	 * 
	 * @param state
	 * @return
	 */
	boolean removeFinalState(State state) {
		return false;
	}

	/**
	 * 
	 * @param states
	 * @return
	 */
	public Set<State> epsilonClosure(Set<State> states) {
		// TODO: Get epsilon closure of set of states
		return null;
	}

	/**
	 * 
	 * @param string
	 * @return
	 */
	public boolean accepts(String string) {
		// TODO: Should (in principle) work regardless of whether NFA is
		// running or not, and should not have any side effects
		return false;
	}

	/**
	 * @return the running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * 
	 * @param string
	 */
	public void start(String string) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);
		// TODO: initialize
	}

	/**
	 * @return the string
	 */
	public String getString() {
		if (!running)
			throw new IllegalStateException(NFA_NOT_RUNNING);

		return string;
	}

	/**
	 * @return the position
	 */
	public int getPosition() {
		if (!running)
			throw new IllegalStateException(NFA_NOT_RUNNING);

		return position;
	}

	/**
	 * 
	 */
	public void step() {
		if (!running)
			throw new IllegalStateException(NFA_NOT_RUNNING);
		// TODO
	}

	/**
	 * @return the currentStates
	 */
	public Set<State> getCurrentStates() {
		if (!running)
			throw new IllegalStateException(NFA_NOT_RUNNING);

		return new HashSet<State>(currentStates);
	}

}
