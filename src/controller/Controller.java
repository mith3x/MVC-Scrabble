package controller;

import model.AiPlayer;
import model.Model;
import model.Player;
import view.View;
import model.Position;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The Controller class handles the game logic and user interactions.
 */
public class Controller {
    private Model model;
    private View view;
    private Character selectedPlayerChar;
    private JButton selectedPlayerTileBtn;
    private TimerTask timerTask;

    /**
     * Constructs a Controller with the specified model and view.
     *
     * @param m the model
     * @param v the view
     */
    public Controller(Model m, View v) {
        model = m;
        view = v;
        selectedPlayerChar = null;
        selectedPlayerTileBtn = null;


        // Add action listeners to view components
        view.getSaveButton().addActionListener(e -> onSaveButtonClicked());
        view.getLoadButton().addActionListener(e -> onLoadButtonClicked());
        view.getSubmitButton().addActionListener(e -> onSubmitButtonClicked());
        view.getSkipTurnButton().addActionListener(e -> onSkipTurnClicked());
        view.getUndoButton().addActionListener(e -> onUndoButtonClicked());
        view.getRedoButton().addActionListener(e -> onRedoButtonClicked());

        // Loop through each cell button in the board and add action listener to each board cell button
        for (int row = 0; row < model.getBoardSize(); row++) {
            for (int col = 0; col < model.getBoardSize(); col++) {
                JButton cellButton = view.getBoardCellButton(row, col);
                final int currentRow = row;
                final int currentCol = col;
                cellButton.addActionListener(e -> onBoardCellClicked(currentRow, currentCol, (JButton) e.getSource()));
            }
        }

        // Add action listeners to player tile rack buttons
        for (JButton tileButton : view.getPlayerTiles()) {
            tileButton.addActionListener(e -> onPlayerTileSelected((JButton) e.getSource()));
        }

        if (model.isTimerMode()) {
            startTimer();
        }
    }

    /**
     * Handles the event when the save button is clicked.
     */
    private void onSaveButtonClicked() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("game_save.ser"))) {
            out.writeObject(model);
            view.showMessage("Game saved successfully!");
        } catch (IOException e) {
            view.showMessage("Error saving game: " + e.getMessage());
        }
    }

    /**
     * Handles the event when the load button is clicked.
     */
    private void onLoadButtonClicked() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("game_save.ser"))) {
            model = (Model) in.readObject();
            model.addObserver(view); // Reattach the view as an observer
            view.update("initialize", model);
            view.showMessage("Game loaded successfully!");
        } catch (FileNotFoundException e) {
            view.showMessage("Save file not found. Please ensure the save file exists.");
        } catch (InvalidClassException | StreamCorruptedException e) {
            view.showMessage("Save file is corrupted or incompatible. Please try saving the game again.");
        } catch (IOException | ClassNotFoundException e) {
            view.showMessage("Error loading game: " + e.getMessage());
        }
    }


    /**
     * Handles the event when a player tile is selected.
     *
     * @param tileButton the selected player tile button
     */
    public void onPlayerTileSelected(JButton tileButton) {
        if (selectedPlayerTileBtn != null) {
            selectedPlayerTileBtn.setBackground(null); // Deselect previous tile
        }

        selectedPlayerChar = tileButton.getText().charAt(0);
        selectedPlayerTileBtn = tileButton;
        // Highlight selected tile
        tileButton.setBackground(Color.CYAN);
    }

    /**
     * Handles the event when a board cell is clicked.
     *
     * @param row        the row of the board cell
     * @param col        the column of the board cell
     * @param cellButton the board cell button
     */
    public void onBoardCellClicked(int row, int col, JButton cellButton) {
        if (selectedPlayerChar != null) {
            if (model.placeTile(selectedPlayerChar, row, col)) {
                selectedPlayerTileBtn.setBackground(Color.GRAY); // unhighlight the selected tile
                selectedPlayerTileBtn.setEnabled(false); // disable the selected tile
                selectedPlayerChar = null;
                selectedPlayerTileBtn = null;
            }
        }
    }

    /**
     * Handles the event when the skip turn button is clicked.
     */
    public void onSkipTurnClicked() {
        if (model.isFirstTurn()){
            model.nextTurn(); // to send a notif
            return;
        }

        model.restorePlayerTiles();
        reenablePlayerTiles();
        model.nextTurn();
        handleAITurn();
        if (model.isTimerMode()) {
            resetTimer();
        }
    }

    /**
     * Handles the event when the submit button is clicked.
     */
    public void onSubmitButtonClicked() {
        if (model.submitWord()) {
            if (model.isFirstTurn() && !model.isCenterCovered()) {
                model.restorePlayerTiles();
            } else {
                model.nextTurn(); // Move to the next player
                reenablePlayerTiles();
                if (model.isGameOver()) {
                    endGame();
                } else {
                    handleAITurn();
                }
            }
            if (model.isTimerMode()) {
                resetTimer();
            }
        }
    }

    /**
     * Handles the event when the undo button is clicked.
     */
    public void onUndoButtonClicked() {
        Player currentPlayer = model.getCurrentPlayer();
        Position lastPositionPlayed = currentPlayer.history.removeLast();
        Character lastTilePlayed = model.removeCurrentPlacementTile(lastPositionPlayed);
        currentPlayer.undoHistory.add(lastPositionPlayed);
        model.removeTileFromBoard(lastPositionPlayed.row, lastPositionPlayed.col);
        currentPlayer.addTile(lastTilePlayed);
        view.enableTile(lastTilePlayed);
    }

    /**
     * Handles the event when the redo button is clicked.
     */
    public void onRedoButtonClicked() {
        Player currentPlayer = model.getCurrentPlayer();
        Position lastUndoPosition = currentPlayer.undoHistory.removeLast();
        Character lastUndoTile = currentPlayer.tiles.removeLast();
        currentPlayer.history.add(lastUndoPosition);
        model.addTileToBoard(lastUndoTile, lastUndoPosition.row, lastUndoPosition.col);
        view.disableTile(lastUndoTile);
    }

    /**
     * Re-enables the player tiles.
     */
    private void reenablePlayerTiles() {
        for (JButton tileButton : view.getPlayerTiles()) {
            tileButton.setEnabled(true);
            tileButton.setBackground(null);
        }
    }

    /**
     * Ends the game and displays the final scores.
     */
    private void endGame() {
        List<Player> players = model.getPlayers();
        StringBuilder finalScores = new StringBuilder("Game Over! Final Scores:\n");
        for (Player player : players) {
            finalScores.append(player.getName()).append(": ").append(player.getScore()).append("\n");
        }
        view.showMessage(finalScores.toString());
    }

    /**
     * Handles the AI player's turn.
     */
    private void handleAITurn() {
        if (model.isFirstTurn()) {
            return;
        }
        // silence pop up messages.
        model.toggleDisplayMessages();

        while (model.getCurrentPlayer().isAi()) {
            AiPlayer aiPlayer = (AiPlayer) model.getCurrentPlayer();
            if (!(aiPlayer.play())) {
                view.showMessage(aiPlayer.getName() + " skipped their turn.");
            }
            model.nextTurn();
            reenablePlayerTiles();
            if (model.isGameOver()) {
                endGame();
                break;
            }
        }

        // un silence pop up messages.
        model.toggleDisplayMessages();
    }


    private void startTimer() {
        Timer timer = new Timer();
        timerTask = new TimerTask() {
            int timeRemaining = 30;

            @Override
            public void run() {
                if (timeRemaining > 0) {
                    timeRemaining--;
                    SwingUtilities.invokeLater(() -> view.getTimerLabel().setText("Timer: " + timeRemaining + "s"));
                } else {
                    SwingUtilities.invokeLater(() -> {
                        view.showMessage("Time's up! Next player's turn.");
                        onSkipTurnClicked();
                    });
                    cancel();
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    private void resetTimer() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        startTimer();
    }
}