package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import main.Equation;

class EquationTest {

	@Test
	void createEquation() {
		BigDecimal three = new BigDecimal(3);
		assertEquals(13, new Equation("k + 5 * 2", "k").evaluate(three));
		assertEquals(17, new Equation("k * 5 + 2", "k").evaluate(three));
		assertEquals(13.5, new Equation("k * 5 - 2 + 1 / 2", "k").evaluate(three));
		assertEquals(16.5, new Equation("k * 5 + 2 - 1 / 2", "k").evaluate(three));
		assertEquals(-14, new Equation("k - 2 - 3*5", "k").evaluate(three));
		assertEquals(8, new Equation("k - (2 - 3) * 5", "k").evaluate(three));
		assertEquals(20, new Equation("(k - (2 - 3)) * 5", "k").evaluate(three));
		assertEquals(16, new Equation("k - (2 - (3 * 5))", "k").evaluate(three));
	}

}
