package com.spket.skiawt.demo;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.jetbrains.skija.BackendRenderTarget;
import org.jetbrains.skija.Canvas;
import org.jetbrains.skija.ColorSpace;
import org.jetbrains.skija.DirectContext;
import org.jetbrains.skija.FramebufferFormat;
import org.jetbrains.skija.Surface;
import org.jetbrains.skija.SurfaceColorFormat;
import org.jetbrains.skija.SurfaceOrigin;

public class SkiaDemo {
	public static void main(String[] args) {
		new SkiaDemo().run();
	}
	
	private Display display;
	private GLCanvas glCanvas;
	private Surface surface;
	private DirectContext context;
	private BackendRenderTarget renderTarget;
	
	private RenderingTask task;
	private ExecutorService executor;
	
	protected void run() {
		display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Skia Demo");
		shell.setLayout(new FillLayout());
		
		GLData data = new GLData();
		data.doubleBuffer = true;
		
		glCanvas = new GLCanvas(shell, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE, data);
		glCanvas.setCurrent();
		context = DirectContext.makeGL();
		
		Listener listener = event -> {
			switch (event.type) {
				case SWT.Paint:
					onPaint(event);
					break;
				case SWT.Resize:
					onResize(event);
					break;
				case SWT.Dispose:
					onDispose();
					break;
			}
		};
		glCanvas.addListener(SWT.Paint, listener);
		glCanvas.addListener(SWT.Resize, listener);
		//glCanvas.addListener(SWT.Dispose, listener);
		shell.addListener(SWT.Dispose, listener);
		
		createMenu(shell);
		
		executor = Executors.newFixedThreadPool(1);
		
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		shutdownTask();

		executor.shutdown();

		display.dispose();
	}
	
	protected void createMenu(Shell shell) {
		Menu bar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(bar);
		MenuItem fileItem = new MenuItem(bar, SWT.CASCADE);
		fileItem.setText("&File");
		Menu submenu = new Menu(shell, SWT.DROP_DOWN);
		fileItem.setMenu(submenu);
		MenuItem item = new MenuItem(submenu, SWT.PUSH);
		item.setText("Open &Lottie\tCtrl+L");
		item.setAccelerator(SWT.MOD1 + 'L');
		item.addListener(SWT.Selection, e -> onOpenLottie());
		
		item = new MenuItem(submenu, SWT.PUSH);
		item.setText("Open &SVG\tCtrl+S");
		item.setAccelerator(SWT.MOD1 + 'S');
		item.addListener(SWT.Selection, e -> onOpenSVG());	}
	
	protected void release() {
		if (surface != null) {
			surface.close();
			surface = null;
		}
		if (renderTarget != null) {
			renderTarget.close();
			renderTarget = null;
		}
	}
	
	protected void onResize(Event event) {
		int fbid = 0;
		
		release();
		
		Rectangle rect = glCanvas.getClientArea();
		renderTarget = BackendRenderTarget.makeGL(rect.width, rect.height, /*samples*/0, /*stencil*/8, fbid, FramebufferFormat.GR_GL_RGBA8);
		surface = Surface.makeFromBackendRenderTarget(context, renderTarget, SurfaceOrigin.BOTTOM_LEFT, SurfaceColorFormat.RGBA_8888, ColorSpace.getDisplayP3());
	}
	
	protected void onPaint(Event event) {
		if (surface == null)
			return;
		
		Canvas canvas = surface.getCanvas();
		
		paint(canvas);
		
		context.flush();
		
		glCanvas.swapBuffers();
	}
	
	protected void onDispose() {
		shutdownTask();
		
		release();
		
		context.close();
	}
	
	protected void onOpenLottie() {
		openFile(SkottiePlayer.getExtensions(), SkottiePlayer::createTask, SkottiePlayer.getSavedPath());
	}
	
	protected void onOpenSVG() {
		openFile(SVGBrowser.getExtensions(), SVGBrowser::createTask, SVGBrowser.getSavedPath());
	}
	
	protected void openFile(String[] exts, BiFunction<GLCanvas, String, RenderingTask> creator, String path) {
		FileDialog dialog = new FileDialog(display.getActiveShell(), SWT.OPEN);
		if (path != null) {
			File f = new File(path);
			if (f.isFile())
				path = f.getParent();
			dialog.setFilterPath(path);
		}
		if (exts != null)
			dialog.setFilterExtensions(exts);
		String file = dialog.open();
		if (file != null) {
			if (task != null)
				task.cancel();
			
			task = creator.apply(glCanvas, file);
			if (task != null) {
				executor.submit(task);
			
				glCanvas.setCursor(display.getSystemCursor(SWT.CURSOR_WAIT));
			}
		}
	}
	
	protected void paint(Canvas canvas) {
		if (task != null) {
			task.paint(canvas);
		} else {
			canvas.clear(0xFFFFFFFF);
		}
	}
	
	private void shutdownTask() {
		if (task != null) {
			long timeout = 5000;
			task.cancel();
			while (task.isRunning() && timeout > 0) {
				timeout -= 100;
				try {
					if (executor.awaitTermination(100, TimeUnit.MILLISECONDS)) {////blocked even no task
						task = null;
						break;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
