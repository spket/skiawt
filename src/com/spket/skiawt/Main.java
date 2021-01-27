package com.spket.skiawt;

import org.jetbrains.skija.impl.Library;

import com.spket.skiawt.demo.SkiaDemo;

public class Main {
	public static void main(String[] args) {
		loadLibrary();
		
		SkiaDemo.main(args);
	}
	
	private static void loadLibrary() {
		System.loadLibrary("skija");
		System.setProperty("skija.staticLoad", String.valueOf(true));
		Library._loaded = true;
		Library._nAfterLoad();
	}
}
