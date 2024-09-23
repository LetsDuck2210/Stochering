package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import main.Equation;

class EquationTest {

	@Test
	void createEquation() {
		BigDecimal three = new BigDecimal(3);
		assertEquals(13, ((BigDecimal) new Equation("k + 5 * 2", "k").evaluate(three)).intValue());
		assertEquals(17, ((BigDecimal) new Equation("k * 5 + 2", "k").evaluate(three)).intValue());
		assertEquals(13.5, ((BigDecimal) new Equation("k * 5 - 2 + 1 / 2", "k").evaluate(three)).doubleValue());
		assertEquals(16.5, ((BigDecimal) new Equation("k * 5 + 2 - 1 / 2", "k").evaluate(three)).doubleValue());
		assertEquals(-14, ((BigDecimal) new Equation("k - 2 - 3*5", "k").evaluate(three)).intValue());
		assertEquals(8, ((BigDecimal) new Equation("k - (2 - 3) * 5", "k").evaluate(three)).intValue());
		assertEquals(20, ((BigDecimal) new Equation("(k - (2 - 3)) * 5", "k").evaluate(three)).intValue());
		assertEquals(16, ((BigDecimal) new Equation("k - (2 - (3 * 5))", "k").evaluate(three)).intValue());
	}

}
