package ca.ericbannatyne.nfasim;

import java.util.HashSet;
import java.util.Set;

import ca.ericbannatyne.nfa.NFA;
import ca.ericbannatyne.nfa.State;

public class NFASimulator {

	public static void main(String[] args) {
		Set<Character> alphabet = new HashSet<Character>();
		alphabet.add('0');
		alphabet.add('1');

		NFA nfa = new NFA(alphabet);
		State state0 = nfa.newState("s0");
		State state1 = nfa.newState("s1");

		state0.addTransition('0', state0);
		state0.addTransition('1', state1);
		state1.addTransition('0', state0);
		state1.addTransition('1', state1);
		
		state0.setStartState(true);
		state1.setFinalState(true);
		
		String string = "00110101010";
		nfa.start(string);
		while (nfa.getPosition() < string.length()) {
			System.out.println("---------------");
			System.out.println("Position: " + nfa.getPosition());
			Set<State> currentStates = nfa.getCurrentStates();
			
			System.out.println(currentStates);
			
			/*
			for (State state : currentStates)
				System.out.println(state);
				*/
			
			nfa.step();
		}
		
		if (nfa.currentlyOnFinalState())
			System.out.println("Accepts!");
		else
			System.out.println("Rejects!");
	}
}
