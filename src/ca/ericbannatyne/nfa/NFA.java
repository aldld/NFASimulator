package ca.ericbannatyne.nfa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

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
	private static final String INVALID_CURRENT_STATE = "Invalid current state";

	private Set<Character> alphabet;
	private Pattern alphabetPattern;

	private Set<State> states;
	private Set<State> startStates; // Invariant: subset of states
	private Set<State> finalStates; // Invariant: subset of states

	private boolean running = false;
	private String string = null;
	private int position = -1;
	private Set<State> currentStates = null;

	/*
	 * Used as cache of steps that have already been generated. The index refers
	 * to the position of the string at that step. This is used for going
	 * forwards and backwards through the NFA.
	 */
	private List<Set<State>> steps;

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
	NFA(Set<Character> alphabet, Set<State> states) {
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
	NFA(Set<Character> alphabet, Set<State> states, Set<State> startStates) {
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
	NFA(Set<Character> alphabet, Set<State> states, Set<State> startStates,
			Set<State> finalStates) {
		this.alphabet = new HashSet<Character>(alphabet);

		// Generate pattern for determining whether a given string is in the
		// language alphabet* (set of all strings over alphabet)
		StringBuilder regex = new StringBuilder("^[\\Q");
		for (Character c : alphabet) {
			regex.append(c);
		}
		regex.append("\\E]*$");
		alphabetPattern = Pattern.compile(regex.toString());

		checkStateSetIsValid(states);
		this.states = new HashSet<State>(states);

		if (!this.states.containsAll(startStates))
			throw new UnknownStateException(INVALID_START_STATE);
		if (!this.states.containsAll(finalStates))
			throw new UnknownStateException(INVALID_FINAL_STATE);

		this.startStates = new HashSet<State>(startStates);
		this.finalStates = new HashSet<State>(finalStates);
	}

	/**
	 * Constructs a new NFA over the same alphabet as the original NFA, with an
	 * identical (isomorphic) structure to the original. This produces a deep
	 * copy of the original NFA.
	 * <p>
	 * <b>NOTE:</b> The states of this NFA will still internally be associated
	 * with the original NFA, and can therefore have side effects on the
	 * original.
	 * 
	 * @param nfa
	 *            the original NFA
	 */
	NFA(NFA nfa) {
		this(nfa.alphabet, nfa.states, nfa.startStates, nfa.finalStates);
	}

	/**
	 * Gets a deep copy of the alphabet of this NFA.
	 * 
	 * @return the alphabet
	 */
	public Set<Character> getAlphabet() {
		return new HashSet<Character>(alphabet);
	}

	/**
	 * @param alphabet
	 *            the alphabet to set
	 */
	public void setAlphabet(Set<Character> alphabet) {
		this.alphabet = alphabet;
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
	 * Checks whether the given string consists only of letters in this NFA's
	 * alphabet.
	 * 
	 * @param string
	 *            the string
	 * @return true if all characters in the string are in this NFA's alphabet
	 */
	public boolean stringIsOverAlphabet(String string) {
		return alphabetPattern.matcher(string).matches();
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
	void setStates(Set<State> states) {
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
	void checkStateIsValid(State state, Set<State> stateSet)
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
	 * Creates a new state with the specified transition function and attempts
	 * to add it to this NFA.
	 * 
	 * @param name
	 *            name of the state
	 * @param transitions
	 *            the state's transition function
	 * @return the state created
	 */
	public State newState(String name, Map<Character, Set<State>> transitions) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);
		
		State state = new State(this, name, transitions);
		addState(state);
		return state;
	}

	/**
	 * Creates a new state with an empty transition function ad adds it to this
	 * NFA.
	 * 
	 * @param name
	 *            name of the state
	 * @return the state created
	 */
	public State newState(String name) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);
		
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

		for (State s : states) {
			for (Character c : alphabet) {
				s.removeTransition(c, state);
			}
		}

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
			throw new UnknownStateException(INVALID_START_STATE);

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
		return startStates.contains(state);
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

		if (!states.contains(state))
			throw new UnknownStateException(INVALID_START_STATE);

		return startStates.add(state);
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

		return startStates.remove(state);
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
			throw new UnknownStateException(INVALID_FINAL_STATE);

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
		return finalStates.contains(state);
	}

	/**
	 * Adds the given state to the set of final states of this NFA.
	 * 
	 * @param state
	 *            the state to be added to the set of final states
	 * @return true if the state was not already a final state
	 */
	boolean addFinalState(State state) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);

		if (!states.contains(state))
			throw new UnknownStateException(INVALID_FINAL_STATE);

		return finalStates.add(state);
	}

	/**
	 * Removes the given state from the set of final states of this NFA.
	 * 
	 * @param state
	 *            the state to remove from the set of final states
	 * @return true if the set of final states of this NFA was changed as a
	 *         result of this operation
	 */
	boolean removeFinalState(State state) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);

		return finalStates.remove(state);
	}

	/**
	 * Generates the epsilon-closure of a given set of states: the set of all
	 * states reachable from any state in the given set using either no moves or
	 * using only epsilon-transitions.
	 * 
	 * @param states
	 *            the set of states
	 * @return the epsilon-closure of the given set of states
	 */
	public Set<State> epsilonClosure(Set<State> states) {
		Set<State> closure = new HashSet<State>(states);
		for (State state : states) {
			closure.addAll(state.epsilonClosure());
		}

		return closure;
	}

	/**
	 * Determines whether this NFA accepts a given input string.
	 * 
	 * @param string
	 *            the input string
	 * @return true if this NFA accepts the input string
	 */
	public boolean accepts(String string) {
		// TODO: Should (in principle) work regardless of whether NFA is
		// running or not, and should not have any side effects
		return false;
	}

	/**
	 * Checks whether this NFA is currently running.
	 * 
	 * @return true if this NFA is currently running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Initializes this NFA and places it in running mode, ready to process the
	 * given input string.
	 * 
	 * @param string
	 *            the input string
	 */
	public void start(String string) {
		if (running)
			throw new IllegalStateException(NFA_RUNNING);

		running = true;
		if (!stringIsOverAlphabet(string))
			throw new RuntimeException(
					"Input string must be over this NFA's alphabet");
		// TODO: Needs more specific exception

		this.string = string;
		position = 0;
		currentStates = new HashSet<State>(startStates);

		steps = new ArrayList<Set<State>>();
		steps.add(currentStates);
	}

	/**
	 * Stops the NFA's current operation, clearing any current states or
	 * computed steps, and returns to build mode.
	 */
	public void stop() {
		if (!running)
			throw new IllegalStateException(NFA_NOT_RUNNING);

		string = null;
		position = -1;
		currentStates = null;
		steps = null;
		running = false;
	}

	/**
	 * Gets the input string to this NFA.
	 * 
	 * @return the input string
	 */
	public String getString() {
		if (!running)
			throw new IllegalStateException(NFA_NOT_RUNNING);

		return string;
	}

	/**
	 * Gets the current position of this NFA along the input string.
	 * 
	 * @return the position of this NFA along the input string
	 */
	public int getPosition() {
		if (!running)
			throw new IllegalStateException(NFA_NOT_RUNNING);

		return position;
	}

	/**
	 * Sets the position of this NFA along the input string.
	 * 
	 * @param position
	 *            position to set
	 */
	private void setPosition(int position) {
		if (position < 0)
			throw new IndexOutOfBoundsException(Integer.toString(position));
		if (position >= string.length())
			throw new IndexOutOfBoundsException("Step: " + position
					+ ", Length: " + string.length());

		this.position = position;
	}

	/**
	 * Performs, if possible, one step of this NFA's operation along the input
	 * string, updating the internal set of current states and position of this
	 * NFA.
	 */
	public void step() {
		if (!running)
			throw new IllegalStateException(NFA_NOT_RUNNING);

		position += 1;
		if (position == string.length())
			return; // Do nothing other than increment position
		if (position < 0)
			throw new IndexOutOfBoundsException(Integer.toString(position));
		if (position > string.length())
			throw new IndexOutOfBoundsException("Position: " + position
					+ ", Length: " + string.length());

		if (position < steps.size()) {
			currentStates = steps.get(position);
		} else if (position == steps.size()) {
			Character c = string.charAt(position);
			Set<State> next = new HashSet<State>();

			for (State state : currentStates) {
				Set<State> cTransitionStates = state.transition(c);
				if (cTransitionStates != null)
					next.addAll(cTransitionStates);
			}

			currentStates = epsilonClosure(next);

			steps.add(currentStates);
		} else { // Something has gone horribly wrong. (Steps somehow skipped)
			// TODO: DO something about this
		}
	}

	/**
	 * Resets, if possible, the set of current states and the position of this
	 * NFA to the configuration prior to the previous step.
	 */
	public void stepBack() {
		goToStep(position - 1);
	}

	/**
	 * Sets the position and the set of current states of this NFA to the
	 * position and set of current states of this NFA after the given number of
	 * steps from the starting states.
	 * 
	 * @param step
	 *            the step to go to
	 */
	public void goToStep(int step) {
		if (step < 0)
			throw new IndexOutOfBoundsException(Integer.toString(step));
		if (step >= string.length())
			throw new IndexOutOfBoundsException("Step: " + step + ", Length: "
					+ string.length());

		if (step < steps.size()) {
			// Set position and current states to cached values
			setPosition(step);
			setCurrentStates(steps.get(step));
		} else {
			// Step through and generate sets of states as necessary
			if (step < position) {
				// Something went horribly wrong; this step should have been
				// generated before and cached
				throw new RuntimeException();
				// Throw exception for now... maybe just restart from 0 and
				// step through to position?
			}

			while (position < step) {
				step();
			}
		}
	}

	/**
	 * Gets the set of current states of this NFA.
	 * 
	 * @return the set of current states of this NFA
	 */
	public Set<State> getCurrentStates() {
		if (!running)
			throw new IllegalStateException(NFA_NOT_RUNNING);

		return new HashSet<State>(currentStates);
	}

	/**
	 * Sets the set of current states of this NFA.
	 * 
	 * @param currentStates
	 *            the set of current states to set
	 */
	private void setCurrentStates(Set<State> currentStates) {
		if (!states.contains(currentStates))
			throw new UnknownStateException(INVALID_CURRENT_STATE);

		this.currentStates = new HashSet<State>(currentStates);
	}
	
	/**
	 * Checks whether the current states of this NFA contain any final states.
	 * 
	 * @return true if this NFA is currently on a final state
	 */
	public boolean currentlyOnFinalState() {
		if (!running)
			throw new IllegalStateException(NFA_NOT_RUNNING);
		
		Set<State> intersection = getCurrentStates();
		intersection.retainAll(finalStates);
		return intersection.size() > 0;
	}

}
