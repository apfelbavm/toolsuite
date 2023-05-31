package widgets;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;

public class Throbber extends JComponent
{
	private static final long serialVersionUID = 1L;

	private Image image;
	private int width, height, centerX, centerY, offsetX, offsetY;
	// degrees per second
	private float rotationRate = 360.0f;
	// 1 divided by frames per second
	private float deltaTime = 1.0f / 30.0f;
	private float angle = 0;
	private boolean isRunning = false;
	private Timer timer;

	public Throbber(Image image, int width, int height, int paddingX, int paddingY)
	{
		this.width = width;
		this.height = height;
		this.centerX = (width + paddingX * 2) / 2;
		this.centerY = (height + paddingY * 2) / 2;
		this.offsetX = paddingX;
		this.offsetY = paddingY;
		this.image = image;
	}

	public void startAnimation()
	{
		timer = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				updateRotation();
			}
		};
		timer.scheduleAtFixedRate(task, 0, (int) (deltaTime * 1000.0f));
		isRunning = true;
	}

	public void stopAnimation()
	{
		isRunning = false;
		timer.cancel();
		timer.purge();
		repaint();
	}

	@Override
	public void paintComponent(Graphics g)
	{
		if (!isRunning) return;
		Graphics2D g2d = (Graphics2D) g;
		g2d.rotate(Math.toRadians(angle), centerX, centerY);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.drawImage(image, offsetX, offsetY, width, height, null);
	}

	private void updateRotation()
	{
		angle += rotationRate * deltaTime;
		if (angle > 360.0f) angle -= 360.0f;
		repaint();
	}
}
