package ca.ericbannatyne.nfa;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class NFATest {
	
	private static Set<Character> alphabet;

	private NFA nfa;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		alphabet = new HashSet<Character>(); 
		alphabet.add('1');
		alphabet.add('0');
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		alphabet = null;
	}

	@Before
	public void setUp() throws Exception {
		nfa = new NFA(alphabet);
	}

	@After
	public void tearDown() throws Exception {
		nfa = null;
	}

	@Test
	public void testNFASetOfCharacter() {
		nfa = new NFA(alphabet);
		
		assertSame(nfa.getClass(), NFA.class);
		assertFalse(nfa.isRunning());
		assertEquals(nfa.getStates().size(), 0);
		assertEquals(nfa.getStartStates().size(), 0);
		assertEquals(nfa.getFinalStates().size(), 0);
	}

	@Test
	public void testNFASetOfCharacterSetOfState() {
		State state1 = new State(nfa, "state1");
		State state2 = new State(nfa, "state2");
		State state3 = new State(nfa, "state3");

		Set<State> states = new HashSet<State>();
		states.add(state1);
		states.add(state2);
		states.add(state3);
		
		nfa = new NFA(alphabet, states);
		
		assertFalse(nfa.isRunning());
		assertEquals(nfa.getStates(), states);
		assertEquals(nfa.getStartStates().size(), 0);
		assertEquals(nfa.getFinalStates().size(), 0);
	}

	@Test
	public void testNFASetOfCharacterSetOfStateSetOfState() {
		State state1 = new State(nfa, "state1");
		State state2 = new State(nfa, "state2");
		State state3 = new State(nfa, "state3");

		Set<State> states = new HashSet<State>();
		states.add(state1);
		states.add(state2);
		states.add(state3);
		
		Set<State> startStates = new HashSet<State>();
		startStates.add(state1);
		
		nfa = new NFA(alphabet, states, startStates);
		
		assertFalse(nfa.isRunning());
		assertEquals(nfa.getStates(), states);
		assertEquals(nfa.getStartStates(), startStates);
		assertEquals(nfa.getFinalStates().size(), 0);
		
		State state4 = new State(nfa, "state4");
		startStates.add(state4);
		try {
			new NFA(alphabet, states, startStates);
			fail();
		} catch (UnknownStateException e) {
		}
	}

	@Test
	public void testNFASetOfCharacterSetOfStateSetOfStateSetOfState() {
		State state1 = new State(nfa, "state1");
		State state2 = new State(nfa, "state2");
		State state3 = new State(nfa, "state3");

		Set<State> states = new HashSet<State>();
		states.add(state1);
		states.add(state2);
		states.add(state3);
		
		Set<State> startStates = new HashSet<State>();
		startStates.add(state1);
		
		Set<State> finalStates = new HashSet<State>();
		finalStates.add(state3);
		
		nfa = new NFA(alphabet, states, startStates, finalStates);
		
		assertFalse(nfa.isRunning());
		assertEquals(nfa.getStates(), states);
		assertEquals(nfa.getStartStates(), startStates);
		assertEquals(nfa.getFinalStates().size(), 0);
		
		State state4 = new State(nfa, "state4");
		finalStates.add(state4);
		try {
			new NFA(alphabet, states, startStates, finalStates);
			fail();
		} catch (UnknownStateException e) {
		}
	}

	@Test
	public void testNFANFA() {
		NFA nfa2 = new NFA(nfa);

		assertNotSame(nfa, nfa2);

		assertEquals(nfa.getStates(), nfa2.getStates());
		assertNotSame(nfa.getStates(), nfa2.getStates());
		
		assertEquals(nfa.getStartStates(), nfa2.getStartStates());
		assertNotSame(nfa.getStartStates(), nfa2.getStartStates());
		
		assertEquals(nfa.getFinalStates(), nfa2.getFinalStates());
		assertNotSame(nfa.getFinalStates(), nfa2.getFinalStates());
	}
	
	@Test
	public void testGetAlphabet() {
		assertEquals(alphabet, nfa.getAlphabet());
	}

	@Test
	public void testLetterInAlphabet() {
		assertTrue(nfa.letterInAlphabet('1'));
		assertTrue(nfa.letterInAlphabet('0'));
		assertFalse(nfa.letterInAlphabet('2'));
		assertFalse(nfa.letterInAlphabet(NFA.EMPTY_STR));
	}
	
	@Test
	public void testStringIsOverAlphabet() {
		assertTrue(nfa.stringIsOverAlphabet(""));
		assertTrue(nfa.stringIsOverAlphabet("0"));
		assertTrue(nfa.stringIsOverAlphabet("1"));
		assertTrue(nfa.stringIsOverAlphabet("10"));
		assertTrue(nfa.stringIsOverAlphabet("11"));
		assertTrue(nfa.stringIsOverAlphabet("101101"));
		assertTrue(nfa.stringIsOverAlphabet("110110101"));
		assertTrue(nfa.stringIsOverAlphabet("01"));
		assertTrue(nfa.stringIsOverAlphabet("00"));
		assertTrue(nfa.stringIsOverAlphabet("010010"));
		assertTrue(nfa.stringIsOverAlphabet("001001010"));
		
		assertFalse(nfa.stringIsOverAlphabet("2"));
		assertFalse(nfa.stringIsOverAlphabet("020"));
		assertFalse(nfa.stringIsOverAlphabet("01210"));
	}

	@Test
	public void testGetStates() {
		State state1 = new State(nfa, "state1");
		State state2 = new State(nfa, "state2");
		State state3 = new State(nfa, "state3");

		Set<State> states = new HashSet<State>();
		states.add(state1);
		states.add(state2);
		states.add(state3);
		
		nfa = new NFA(alphabet, states);
		
		Set<State> actual = nfa.getStates();
		assertEquals(actual, states);
		assertNotSame(actual, states);
	}

	@Test
	public void testSetStates() {
		State state1 = new State(nfa, "state1");
		State state2 = new State(nfa, "state2");
		State state3 = new State(nfa, "state3");

		Set<State> states = new HashSet<State>();
		states.add(state1);
		states.add(state2);
		states.add(state3);
		
		nfa.setStates(states);
	}

	@Test
	public void testHasState() {
		fail("Not yet implemented");
	}

	@Test
	public void testCheckStateIsValid() {
		fail("Not yet implemented");
	}

	@Test
	public void testNewStateStringMapOfCharacterSetOfState() {
		fail("Not yet implemented");
	}

	@Test
	public void testNewStateString() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveState() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStartStates() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetStartStates() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsStartState() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddStartState() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveStartState() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFinalStates() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetFinalStates() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsFinalState() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddFinalState() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveFinalState() {
		fail("Not yet implemented");
	}

	@Test
	public void testEpsilonClosure() {
		fail("Not yet implemented");
	}

	@Test
	public void testAccepts() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsRunning() {
		fail("Not yet implemented");
	}

	@Test
	public void testStart() {
		fail("Not yet implemented");
	}

	@Test
	public void testStop() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetString() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPosition() {
		fail("Not yet implemented");
	}

	@Test
	public void testStep() {
		fail("Not yet implemented");
	}

	@Test
	public void testStepBack() {
		fail("Not yet implemented");
	}

	@Test
	public void testGoToStep() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCurrentStates() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testCurrentlyOnFinalState() {
		fail("Not yet implemented");
	}

}
