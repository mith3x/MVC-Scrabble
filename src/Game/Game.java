package Game;

import controller.Controller;
import model.Model;
import model.Player;
import view.View;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Objects;

public class Game {

    public static void main(String[] args) {
        // Create the main menu panel
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JTextField boardSizeField = new JTextField("15");
        JTextField numPlayersField = new JTextField("2");
        JTextField numAiPlayersField = new JTextField("0");
        JCheckBox timerModeCheckBox = new JCheckBox("Enable Timer Mode");

        // Add components to the panel
        panel.add(new JLabel("Enter board size:"));
        panel.add(boardSizeField);
        panel.add(new JLabel("Enter number of players:"));
        panel.add(numPlayersField);
        panel.add(new JLabel("Enter number of AI players (max 5):"));
        panel.add(numAiPlayersField);
        panel.add(timerModeCheckBox);

        // Add dropdown for board configuration files
        panel.add(new JLabel("Select board configuration:"));
        JComboBox<String> boardConfigDropdown = new JComboBox<>();
        File configDir = new File("src/model");
        for (File file : Objects.requireNonNull(configDir.listFiles((dir, name) -> name.endsWith(".xml")))) {
            boardConfigDropdown.addItem(file.getPath());
        }
        panel.add(boardConfigDropdown);

        int result = JOptionPane.showConfirmDialog(null, panel, "Game Setup", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            System.exit(0);
        }

        int boardSize = Integer.parseInt(boardSizeField.getText());
        if (boardSize % 2 == 0) {
            boardSize += 1;
        }

        int numPlayers = Integer.parseInt(numPlayersField.getText());
        int numAiPlayers = Integer.parseInt(numAiPlayersField.getText());
        if (numAiPlayers > 5) {
            numAiPlayers = 5;
        }
        boolean timerMode = timerModeCheckBox.isSelected();
        String boardConfigPath = (String) boardConfigDropdown.getSelectedItem();

        // Initialize the model
        Model model = Model.getInstance(boardSize, boardConfigPath);
        model.setTimerMode(timerMode);

        // Add players
        for (int i = 1; i <= numPlayers; i++) {
            String playerName = JOptionPane.showInputDialog("Enter name for player " + i + ":");
            model.addPlayer(new Player(playerName));
        }

        // Add AI players
        model.addAiPlayers(numAiPlayers);

        // Initialize the view
        View view = new View(boardSize);
        model.addObserver(view);
        view.update("initialize", model);

        // Initialize the controller
        new Controller(model, view);
    }
}