package commands;

import java.awt.Color;

import main.PFLAP;

//import main.appletVariables;

public class setBackgroundColor implements Command {

	private Color oldColor, newColor;
	
	public setBackgroundColor(Color c) {
		oldColor = PFLAP.bgColour;
		this.newColor = c;

	}

	@Override
	public void execute() {
		PFLAP.bgColour = newColor;
	}

	@Override
	public void undo() {
		PFLAP.bgColour = oldColor;
	}

	@Override
	public String description() {
		return "Set background color: " + newColor;
	}

}