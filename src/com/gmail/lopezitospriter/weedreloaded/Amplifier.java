package com.gmail.lopezitospriter.weedreloaded;

public enum Amplifier{
	plus,
	minus,
	times,
	divide,
	equals;

	public int getResult(int value, int arg, int max){
		int i = getResult(value, arg);

		if(i > max)
			return max;
		return i;
	}

	public int getResult(int value, int arg){
		switch(this){
		case plus:
			value += arg;
			break;
		case minus:
			value -= arg;
			break;
		case times:
			value *= arg;
			break;
		case divide:
			value /= arg;
			break;
		case equals:
			value = arg;
			break;
		}
		return value;
	}

	@Override
	public String toString(){
		switch(this){
		case plus:
			return "+";
		case minus:
			return "-";
		case times:
			return "*";
		case divide:
			return "/";
		case equals:
			return "=";
		}
		return "N";
	}

	public static Amplifier parseAmplifier(char c){
		switch(c){
		case '+':
			return plus;
		case '-':
			return minus;
		case '*':
			return times;
		case '/':
			return divide;
		case '=':
			return equals;
		}
		return null;
	}
}