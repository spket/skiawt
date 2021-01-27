package com.spket.skiawt.demo;

import org.eclipse.swt.opengl.GLCanvas;
import org.jetbrains.skija.Canvas;
import org.jetbrains.skija.skottie.Animation;

public class SkottiePlayer extends RenderingTask {
	private static final String[] LOTTIE_EXTS = { "*.json" };
	private static String savedPath;
	
	public static final String getSavedPath() {
		return savedPath;
	}
	
	public static final String[] getExtensions() {
		return LOTTIE_EXTS;
	}
	
	public static final RenderingTask createTask(GLCanvas canvas, String file) {
		savedPath = file;
		
		return new SkottiePlayer(canvas, file);
	}
	
	private long start;
	private Animation animation;
	//private InvalidationController ic = new InvalidationController();

	private SkottiePlayer(GLCanvas target, String file) {
		super(target, file);
	}

	@Override
	public void paint(Canvas canvas) {
		canvas.clear(0xFFFFFFFF);
		if (animation != null) {
			float progress;
			if (start == 0) {
				progress = 0;
				start = System.currentTimeMillis();
			} else {
				progress = ((System.currentTimeMillis() - start) % (long) (1000 * animation.getDuration())) / (1000 * animation.getDuration());
			}
			animation.seek(progress/*, ic*/);
			animation.render(canvas/*, Rect.makeXYWH(0, 0, animation.getWidth(), animation.getHeight()), RenderFlag.SKIP_TOP_LEVEL_ISOLATION*/);
		}
	}

	@Override
	protected void releaseData() {
		if (animation != null) {
			animation.close();
			animation = null;
		}
	}

	@Override
	protected void accept(Object data) {
		animation = (Animation) data;
	}

	@Override
	protected void onTask() {
		Animation animation = null;
		try {
			animation = Animation.makeFromFile(getFile());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		postData(animation);
		
		if (animation != null) {
			long duration = 1000;
			float fps = animation.getFPS();
			if (fps > 1)
				duration = (long) Math.floor(duration / fps);
			while (!isCancelled()) {
				redraw();
				if (duration > 0)
					sleep(duration);
			}
			postData(null);
		}
	}
	
}
