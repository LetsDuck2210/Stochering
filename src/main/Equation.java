package main;

import java.util.function.BiFunction;

public class Equation {
	public enum Operation {
		MULT('*', (a, b) -> a*b), DIV('/', (a,b) -> a/b), SUB('-', (a, b) -> a-b), ADD('+', (a, b) -> a+b), NOP('\0', (a,b) -> 0.0);
		
		private final char str;
		private final BiFunction<Double, Double, Double> applyF;
		private Operation(char c, BiFunction<Double, Double, Double> applyF) {
			this.str = c;
			this.applyF = applyF;
			
		}
		public char character() {
			return str;
		}
		public double apply(double a, double b) {
			return applyF.apply(a, b);
		}
	}
	
	private Operation operation = Operation.NOP;
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
		
		int level = 0;
		String left = "", right = "";
		boolean parsingRight = false;
		if(this.term.startsWith("(") && this.term.endsWith(")")) {
			// cut '(' and ')' from start and end
			this.term = term = this.term.substring(1, this.term.length() - 1);
		}
		
		outer:
		for(int i = 0; i < term.length(); i++) {
			char c = term.charAt(i);
			if(Character.isWhitespace(c) || c == '\0') continue;
			if(c == '(') level++;
			if(c == ')') level--;
			
			if(level == 0) {
				for(Operation op : Operation.values()) {
					if(op.ordinal() < operation.ordinal() && operation != Operation.NOP) continue;
					
					if(op.character() == c) {
						if(operation != Operation.NOP)
							left += operation.character() + right;
						operation = op;
						parsingRight = true;
						right = "";
						continue outer;
					}
				}
			}
			if(parsingRight)
				right += c;
			else
				left += c;
		}
		if(operation == Operation.NOP) return;
		this.left = new Equation(left, paramName);
		this.right = new Equation(right, paramName);
	}
	public Equation(Number n) {
		term = "" + n;
	}
	
	public double evaluate(double param) {
		if(left != null && right != null)
			return operation.apply(left.evaluate(param), right.evaluate(param));
		if(term.equals(paramName))
			return param;
		return Double.parseDouble(term.trim());
	}
}
