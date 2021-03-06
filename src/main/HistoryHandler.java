package main;

import static main.PFLAP.p;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import commands.Command;
import commands.setColorBackground;
import commands.setColorState;
import commands.setColorStateSelected;
import commands.setColorTransition;
import main.PFLAP.PApplet;
import main.PFLAP.modes;
import model.Model;
import p5.Notification;

public final class HistoryHandler {

	private static int historyStateIndex = -1;
	private static final ArrayList<Command> history = new ArrayList<>();
	private static final Queue<Command> pendingExecute = new LinkedList<>();

	private HistoryHandler() {
		throw new AssertionError();
	}

	public static void reset() {
		history.clear();
		pendingExecute.clear();
		historyStateIndex = -1;
		// PApplet.historyList.update(); todo
	}

	public static void buffer(Command c) {
		pendingExecute.add(c);
	}

	protected static void executeBufferedCommands() {
		if (!(pendingExecute.isEmpty())) {
			PApplet.controller.setUndoEnable(true);
			if (historyStateIndex != history.size() - 1) {
				// if index not at end clear history from then
				for (int i = history.size() - 1; i > historyStateIndex; i--) {
					history.remove(i);
				}
			}

			while (!(pendingExecute.isEmpty())) {
				pendingExecute.peek().execute();
				history.add(pendingExecute.poll());
				historyStateIndex += 1;
			}
			PApplet.historyList.update();
		}
	}

	public static void undo() {
		if (historyStateIndex > -1) { // can undo first command
			history.get(historyStateIndex).undo();
			historyStateIndex -= 1;
			PApplet.controller.setRedoEnable(true);
		} else {
			PApplet.controller.setUndoEnable(false);
		}
	}

	public static void redo() {
		if (historyStateIndex <= history.size() - 2) { // if not at end of history
			historyStateIndex += 1;
			history.get(historyStateIndex).execute();
			PApplet.controller.setUndoEnable(true);
		} else {
			PApplet.controller.setRedoEnable(false);
		}
	}

	protected static void movetoIndex(int index) {
		if (index > history.size() + 1 || index < -1) {
			return;
		}

		if (index > historyStateIndex) {
			while (index > historyStateIndex) {
				redo();
			}
		} else {
			if (index < historyStateIndex) {
				while (index < historyStateIndex) {
					undo();
				}
			}
		}
	}

	public static int getHistoryStateIndex() {
		return historyStateIndex;
	}

	protected static ArrayList<Command> export() {
		return history;
	}

	public static void saveHistory(String path) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
			ArrayList<Command> liveHistory = new ArrayList<>();
			for (int i = history.size() - historyStateIndex - 1; i < history.size(); i++) {
				if (!(history.get(i) instanceof setColorBackground
						|| history.get(i) instanceof setColorState
						|| history.get(i) instanceof setColorStateSelected
						|| history.get(i) instanceof setColorTransition)) {
					liveHistory.add(history.get(i)); // don't save color changes
				}
			}
			Object[] objects = new Object[]{liveHistory, PApplet.view.save(), PFLAP.mode};
			out.writeObject(objects);
			out.close();
		} catch (IOException e) {
			Notification.addNotification("Saving Failed", "Could not save the machine file.");
			System.err.println(e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public static void loadHistory(String path) {
		try {
			p.noLoop();
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
			Object[] data = (Object[]) in.readObject();
			PFLAP.mode = (modes) data[2];
			PApplet.reset();
			PApplet.view.load(data[1]); // p5 states
			history.addAll((ArrayList<Command>) data[0]);
			history.forEach(c -> c.execute());
			historyStateIndex = history.size() - 1;
			// PApplet.historyList.update(); todo
			Model.setnextStateID(Model.nStates());
			in.close();
		} catch (InvalidClassException e) {
			Notification.addNotification("Loading Failed",
					"Could not load the machine file. (You are attempting to load a save from a different version of PFLAP).");
		} catch (IOException | ClassNotFoundException e) {
			Notification.addNotification("Loading Failed", "Could not load the machine file (IO Error).");
		}
		p.loop();
	}
}