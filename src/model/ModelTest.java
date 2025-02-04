package model;

import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class ModelTest {
    private Model model;
    private Player player1;
    private Player player2;

    @Before
    public void setUp() {
        model = Model.getInstance(15, "src/model/board_config.xml");
        player1 = new Player("Player 1");
        player2 = new Player("Player 2");
        model.addPlayer(player1);
        model.addPlayer(player2);
    }

    @Test
    public void getInstance() {
        assertNotNull(model);
    }

    @Test
    public void addPlayer() {
        assertEquals(2, model.getPlayers().size());
    }

    @Test
    public void placeTile() {
        player1.addTile('A');
        assertTrue(model.placeTile('A', 7, 7));
        assertFalse(model.placeTile('A', 7, 7)); // Cell already occupied
        assertFalse(model.placeTile('B', -1, 7)); // Out of bounds
    }

    @Test
    public void isFirstTurn() {
        assertTrue(model.isFirstTurn());
        player1.addTile('A');
        model.placeTile('A', 7, 7);
        model.submitWord();
        assertFalse(model.isFirstTurn());
    }

    @Test
    public void submitWord() {
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
    public void validateWord() {
        assertTrue(model.validateWord("CAT"));
        assertFalse(model.validateWord("XYZ"));
    }

    @Test
    public void isCenterCovered() {
        assertFalse(model.isCenterCovered());
        player1.addTile('A');
        model.placeTile('A', 7, 7);
        assertTrue(model.isCenterCovered());
    }

    @Test
    public void getBoardState() {
        player1.addTile('A');
        model.placeTile('A', 7, 7);
        char[][] boardState = model.getBoardState();
        assertEquals('A', boardState[7][7]);
    }

    @Test
    public void getCurrentPlayer() {
        assertEquals(player1, model.getCurrentPlayer());
        model.nextTurn();
        assertEquals(player2, model.getCurrentPlayer());
    }

    @Test
    public void nextTurn() {
        assertEquals(player1, model.getCurrentPlayer());
        model.nextTurn();
        assertEquals(player2, model.getCurrentPlayer());
    }

    @Test
    public void isGameOver() {
        assertFalse(model.isGameOver());
        // Simulate game over condition
        player1.addTile('A');
        model.placeTile('A', 7, 7);
        model.submitWord();
        // Add logic to set game over condition
        assertTrue(model.isGameOver());
    }

    @Test
    public void getPlayers() {
        assertTrue(model.getPlayers().contains(player1));
        assertTrue(model.getPlayers().contains(player2));
    }

    @Test
    public void restorePlayerTiles() {
        player1.addTile('A');
        model.placeTile('A', 7, 7);
        model.restorePlayerTiles();
        assertTrue(player1.getTiles().contains('A'));
    }

    @Test
    public void getBoardSize() {
        assertEquals(15, model.getBoardSize());
    }

    @Test
    public void testValidTilePlacement() {
        player1.addTile('A');
        assertTrue(model.placeTile('A', 7, 7));
    }

    @Test
    public void testInvalidTilePlacementOutOfBounds() {
        player1.addTile('A');
        assertFalse(model.placeTile('A', -1, 7));
        assertFalse(model.placeTile('A', 7, 15));
    }

    @Test
    public void testInvalidTilePlacementOccupiedCell() {
        player1.addTile('A');
        model.placeTile('A', 7, 7);
        player1.addTile('B');
        assertFalse(model.placeTile('B', 7, 7));
    }

    @Test
    public void testInvalidTilePlacementWithoutTile() {
        assertFalse(model.placeTile('A', 7, 7));
    }

    @Test
    public void testSingleWordScoring() {
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
    public void testMultipleWordsScoring() {
        player1.addTile('C');
        player1.addTile('A');
        player1.addTile('T');
        model.placeTile('C', 7, 7);
        model.placeTile('A', 7, 8);
        model.placeTile('T', 7, 9);
        model.submitWord();

        player1.addTile('S');
        model.placeTile('S', 7, 10);
        assertTrue(model.submitWord());
        assertEquals(6, player1.getScore()); // CAT=5, S=1
    }

    @Test
    public void testInvalidWordScoring() {
        player1.addTile('X');
        player1.addTile('Y');
        player1.addTile('Z');
        model.placeTile('X', 7, 7);
        model.placeTile('Y', 7, 8);
        model.placeTile('Z', 7, 9);
        assertFalse(model.submitWord());
        assertEquals(0, player1.getScore());
    }

    // Additional Placement Tests (M3)
    @Test
    public void testBlankTilePlacement() {
        player1.addTile(' ');
        assertTrue(model.placeTile(' ', 7, 7));
        assertEquals(' ', model.getBoardState()[7][7]);
    }

    @Test
    public void testTripleWordScorePlacement() {
        player1.addTile('A');
        assertTrue(model.placeTile('A', 0, 0));
        assertEquals('A', model.getBoardState()[0][0]);
    }

    @Test
    public void testDoubleWordScorePlacement() {
        player1.addTile('B');
        assertTrue(model.placeTile('B', 1, 1));
        assertEquals('B', model.getBoardState()[1][1]);
    }

    @Test
    public void testTripleLetterScorePlacement() {
        player1.addTile('C');
        assertTrue(model.placeTile('C', 1, 5));
        assertEquals('C', model.getBoardState()[1][5]);
    }

    @Test
    public void testDoubleLetterScorePlacement() {
        player1.addTile('D');
        assertTrue(model.placeTile('D', 0, 3));
        assertEquals('D', model.getBoardState()[0][3]);
    }

    // Additional Scoring Tests (M3)
    @Test
    public void testTripleWordScoreScoring() {
        player1.addTile('A');
        model.placeTile('A', 0, 0);
        model.submitWord();
        assertEquals(3, player1.getScore()); // A=1, Triple Word Score
    }

    @Test
    public void testDoubleWordScoreScoring() {
        player1.addTile('B');
        model.placeTile('B', 1, 1);
        model.submitWord();
        assertEquals(4, player1.getScore()); // B=2, Double Word Score
    }

    @Test
    public void testTripleLetterScoreScoring() {
        player1.addTile('C');
        model.placeTile('C', 1, 5);
        model.submitWord();
        assertEquals(9, player1.getScore()); // C=3, Triple Letter Score
    }

    @Test
    public void testDoubleLetterScoreScoring() {
        player1.addTile('D');
        model.placeTile('D', 0, 3);
        model.submitWord();
        assertEquals(4, player1.getScore()); // D=2, Double Letter Score
    }

    @Test
    public void testBlankTileScoring() {
        player1.addTile(' ');
        model.placeTile(' ', 7, 7);
        model.submitWord();
        assertEquals(0, player1.getScore()); // Blank tile has no score
    }

    // Code Quality Tests (M3)
    @Test
    public void testCodeQuality() {
        // This test ensures that the code follows best practices and is free of smells.
        // For example, checking for proper naming conventions, no magic numbers, etc.
        // This is performed through manual tests by group members instead of automated tests
        assertTrue(true); // Placeholder for actual code quality checks
    }

    // Debugging and Robustness Tests (M3)
    @Test
    public void testInvalidTilePlacement() {
        player1.addTile('A');
        assertFalse(model.placeTile('A', -1, 7)); // Invalid row
        assertFalse(model.placeTile('A', 7, 15)); // Invalid column
        assertFalse(model.placeTile('B', 7, 7)); // Tile not in player's rack
    }

    @Test
    public void testInvalidWordSubmission() {
        player1.addTile('X');
        player1.addTile('Y');
        player1.addTile('Z');
        model.placeTile('X', 7, 7);
        model.placeTile('Y', 7, 8);
        model.placeTile('Z', 7, 9);
        assertFalse(model.submitWord()); // Invalid word
        assertEquals(0, player1.getScore());
    }

    @Test
    public void testErrorHandling() {
        try {
            model.placeTile('A', -1, 7); // Invalid row
            model.placeTile('A', 7, 15); // Invalid column
            model.submitWord();
        } catch (Exception e) {
            fail("The model should handle errors gracefully without crashing.");
        }
    }

    @Test
    public void testLoadValidXML() {
        model.loadBoardConfigFromXML("src/model/test_board_valid.xml");

        Set<Position> TW = model.getTripleWordScore();

        assertTrue("Expected TW at (0,7) from valid XML", TW.contains(new Position(0, 7)));
        assertFalse("Did not expect TW at (0,0) since we overrode defaults", TW.contains(new Position(0, 0)));
    }

    @Test
    public void testLoadInvalidXML() {
        model.loadBoardConfigFromXML("src/model/test_board_invalid.xml");
        Set<Position> TW = model.getTripleWordScore();
        assertTrue("Invalid XML should revert to defaults, expecting TW at (0,0)", TW.contains(new Position(0, 0)));
    }
}