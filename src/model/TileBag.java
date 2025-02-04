package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The TileBag class represents the bag of tiles that players draw from.
 * The bag contains 100 tiles, each with a letter from A to Z.
 * The number of each tile is based on the official Scrabble tile distribution.
 * The bag is shuffled at the start of the game to randomize the draw order.
 */
public class TileBag implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Character> tiles;

    public TileBag() {
        tiles = new ArrayList<>();
        initializeBag();
        Collections.shuffle(tiles); // Shuffle the tiles to randomize the draw order
    }

    /**
     * The number of each tile is based on the official Scrabble tile distribution.
     * The number of each tile is as follows:
     * A: 9, B: 2, C: 2, D: 4, E: 12, F: 2, G: 3, H: 2, I: 9, J: 1, K: 1, L: 4, M: 2, N: 6, O: 8, P: 2, Q: 1, R: 6, S: 4, T: 6, U: 4, V: 2, W: 2, X: 1, Y: 2, Z: 1
     */
    private void initializeBag() {
        addTiles('A', 9);
        addTiles('B', 2);
        addTiles('C', 2);
        addTiles('D', 4);
        addTiles('E', 12);
        addTiles('F', 2);
        addTiles('G', 3);
        addTiles('H', 2);
        addTiles('I', 9);
        addTiles('J', 1);
        addTiles('K', 1);
        addTiles('L', 4);
        addTiles('M', 2);
        addTiles('N', 6);
        addTiles('O', 8);
        addTiles('P', 2);
        addTiles('Q', 1);
        addTiles('R', 6);
        addTiles('S', 4);
        addTiles('T', 6);
        addTiles('U', 4);
        addTiles('V', 2);
        addTiles('W', 2);
        addTiles('X', 1);
        addTiles('Y', 2);
        addTiles('Z', 1);
    }

    /**
     * @param letter The letter to add to the bag
     * @param count  The number of tiles to add
     */
    private void addTiles(char letter, int count) {
        for (int i = 0; i < count; i++) {
            tiles.add(letter);
        }
    }

    /**
     * @return The tile drawn from the bag
     */
    public Character drawTile() {
        if (tiles.isEmpty()) {
            return null; // No more tiles available
        }
        return tiles.remove(tiles.size() - 1); // Draw from the end of the list
    }

    /**
     * @return The number of remaining tiles in the bag
     */
    public int remainingTiles() {
        return tiles.size();
    }

    /**
     * @return True if the bag is empty, false otherwise
     */
    public boolean isEmpty() {
        return tiles.isEmpty();
    }
}
