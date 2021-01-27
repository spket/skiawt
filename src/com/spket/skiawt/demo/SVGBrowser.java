package com.spket.skiawt.demo;

import org.eclipse.swt.opengl.GLCanvas;
import org.jetbrains.skija.Canvas;
import org.jetbrains.skija.Data;
import org.jetbrains.skija.svg.DOM;

public class SVGBrowser extends RenderingTask {
	private static final String[] SVG_EXTS = { "*.svg" };
	private static String savedPath;
	
	public static final String getSavedPath() {
		return savedPath;
	}
	
	public static final String[] getExtensions() {
		return SVG_EXTS;
	}
	
	public static final RenderingTask createTask(GLCanvas canvas, String file) {
		savedPath = file;
		
		return new SVGBrowser(canvas, file);
	}
	
	private DOM svg;
	
	private SVGBrowser(GLCanvas target, String file) {
		super(target, file);
	}

	@Override
	public void paint(Canvas canvas) {
		canvas.clear(0xFFFFFFFF);
		if (svg != null)
			svg.render(canvas);
	}

	@Override
	protected void releaseData() {
		if (svg != null) {
			svg.close();
			svg = null;
		}
	}

	@Override
	protected void accept(Object data) {
		svg = (DOM) data;
	}

	@Override
	protected void onTask() {
		DOM svg = null;
		try (Data data = Data.makeFromFileName(getFile())) {
			svg = new DOM(data);
		}
		postData(svg);
	}
}
