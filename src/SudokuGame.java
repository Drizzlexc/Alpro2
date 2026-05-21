import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

public class SudokuGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SudokuFrame frame = new SudokuFrame();
            frame.setVisible(true);
        });
    }
}

enum Difficulty {
    EASY("Mudah 4x4", 4, 2, 6),
    MEDIUM("Sedang 9x9", 9, 3, 40),
    HIGH("Sulit 16x16", 16, 4, 160);

    private final String label;
    private final int size;
    private final int base;
    private final int removals;

    Difficulty(String label, int size, int base, int removals) {
        this.label = label;
        this.size = size;
        this.base = base;
        this.removals = removals;
    }

    public String getLabel() {
        return label;
    }

    public int getSize() {
        return size;
    }

    public int getBase() {
        return base;
    }

    public int getRemovals() {
        return removals;
    }
}

class SudokuFrame extends JFrame {
    private static final Color BACKGROUND = new Color(245, 246, 250);
    private static final Color ACCENT = new Color(26, 115, 232);
    private static final Color ACCENT_DARK = new Color(20, 82, 170);
    private static final Color MENU_START = new Color(32, 201, 151);

    private SudokuBoard board;
    private SudokuCell[][] cells;
    private JPanel gridPanel;
    private final JPanel gamePanel;
    private final MenuPanel menuPanel;
    private final CardLayout viewLayout;
    private final JPanel rootPanel;
    private JLabel statusLabel;
    private JLabel difficultyLabel;
    private JLabel playerLabel;
    private JLabel timerLabel;
    private JLabel livesLabel;
    private int[][] puzzleSnapshot;
    private Timer timer;
    private int elapsedSeconds;
    private int lives;
    private String playerName = "Pemain";
    private String selectedSymbol = "";
    private final List<JButton> symbolButtons = new ArrayList<>();

    public SudokuFrame() {
        setTitle("Sudoku Arena");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 720));

        viewLayout = new CardLayout();
        rootPanel = new JPanel(viewLayout);
        menuPanel = new MenuPanel();
        menuPanel.setStartAction(event -> startFromMenu());
        menuPanel.setExitAction(event -> dispose());
        gamePanel = buildGamePanel();

        rootPanel.add(menuPanel, "menu");
        rootPanel.add(gamePanel, "game");
        setContentPane(rootPanel);

        viewLayout.show(rootPanel, "menu");
        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private JPanel buildGamePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        JPanel header = buildHeader();
        gridPanel = new JPanel(new BorderLayout());
        gridPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        gridPanel.setBackground(BACKGROUND);
        JPanel footer = buildFooter();

        panel.add(header, BorderLayout.NORTH);
        panel.add(gridPanel, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildHeader() {
        GradientPanel header = new GradientPanel(new Color(26, 115, 232), new Color(69, 160, 255));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Sudoku Arena");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        difficultyLabel = new JLabel();
        difficultyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        difficultyLabel.setForeground(new Color(232, 243, 255));

        statusLabel = new JLabel("Isi semua kotak lalu cek jawaban.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(215, 231, 252));

        playerLabel = new JLabel();
        playerLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        playerLabel.setForeground(new Color(232, 243, 255));

        timerLabel = new JLabel("Waktu: 00:00");
        timerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        timerLabel.setForeground(new Color(232, 243, 255));

        livesLabel = new JLabel("Nyawa: 3/3");
        livesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        livesLabel.setForeground(new Color(232, 243, 255));

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(title, BorderLayout.NORTH);
        textPanel.add(statusLabel, BorderLayout.SOUTH);

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        playerLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        difficultyLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        timerLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        livesLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        infoPanel.add(playerLabel);
        infoPanel.add(difficultyLabel);
        infoPanel.add(timerLabel);
        infoPanel.add(livesLabel);

        header.add(textPanel, BorderLayout.WEST);
        header.add(infoPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new GridLayout(1, 4, 12, 12));
        footer.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));
        footer.setBackground(BACKGROUND);

        JButton checkButton = createButton("Cek Jawaban", ACCENT, Color.WHITE);
        checkButton.addActionListener(event -> checkPuzzle());

        JButton resetButton = createButton("Reset", new Color(236, 240, 244), new Color(42, 42, 42));
        resetButton.addActionListener(event -> resetPuzzle());

        JButton menuButton = createButton("Menu", MENU_START, Color.WHITE);
        menuButton.addActionListener(event -> showMenu());

        JButton solutionButton = createButton("Solusi", new Color(248, 249, 250), ACCENT_DARK);
        solutionButton.addActionListener(event -> revealSolution());

        footer.add(checkButton);
        footer.add(resetButton);
        footer.add(menuButton);
        footer.add(solutionButton);
        return footer;
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void startFromMenu() {
        String inputName = menuPanel.getPlayerName();
        this.playerName = inputName.isEmpty() ? "Pemain" : inputName;
        Difficulty selected = menuPanel.getSelectedDifficulty();
        startNewGame(selected);
        viewLayout.show(rootPanel, "game");
    }

    private void showMenu() {
        stopTimer();
        viewLayout.show(rootPanel, "menu");
    }

    private void startNewGame(Difficulty difficulty) {
        this.board = new SudokuBoard(difficulty);
        this.puzzleSnapshot = board.getPuzzleCopy();
        this.difficultyLabel.setText("Mode: " + difficulty.getLabel());
        this.statusLabel.setText("Isi semua kotak lalu cek jawaban.");
        this.playerLabel.setText("Pemain: " + playerName);
        this.elapsedSeconds = 0;
        this.lives = 3;
        updateTimerLabel();
        updateLivesLabel();
        rebuildGrid();
        startTimer();
    }

    private void rebuildGrid() {
        gridPanel.removeAll();
        int size = board.getSize();
        int base = board.getBase();
        int gap = size <= 9 ? 2 : 1;
        JPanel grid = new JPanel(new GridLayout(size, size, gap, gap));
        grid.setBackground(new Color(210, 216, 224));
        grid.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 188, 199), 2),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        cells = new SudokuCell[size][size];
        int[][] puzzle = board.getPuzzleCopy();
        Font cellFont = new Font("Segoe UI", Font.BOLD, size == 16 ? 18 : 22);
        Dimension cellSize = size == 16 ? new Dimension(36, 36) : new Dimension(48, 48);
        char[] symbols = board.getSymbols();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int value = puzzle[row][col];
                boolean given = value != 0;
                String text = board.valueToSymbol(value);
                SudokuCell cell = new SudokuCell(
                    row,
                    col,
                    base,
                    given,
                    text,
                    cellFont,
                    cellSize
                );
                cell.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent event) {
                        handleCellSelection(cell);
                    }
                });
                cells[row][col] = cell;
                grid.add(cell);
            }
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BACKGROUND);
        wrapper.add(grid, BorderLayout.CENTER);
        wrapper.add(buildNumberPad(symbols), BorderLayout.SOUTH);
        gridPanel.add(wrapper, BorderLayout.CENTER);
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel buildNumberPad(char[] symbols) {
        symbolButtons.clear();
        JPanel pad = new JPanel(new BorderLayout());
        pad.setBackground(BACKGROUND);
        pad.setBorder(BorderFactory.createEmptyBorder(14, 12, 6, 12));

        JLabel label = new JLabel("Pilih angka");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(70, 76, 84));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        int size = symbols.length;
        int columns = size <= 9 ? size + 1 : 8;
        JPanel grid = new JPanel(new GridLayout(0, columns, 8, 8));
        grid.setOpaque(false);

        for (char symbol : symbols) {
            JButton button = createChoiceButton(String.valueOf(symbol));
            registerSymbolButton(button, String.valueOf(symbol));
            grid.add(button);
        }

        JButton clearButton = createChoiceButton("Hapus");
        clearButton.setBackground(new Color(236, 240, 244));
        clearButton.setForeground(new Color(62, 62, 62));
        registerSymbolButton(clearButton, "");
        grid.add(clearButton);

        pad.add(label, BorderLayout.NORTH);
        pad.add(grid, BorderLayout.CENTER);

        if (symbols.length > 0) {
            setSelectedSymbol(String.valueOf(symbols[0]));
        }
        return pad;
    }

    private JButton createChoiceButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(45, 45, 45));
        button.setBorder(BorderFactory.createLineBorder(new Color(210, 216, 224)));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void registerSymbolButton(JButton button, String symbol) {
        button.putClientProperty("symbol", symbol);
        button.putClientProperty("baseBg", button.getBackground());
        button.putClientProperty("baseFg", button.getForeground());
        button.addActionListener(event -> setSelectedSymbol(symbol));
        symbolButtons.add(button);
    }

    private void setSelectedSymbol(String symbol) {
        selectedSymbol = symbol;
        for (JButton button : symbolButtons) {
            String value = (String) button.getClientProperty("symbol");
            boolean isSelected = symbol.equals(value);
            Color baseBg = (Color) button.getClientProperty("baseBg");
            Color baseFg = (Color) button.getClientProperty("baseFg");
            if (isSelected) {
                button.setBackground(ACCENT);
                button.setForeground(Color.WHITE);
            } else {
                button.setBackground(baseBg);
                button.setForeground(baseFg);
            }
        }
    }

    private void handleCellSelection(SudokuCell cell) {
        if (cell.isGiven() || lives <= 0) {
            return;
        }

        if (selectedSymbol == null) {
            return;
        }

        if (selectedSymbol.isEmpty()) {
            cell.setText("");
            cell.setError(false);
            return;
        }

        if (selectedSymbol.equals(cell.getText())) {
            return;
        }

        int value = board.symbolToValue(selectedSymbol);
        int solution = board.getSolutionValue(cell.getRow(), cell.getCol());
        cell.setText(selectedSymbol);
        if (value != solution) {
            cell.setError(true);
            lives = Math.max(0, lives - 1);
            updateLivesLabel();
            if (lives == 0) {
                stopTimer();
                lockBoard();
                statusLabel.setText("Nyawa habis. Game over.");
                JOptionPane.showMessageDialog(
                    this,
                    "Nyawa habis. Game over.",
                    "Game Over",
                    JOptionPane.ERROR_MESSAGE
                );
            }
            return;
        }

        cell.setError(false);
        if (isSolved()) {
            handleWin();
        }
    }

    private boolean isSolved() {
        int size = board.getSize();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                SudokuCell cell = cells[row][col];
                if (cell.isGiven()) {
                    continue;
                }
                int value = board.symbolToValue(cell.getText());
                if (value == 0 || value != board.getSolutionValue(row, col)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void handleWin() {
        stopTimer();
        lockBoard();
        statusLabel.setText("Selamat! Semua jawaban benar.");
        JOptionPane.showMessageDialog(
            this,
            "Selamat! Sudoku selesai dalam " + formatDuration(elapsedSeconds) + ".",
            "Selesai",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void checkPuzzle() {
        if (lives <= 0) {
            return;
        }
        int size = board.getSize();
        boolean anyEmpty = false;
        boolean anyError = false;

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                SudokuCell cell = cells[row][col];
                if (cell.isGiven()) {
                    cell.setError(false);
                    continue;
                }

                int value = board.symbolToValue(cell.getText());
                if (value == 0) {
                    anyEmpty = true;
                    cell.setError(false);
                    continue;
                }

                int solution = board.getSolutionValue(row, col);
                if (value != solution) {
                    anyError = true;
                    cell.setError(true);
                } else {
                    cell.setError(false);
                }
            }
        }

        if (!anyEmpty && !anyError) {
            handleWin();
        } else if (anyError) {
            statusLabel.setText("Masih ada jawaban yang salah. Cek lagi.");
            JOptionPane.showMessageDialog(this, "Ada jawaban yang salah.", "Periksa", JOptionPane.WARNING_MESSAGE);
        } else {
            statusLabel.setText("Masih ada kotak kosong.");
            JOptionPane.showMessageDialog(this, "Masih ada kotak kosong.", "Periksa", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void resetPuzzle() {
        int size = board.getSize();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                SudokuCell cell = cells[row][col];
                if (cell.isGiven()) {
                    cell.setError(false);
                    continue;
                }
                int value = puzzleSnapshot[row][col];
                cell.setText(board.valueToSymbol(value));
                cell.setError(false);
            }
        }
        statusLabel.setText("Puzzle direset ke awal.");
    }

    private void revealSolution() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Tampilkan semua solusi?",
            "Solusi",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        int size = board.getSize();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                SudokuCell cell = cells[row][col];
                cell.setText(board.valueToSymbol(board.getSolutionValue(row, col)));
                cell.setEditable(false);
                cell.setError(false);
            }
        }
        stopTimer();
        statusLabel.setText("Solusi ditampilkan.");
    }

    private void lockBoard() {
        int size = board.getSize();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                SudokuCell cell = cells[row][col];
                if (!cell.isGiven()) {
                    cell.setEditable(false);
                }
            }
        }
    }

    private void startTimer() {
        stopTimer();
        timer = new Timer(1000, event -> {
            elapsedSeconds++;
            updateTimerLabel();
        });
        timer.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private void updateTimerLabel() {
        if (timerLabel != null) {
            timerLabel.setText("Waktu: " + formatDuration(elapsedSeconds));
        }
    }

    private void updateLivesLabel() {
        if (livesLabel != null) {
            livesLabel.setText("Nyawa: " + lives + "/3");
        }
    }

    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remaining = seconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, remaining);
    }
}

class MenuPanel extends GradientPanel {
    private final JTextField nameField;
    private final JRadioButton easyButton;
    private final JRadioButton mediumButton;
    private final JRadioButton highButton;
    private final JButton startButton;
    private final JButton exitButton;

    MenuPanel() {
        super(new Color(12, 30, 63), new Color(38, 111, 184));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Sudoku Arena");
        title.setFont(new Font("Segoe UI", Font.BOLD, 42));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Tantang logikamu dan kejar waktu terbaik.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(new Color(220, 232, 245));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel card = new JPanel();
        card.setBackground(new Color(255, 255, 255, 235));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 231, 240)),
            BorderFactory.createEmptyBorder(24, 28, 24, 28)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(520, 360));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel("Nama Pemain");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        nameField = new JTextField();
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        nameField.setMaximumSize(new Dimension(480, 38));
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 206, 216)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        JLabel difficultyLabel = new JLabel("Pilih Kesulitan");
        difficultyLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        difficultyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel difficultyPanel = new JPanel(new GridLayout(1, 3, 12, 12));
        difficultyPanel.setOpaque(false);
        difficultyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        easyButton = new JRadioButton(Difficulty.EASY.getLabel());
        mediumButton = new JRadioButton(Difficulty.MEDIUM.getLabel());
        highButton = new JRadioButton(Difficulty.HIGH.getLabel());

        ButtonGroup group = new ButtonGroup();
        group.add(easyButton);
        group.add(mediumButton);
        group.add(highButton);
        easyButton.setSelected(true);

        styleRadio(easyButton);
        styleRadio(mediumButton);
        styleRadio(highButton);

        difficultyPanel.add(easyButton);
        difficultyPanel.add(mediumButton);
        difficultyPanel.add(highButton);

        startButton = new JButton("Mulai Bermain");
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        startButton.setForeground(Color.WHITE);
        startButton.setBackground(new Color(32, 201, 151));
        startButton.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        startButton.setFocusPainted(false);
        startButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        exitButton = new JButton("Keluar");
        exitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exitButton.setForeground(new Color(60, 60, 60));
        exitButton.setBackground(new Color(232, 236, 240));
        exitButton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        exitButton.setFocusPainted(false);
        exitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(nameLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(nameField);
        card.add(Box.createVerticalStrut(18));
        card.add(difficultyLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(difficultyPanel);
        card.add(Box.createVerticalStrut(22));
        card.add(startButton);
        card.add(Box.createVerticalStrut(10));
        card.add(exitButton);

        add(Box.createVerticalGlue());
        add(title);
        add(Box.createVerticalStrut(6));
        add(subtitle);
        add(Box.createVerticalStrut(26));
        add(card);
        add(Box.createVerticalGlue());
    }

    public void setStartAction(java.awt.event.ActionListener listener) {
        startButton.addActionListener(listener);
    }

    public void setExitAction(java.awt.event.ActionListener listener) {
        exitButton.addActionListener(listener);
    }

    public String getPlayerName() {
        return nameField.getText().trim();
    }

    public Difficulty getSelectedDifficulty() {
        if (mediumButton.isSelected()) {
            return Difficulty.MEDIUM;
        }
        if (highButton.isSelected()) {
            return Difficulty.HIGH;
        }
        return Difficulty.EASY;
    }

    private void styleRadio(JRadioButton button) {
        button.setOpaque(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(new Color(46, 52, 64));
    }
}

class SudokuBoard {
    private static final String SYMBOL_BANK = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final int size;
    private final int base;
    private final char[] symbols;
    private final Map<Character, Integer> symbolToValue;
    private final Random random;
    private int[][] solution;
    private int[][] puzzle;

    public SudokuBoard(Difficulty difficulty) {
        this.size = difficulty.getSize();
        this.base = difficulty.getBase();
        this.symbols = buildSymbols(size);
        this.symbolToValue = buildSymbolMap(symbols);
        this.random = new Random();
        generate(difficulty.getRemovals());
    }

    public int getSize() {
        return size;
    }

    public int getBase() {
        return base;
    }

    public char[] getSymbols() {
        return Arrays.copyOf(symbols, symbols.length);
    }

    public int[][] getPuzzleCopy() {
        return copyGrid(puzzle);
    }

    public int getSolutionValue(int row, int col) {
        return solution[row][col];
    }

    public int symbolToValue(String text) {
        if (text == null) {
            return 0;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        char symbol = Character.toUpperCase(trimmed.charAt(0));
        return symbolToValue.getOrDefault(symbol, 0);
    }

    public String valueToSymbol(int value) {
        if (value <= 0 || value > symbols.length) {
            return "";
        }
        return String.valueOf(symbols[value - 1]);
    }

    private void generate(int removals) {
        // Base pattern ensures valid blocks; shuffling rows/cols keeps solution valid.
        List<Integer> rowOrder = buildShuffledIndices();
        List<Integer> colOrder = buildShuffledIndices();
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers, random);

        solution = new int[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int pattern = (base * (rowOrder.get(r) % base) + rowOrder.get(r) / base + colOrder.get(c)) % size;
                solution[r][c] = numbers.get(pattern);
            }
        }

        puzzle = copyGrid(solution);
        int totalCells = size * size;
        int minGivens = (int) Math.ceil(totalCells * 0.35);
        int maxRemovals = Math.max(0, totalCells - minGivens);
        int removeCount = Math.min(removals, maxRemovals);
        removeCells(removeCount);
    }

    private List<Integer> buildShuffledIndices() {
        List<Integer> result = new ArrayList<>();
        List<Integer> group = range(base);
        Collections.shuffle(group, random);
        for (int g : group) {
            List<Integer> row = range(base);
            Collections.shuffle(row, random);
            for (int r : row) {
                result.add(g * base + r);
            }
        }
        return result;
    }

    private void removeCells(int count) {
        int removed = 0;
        while (removed < count) {
            int row = random.nextInt(size);
            int col = random.nextInt(size);
            if (puzzle[row][col] != 0) {
                puzzle[row][col] = 0;
                removed++;
            }
        }
    }

    private List<Integer> range(int max) {
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            values.add(i);
        }
        return values;
    }

    private int[][] copyGrid(int[][] grid) {
        int[][] copy = new int[grid.length][grid[0].length];
        for (int i = 0; i < grid.length; i++) {
            copy[i] = Arrays.copyOf(grid[i], grid[i].length);
        }
        return copy;
    }

    private char[] buildSymbols(int size) {
        if (size > SYMBOL_BANK.length()) {
            throw new IllegalArgumentException("Ukuran papan terlalu besar");
        }
        return SYMBOL_BANK.substring(0, size).toCharArray();
    }

    private Map<Character, Integer> buildSymbolMap(char[] symbols) {
        Map<Character, Integer> map = new HashMap<>();
        for (int i = 0; i < symbols.length; i++) {
            map.put(symbols[i], i + 1);
        }
        return map;
    }
}

class SudokuCell extends JTextField {
    private static final Color GIVEN_BG = new Color(233, 238, 244);
    private static final Color INPUT_BG = Color.WHITE;
    private static final Color ERROR_BG = new Color(255, 218, 218);

    private final boolean given;
    private final int row;
    private final int col;

    public SudokuCell(
        int row,
        int col,
        int base,
        boolean given,
        String text,
        Font font,
        Dimension size
    ) {
        super(text);
        this.given = given;
        this.row = row;
        this.col = col;

        setHorizontalAlignment(SwingConstants.CENTER);
        setFont(font);
        setPreferredSize(size);
        setEditable(false);
        setFocusable(true);
        setBackground(given ? GIVEN_BG : INPUT_BG);
        setForeground(new Color(40, 40, 40));
        setBorder(buildBorder(row, col, base));
        setCaretColor(new Color(20, 20, 20));
    }

    public boolean isGiven() {
        return given;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setError(boolean error) {
        if (given) {
            return;
        }
        setBackground(error ? ERROR_BG : INPUT_BG);
    }

    private static javax.swing.border.Border buildBorder(int row, int col, int base) {
        int top = row % base == 0 ? 2 : 1;
        int left = col % base == 0 ? 2 : 1;
        int bottom = (row + 1) % base == 0 ? 2 : 1;
        int right = (col + 1) % base == 0 ? 2 : 1;
        return BorderFactory.createMatteBorder(top, left, bottom, right, new Color(172, 180, 191));
    }
}

class GradientPanel extends JPanel {
    private final Color start;
    private final Color end;

    GradientPanel(Color start, Color end) {
        this.start = start;
        this.end = end;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int width = getWidth();
        int height = getHeight();
        GradientPaint paint = new GradientPaint(0, 0, start, width, height, end);
        g2.setPaint(paint);
        g2.fillRect(0, 0, width, height);
        g2.dispose();
        super.paintComponent(g);
    }
}
