# Scrabble Game - Team 11 Project

## Overview of the Project
This project is a Java version of the Scrabble game, designed using the Model-View-Controller (MVC) architecture. Players can interactively play Scrabble, adhering to standard rules regarding word creation, tile placement, and score calculation.

## Setup

The following are detailed steps to run the Scrabble game.

### Prerequisites 
1. Ensure `wordlist.txt` is in the same directory as the model.
2. The system must be running Java. Java can be installed for your specific device from here: [Java Downloads](https://www.oracle.com/ca-en/java/technologies/downloads/)

### Downloading Scrabble from GitHub and Running the Game
1. Clone the repository or extract the archive to your desired location.
2. Open the project in your preferred Java IDE or navigate to the project directory in your command line.
3. Run `Game.java` to start the game (e.g., in the command line, execute `java Game`).
4. The game Scrabble is now running and can be played by the players. 

## Usage - Playing the Game

### Rules of Scrabble
The rules of the game can be found here: [Scrabble Rules](https://en.wikipedia.org/wiki/Scrabble)

### Gameplay
1. Enter board size, number of players, number of ai players. 
2. If you would like a timer for each turn, check the timer mode option. (BONUS)
3. Select a board configuration from the different kinds of board configurations available.
4. Enter names for each player and click OK.
5. The game is now loaded. At the top, the current player and score is displayed.
6. If you would like to load a previously saved game, click on Load Game.
7. Click on the letter you want to place on the board, then click the tile where it should be placed (The game must start with the word placed in the center, marked in orange).
8. If you would like to undo a letter placement, click on Undo.
9. If you would like to redo a letter placement, click on Redo.
10. If you would like to skip your turn, click on Skip Turn. 
11. Hit submit to submit the word once you are satisfied with word placement.
12. The score of the current player will be displayed on top.
13. Keep playing until a winner is found.
14. View the Images folder for screenshots of gameplay.

## Project Structure

- src/Game
- **Game.java**: The main class for the Scrabble game, responsible for initializing the game, setting up the board size, number of players, and AI players, and starting the game by creating instances of Model, View, and Controller.

- src/META-INF
- **MANIFEST.MF**: Metadata file containing information about the files contained in the JAR (Java ARchive) file.

- src/controller
- **Controller.java**: Acts as an intermediary between the `Model` and the `View`, processing player moves, handling user interactions, and updating the game state.
- **ControllerTests**: Tests for the controller class

- src/model
- **AiPlayer.java**: Represents an AI player; extends the Player class with additional AI-specific logic.
- **Model.java**: Implements the business logic of the game; maintains the game board, players, tile bag, and Scrabble rules.
- **ModelObserver.java**: Interface for listeners of `Model` changes, allowing the `View` to update its state when the `Model` changes.
- **ModelTest.java**: Contains unit tests for the `Model` class to ensure the game logic is implemented correctly.
- **Player.java**: Represents a player in the game, maintaining their current score, tile rack, and move history.
- **Position.java**: Helper class for handling positions on the game board, used in tile placement.
- **TileBag.java**: Manages the pool of tiles available for drawing by players, implementing the tile drawing and tracking remaining tiles.
- **wordlist.txt**: Contains a list of valid words for the Scrabble game, used by the `Model` to validate word submissions.
- **board_config.xml, board_invalid.xml, board_valid.xml**: Different configurations of the board that can be loaded into the game.

- src/view
- **View.java**: The graphical user interface (GUI) for the game, displaying the board status, player points, and messages, and updating automatically on `Model` changes.

- src/wordlist.txt
- **wordlist.txt**: Another text file containing a list of valid words for the Scrabble game, used by the `Model` to validate word submissions. This may be a duplicate or an alternate word list.

## Features

- **Updated game board**: The game board now contains multiple premium scores allowing for scoring of multiple points when a word is formed using them.
- **Names of players**: Asks for names of all human players in the game.
- **Turn-based Multiplayer**: Allows multiple players to play in turns by placing tiles and forming words.
- **Word Validator**: Checks every played word against a predetermined list to ensure only legal Scrabble words are accepted.
- **Scoring Calculations**: Calculates and tracks the current score of players placing valid words on the board.
- **Dynamic Board Update**: The board and player scores dynamically update as players make moves.
- **AI Integration**: Added AI functionality for automated gameplay.
- **New AI popup**: Asks for the number of AI players.
- **Premium Squares**: Introduced premium squares to enhance strategic gameplay.
- **Improved Scoring Logic**: Enhanced the accuracy and efficiency of scoring calculations.
- **Option to Enter Board Size**: Custom board size can now be entered.
- **Option to Enter Number of Players**: Number of players can be entered.
- **Select Board Configuration**: Custom board configurations can now be selected.
- **BONUS: Timer Mode**: Timer mode can be enabled that puts a timer for each player's turn.
- **Undo, Redo**: Undo and Redo is now available. Players can undo and redo their letter placement.
- **Save Game, Load Game**: Game can be saved and loaded later now.

## Technologies Used

- **Java**: Primary programming language for game logic and GUI development.
- **Swing**: Used for designing the GUI.
- **MVC Architecture**: Helps segregate the game's logic, user interface, and interaction handling for a clean and maintainable codebase.

## Critical Changes/Improvements

- Added GUI to enhance user interaction.
- Added AI player functionality with speed.
- Added action listeners via the controller.
- The view is now a GUI only, and all logic has been decoupled and given to the controller or model.
- Implemented ALL events through a streamlined process (bottom of the view).
- AI strategy is algorithmic; it finds all possible words, ranks them from high to low score, finds a place that the model allows it to place (brute force, hence notifications were silenced during AI turns), then places them.
- Changed the architecture from:
    ```
    model <-> controller <-> view
    ```
  to:
   ```
    model <-> controller
    |       /
    view  /
    ```
  This was done to improve code coherence and maintainability after a team member made conflicting changes in the master branch.
- Several new features were added:
- **Option to Enter Board Size**: Custom board size can now be entered.
- **Option to Enter Number of Players**: Number of players can be entered.
- **Select Board Configuration**: Custom board configurations can now be selected.
- **BONUS: Timer Mode**: Timer mode can be enabled that puts a timer for each player's turn.
- **Undo, Redo**: Undo and Redo is now available. Players can undo and redo their letter placement.
- **Save Game, Load Game**: Game can be saved and loaded later now.

- BONUS: Timer mode was chosen as bonus and tests for timer are available in controller tests.


## Known Bugs
  - Ai plays on top of premium squares

## BONUS:
  - you can find bonus tests in controllerTests


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
