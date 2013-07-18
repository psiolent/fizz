package com.ajawalker.fizz;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class ViewPort extends Component {

	private static double WHEEL_SCALE = 0.1;
	
	private static final long serialVersionUID = 1L;
	
	private double centerX = 0.0;
	private double centerY = 0.0;
	private double scale = 1.0;
	
	private int curWidth = 0;
	private int curHeight = 0;
	
	private AffineTransform transform = new AffineTransform();
	private BufferedImage activeBuffer = null;
	private BufferedImage currentBuffer = null;
	private final Object mutex = new Object();
	
	public ViewPort() {
		
		// init Component
		super();
		
		// handle wheel events
		addMouseWheelListener(new Wheeler());
		
		// handle drag events
		Dragger dragger = new Dragger();
		addMouseListener(dragger);
		addMouseMotionListener(dragger);
		
	}

	@Override
	public void paint(Graphics g) {
		synchronized (mutex) {
			if (currentBuffer != null) g.drawImage(currentBuffer, 0, 0, null);
		}
	}
	
	public Graphics2D swap() {
		synchronized (mutex) {
			
			int width = getWidth();
			int height = getHeight();
			if (width == 0) width = 1;
			if (height == 0) height = 1;
			
			// see if this is our first time
			if (currentBuffer == null) {
				
				// initialize
				curWidth = width;
				curHeight = height;
				createTransform();
				activeBuffer = new BufferedImage(curWidth, curHeight, BufferedImage.TYPE_INT_RGB);
				currentBuffer = new BufferedImage(curWidth, curHeight, BufferedImage.TYPE_INT_RGB);
				Graphics2D g = currentBuffer.createGraphics();
				g.setBackground(getBackground());
				g.clearRect(0, 0, curWidth, curHeight);
				
			} else {

				// swap buffers and schedule a repaint
				BufferedImage temp = currentBuffer;
				currentBuffer = activeBuffer;
				activeBuffer = temp;
				repaint();

				// see if dimensions changed
				if (curWidth != width || curHeight != height) {
					// adjust center so it stays in same place relative to upper left corner
					centerX += (width - curWidth) / (2 * scale);
					centerY -= (height - curHeight) / (2 * scale);
					// save new dimensions
					curWidth = width;
					curHeight = height;
					createTransform();
				}
				
				// see if active buffer is right size
				if (activeBuffer.getWidth() != curWidth || activeBuffer.getHeight() != curHeight) {
					// need a new buffer
					activeBuffer = new BufferedImage(curWidth, curHeight, BufferedImage.TYPE_INT_RGB);
				}
				
			}
			
			// get and clear graphics for active buffer
			Graphics2D g = activeBuffer.createGraphics();
			g.setBackground(getBackground());
			g.clearRect(0, 0, width, height);
			
			// set transform
			g.setTransform(transform);
			
			// and give them the graphics instance
			return g;
			
		}
	}
	
	private void createTransform() {
		transform = new AffineTransform();
		transform.translate(curWidth / 2.0, curHeight / 2.0);
		transform.scale(scale, -scale);
		transform.translate(-centerX, -centerY);
	}

	public double getCenterX() {
		synchronized (mutex) {
			return centerX;
		}
	}

	public void setCenterX(double centerX) {
		synchronized (mutex) {
			this.centerX = centerX;
			createTransform();
		}
	}

	public double getCenterY() {
		synchronized (mutex) {
			return centerY;
		}
	}

	public void setCenterY(double centerY) {
		synchronized (mutex) {
			this.centerY = centerY;
			createTransform();
		}
	}
	
	public Point2D getCenter() {
		synchronized (mutex) {
			return new Point2D.Double(centerX, centerY);
		}
	}
	
	public void setCenter(Point2D center) {
		synchronized (mutex) {
			centerX = center.getX();
			centerY = center.getY();
			createTransform();
		}
	}
	
	public void panCenter(double xoff, double yoff) {
		synchronized (mutex) {
			centerX += xoff;
			centerY += yoff;
			createTransform();
		}
	}
	
	public void panViewPort(double xoff, double yoff) {
		synchronized (mutex) {
			centerX -= xoff / scale;
			centerY += yoff / scale;
			createTransform();
		}
	}

	public double getScale() {
		synchronized (mutex) {
			return scale;
		}
	}

	public void setScale(double scale) {
		if (scale == 0.0) throw new IllegalArgumentException("cannot scale to 0");
		synchronized (mutex) {
			this.scale = scale;
			createTransform();
		}
	}
	
	public Point2D deviceToUser(Point2D p) {
		synchronized (mutex) {
			try {
				return transform.inverseTransform(p, null);
			} catch (NoninvertibleTransformException e) {
				// should never happen, as we don't allow 0 scaling
				throw new AssertionError(e);
			}
		}
	}
	
	public Point2D deviceToUser(double x, double y) {
		return deviceToUser(new Point2D.Double(x, y));
	}
	
	public Point2D userToDevice(Point2D p) {
		synchronized (mutex) {
			return transform.transform(p, null);
		}
	}
	
	public Point2D userToDevice(double x, double y) {
		return userToDevice(new Point2D.Double(x, y));
	}
	
	private class Wheeler implements MouseWheelListener {
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			synchronized (mutex) {

				// calc scale factor
				double scaleFactor = 1.0;
				if (e.getPreciseWheelRotation() < 0.0) {
					scaleFactor *= Math.pow(1.0 + WHEEL_SCALE, -e.getPreciseWheelRotation());
				} else {
					scaleFactor *= Math.pow(1.0 / (1.0 + WHEEL_SCALE), e.getPreciseWheelRotation());
				}
				
				// scale our scale
				scale *= scaleFactor;
				
				// recreate transform
				createTransform();
				
			}
		}
	}
	
	private class Dragger implements MouseListener, MouseMotionListener {

		private int lastX = 0;
		private int lastY = 0;
		
		@Override
		public void mousePressed(MouseEvent e) {
			lastX = e.getX();
			lastY = e.getY();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			panViewPort(e.getX() - lastX, e.getY() - lastY);
			lastX = e.getX();
			lastY = e.getY();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

	}
	
}
