package main;

import java.util.function.BiFunction;

public class Equation {
	public enum Operation {
		ADD("+", (a, b) -> a+b), SUB("-", (a, b) -> a-b), MULT("*", (a, b) -> a*b), DIV("/", (a,b) -> a/b), NOP("", (a,b) -> 0.0);
		
		private final String str;
		private final BiFunction<Double, Double, Double> applyF;
		private Operation(String str, BiFunction<Double, Double, Double> applyF) {
			this.str = str;
			this.applyF = applyF;
			
		}
		public String toString() {
			return str;
		}
		public double apply(double a, double b) {
			return applyF.apply(a, b);
		}
	}
	
	private Operation operation;
	private Equation left, right;
	private String term, paramName;
	public Equation(String term, String paramName) {
		this.term = term.trim();
		this.paramName = paramName.trim();
		if(term.isBlank()) {
			operation = Operation.NOP;
			left = right = null;
			return;
		}
		int opIndex = -1;
		for(Operation op : Operation.values()) {
			int i = term.lastIndexOf(op.toString());
			if(i < 0) continue;
			opIndex = i;
			operation = op;
			break;
		}
		if(opIndex < 0 || opIndex == term.length()) return;
		left = new Equation(term.substring(0, opIndex), paramName);
		right = new Equation(term.substring(opIndex + 1), paramName);
	}
	
	public double evaluate(double param) {
		if(left != null && right != null)
			return operation.apply(left.evaluate(param), right.evaluate(param));
		if(term.equals(paramName))
			return param;
		return Double.parseDouble(term.trim());
	}
}
