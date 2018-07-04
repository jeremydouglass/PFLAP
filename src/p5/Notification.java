package p5;

import main.Consts;
import main.PFLAP;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import static main.PFLAP.p;

import java.util.LinkedList;

import static main.Consts.notificationWidth;
import static main.Consts.notificationHeight;
import static main.Consts.notificationTextPadding;
import static main.Consts.notificationLifetime;
import static main.Consts.notificationLifetimeFast;
import static main.Consts.notificationLifetimeVeryFast;

/**
 * Handles both drawing and logic of notifications.
 */
public class Notification {

	private static LinkedList<Notification> notifications = new LinkedList<>();

	private static PImage background;
	private static final PVector positionTarget = new PVector(p.width - notificationWidth,
			p.height - notificationHeight);
	private PVector position = new PVector(p.width - notificationWidth, p.height);
	private String title, message;
	private int lifetime, startTime, alpha = 255;

	static {
		background = PFLAP.p.createImage(notificationWidth, notificationHeight, PApplet.ARGB);
		for (int i = 0; i < background.pixels.length; i++) {
			float a = PApplet.map(i, 0, background.pixels.length, 255, 0);
			background.pixels[i] = p.color(10, 100, 100, a);
		}
	}

	public Notification(String title, String message) {
		this.title = title;
		this.message = message;
	}

	public static void addNotification(Consts.notificationData data) {
		notifications.add(new Notification(data.title(), data.message()));
	}

	public static void run() {
		/**
		 * Call this within PApplet's draw() loop.
		 */
		if (!(notifications.isEmpty())) {
			notifications.getFirst().draw();
		}
	}

	private void draw() {
		if (startTime == 0) {
			p.tint(255, 255);
			startTime = p.frameCount;
			if (notifications.size() > 2) {
				if (notifications.size() < 6) {
					lifetime = notificationLifetimeFast;
				} else {
					lifetime = notificationLifetimeVeryFast;
				}
			} else {
				lifetime = notificationLifetime;
			}
		}
		if (position.y > positionTarget.y) {
			position.y -= 10;
		}
		if ((p.frameCount - startTime) >= lifetime) {
			p.tint(255, alpha);
			alpha -= 10;
			if (alpha < 0) {
				notifications.removeFirst();
			}
		}
		p.fill(255, 255, 255, alpha);
		p.textAlign(PApplet.LEFT, PApplet.TOP);
		p.image(background, position.x, position.y);
		p.textSize(14);
		// @formatter:off
		p.text("Notification: " + title,
				position.x + notificationTextPadding,
				position.y + notificationTextPadding,
				notificationWidth - notificationTextPadding, 
				notificationHeight - notificationTextPadding);
		p.textSize(12);
		p.textLeading(14);
		p.fill(0,0,0,alpha);
		p.text(message,
				position.x + notificationTextPadding,
				position.y + notificationTextPadding + 32,
				notificationWidth - notificationTextPadding, 
				notificationHeight - notificationTextPadding);
		 // @formatter:on
	}
}