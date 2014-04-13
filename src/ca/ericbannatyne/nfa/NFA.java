package ca.ericbannatyne.nfa;

import java.util.HashSet;
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

	private Set<Character> alphabet;

	private Set<State> states;
	private Set<State> startStates;
	private Set<State> finalStates;

	private boolean running = false;
	private String string;
	private int position = 0;
	private Set<State> currentStates;

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
		this.states = new HashSet<State>(states);
		this.startStates = new HashSet<State>(startStates);
		this.finalStates = new HashSet<State>(finalStates);
	}
	
	/**
	 * Constructs a new NFA over the same alphabet as the original NFA, with the
	 * same states, starting states and final states as the original. This
	 * produces a deep copy of the original NFA.
	 * 
	 * @param nfa the original NFA
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
	 * Returns a deep copy of the states of this NFA
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
	 * Adds the specified state to the NFA.
	 * 
	 * @param state
	 *            the state to add
	 * @return true if the given state was not already in this NFA
	 */
	public boolean addState(State state) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);

		return states.add(state);
	}

	/**
	 * Remove the specified state from the NFA.
	 * 
	 * @param state
	 *            the state to remove
	 * @return true if the set of states of this NFA was changed as a result of
	 *         this operation
	 */
	public boolean removeState(State state) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);

		return states.remove(state);
	}

	/**
	 * Get a deep copy of the starting states of this NFA
	 * 
	 * @return the starting states
	 */
	public Set<State> getStartStates() {
		return new HashSet<State>(startStates);
	}

	/**
	 * @param startStates
	 *            subset of states to be set as starting states
	 */
	public void setStartStates(Set<State> startStates) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);
		
		if (!states.containsAll(startStates)) {
			throw new RuntimeException(); // FIXME: better exception needed
		}

		// TODO: Check that param is subset of states
		this.startStates = new HashSet<State>(startStates);
	}

	/**
	 * 
	 * @param state
	 * @return
	 */
	public boolean isStartState(State state) {
		return states.contains(state);
	}

	/**
	 * 
	 * @param state
	 */
	public void addStartState(State state) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);

		// TODO: Check that this is in states
	}

	/**
	 * 
	 * @param state
	 * @return
	 */
	public boolean removeStartState(State state) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);

		// TODO
		return false;
	}

	/**
	 * @return the finalStates
	 */
	public Set<State> getFinalStates() {
		return new HashSet<State>(finalStates);
	}

	/**
	 * @param finalStates
	 *            the finalStates to set
	 */
	public void setFinalStates(Set<State> finalStates) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);

		// TODO: Check if subset of states
		this.finalStates = new HashSet<State>(finalStates);
	}

	/**
	 * 
	 * @param state
	 * @return
	 */
	public boolean isFinalState(State state) {
		return false;
	}

	/**
	 * 
	 * @param state
	 */
	public void addFinalState(State state) {

	}

	/**
	 * 
	 * @param state
	 * @return
	 */
	public boolean removeFinalState(State state) {
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
