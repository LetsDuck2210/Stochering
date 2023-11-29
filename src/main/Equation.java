package main;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class Equation {
	public enum Operation {
		POW('^', (a,b) -> new BigDecimal(Math.pow(a.doubleValue(), b.doubleValue()))), 
		MULT('*', (a, b) -> a.multiply(b)), 
		DIV('/', (a,b) -> a.divide(b, Main.<Integer>getOption("scale"), RoundingMode.HALF_UP)), 
		MOD_OR_PERCENT('%', (a,b) -> b.compareTo(BigDecimal.ZERO) != 0 ? a.divideAndRemainder(b)[1] : a.divide(new BigDecimal("100"))), 
		SUB('-', (a, b) -> a.subtract(b)), 
		ADD('+', (a, b) -> a.add(b)), 
		NOP('\0', (a, b) -> a);
		
		private final char str;
		private final BiFunction<BigDecimal, BigDecimal, BigDecimal> applyF;
		private Operation(char c, BiFunction<BigDecimal, BigDecimal, BigDecimal> applyF) {
			this.str = c;
			this.applyF = applyF;
		}
		public char character() {
			return str;
		}
		public BigDecimal apply(BigDecimal a, BigDecimal b) {
			return applyF.apply(a, b);
		}
	}
	
	private Operation operation = Operation.NOP;
	private Equation left, right;
	private String term, paramName;
	public Equation(String term, String paramName) {
		this.term = term.trim();
		this.paramName = paramName.trim();
		if(this.term.isEmpty()) {
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
	public String getTerm() {
		return term;
	}
	
	public Object evaluate(BigDecimal param) {
		if(left != null && right != null)
			return operation.apply(left.evaluateDouble(param), right.evaluateDouble(param));
		if(term.equals(paramName))
			return param;
		if(term.matches("[a-zA-Z]+\\s*\\(.*\\)")) { // function
			String func = term.substring(0, term.indexOf('('));
			String paramStr = term.substring(term.indexOf('(') + 1, term.length() - 1);
			
			try {
				return Main.invokeFunction(func, parseArgs(paramStr));
			} catch (InvocationTargetException | IllegalAccessException e) {
				System.out.println("couldn't invoke function \"" + func + "\": " + e.getMessage());
				if(e.getMessage() == null)
					e.printStackTrace();
				return null;
			}
		}
		if(term.matches("[a-zA-Z]+")) {
			try {
				return Main.getField(term);
			} catch(IllegalArgumentException | IllegalAccessException e) {
				System.out.println("couldn't get field \"" + term + "\": " + e.getMessage());
				if(e.getMessage() == null)
					e.printStackTrace();
				return null;
			}
		}
		if(term.matches("(\".*\")|('.*')")) // string
			return term.substring(1, term.length() - 1);
		return Double.parseDouble(term.trim());
	}
	public BigDecimal evaluateDouble(BigDecimal param) {
		return new BigDecimal("" + evaluate(param));
	}
	public BigInteger evaluateInt(BigDecimal param) {
		return new BigInteger("" + ((Number) evaluate(param)).intValue());
	}
	
	private static String[] parseArgs(String argStr) {
		List<String> args = new ArrayList<>();
		args.add("");
		int totalLvl = 0, quote = 0;
		for(int i = 0; i < argStr.length(); i++) {
			char c = argStr.charAt(i);
			
			switch(c) {
				case '(', '{', '[' -> totalLvl++;
				case ')', '}', ']' -> totalLvl--;
				case '"' -> quote ^= 2; // flip 2nd bit
				case '\'' -> quote ^= 1; // flip 1st bit
				case ',' -> { if(totalLvl == 0 && quote == 0) args.add(""); }
			}
			if(totalLvl < 0) {
				System.out.println("unmatched closing character (')', '}', ']') at " + i);
				return new String[0];
			}

			if(c != ',' || totalLvl != 0 || quote != 0)
				args.set(args.size() - 1, args.get(args.size() - 1) + c);
		}
		if(quote != 0) {
			System.out.println("unclosed quote in " + argStr);
			return new String[0];
		}
		return args.toArray(new String[0]);
	}
}
