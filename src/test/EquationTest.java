package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import main.Equation;

class EquationTest {

	@Test
	void createEquation() {
		assertEquals(13, new Equation("k + 5 * 2", "k").evaluate(3));
		assertEquals(17, new Equation("k * 5 + 2", "k").evaluate(3));
		assertEquals(13.5, new Equation("k * 5 - 2 + 1 / 2", "k").evaluate(3));
		assertEquals(16.5, new Equation("k * 5 + 2 - 1 / 2", "k").evaluate(3));
		assertEquals(-14, new Equation("k - 2 - 3*5", "k").evaluate(3));
		assertEquals(8, new Equation("k - (2 - 3) * 5", "k").evaluate(3));
		assertEquals(20, new Equation("(k - (2 - 3)) * 5", "k").evaluate(3));
		assertEquals(16, new Equation("k - (2 - (3 * 5))", "k").evaluate(3));
	}

}
