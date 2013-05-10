package com.ajawalker.fizz;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class VizTest {

	public static void main(String[] args) {
		
		final int size = 600;

		final Viz viz = new Viz("test", size, size) {
			private final Random r = new Random();
			@Override
			protected void drawFrame(Graphics2D g, long elapsedNanos) {
				g.setColor(new Color(r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat()));
				g.drawLine(r.nextInt(size), r.nextInt(size), r.nextInt(size), r.nextInt(size));
			}
		};
		
		new Thread() {
			@Override
			public void run() {
				while (true) {
					viz.draw();
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
					}
				}
			}
		}.start();
		
	}

}
