package com.ajawalker.fizz;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A visualization window.
 * @author aja
 *
 */
public abstract class Viz {

	private final BufferedImage buffer;
	private final JPanel panel;
	private long drawTime = 0;
	
	/**
	 * Creates a new visualization window.
	 * @param title the window title
	 * @param width the visualization width
	 * @param height the visualization height
	 */
	public Viz(String title, int width, int height) {
		
		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		JFrame frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension size = new Dimension(width, height);
		panel = new JPanel() {
			private static final long serialVersionUID = 7289981047950893095L;
			@Override
			public void paint(Graphics g) {
				synchronized (buffer) {
					g.drawImage(buffer, 0, 0, null);
				}
			}
		};
		panel.setPreferredSize(size);
		panel.setMinimumSize(size);
		panel.setMaximumSize(size);
		panel.setSize(size);
		panel.addComponentListener(new ComponentListener() {
			@Override
			public void componentHidden(ComponentEvent arg0) {
			}
			@Override
			public void componentMoved(ComponentEvent arg0) {
			}
			public void componentResized(ComponentEvent arg0) {
				System.out.format("%d %d%n", panel.getWidth(), panel.getHeight());
			}
			@Override
			public void componentShown(ComponentEvent arg0) {
			}
		});
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);	
		drawTime = System.nanoTime();

	}
	
	/**
	 * Causes the next frame to be drawn.
	 */
	public void draw() {
		synchronized (buffer) {
			long curTime = System.nanoTime();
			drawFrame(buffer.createGraphics(), curTime - drawTime);
			drawTime = curTime;
		}
		panel.repaint();
	}
	
	/**
	 * Draws the next frame.
	 * @param g the graphics instance to draw on
	 * @param elapsedNanos how many nano seconds have elapsed since the last draw
	 */
	protected abstract void drawFrame(Graphics2D g, long elapsedNanos);

}
