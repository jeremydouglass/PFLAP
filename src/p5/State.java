package p5;

import java.util.ArrayList;

import commands.addTransition;
import controlP5.CallbackEvent;
import controlP5.CallbackListener;
import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.ScrollableList;
import controlP5.Slider;
import controlP5.Textfield;

import static main.Consts.stateRadius;
import static main.Consts.initialNodeIndicatorSize;

import main.HistoryHandler;
import main.PFLAP;
import static main.PFLAP.p;
import static main.PFLAP.stateColour;
import static main.PFLAP.stateSelectedColour;
import static main.PFLAP.machine;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class State {

	private String label;
	private ControlP5 cp5, resizeGUI;
	private ScrollableList stateOptions;
	private Slider sizeSlider;
	private ControlListener listener, sizeSliderListener;
	private PVector position, selectedPosition;
	private ArrayList<Arrow> arrowHeads = new ArrayList<>();
	private ArrayList<Arrow> arrowTails = new ArrayList<>();
	private boolean selected = false, accepting = false, initial = false, highlighted = false;
	// private boolean running; //TODO
	private int radius = stateRadius, highlightColor, liveID;
	private static Textfield rename;
	private static State renameState;
	private static PGraphics initialIndicator;

	static { // only one rename box for class
		// @formatter:off
		rename = PFLAP.cp5.addTextfield("Rename State");
		rename.hide()
		.setSize(45, 20)
		.setFocus(false) //TODO change for state size?
		.addCallback(new CallbackListener() {
			@Override
			public void controlEvent(CallbackEvent input) {
				if (input.getAction() == 100) {
				renameState.label = rename.getStringValue();
				rename.setFocus(false).hide();
				rename.clear();
				renameState = null;
				}
			}
		});
		// @formatter:on

		initialIndicator = p.createGraphics(50, 50);
		initialIndicator.beginDraw();
		initialIndicator.fill(255, 0, 0);
		initialIndicator.noStroke();
		initialIndicator.triangle(0, 0, 0, initialNodeIndicatorSize * 2, initialNodeIndicatorSize,
				initialNodeIndicatorSize);
		initialIndicator.endDraw();
	}

	public State(PVector XY, int liveID) {
		switch (PFLAP.mode) {
			case DFA :
				// DFA.addNode(this); TODO replaced in addState command
				break;
			case DPA :
//				machine.addNode(this); TODO replaced in addState command?
				break;
			case MEALY :
				break;
			case MOORE :
				break;
			default :
				break;
		}

		label = "q" + liveID;
		this.liveID = liveID;
		position = XY;
		initCP5();
	}

	private void initCP5() {
		cp5 = new ControlP5(p);
		cp5.hide();

		resizeGUI = new ControlP5(p);
		// @formatter:off
		sizeSlider = resizeGUI.addSlider("Size Slider")
				.setWidth(100)
				.setHeight(15)
				.setValue(stateRadius)
				.setMin(25)
				.setMax(150)
				.setSliderMode(Slider.FLEXIBLE)
				.hide()
				;
		// @formatter:on
		sizeSliderListener = new ControlListener() {
			@Override
			public void controlEvent(ControlEvent radiusChange) {
				radius = (int) sizeSlider.getValue();
				arrowHeads.forEach(a -> a.update());
				arrowTails.forEach(a -> a.update());
			}
		};
		sizeSlider.addListener(sizeSliderListener);

		listener = new ControlListener() {
			@Override
			public void controlEvent(ControlEvent optionSelected) {
				cp5.hide();
				switch ((int) optionSelected.getValue()) {
					case 0 : // Add Self-Transition
						HistoryHandler.buffer(new addTransition(State.this, State.this));
						break;
					case 1 : // Set As Initial
						PFLAP.nodes.forEach(s -> s.initial = false);
						initial = true;
						machine.setInitialState(State.this);
						break;
					case 2 : // Toggle Accepting
						accepting = !accepting;
						break;
					case 3 : // Relabel
						renameState = State.this;
						rename.setPosition(position.x - rename.getWidth() / 2, position.y + 30);
						rename.setFocus(true);
						rename.show();
						break;
					case 4 : // Resize
						// TODO
						sizeSlider.bringToFront();
						resizeGUI.show();
						sizeSlider.show();
						break;
					case 5 : // Delete
						HistoryHandler.buffer(new commands.deleteState(State.this));
						break;
				}

			}
		};
		stateOptions = cp5.addScrollableList("Options");
		// @formatter:off
		stateOptions.setType(1)
				.addItems(new String[]{"Add Self-Transition.", "Set As Inititial", "Toggle Accepting", "Relabel", "Resize", "Delete"})
				.open()
				.addListener(listener);
		// @formatter:on
	}

	public void kill() {
		for (Arrow a : arrowHeads) {
			machine.removeTransition(a);
		}
		machine.deleteNode(this);
		if (initial) {
			machine.setInitialState(null);
		}
		arrowHeads.forEach(a -> a.parentKill());
		arrowTails.forEach(a -> a.parentKill());
	}

	protected void childKill(Arrow a) {
		// Transition arrows call this.
		if (arrowHeads.contains(a)) {
			arrowHeads.remove(a);
		} else {
			arrowTails.remove(a);
		}
	}

	public void draw() {
		p.strokeWeight(3);
		if (initial) {
			p.image(initialIndicator, position.x - radius / 2 - 14, position.y - initialNodeIndicatorSize);
		}
		if (!selected) {
			if (highlighted) {
				p.fill(highlightColor);
			} else {
				p.fill(stateColour.getRGB());
			}
			p.ellipse(position.x, position.y, radius, radius);
			p.fill(0);
		} else {
			p.fill(stateSelectedColour.getRGB());
			p.ellipse(position.x, position.y, radius, radius);
			p.fill(255);
		}
		if (accepting) {
			p.noFill();
			p.strokeWeight(2);
			p.ellipse(position.x, position.y, radius - 9, radius - 9);
		}
		p.textSize(PApplet.max(14, (PApplet.sqrt(radius) * 10) - 50));
		p.text(label, position.x, position.y);
		highlighted = false;
	}

	public void setPosition(PVector position) {
		this.position = position;
		cp5.setPosition((int) this.position.x + 10, (int) this.position.y + 10);
		resizeGUI.setPosition((int) (position.x)-50, (int) (position.y));
		arrowHeads.forEach(a -> a.update());
		arrowTails.forEach(a -> a.update());
	}

	public void select() {
		stateOptions.open();
		selected = true;
		selectedPosition = position;
	}

	public void deselect() {
		cp5.hide();
		resizeGUI.hide();
		selected = false;
		selectedPosition = null;
	}

	public void toggleAccepting() {
		accepting = !accepting;
	}

	public void highLight(int highlightColor) {
		this.highlightColor = highlightColor;
		highlighted = true;
		deselect();
	}

	public PVector getPosition() {
		return position;
	}

	public void setAsInitial() {
		initial = true;
		machine.setInitialState(this);
	}

	public PVector getSelectedPosition() {
		return selectedPosition;
	}

	public String getLabel() {
		return label;
	}

	public int getRadius() {
		return radius;
	}
	
	public int getID() {
		return liveID;
	}

	public void addArrowHead(Arrow a) {
		arrowHeads.add(a);
	}

	public void addArrowTail(Arrow a) {
		arrowTails.add(a);
	}

	public void hideUI() {
		cp5.hide();
	}

	public void showUI() {
		cp5.show();
	}

	public boolean UIOpen() {
		return cp5.isVisible();
	}

	public boolean isAccepting() {
		return accepting;
	}

	public int connectedTailCount() {
		return arrowTails.size();
	}
	
	public int connectedHeadCount() {
		return arrowHeads.size();
	}

	public boolean isMouseOver() {
		return (cp5.isMouseOver() && cp5.isVisible()) || (resizeGUI.isMouseOver() && sizeSlider.isVisible());
	}
}