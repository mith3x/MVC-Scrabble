package controller;

import model.Model;
import model.Player;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import view.View;

import javax.swing.*;
import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ControllerTests {
    private Model model = Model.getInstance(15, "src/model/board_config.xml");
    private View view = new View(15);
    private Player player1 = new Player("Player 1");
    private Player player2 = new Player("Player 2");
    private Controller controller = new Controller(model, view);


    public ControllerTests() {
        model.addPlayer(player1);
        model.addPlayer(player2);
    }


    @Test
    public void test1SubmitWord() {
        player1.addTile('C');
        player1.addTile('A');
        player1.addTile('T');
        model.placeTile('C', 7, 7);
        model.placeTile('A', 7, 8);
        model.placeTile('T', 7, 9);
        assertTrue(model.submitWord());
        assertEquals(5, player1.getScore()); // C=3, A=1, T=1
    }

    @Test
    public void test2SaveGame() {
        JButton saveButton = view.getSaveButton();
        saveButton.doClick();

        File saveFile = new File("game_save.ser");
        assertTrue(saveFile.exists());
    }

    @Test
    public void test3LoadGame() {
        JButton loadButton = view.getLoadButton();
        loadButton.doClick();

        // Verify the loaded game state
        assertEquals(15, model.getBoardSize());
        assertEquals(5, model.getPlayers().getFirst().getScore()); // C=3, A=1, T=1

        // Verify the board state
        char[][] board = model.getBoardState();
        assertEquals('C', board[7][7]);
        assertEquals('A', board[7][8]);
        assertEquals('T', board[7][9]);
    }


    @Test
    public void testTimerModeAndLabelContent() {
        // Set the model to timer mode
        model.setTimerMode(true);
        assertTrue(model.isTimerMode());

        // Update the view to reflect the timer mode
        view.update("timerModeChanged", model);

        // Check the contents of the label in the view
        JLabel timerLabel = view.getTimerLabel();
        assertTrue(timerLabel.isVisible());

        // Set the model to non-timer mode
        model.setTimerMode(false);
        assertFalse(model.isTimerMode());

        // Update the view to reflect the non-timer mode
        view.update("timerModeChanged", model);

        // Check the contents of the label in the view
        assertFalse(timerLabel.isVisible());
    }

    @Test
    public void testResetTimer() {
        // Simulate the reset timer notification
        view.update("resetTimer", model);

        // Check the contents of the label in the view
        JLabel timerLabel = view.getTimerLabel();
        assertEquals("Timer: 30s", timerLabel.getText());
    }

    @Test
    public void testUndo() {
        JButton undoButton = view.getUndoButton();

        player1.addTile('B');
        player1.addTile('A');
        player1.addTile('T');
        model.placeTile('B', 7, 7);
        model.placeTile('A', 7, 8);
        model.placeTile('T', 7, 9);

        undoButton.doClick();

        char[][] board = model.getBoardState();
        assertEquals('\0', board[7][9]);

        List<Character> playerTiles = player1.getTiles();
        assertTrue(playerTiles.contains('T'));
    }


    @Test
    public void testRedo() {
        JButton undoButton = view.getUndoButton();

        player1.addTile('B');
        player1.addTile('A');
        player1.addTile('T');
        model.placeTile('B', 7, 7);
        model.placeTile('A', 7, 8);
        model.placeTile('T', 7, 9);

        undoButton.doClick();

        char[][] board = model.getBoardState();
        assertEquals('\0', board[7][9]);

        List<Character> playerTiles = player1.getTiles();
        assertTrue(playerTiles.contains('T'));
    }

}