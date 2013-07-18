package com.ajawalker.fizz;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;


public class ViewPortTest {
	
	private static final int SIZE = 400;

	public static void main(String[] args) {

		Dimension size = new Dimension(SIZE, SIZE);
		
		final ViewPort vp = new ViewPort();
		vp.setPreferredSize(size);
		vp.setBackground(Color.BLACK);
		
		JFrame frame = new JFrame("View Point Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBackground(Color.BLACK);
		frame.getContentPane().add(vp);
		frame.pack();
		frame.setVisible(true);

		javax.swing.Timer timer = new javax.swing.Timer(50, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Graphics2D g = vp.swap();
				g.drawRect(-SIZE / 2, -SIZE / 2, SIZE, SIZE);
				for (int i = 0; i < 2000; i++) {
					double a = Math.random();
					double b = Math.random();
					int r = (int)(a * 256);
					int gr = (int)(b * 256);
					int x = (int)(a * (SIZE - 20) - SIZE / 2);
					int y = (int)(b * (SIZE - 20) - SIZE / 2);
					g.setColor(new Color(r, gr, 255));
					g.drawOval(x, y, 20, 20);
				}
				g.drawLine(
						(int)(Math.random() * SIZE - SIZE / 2),
						(int)(Math.random() * SIZE - SIZE / 2),
						(int)(Math.random() * SIZE - SIZE / 2),
						(int)(Math.random() * SIZE - SIZE / 2));
			}
		});
		timer.start();

	}
	
}
