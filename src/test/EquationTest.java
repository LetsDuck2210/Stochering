package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import main.Equation;

class EquationTest {

	@Test
	void createEquation() {
		assertEquals(13, new Equation("k + 5 * 2", "k").evaluate(3));
		assertEquals(17, new Equation("k * 5 + 2", "k").evaluate(3));
		assertEquals(13.5, new Equation("k * 5 - 2 + 1/2", "k").evaluate(3));
	}

}
