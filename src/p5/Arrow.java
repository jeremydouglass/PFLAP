package p5;

import DFA.Machine;

import controlP5.CallbackEvent;
import controlP5.CallbackListener;
import controlP5.Textfield;

import main.Consts;
import main.PFLAP;

import processing.core.PApplet;
import processing.core.PVector;

public class Arrow {

	public static PApplet p;
	public State tail, head;
	private PVector tailXY, headXY;
	private float rotationOffset;
	private Textfield transitionSymbolEntry;
	public char transitionSymbol = ' ';
	private int ID;

	public Arrow(PVector startXY, State tail) {
		PFLAP.allowNewArrow = false;
		ID = this.hashCode();
		this.tail = tail;
		tailXY = startXY;
		headXY = tailXY;
		// @formatter:off
		transitionSymbolEntry = PFLAP.cp5.addTextfield(String.valueOf(ID))
				.setColorLabel(0)
				.keepFocus(true)
				.setLabel("")
				.setSize(30, 15)
				.hide()
				.addCallback(new CallbackListener() {
					// @formatter:on
					@Override
					public void controlEvent(CallbackEvent input) {
						if (transitionSymbolEntry.getStringValue().length() == 1) { //TODO && transition has unique symbol
							transitionSymbol = transitionSymbolEntry.getStringValue().charAt(0);
							PFLAP.cp5.remove(String.valueOf(ID));
							PFLAP.allowNewArrow = true;
							Machine.addTransition(tail, head, transitionSymbol);
						} else {
							// notification object make!!!
						}

					}
				});

	}

	public void update() {
		float theta1 = PFLAP.angleBetween(head.getPosition(), tail.getPosition());
		headXY = new PVector(head.getPosition().x + Consts.stateRadius * 0.5f * PApplet.cos(theta1),
				head.getPosition().y + Consts.stateRadius * 0.5f * PApplet.sin(theta1));

		float theta2 = PFLAP.angleBetween(tail.getPosition(), head.getPosition());
		tailXY = new PVector(tail.getPosition().x + Consts.stateRadius * 0.5f * PApplet.cos(theta2),
				tail.getPosition().y + Consts.stateRadius * 0.5f * PApplet.sin(theta2));
		rotationOffset = theta2;
		transitionSymbolEntry.setPosition((headXY.x + tailXY.x) / 2, (headXY.y + tailXY.y) / 2).show().setFocus(true);

		
//		if ( (-3/2*PApplet.PI)<=theta2 &&  (-1/2*PApplet.PI)>=theta2) {
//			rotationOffset = theta1;
//		}
		//if (p.frameCount % 5 == 0)p.println(theta1,theta2, "|", rotationOffset);
	}

	public void kill() {
		PFLAP.cp5.remove(String.valueOf(ID));
		PFLAP.arrows.remove(this);
		//Machine.removeTransition(tail, head, symbol); TODO
	}

	public void draw() {
		p.line(tailXY.x, tailXY.y, headXY.x, headXY.y);

		// p.text(transitionSymbol, (tailXY.x + headXY.x) / 2, (tailXY.y +
		// headXY.y) / 2);

		p.noFill();
		p.bezier(tailXY.x, tailXY.y, tailXY.x+65, tailXY.y-45, tailXY.x+65, tailXY.y+45, tailXY.x, tailXY.y); //RANDOM POINT  ON SPHERE
		p.pushMatrix();

		p.translate(headXY.x, headXY.y);
		p.rotate(rotationOffset);
		p.text(transitionSymbol, -PApplet.dist(tailXY.x, tailXY.y, headXY.x, headXY.y)/2, 7);
		//p.text(rotationOffset, -PApplet.dist(tailXY.x, tailXY.y, headXY.x, headXY.y)/2, 7);

		p.beginShape();
		p.vertex(-10, -7);
		p.vertex(0, 0);
		p.vertex(-10, 7);
		p.endShape();
		p.popMatrix();
	}

	public PVector getTailXY() {
		return tailXY;
	}

	public void setHeadXY(PVector headXY) {
		this.headXY = headXY;
		rotationOffset = PFLAP.angleBetween(tailXY, headXY);
	}
}