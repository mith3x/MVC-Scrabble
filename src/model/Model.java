package model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.*;


/**
 * Represents the game model.
 */
public class Model implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static Model instance;

    private final char[][] board;
    private final List<Player> players;
    private int currentPlayerIndex;
    private final TileBag tileBag;
    private transient List<ModelObserver> observers; // Marked as transient
    private final Set<String> wordlist;
    private final int boardSize;
    private final Map<Position, Character> currentTurnPlacements;
    private boolean isFirstTurn;
    private boolean displayMessages = true; // whether to notify observers
    private boolean timerMode;


    private final Set<Position> TRIPLE_WORD_SCORE = new HashSet<>();
    private final Set<Position> DOUBLE_WORD_SCORE = new HashSet<>();
    private final Set<Position> TRIPLE_LETTER_SCORE = new HashSet<>();
    private final Set<Position> DOUBLE_LETTER_SCORE = new HashSet<>();

    /**
     * Initializes the game model with the specified board size.
     *
     * @param boardSize       the size of the board (e.g., 15 for a 15x15 board)
     * @param boardConfigPath the path to the board configuration XML file
     */
    private Model(int boardSize, String boardConfigPath) {
        this.boardSize = boardSize;
        this.board = new char[boardSize][boardSize];
        this.players = new ArrayList<>();
        this.currentPlayerIndex = 0;
        this.tileBag = new TileBag();
        this.observers = new ArrayList<>();
        this.wordlist = loadWordList("src/model/wordlist.txt");
        this.currentTurnPlacements = new HashMap<>();
        this.isFirstTurn = true;
        loadBoardConfigFromXML(boardConfigPath);

    }

    /**
     * Loads the board configuration from an XML file.
     * If the file is invalid or not found, default configurations are loaded instead.
     *
     * @param xmlFileName the path to the XML configuration file
     */
    void loadBoardConfigFromXML(String xmlFileName) {
        File xmlFile = new File(xmlFileName);
        if (!xmlFile.exists()) {
            System.out.println("XML configuration file not found. Using default configuration.");
            loadDefaultPremiumSquares();
            return;
        }

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            doc.getDocumentElement().normalize();

            // Validate the presence of <size>
            NodeList sizeList = doc.getElementsByTagName("size");
            if (sizeList.getLength() == 0) {
                System.out.println("No <size> element found in XML. Using defaults.");
                loadDefaultPremiumSquares();
                return;
            }

            int xmlSize = Integer.parseInt(sizeList.item(0).getTextContent().trim());
            if (xmlSize != this.boardSize) {
                System.out.println("Warning: XML board size (" + xmlSize + ") does not match expected Model board size (" + boardSize + "). Using defaults.");
                loadDefaultPremiumSquares();
                return;
            }

            // Validate the presence of <premiumSquares>
            NodeList premiumSquaresList = doc.getElementsByTagName("premiumSquares");
            if (premiumSquaresList.getLength() == 0) {
                System.out.println("No <premiumSquares> element found. Using defaults.");
                loadDefaultPremiumSquares();
                return;
            }

            NodeList squares = doc.getElementsByTagName("square");
            // Temporary sets to hold data before we finalize them
            Set<Position> tempTW = new HashSet<>();
            Set<Position> tempDW = new HashSet<>();
            Set<Position> tempTL = new HashSet<>();
            Set<Position> tempDL = new HashSet<>();

            boolean validationFailed = false;

            for (int i = 0; i < squares.getLength(); i++) {
                Element squareElement = (Element) squares.item(i);
                String rowStr = squareElement.getAttribute("row");
                String colStr = squareElement.getAttribute("col");
                String type = squareElement.getAttribute("type");

                // Validate attributes
                if (rowStr.isEmpty() || colStr.isEmpty() || type.isEmpty()) {
                    System.out.println("Invalid square definition: Missing row/col/type. Using defaults.");
                    validationFailed = true;
                    break;
                }

                int row = Integer.parseInt(rowStr);
                int col = Integer.parseInt(colStr);

                // Check range
                if (row < 0 || row >= boardSize || col < 0 || col >= boardSize) {
                    System.out.println("Square (" + row + "," + col + ") is out of board range. Using defaults.");
                    validationFailed = true;
                    break;
                }

                // Check type validity
                Position pos = new Position(row, col);
                switch (type) {
                    case "TW":
                        // Check no overlap
                        if (tempDW.contains(pos) || tempTL.contains(pos) || tempDL.contains(pos) || tempTW.contains(pos)) {
                            System.out.println("Square (" + row + "," + col + ") defined multiple times. Using defaults.");
                            validationFailed = true;
                        } else {
                            tempTW.add(pos);
                        }
                        break;
                    case "DW":
                        if (tempTW.contains(pos) || tempTL.contains(pos) || tempDL.contains(pos) || tempDW.contains(pos)) {
                            System.out.println("Square (" + row + "," + col + ") defined multiple times. Using defaults.");
                            validationFailed = true;
                        } else {
                            tempDW.add(pos);
                        }
                        break;
                    case "TL":
                        if (tempTW.contains(pos) || tempDW.contains(pos) || tempDL.contains(pos) || tempTL.contains(pos)) {
                            System.out.println("Square (" + row + "," + col + ") defined multiple times. Using defaults.");
                            validationFailed = true;
                        } else {
                            tempTL.add(pos);
                        }
                        break;
                    case "DL":
                        if (tempTW.contains(pos) || tempDW.contains(pos) || tempTL.contains(pos) || tempDL.contains(pos)) {
                            System.out.println("Square (" + row + "," + col + ") defined multiple times. Using defaults.");
                            validationFailed = true;
                        } else {
                            tempDL.add(pos);
                        }
                        break;
                    default:
                        System.out.println("Unknown premium type: " + type + ". Using defaults.");
                        validationFailed = true;
                        break;
                }

                if (validationFailed) {
                    break;
                }
            }

            if (validationFailed) {
                loadDefaultPremiumSquares();
            } else {
                // Assign to actual sets only after successful validation
                TRIPLE_WORD_SCORE.clear();
                DOUBLE_WORD_SCORE.clear();
                TRIPLE_LETTER_SCORE.clear();
                DOUBLE_LETTER_SCORE.clear();

                TRIPLE_WORD_SCORE.addAll(tempTW);
                DOUBLE_WORD_SCORE.addAll(tempDW);
                TRIPLE_LETTER_SCORE.addAll(tempTL);
                DOUBLE_LETTER_SCORE.addAll(tempDL);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to parse or validate XML. Using defaults.");
            loadDefaultPremiumSquares();
        }
    }

    /**
     * Loads default premium squares if validation fails or XML not found.
     */
    private void loadDefaultPremiumSquares() {
        // Clear existing sets
        TRIPLE_WORD_SCORE.clear();
        DOUBLE_WORD_SCORE.clear();
        TRIPLE_LETTER_SCORE.clear();
        DOUBLE_LETTER_SCORE.clear();

        // Populate TRIPLE_WORD_SCORE
        TRIPLE_WORD_SCORE.add(new Position(0, 0));
        TRIPLE_WORD_SCORE.add(new Position(0, 7));
        TRIPLE_WORD_SCORE.add(new Position(0, 14));
        TRIPLE_WORD_SCORE.add(new Position(7, 0));
        TRIPLE_WORD_SCORE.add(new Position(7, 14));
        TRIPLE_WORD_SCORE.add(new Position(14, 0));
        TRIPLE_WORD_SCORE.add(new Position(14, 7));
        TRIPLE_WORD_SCORE.add(new Position(14, 14));

        // Populate DOUBLE_WORD_SCORE
        DOUBLE_WORD_SCORE.add(new Position(1, 1));
        DOUBLE_WORD_SCORE.add(new Position(2, 2));
        DOUBLE_WORD_SCORE.add(new Position(3, 3));
        DOUBLE_WORD_SCORE.add(new Position(4, 4));
        DOUBLE_WORD_SCORE.add(new Position(10, 10));
        DOUBLE_WORD_SCORE.add(new Position(11, 11));
        DOUBLE_WORD_SCORE.add(new Position(12, 12));
        DOUBLE_WORD_SCORE.add(new Position(13, 13));
        DOUBLE_WORD_SCORE.add(new Position(1, 13));
        DOUBLE_WORD_SCORE.add(new Position(2, 12));
        DOUBLE_WORD_SCORE.add(new Position(3, 11));
        DOUBLE_WORD_SCORE.add(new Position(4, 10));
        DOUBLE_WORD_SCORE.add(new Position(10, 4));
        DOUBLE_WORD_SCORE.add(new Position(11, 3));
        DOUBLE_WORD_SCORE.add(new Position(12, 2));
        DOUBLE_WORD_SCORE.add(new Position(13, 1));

        // Populate TRIPLE_LETTER_SCORE
        TRIPLE_LETTER_SCORE.add(new Position(1, 5));
        TRIPLE_LETTER_SCORE.add(new Position(1, 9));
        TRIPLE_LETTER_SCORE.add(new Position(5, 1));
        TRIPLE_LETTER_SCORE.add(new Position(5, 5));
        TRIPLE_LETTER_SCORE.add(new Position(5, 9));
        TRIPLE_LETTER_SCORE.add(new Position(5, 13));
        TRIPLE_LETTER_SCORE.add(new Position(9, 1));
        TRIPLE_LETTER_SCORE.add(new Position(9, 5));
        TRIPLE_LETTER_SCORE.add(new Position(9, 9));
        TRIPLE_LETTER_SCORE.add(new Position(9, 13));
        TRIPLE_LETTER_SCORE.add(new Position(13, 5));
        TRIPLE_LETTER_SCORE.add(new Position(13, 9));

        // Populate DOUBLE_LETTER_SCORE
        DOUBLE_LETTER_SCORE.add(new Position(0, 3));
        DOUBLE_LETTER_SCORE.add(new Position(0, 11));
        DOUBLE_LETTER_SCORE.add(new Position(2, 6));
        DOUBLE_LETTER_SCORE.add(new Position(2, 8));
        DOUBLE_LETTER_SCORE.add(new Position(3, 0));
        DOUBLE_LETTER_SCORE.add(new Position(3, 14));
        DOUBLE_LETTER_SCORE.add(new Position(6, 2));
        DOUBLE_LETTER_SCORE.add(new Position(6, 6));
        DOUBLE_LETTER_SCORE.add(new Position(6, 8));
        DOUBLE_LETTER_SCORE.add(new Position(6, 12));
        DOUBLE_LETTER_SCORE.add(new Position(8, 2));
        DOUBLE_LETTER_SCORE.add(new Position(8, 6));
        DOUBLE_LETTER_SCORE.add(new Position(8, 8));
        DOUBLE_LETTER_SCORE.add(new Position(8, 12));
        DOUBLE_LETTER_SCORE.add(new Position(11, 0));
        DOUBLE_LETTER_SCORE.add(new Position(11, 3));
        DOUBLE_LETTER_SCORE.add(new Position(11, 11));
        DOUBLE_LETTER_SCORE.add(new Position(11, 14));
        DOUBLE_LETTER_SCORE.add(new Position(12, 6));
        DOUBLE_LETTER_SCORE.add(new Position(12, 8));
        DOUBLE_LETTER_SCORE.add(new Position(14, 3));
        DOUBLE_LETTER_SCORE.add(new Position(14, 11));
    }


    /**
     * Custom serialization logic for transient field observers.
     *
     * @param in the input stream
     * @throws IOException if an I/O error occurs
     */
    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.observers = new ArrayList<>(); // Reinitialize transient field
    }

    /**
     * Gets the singleton instance of the model.
     *
     * @param boardSize the size of the board
     * @return the singleton instance of the model
     */
    public static Model getInstance(int boardSize, String boardConfigPath) {
        if (instance == null) {
            instance = new Model(boardSize, boardConfigPath);
        }
        return instance;
    }

    /**
     * Resets the singleton instance of the model.
     */
    public static void resetInstance() {
        instance = null;
    }

    /**
     * Loads a word list from a file.
     *
     * @param fileName the name of the file to load
     * @return a set of words loaded from the file
     */
    private Set<String> loadWordList(String fileName) {
        Set<String> words = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            System.err.println("Error loading dictionary: " + e.getMessage());
        }
        return words;
    }

    /**
     * Adds a player to the game.
     *
     * @param player the player to add
     */
    public void addPlayer(Player player) {
        player.replenishTiles(tileBag);
        players.add(player);
    }

    /**
     * Adds AI players to the game.
     *
     * @param numAiPlayers
     */
    public void addAiPlayers(int numAiPlayers) {
        for (int i = 0; i < numAiPlayers; i++) {
            AiPlayer aiPlayer = new AiPlayer("AI " + (i + 1), getWordList(), this);
            aiPlayer.replenishTiles(tileBag);
            players.add(aiPlayer);

        }
    }

    /**
     * Places a tile on the board at the specified position.
     *
     * @param tile the tile to place
     * @param row  the row to place the tile
     * @param col  the column to place the tile
     * @return true if the tile was placed, false otherwise
     */
    public boolean placeTile(char tile, int row, int col) {

        // check if the player has the tile
        if (!(getCurrentPlayer().hasTile(tile))) {
            return false;
        }

        // check if the position is valid
        if (!isValidPosition(row, col)) {
            return false;
        }

        // check if the position is empty
        if (board[row][col] != '\0') {
            return false;
        }

        board[row][col] = tile;
        getCurrentPlayer().history.add(new Position(row, col));
        currentTurnPlacements.put(new Position(row, col), tile);
        getCurrentPlayer().removeTile(tile);
        notifyObservers("tilePlaced");
        return true;
    }

    /**
     * Checks if it is the first turn of the game.
     *
     * @return true if it is the first turn, false otherwise
     */
    public boolean isFirstTurn() {
        return isFirstTurn;
    }

    /**
     * Submits the current word placements and updates the game state.
     *
     * @return true if the word placements are valid, false otherwise
     */
    public boolean submitWord() {
        if (!isFirstTurn && !hasAdjacentTiles()) {
            restorePlayerTiles(); // Undo invalid move
            notifyObservers("noAdjacentTiles");
            System.out.println("No adjacent tiles");
            return false;
        }


        List<String> newWords = getAllNewWords();
        //System.out.println(newWords);
        if (newWords.isEmpty()) {
            //revertPlacements();  // should this be here? adding it
            restorePlayerTiles();
            notifyObservers("noWordFound");
            System.out.println("No words found");
            return false; // No tiles placed
        }

        // Validate all new words
        for (String word : newWords) {
            if (!validateWord(word)) {
                //revertPlacements();  // should this be here? adding it
                restorePlayerTiles();
                notifyObservers("invalidWord");
                System.out.println("Invalid word: " + word);
                return false; // At least one word is invalid
            }
        }

        // Check if this was the first turn and validate center coverage
        if (isFirstTurn) {
            if (!coversCenter()) {
                // Invalid first move; revert placements

                //revertPlacements();
                restorePlayerTiles();
                clearPlacements();
                notifyObservers("centerNotCovered");
                System.out.println("Center not covered");

                return false;
            }
            isFirstTurn = false; // First turn completed
        }

        // All words are valid, calculate total score
        int totalScore = calculateTotalScore(newWords);


        getCurrentPlayer().addScore(totalScore);

        // After validation, replenish player's tiles
        getCurrentPlayer().replenishTiles(tileBag);


        // Clear current turn placements
        clearPlacements();
        notifyObservers("wordSubmitted");
        return true;
    }


    /**
     * Checks if there are adjacent tiles to the current turn's placements.
     *
     * @return true if there are adjacent tiles, false otherwise
     */
    private boolean hasAdjacentTiles() {
        for (Position pos : currentTurnPlacements.keySet()) {
            int row = pos.row;
            int col = pos.col;

            if (isValidPosition(row - 1, col) && board[row - 1][col] != '\0' && !currentTurnPlacements.containsKey(new Position(row - 1, col))) {
                return true;
            }

            if (isValidPosition(row + 1, col) && board[row + 1][col] != '\0' && !currentTurnPlacements.containsKey(new Position(row + 1, col))) {
                return true;
            }

            if (isValidPosition(row, col - 1) && board[row][col - 1] != '\0' && !currentTurnPlacements.containsKey(new Position(row, col - 1))) {
                return true;
            }

            if (isValidPosition(row, col + 1) && board[row][col + 1] != '\0' && !currentTurnPlacements.containsKey(new Position(row, col + 1))) {
                return true;
            }
        }
        return false;
    }


    /**
     * Gets all new words formed by the current turn's placements.
     *
     * @return a list of new words
     */
    public List<String> getAllNewWords() {
        List<String> newWords = new ArrayList<>();
        Set<String> uniqueWords = new HashSet<>();

        for (Position pos : currentTurnPlacements.keySet()) {
            // Check horizontal word
            String horizontalWord = getWordAtPosition(pos.row, pos.col, true);
            if (horizontalWord.length() > 1 && !uniqueWords.contains(horizontalWord)) {
                newWords.add(horizontalWord);
                uniqueWords.add(horizontalWord);
            }

            // Check vertical word
            String verticalWord = getWordAtPosition(pos.row, pos.col, false);
            if (verticalWord.length() > 1 && !uniqueWords.contains(verticalWord)) {
                newWords.add(verticalWord);
                uniqueWords.add(verticalWord);
            }
        }

        return newWords;
    }

    /**
     * Gets the word at the specified position in the given direction.
     *
     * @param row          the row of the starting position
     * @param col          the column of the starting position
     * @param isHorizontal true if the word is horizontal, false if it is vertical
     * @return the word at the specified position
     */
    private String getWordAtPosition(int row, int col, boolean isHorizontal) {
        StringBuilder word = new StringBuilder();
        int deltaRow = isHorizontal ? 0 : -1;
        int deltaCol = isHorizontal ? -1 : 0;

        // Move to the start of the word
        int currentRow = row;
        int currentCol = col;
        while (isValidPosition(currentRow + deltaRow, currentCol + deltaCol) && board[currentRow + deltaRow][currentCol + deltaCol] != '\0') {
            currentRow += deltaRow;
            currentCol += deltaCol;
        }

        // Move forward and build the word
        deltaRow = isHorizontal ? 0 : 1;
        deltaCol = isHorizontal ? 1 : 0;
        while (isValidPosition(currentRow, currentCol) && board[currentRow][currentCol] != '\0') {
            word.append(board[currentRow][currentCol]);
            currentRow += deltaRow;
            currentCol += deltaCol;
        }

        return word.toString();
    }

    /**
     * Validates a word against the dictionary.
     *
     * @param word the word to validate
     * @return true if the word is valid, false otherwise
     */
    public boolean validateWord(String word) {
        return wordlist.contains(word.toLowerCase());
    }

    /**
     * Gets the word list.
     *
     * @return the word list
     */
    private Set<String> getWordList() {
        return wordlist;
    }

    /**
     * Calculates the total score for a list of words.
     *
     * @param words the list of words to calculate the score for
     * @return the total score
     */
    public int calculateTotalScore(List<String> words) {
        int total = 0;

        if (words == null || words.isEmpty()) {
            return total; // No words formed
        }

        Set<Position> processedPositions = new HashSet<>(); // Track tiles already processed

        for (String word : words) {
            int wordScore = 0;
            int wordMultiplier = 1;

            for (Position pos : currentTurnPlacements.keySet()) {
                if (processedPositions.contains(pos)) {
                    continue; // Skip already processed positions
                }

                char tile = currentTurnPlacements.get(pos);
                int letterScore = getTileScore(tile);
                int originalLetterScore = letterScore; // Save the original score for comparison

                // Apply premium letter scores
                String premiumEffect = "None";
                if (DOUBLE_LETTER_SCORE.contains(pos)) {
                    letterScore *= 2; // Double Letter Score
                    premiumEffect = "Double Letter Score (2×LS)";
                } else if (TRIPLE_LETTER_SCORE.contains(pos)) {
                    letterScore *= 3; // Triple Letter Score
                    premiumEffect = "Triple Letter Score (3×LS)";
                }

                // Print individual tile details
                System.out.printf("Tile: %c, Position: (%d, %d), Base Score: %d, Premium: %s, Final Letter Score: %d%n", tile, pos.row, pos.col, originalLetterScore, premiumEffect, letterScore);

                wordScore += letterScore; // Add letter score to the word's total score

                // Apply premium word multipliers
                if (DOUBLE_WORD_SCORE.contains(pos)) {
                    wordMultiplier *= 2; // Double Word Score
                    System.out.printf("Tile: %c at (%d, %d) contributes to Double Word Score (2×WS)%n", tile, pos.row, pos.col);
                } else if (TRIPLE_WORD_SCORE.contains(pos)) {
                    wordMultiplier *= 3; // Triple Word Score
                    System.out.printf("Tile: %c at (%d, %d) contributes to Triple Word Score (3×WS)%n", tile, pos.row, pos.col);
                }

                // Mark this position as processed
                processedPositions.add(pos);
            }

            // Print word-specific details
            System.out.printf("Word: %s, Word Score Before Multiplier: %d, Word Multiplier: %d, Final Word Score: %d%n", word, wordScore, wordMultiplier, wordScore * wordMultiplier);

            // Apply the word multiplier to the word's total score
            total += wordScore * wordMultiplier;
        }

        return total;
    }

    /**
     * Calculates the score for a word.
     *
     * @param word the word to calculate the score for
     * @return the score for the word
     */
    public int calculateWordScore(String word) {
        int score = 0;
        // Implement premium squares logic here (to be done in Milestone 3)
        // For now, sum the tile scores
        for (char c : word.toCharArray()) {
            score += getTileScore(c);
        }
        return score;
    }

    /**
     * Gets the score for a tile based on Scrabble letter values.
     *
     * @param tile the tile to get the score for
     * @return the score for the tile
     */
    private int getTileScore(char tile) {
        switch (Character.toUpperCase(tile)) {
            case 'A':
            case 'E':
            case 'I':
            case 'O':
            case 'N':
            case 'R':
            case 'T':
            case 'L':
            case 'S':
            case 'U':
                return 1;
            case 'D':
            case 'G':
                return 2;
            case 'B':
            case 'C':
            case 'M':
            case 'P':
                return 3;
            case 'F':
            case 'H':
            case 'V':
            case 'W':
            case 'Y':
                return 4;
            case 'K':
                return 5;
            case 'J':
            case 'X':
                return 8;
            case 'Q':
            case 'Z':
                return 10;
            default:
                return 0;
        }
    }

    /**
     * Checks if the center of the board is covered.
     *
     * @return true if the center is covered, false otherwise
     */
    public boolean isCenterCovered() {
        int center = boardSize / 2;
        return board[center][center] != '\0'; // Check if the center tile is occupied
    }


    /**
     * Clears the current turn's placements.
     */
    private void clearPlacements() {
        currentTurnPlacements.clear();
    }

    /**
     * Reverts the current turn's placements.
     */
    private void revertPlacements() {
        for (Position pos : currentTurnPlacements.keySet()) {
            board[pos.row][pos.col] = '\0';
        }
        clearPlacements();
    }

    /**
     * Checks if the first word covers the center of the board.
     *
     * @return true if the center is covered, false otherwise
     */
    private boolean coversCenter() {
        int center = boardSize / 2;
        return board[center][center] != '\0';
    }

    /**
     * Gets the current game board state.
     *
     * @return the current game board state
     */
    public char[][] getBoardState() {
        return board;
    }

    /**
     * Gets the current player's tiles.
     *
     * @return the current player's tiles
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /**
     * Moves to the next player's turn.
     */
    public void nextTurn() {
        if (isFirstTurn()) {
            notifyObservers("firstTurn");
            return;
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        getCurrentPlayer().undoHistory.clear();
        getCurrentPlayer().history.clear();
        notifyObservers("nextTurn");
    }

    /**
     * Checks if the game is over.
     *
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() {
        // Example condition: when the tile bag is empty and a player has less than 3 tiles
        boolean status = tileBag.isEmpty() && players.stream().anyMatch(player -> player.getTiles().size() < 3);
        if (status) {
            notifyObservers("gameOver");
        }
        return status;
    }

    /**
     * Gets the list of players.
     *
     * @return the list of players
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Adds an observer to the model.
     *
     * @param observer the observer to add
     */
    public void addObserver(ModelObserver observer) {
        observers.add(observer);
    }

    /**
     * Notifies all observers that the model has changed.
     */
    private void notifyObservers(String eventType) {
        for (ModelObserver observer : observers) {
            observer.update(eventType, this);
        }
    }

    /**
     * Restores the current player's tiles after an invalid submission.
     */
    public void restorePlayerTiles() {
        Player currentPlayer = getCurrentPlayer();
        for (Position pos : currentTurnPlacements.keySet()) {
            char tile = board[pos.row][pos.col];
            currentPlayer.addTile(tile);
            board[pos.row][pos.col] = '\0'; // Remove the tile from the board
        }
        clearPlacements();
        notifyObservers("resetTiles");
    }

    /**
     * Checks if a position is within the board.
     *
     * @param row the row of the position
     * @param col the column of the position
     * @return true if the position is valid, false otherwise
     */
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < boardSize && col >= 0 && col < boardSize;
    }

    /**
     * Gets the size of the board.
     *
     * @return the size of the board
     */
    public int getBoardSize() {
        return boardSize;
    }

    /**
     * Gets the triple word score positions.
     *
     * @return the triple word score positions
     */
    public Set<Position> getTripleWordScore() {
        return TRIPLE_WORD_SCORE;
    }

    /**
     * Gets the double word score positions.
     *
     * @return the double word score positions
     */
    public Set<Position> getDoubleWordScore() {
        return DOUBLE_WORD_SCORE;
    }

    /**
     * Gets the triple letter score positions.
     *
     * @return the triple letter score positions
     */
    public Set<Position> getTripleLetterScore() {
        return TRIPLE_LETTER_SCORE;
    }

    /**
     * Gets the double letter score positions.
     *
     * @return the double letter score positions
     */
    public Set<Position> getDoubleLetterScore() {
        return DOUBLE_LETTER_SCORE;
    }

    /**
     * Toggles the display of messages.
     */
    public void toggleDisplayMessages() {
        this.displayMessages = !(this.displayMessages);
        notifyObservers("toggleMessages");
    }

    /**
     * Gets the display messages status.
     *
     * @return the display messages status
     */
    public boolean getDisplayMessages() {
        return this.displayMessages;
    }

    /**
     * Gets the remaining tiles in the tile bag.
     *
     * @return the number of remaining tiles
     */
    public int getRemainingTiles() {
        return tileBag.remainingTiles();
    }

    /**
     * Sets the timer mode.
     *
     * @param timerMode the timer mode to set
     */
    public void setTimerMode(boolean timerMode) {
        this.timerMode = timerMode;
        notifyObservers("timerModeChanged");
    }

    /**
     * Gets the timer mode.
     *
     * @return the timer mode
     */
    public boolean isTimerMode() {
        return timerMode;
    }

    /**
     * Resets the timer.
     */
    public void resetTimer() {
        notifyObservers("resetTimer");
    }

    /**
     * Removes the tile at the position passed as an argument.
     * Tile is removed as part of current player's turn.
     *
     * @param position
     * @return the tile that is removed
     */
    public Character removeCurrentPlacementTile(Position position) {
        return currentTurnPlacements.remove(position);
    }

    /**
     * Removes the tile at the position passed as argument from the board.
     *
     * @param row
     * @param col
     */
    public void removeTileFromBoard(int row, int col) {
        board[row][col] = '\0';
        notifyObservers("board");
    }

    /**
     * Adds the tile at the position passed as argument to the board.
     *
     * @param tile
     * @param row
     * @param col
     */
    public void addTileToBoard(Character tile, int row, int col) {
        board[row][col] = tile;
        currentTurnPlacements.put(new Position(row, col), tile);
        notifyObservers("board");
    }

}