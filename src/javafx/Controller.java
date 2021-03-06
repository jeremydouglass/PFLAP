package javafx;

import static main.Consts.notificationData.noInitialState;
import static main.PFLAP.p;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import main.Consts;
import main.HistoryHandler;
import main.PFLAP;
import main.PFLAP.PApplet;
import main.Step;

import commands.Batch;
import commands.setColorBackground;
import commands.setColorState;
import commands.setColorStateSelected;
import commands.setColorTransition;
import model.Model;
import p5.Notification;
import processing.javafx.PSurfaceFX;

public class Controller implements Initializable {

	public static PSurfaceFX surface;
	public static Stage stage;
	private static final ExtensionFilter dat, png;

	static {
		dat = new FileChooser.ExtensionFilter("Machine files (*.dat)", "*.dat");
		png = new FileChooser.ExtensionFilter("Image File", "*.png");
	}

	@FXML
	private StackPane pflap;
	@FXML
	private MenuItem open, save, undo, redo, close, deleteSelection, selectAllStates, history;
	@FXML
	private MenuItem machine_DFA, machine_DPA, machine_mealy, machine_moore;
	@FXML
	public ColorPicker colourPicker_state, colourPicker_stateSelected, colourPicker_transition, colourPicker_background;
	@FXML
	private Menu machineMenu;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		PApplet.controller = this;
		Canvas canvas = (Canvas) surface.getNative();
		surface.fx.context = canvas.getGraphicsContext2D();
		pflap.getChildren().add(canvas);
		canvas.widthProperty().bind(pflap.widthProperty());
		canvas.heightProperty().bind(pflap.heightProperty());

		undo.setDisable(true);
		redo.setDisable(true);
		machine_DFA.setDisable(true);

		close.setAccelerator(KeyCombination.keyCombination("Esc"));

		colourPicker_state.setValue(PFLAP.stateColour);
		colourPicker_stateSelected.setValue(PFLAP.stateSelectedColour);
		colourPicker_transition.setValue(PFLAP.transitionColour);
		colourPicker_background.setValue(PFLAP.bgColour);

		colourPicker_state.setOnAction((ActionEvent e) -> {
			HistoryHandler.buffer(new setColorState(colourPicker_state.getValue()));
		});
		colourPicker_stateSelected.setOnAction((ActionEvent e) -> {
			HistoryHandler.buffer(new setColorStateSelected(colourPicker_stateSelected.getValue()));
		});
		colourPicker_transition.setOnAction((ActionEvent e) -> {
			HistoryHandler.buffer(new setColorTransition(colourPicker_transition.getValue()));
		});
		colourPicker_background.setOnAction((ActionEvent e) -> {
			HistoryHandler.buffer(new setColorBackground(colourPicker_background.getValue()));
		});

		machineMenu.getItems().forEach(m -> m.setOnAction(event -> changeMachine(m)));
	}

	public void setUndoEnable(boolean b) {
		undo.setDisable(!b);
	}

	public void setRedoEnable(boolean b) {
		redo.setDisable(!b);
	}

	public void stepModeHelp() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Alert alert = new Alert(AlertType.INFORMATION, Consts.helpStep, ButtonType.OK);
				alert.initOwner(stage);
				alert.setTitle("Help: Step Mode");
				alert.setHeaderText("Help: Step Mode");
				alert.showAndWait();
			}
		});
	}

	/*
	 * File Menu
	 */

	@FXML
	private void open() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Machine File");
		fileChooser.getExtensionFilters().add(dat);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
		File f = fileChooser.showOpenDialog(stage);
		if (f != null) {
			HistoryHandler.loadHistory(f.getAbsolutePath());
			machineMenu.getItems().forEach(m -> m.setDisable(false));
			switch (PFLAP.mode) {
				case DFA :
					machine_DFA.setDisable(true);
					break;
				case DPA :
					machine_DPA.setDisable(true);
					break;
				case MEALY :
					machine_mealy.setDisable(true);
					break;
				case MOORE :
					machine_moore.setDisable(true);
					break;
				default :
					break;
			}
		}
	}

	@FXML
	private void save() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Machine File");
		fileChooser.getExtensionFilters().add(dat);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
		File f = fileChooser.showSaveDialog(stage);
		if (f != null) {
			HistoryHandler.saveHistory(f.getAbsolutePath());
		}
	}

	@FXML
	private void close() {
		p.exit();
	}

	/*
	 * Edit Menu
	 */

	@FXML
	private void undo() {
		HistoryHandler.undo();
		redo.setDisable(false);
		if (HistoryHandler.getHistoryStateIndex() < -1) {
			undo.setDisable(true);
		}
	}

	@FXML
	private void redo() {
		HistoryHandler.redo();
		undo.setDisable(false);
	}

	@FXML
	private void reset() {
		PFLAP.reset();
	}

	@FXML
	private void deleteSelection() {
		p.deleteSelection();
	}

	@FXML
	private void selectAllStates() {
		PApplet.view.selectAllStates();
	}

	@FXML
	private void deleteAllStates() {
		HistoryHandler.buffer(new Batch(Batch.createDeleteBatch(Model.getStates())));
	}

	@FXML
	private void invertSelection() {
		PApplet.view.invertSelectedStates();
	}

	/*
	 * View Menu
	 */
	
	@FXML
	private void fullscreen() {
		stage.setFullScreen(!stage.isFullScreen());
	}

	@FXML
	private void resetZoom() {
		PFLAP.PApplet.setZoom(1);
	}

	@FXML
	private void resetColors() {
		PFLAP.stateColour = Color.rgb(255, 220, 0);
		PFLAP.stateSelectedColour = Color.rgb(0, 35, 255);
		PFLAP.transitionColour = Color.rgb(0, 0, 0);
		PFLAP.bgColour = Color.rgb(255, 255, 255);
	}

	@FXML
	private void saveAsImage() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Stage as Image");
		fileChooser.getExtensionFilters().add(png);
		File file = fileChooser.showSaveDialog(stage);
		if (file != null) {
			String directory = file.getAbsoluteFile().toString();
			p.saveFrame(directory);
			Notification.addNotification("Screenshot", "Image saved to " + directory);
		}
	}

	@FXML
	private void machineInformation() {
		String info = "Transitions: " + Model.nTransitions() + "\r\n" + "States: " + Model.nStates() + "\r\n" + "Type: "
				+ PFLAP.mode;
		Alert alert = new Alert(AlertType.INFORMATION, info, ButtonType.OK);
		alert.initOwner(stage);
		alert.setTitle("Machine Information");
		alert.setHeaderText(null);
		alert.showAndWait();
	}

	@FXML
	/**
	 * Toggle history-viewer.
	 */
	private void history() {
		PApplet.historyList.toggleVisible();
	}

	/*
	 * Machine Menu
	 */

	private void changeMachine(MenuItem i) {
		machineMenu.getItems().forEach(m -> m.setDisable(false));
		i.setDisable(true);
		switch (i.getId()) {
			case "machine_DFA" :
				PFLAP.reset(PFLAP.modes.DFA);
				break;
			case "machine_DPA" :
				PFLAP.reset(PFLAP.modes.DPA);
				break;
			case "machine_mealy" :
				PFLAP.reset(PFLAP.modes.MEALY);
				break;
			case "machine_moore" :
				PFLAP.reset(PFLAP.modes.MOORE);
				break;
			default :
				break;
		}
	}

	/*
	 * Input Menu
	 */

	@FXML
	private void step() {
		if (Model.getInitialState() != -1) {
			TextInputDialog dialog = new TextInputDialog("");
			dialog.setTitle("Step By State Input");
			dialog.setHeaderText(null);
			dialog.setContentText("Machine Input:");
			dialog.initOwner(stage);

			Optional<String> result;
			do {
				dialog.getEditor().clear();
				result = dialog.showAndWait();
				if (!result.isPresent()) {
					return;
				}
				if (result.get().contains(" ")) {
					Notification.addNotification("Invalid Input", "Input cannot contain ' ' characters.");
				}
			} while (result.get().contains(" "));

			switch (PFLAP.mode) {
				case DPA :
					dialog.setTitle("Initial Stack Symbol Input");
					dialog.setContentText("Stack Symbol Input");
					Optional<String> stackSymbol;
					do {
						dialog.getEditor().clear();
						stackSymbol = dialog.showAndWait();
						if (!result.isPresent()) {
							return;
						}
						if (stackSymbol.get().length() == 1) {
							model.Model.setInitialStackSymbol(stackSymbol.get().charAt(0)); // todo test for lambda
							Step.beginStep(result.get());
						} else {
							Notification.addNotification("Invalid Stack Symbol",
									"Initial Stack Symbol must be single character.");
						}
					} while (stackSymbol.get().length() != 1); // or set max length (=1)
					break;
				case DFA :
				case MEALY :
				case MOORE :
					Step.beginStep(result.get());
				default :
					break;
			}
		} else {
			Notification.addNotification(noInitialState);
		}
	}

	@FXML
	private void fastRun() {
		if (Model.getInitialState() != -1) {
			TextInputDialog dialog = new TextInputDialog("");
			dialog.setTitle("Fast-Run Input");
			dialog.setHeaderText(null);
			dialog.setContentText("Machine Input:");
			dialog.initOwner(stage);

			Optional<String> result;
			do {
				dialog.getEditor().clear();
				result = dialog.showAndWait();
				if (!result.isPresent()) {
					return;
				}
				if (result.get().contains(" ")) {
					Notification.addNotification("Invalid Input", "Input cannot contain ' ' characters.");
				}
			} while (result.get().contains(" "));

			switch (PFLAP.mode) {
				case DFA :
					Model.runMachine(result.get());
					break;
				case DPA :
					// todo
					dialog.setTitle("Initial Stack Symbol Input");
					dialog.setContentText("Stack Symbol Input");
					Optional<String> stackSymbol;
					do {
						dialog.getEditor().clear();
						stackSymbol = dialog.showAndWait();
						if (!result.isPresent()) {
							return;
						}
						if (stackSymbol.get().length() == 1) {
							model.Model.setInitialStackSymbol(stackSymbol.get().charAt(0)); // todo test for lambda
							Model.runMachine(result.get());
						} else {
							Notification.addNotification("Invalid Stack Symbol",
									"Initial Stack Symbol must be single character.");
						}
					} while (stackSymbol.get().length() != 1); // or set max length to 1?
					break;
				case MEALY :
					Model.runMachine(result.get());
					break;
				case MOORE :
					Model.runMachine(result.get()); // todo check every state has symbol
					break;
				default :
					break;

			}
		} else {
			Notification.addNotification(noInitialState);
		}
	}

	/*
	 * Help Menu
	 */

	@FXML
	private void help() {
		Alert alert = new Alert(AlertType.INFORMATION, Consts.helpPFLAP, ButtonType.OK);
		alert.initOwner(stage);
		alert.setTitle("Help");
		alert.setHeaderText("Help");
		alert.setGraphic(new ImageView("data/icon.png"));
		alert.showAndWait();
	}

	@FXML
	private void about() {
		Alert alert = new Alert(AlertType.INFORMATION, Consts.about, ButtonType.OK);
		alert.initOwner(stage);
		alert.setTitle("About PFLAP");
		alert.setHeaderText(Consts.title);
		alert.setGraphic(new ImageView("data/icon.png"));
		alert.showAndWait();
	}
}