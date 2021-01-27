package com.spket.skiawt.demo;

import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.widgets.Display;
import org.jetbrains.skija.Canvas;

public abstract class RenderingTask implements Runnable {
	private Display display;
	private GLCanvas target;
	private String file;
	
	private volatile boolean cancelled;
	private volatile boolean running;
	
	private Object monitor = new Object();
	
	private Runnable redrawTask = () -> {
		if (target != null && !target.isDisposed()) {
			target.redraw();
		}
	};
	
	public RenderingTask(GLCanvas target, String file) {
		this.target = target;
		this.file = file;
		
		display = target.getDisplay();
	}

	@Override
	public final void run() {
		running = true;
		try {
			onTask();
		} finally {
			running = false;
		}
	}

	public final void cancel() {
		if (running && !cancelled) {
			cancelled = true;
			synchronized (monitor) {
				monitor.notify();
			}
			releaseData();
		}
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public boolean isRunning() {
		return running;
	}
	
	protected String getFile() {
		return file;
	}
	
	protected void redraw() {
		invokeLater(redrawTask);
	}
	/*
	protected void setCursor(int cursor) {
		invokeLater(() -> {
			if (target != null && !target.isDisposed())
				target.setCursor(display.getSystemCursor(cursor));
		});
	}
	*/
	protected void invokeLater(Runnable task) {
		display.asyncExec(task);
	}
	
	protected void sleep(long timeoutMillis) {
		if (timeoutMillis > 0) {
			synchronized (monitor) {
				try {
					monitor.wait(timeoutMillis);
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}
			}
		}
	}
	
	protected void postData(final Object data) {
		invokeLater(() -> {
			releaseData();		
			accept(data);
			if (target != null && !target.isDisposed()) {
				target.setCursor(null);
				target.redraw();
			}
		});
	}
	
	public abstract void paint(Canvas canvas);
	
	protected void releaseData() {}
	
	protected abstract void onTask();
	protected abstract void accept(Object data);
}
