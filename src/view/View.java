package view;


import model.Model;
import model.ModelObserver;
import model.Player;
import model.Position;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * The View class represents the GUI of the Scrabble game.
 * It is responsible for displaying the game board, tile rack, and status messages to the user.
 * It also handles user input and forwards it to the controller.
 */
public class View extends JFrame implements ModelObserver {
    private JPanel boardPanel;
    private JPanel tileRackPanel;
    private JLabel statusLabel;
    private JLabel timerLabel;
    private JPanel statusPanel;


    private List<JButton> playerTiles;


    private JButton selectedTileButton; //del this?
    private JButton submitButton;
    private JButton skipTurnButton;
    private JButton undoButton;
    private JButton redoButton;

    private JButton saveButton;
    private JButton loadButton;

    // stuff the update() method will update.
    private int boardSize; // init update method
    private int center;

    private Player currentPlayer;
    private char[][] board;

    private boolean displayMessages = true;

    private final Color DARK_RED = new Color(139, 0, 0); // Dark Red
    private final Color DARK_PINK = new Color(255, 182, 193); // Light Pink
    private final Color DARK_BLUE = new Color(173, 216, 230); // Light Blue
    private final Color DARK_CYAN = new Color(0, 139, 139); // Dark Cyan
    private final Color DARK_ORANGE = new Color(255, 140, 0); // Dark Orange

    /**
     * Constructs a View with the specified board size.
     *
     * @param boardSize the size of the board
     */
    public View(int boardSize) {
        this.boardSize = boardSize;
        this.center = boardSize / 2;
        // GUI setup ONLY. Setup ups are in order of declaration.

        // Setup JFrame
        setTitle("Scrabble Game");
        setSize(900, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());


        // Board panel
        boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(boardSize, boardSize));
        add(boardPanel, BorderLayout.CENTER);

        // Initialize board
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                JButton cellButton = initializeEachBoardCell(row, col);
                boardPanel.add(cellButton);
            }
        }


        // Tile rack panel
        tileRackPanel = new JPanel(new FlowLayout());
        add(tileRackPanel, BorderLayout.SOUTH);


        // Status label
        statusLabel = new JLabel("Welcome to Scrabble!");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Timer label
        timerLabel = new JLabel();
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Status panel
        statusPanel = new JPanel(new GridLayout(2, 1));
        statusPanel.add(statusLabel);
        statusPanel.add(timerLabel);
        add(statusPanel, BorderLayout.NORTH);


        // Tile buttons
        playerTiles = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            JButton tileButton = new JButton();
            tileButton.setFont(new Font("Arial", Font.PLAIN, 20));
            tileButton.setFocusable(false);
            tileRackPanel.add(tileButton);
            playerTiles.add(tileButton);
        }

        // Selected tile button
        selectedTileButton = null;

        // Right Panel
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(3, 1));
        add(rightPanel, BorderLayout.EAST);

        // Submit button
        submitButton = new JButton("Submit");
        rightPanel.add(submitButton);

        // Undo button
        undoButton = new JButton("Undo");
        rightPanel.add(undoButton);

        // Redo button
        redoButton = new JButton("Redo");
        rightPanel.add(redoButton);

        // --------------------------------------

        // Left Panel
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(3, 1));
        add(leftPanel, BorderLayout.WEST);

        // Skip turn button
        skipTurnButton = new JButton("Skip Turn");
        leftPanel.add(skipTurnButton);

        // Save Button
        saveButton = new JButton("Save Game");
        leftPanel.add(saveButton);

        // Load Button
        loadButton = new JButton("Load Game");
        leftPanel.add(loadButton);


        // The game isn't actually ready till the controller says so, via the update() method
        setVisible(false);
    }

    /**
     * Initializes each cell of the game board with the appropriate color.
     *
     * @param row the row of the cell
     * @param col the column of the cell
     * @return the JButton representing the cell
     */
    private JButton initializeEachBoardCell(int row, int col) {
        JButton cellButton = new JButton();
        cellButton.setFont(new Font("Arial", Font.PLAIN, 12));
        cellButton.setFocusable(false);
        cellButton.setOpaque(true);

        // Triple Word Score (3×WS) - Red
        if ((row == 0 && (col == 0 || col == 7 || col == 14)) ||
                (row == 7 && (col == 0 || col == 14)) ||

                (row == 14 && (col == 0 || col == 7 || col == 14))) {
            cellButton.setBackground(DARK_RED);
        }
        // Double Word Score (2×WS) - Pink
        else {
            if ((row == col && (row == 1 || row == 2 || row == 3 || row == 4 || row == 10 || row == 11 || row == 12 || row == 13)) ||
                    ((row + col == 14) && (row == 1 || row == 2 || row == 3 || row == 4 || row == 10 || row == 11 || row == 12 || row == 13))) {
                cellButton.setBackground(DARK_PINK);
            }
            // Triple Letter Score (3×LS) - Blue
            else if (((row == 1 || row == 13) && (col == 5 || col == 9)) ||
                    ((row == 5 || row == 9) && (col == 1 || col == 13)) ||
                    (row == 5 && col == 5) || (row == 5 && col == 9) ||
                    (row == 9 && col == 5) || (row == 9 && col == 9)) {
                cellButton.setBackground(DARK_BLUE);
            }
            // Double Letter Score (2×LS) - Cyan
            else if (((row == 0 || row == 14) && (col == 3 || col == 11)) ||
                    ((row == 2 || row == 12) && (col == 6 || col == 8)) ||
                    ((row == 3 || row == 11) && (col == 0 || col == 14)) ||
                    ((row == 6 || row == 8) && (col == 2 || col == 12)) ||
                    ((row == 6 || row == 8) && (col == 6 || col == 8))) {
                cellButton.setBackground(DARK_CYAN);
            }
            // Center Tile - Orange
            else if (row == 7 && col == 7) {
                cellButton.setBackground(DARK_ORANGE);
            } else {
                cellButton.setBackground(Color.WHITE);
            }
        }

        return cellButton;
    }

    /**
     * Returns the save button.
     *
     * @return the save button
     */
    public JButton getSaveButton() {
        return saveButton;
    }

    /**
     * Returns the load button.
     *
     * @return the load button
     */
    public JButton getLoadButton() {
        return loadButton;
    }

    /**
     * Returns the list of player tiles.
     *
     * @return the list of player tiles
     */
    public List<JButton> getPlayerTiles() {
        return playerTiles;
    }

    /**
     * Returns the JButton representing the cell at the specified row and column.
     *
     * @param row the row of the cell
     * @param col the column of the cell
     * @return the JButton representing the cell
     */

    public JButton getBoardCellButton(int row, int col) {
        return (JButton) boardPanel.getComponent(row * boardSize + col);
    }

    /**
     * Returns the submit button.
     *
     * @return the submit button
     */
    public JButton getSubmitButton() {
        return submitButton;
    }

    /**
     * Returns the skip turn button.
     *
     * @return the skip turn button
     */
    public JButton getSkipTurnButton() {
        return skipTurnButton;
    }

    /**
     * Returns the undo button
     *
     * @return the undo button
     */
    public JButton getUndoButton() {
        return undoButton;
    }

    /**
     * Returns the redo button
     *
     * @return the redo button
     */
    public JButton getRedoButton() {
        return redoButton;
    }

    /**
     * Updates the game board with the given board state.
     *
     * @param board the board state to update
     */
    public void updateBoard(char[][] board, Model m) {
        // Assume you have a reference to the model:
        Set<Position> TW = m.getTripleWordScore();
        Set<Position> DW = m.getDoubleWordScore();
        Set<Position> TL = m.getTripleLetterScore();
        Set<Position> DL = m.getDoubleLetterScore();

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                JButton cell = getBoardCellButton(row, col);
                char c = board[row][col];
                cell.setText(c == '\0' ? "" : String.valueOf(c));

                Position pos = new Position(row, col);
                if (TW.contains(pos)) {
                    cell.setBackground(DARK_RED);
                } else if (DW.contains(pos)) {
                    cell.setBackground(DARK_PINK);
                } else if (TL.contains(pos)) {
                    cell.setBackground(DARK_BLUE);
                } else if (DL.contains(pos)) {
                    cell.setBackground(DARK_CYAN);
                } else if (row == center && col == center) {
                    cell.setBackground(DARK_ORANGE);
                } else {
                    cell.setBackground(Color.WHITE);
                }
            }
        }
        boardPanel.revalidate();
        boardPanel.repaint();
    }


    /**
     * Updates the tile rack with the given tiles.
     *
     * @param tiles the tiles to update
     */
    private void loadPlayerTiles(List<Character> tiles) {
        for (int i = 0; i < playerTiles.size(); i++) {
            if (i < tiles.size() && tiles.get(i) != '\0') {
                playerTiles.get(i).setText(tiles.get(i).toString());
                playerTiles.get(i).setVisible(true);
            } else {
                playerTiles.get(i).setText("");
                playerTiles.get(i).setVisible(false);
            }
        }
        tileRackPanel.revalidate();
        tileRackPanel.repaint();
    }


    /**
     * Updates the status label with the given player.
     *
     * @param p the player to update
     */
    public void updateStatus(Player p, int remainingTiles) {
        String status = "Current player: " + p.getName() + " | Score: " + p.getScore() + " | Remaining Tiles: " + remainingTiles;
        statusLabel.setText(status);
    }

    /**
     * Displays a message to the user.
     *
     * @param message the message to display
     */
    public void showMessage(String message) {
        if ((displayMessages)) {
            JOptionPane.showMessageDialog(this, message);
        }
    }

    /**
     * Handles the update method of the observer pattern.
     *
     * @param message the message to update
     * @param m       the model to update
     */
    @Override
    public void update(String message, Model m) {
        // note: keep all the handle methods are under the update method.
        switch (message) {
            case "initialize":
                handleInitialize(m);
                break;
            case "toggleMessages":
                this.displayMessages = m.getDisplayMessages();
                break;
            case "board":
                handleBoardUpdate(m);
                break;
            case "centerNotCovered":
                handleCenterNotCovered(m);
                break;
            case "invalidWord":
                handleInvalidWord(m);
                break;
            case "noWordFound":
                handleNoWordFound(m);
                break;
            case "wordSubmitted":
                handleWordSubmitted(m);
                break;
            case "noAdjacentTiles":
                handleAdjacentWord(m);
                break;
            case "tilePlaced":
                handleBoardUpdate(m);
                break;
            case "updatePlayerTiles":
            case "resetTiles":
                loadPlayerTiles(m.getCurrentPlayer().getTiles());
                break;
            case "nextTurn":
                handleNextTurn(m);
                break;
            case "firstTurn":
                showMessage("Human must play first.");
                break;
            case "gameOver":
                handleGameOver();
                break;
            case "resetTimer":
                handleResetTimer();
                break;
            case "timerModeChanged":
                handleTimerModeChanged(m);
                break;
            default:
                break;
        }
    }


    private void handleResetTimer() {
        timerLabel.setText("Timer: 30s");
    }

    private void handleTimerModeChanged(Model m) {
        timerLabel.setVisible(m.isTimerMode());
    }

    /**
     * Handles the initialize message.
     *
     * @param m the model to initialize
     */
    private void handleInitialize(Model m) {
        this.boardSize = m.getBoardSize();
        this.center = boardSize / 2;
        this.currentPlayer = m.getCurrentPlayer();
        loadPlayerTiles(m.getCurrentPlayer().getTiles());
        updateStatus(m.getCurrentPlayer(), m.getRemainingTiles());
        updateBoard(m.getBoardState(), m);
        setVisible(true);
    }

    /**
     * Handles the board update message.
     *
     * @param m the model to update
     */
    private void handleBoardUpdate(Model m) {
        updateBoard(m.getBoardState(), m);
    }

    /**
     * Handles the center not covered message.
     *
     * @param m the model to update
     */
    private void handleCenterNotCovered(Model m) {
        showMessage("First word must be placed covering the center square.");
        updateBoard(m.getBoardState(), m);
        updateStatus(m.getCurrentPlayer(), m.getRemainingTiles());
        loadPlayerTiles(m.getCurrentPlayer().getTiles());
    }

    /**
     * Handles the invalid word message.
     *
     * @param m the model to update
     */
    private void handleInvalidWord(Model m) {
        showMessage("Invalid word! Please try again.");
        updateBoard(m.getBoardState(), m);
        updateStatus(m.getCurrentPlayer(), m.getRemainingTiles());
        loadPlayerTiles(m.getCurrentPlayer().getTiles());
    }

    /**
     * Handles the no word found message.
     *
     * @param m the model to update
     */
    private void handleNoWordFound(Model m) {
        showMessage("No new word found! Please try again.");
        updateBoard(m.getBoardState(), m);

    }

    /**
     * Handles the adjacent word message.
     *
     * @param m the model to update
     */
    private void handleAdjacentWord(Model m) {
        showMessage("Word not adjacent! Please try again.");
        updateBoard(m.getBoardState(), m);

    }

    /**
     * Handles the word submitted message.
     *
     * @param m the model to update
     */
    private void handleWordSubmitted(Model m) {
        showMessage("Word accepted! Your score has been updated.");
        updateBoard(m.getBoardState(), m);
        updateStatus(m.getCurrentPlayer(), m.getRemainingTiles());
        loadPlayerTiles(m.getCurrentPlayer().getTiles());
    }

    /**
     * Handles the next turn message.
     *
     * @param m the model to update
     */
    private void handleNextTurn(Model m) {
        // hide player tiles
        if (m.getCurrentPlayer().isAi()) {
            for (JButton tileButton : playerTiles) {
                tileButton.setVisible(false);
            }
        } else {
            for (JButton tileButton : playerTiles) {
                tileButton.setVisible(true);
            }
        }

        updateStatus(m.getCurrentPlayer(), m.getRemainingTiles());
        loadPlayerTiles(m.getCurrentPlayer().getTiles());
    }

    /**
     * Handles the game over message.
     */
    private void handleGameOver() {
        showMessage("Game Over!");
        setVisible(false);
    }

    /**
     * Returns the selected tile button.
     *
     * @return the selected tile button
     */
    public JLabel getTimerLabel() {
        return timerLabel;
    }

    /**
     * Enables a player tile
     */
    public void enableTile(Character c) {
        for (JButton button : playerTiles) {
            if (button.getText().equals(c.toString()) && !button.isEnabled()) {
                button.setEnabled(true);
                button.setBackground(null);
                break;
            }
        }
    }

    /**
     * Disables a player tile
     */
    public void disableTile(Character c) {
        for (JButton button : playerTiles) {
            if (button.getText().equals(c.toString()) && button.isEnabled()) {
                button.setEnabled(false);
                button.setBackground(Color.cyan);
            }
        }
    }
}
