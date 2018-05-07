package it.unibo.ai.mulino.CIRAMill.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BitBoardUtils {
	
	private static final HashMap<String, Byte> positionsStringDictionary = new HashMap<String, Byte>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
		//Outer square
		put("a7", (byte) 0);
		put("d7", (byte) 1);
		put("g7", (byte) 2);
		put("g4", (byte) 3);
		put("g1", (byte) 4);
		put("d1", (byte) 5);
		put("a1", (byte) 6);
		put("a4", (byte) 7);
		
		//Middle square
		put("b6", (byte) 8);
		put("d6", (byte) 9);
		put("f6", (byte) 10);
		put("f4", (byte) 11);
		put("f2", (byte) 12);
		put("d2", (byte) 13);
		put("b2", (byte) 14);
		put("b4", (byte) 15);
		
		//Inner square
		put("c5", (byte) 16);
		put("d5", (byte) 17);
		put("e5", (byte) 18);
		put("e4", (byte) 19);
		put("e3", (byte) 20);
		put("d3", (byte) 21);
		put("c3", (byte) 22);
		put("c4", (byte) 23);
	}};
	
	private static final HashMap<Integer, String> positionsByteDictionary = new HashMap<Integer, String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
		//Outer square		
		put(1 << 0, "a7");		
		put(1 << 1, "d7");
		put(1 << 2, "g7");
		put(1 << 3, "g4");
		put(1 << 4, "g1");
		put(1 << 5, "d1");
		put(1 << 6, "a1");
		put(1 << 7, "a4");
		
		//Middle square
		put(1 << 8, "b6");
		put(1 << 9, "d6");
		put(1 << 10, "f6");
		put(1 << 11, "f4");
		put(1 << 12, "f2");
		put(1 << 13, "d2");
		put(1 << 14, "b2");
		put(1 << 15, "b4");
		
		//Inner square
		put(1 << 16, "c5");
		put(1 << 17, "d5");
		put(1 << 18, "e5");
		put(1 << 19, "e4");
		put(1 << 20, "e3");
		put(1 << 21, "d3");
		put(1 << 22, "c3");
		put(1 << 23, "c4");
	}};
	
	public static int boardFromPosition(String position) {
		return 1 << positionsStringDictionary.get(position);
	}
	
	public static String positionFromBoard(int position) {
		String result = positionsByteDictionary.get(position);
		
		if(result == null)
			return "";
		
		return result;
	}
	
	
}
