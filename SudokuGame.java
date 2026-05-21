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

class SudokuGameApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SudokuGameFrame frame = new SudokuGameFrame();
            frame.setVisible(true);
        });
    }
}

enum SudokuDifficulty {
    EASY("Mudah 4x4", 4, 2, 6),
    MEDIUM("Sedang 9x9", 9, 3, 40),
    HIGH("Sulit 16x16", 16, 4, 160);

    private final String label;
    private final int size;
    private final int base;
    private final int removals;

    SudokuDifficulty(String label, int size, int base, int removals) {
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

class SudokuGameFrame extends JFrame {
    private static final Color BACKGROUND = new Color(245, 237, 222);
    private static final Color SURFACE = new Color(235, 227, 212);
    private static final Color HEADER_START = new Color(163, 104, 49);
    private static final Color HEADER_END = new Color(230, 188, 109);
    private static final Color ACCENT = new Color(70, 140, 118);
    private static final Color ACCENT_DARK = new Color(46, 107, 90);
    private static final Color MENU_START = new Color(62, 41, 26);
    private static final Color MENU_END = new Color(147, 97, 52);
    private static final Color TEXT_ON_DARK = new Color(250, 246, 237);
    private static final Color TEXT_SUBTLE = new Color(109, 98, 84);
    private static final Color GRID_LINE = new Color(122, 103, 83);

    private SudokuGameBoard board;
    private SudokuGameCell[][] cells;
    private JPanel gridPanel;
    private final JPanel gamePanel;
    private final SudokuMenuPanel menuPanel;
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
    private SudokuGameCell selectedCell;

    public SudokuGameFrame() {
        setTitle("Sudoku Classic");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 720));

        viewLayout = new CardLayout();
        rootPanel = new JPanel(viewLayout);
        menuPanel = new SudokuMenuPanel();
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
        SudokuGradientPanel header = new SudokuGradientPanel(HEADER_START, HEADER_END);
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Sudoku Classic");
        title.setFont(new Font("Georgia", Font.BOLD, 30));
        title.setForeground(TEXT_ON_DARK);

        difficultyLabel = new JLabel();
        difficultyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        difficultyLabel.setForeground(new Color(250, 238, 214));

        statusLabel = new JLabel("Isi semua kotak lalu cek jawaban.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(248, 239, 223));

        playerLabel = new JLabel();
        playerLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        playerLabel.setForeground(new Color(255, 248, 235));

        timerLabel = new JLabel("Waktu: 00:00");
        timerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        timerLabel.setForeground(new Color(248, 239, 223));

        livesLabel = new JLabel("Nyawa: 3/3");
        livesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        livesLabel.setForeground(new Color(248, 239, 223));

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

        JButton resetButton = createButton("Reset", new Color(236, 229, 217), new Color(70, 62, 52));
        resetButton.addActionListener(event -> resetPuzzle());

        JButton menuButton = createButton("Menu", new Color(189, 120, 61), Color.WHITE);
        menuButton.addActionListener(event -> showMenu());

        JButton solutionButton = createButton("Solusi", new Color(248, 244, 237), ACCENT_DARK);
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
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(164, 136, 103)),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void startFromMenu() {
        String inputName = menuPanel.getPlayerName();
        this.playerName = inputName.isEmpty() ? "Pemain" : inputName;
        SudokuDifficulty selected = menuPanel.getSelectedDifficulty();
        startNewGame(selected);
        viewLayout.show(rootPanel, "game");
    }

    private void showMenu() {
        stopTimer();
        viewLayout.show(rootPanel, "menu");
    }

    private void startNewGame(SudokuDifficulty difficulty) {
        this.board = new SudokuGameBoard(difficulty);
        this.puzzleSnapshot = board.getPuzzleCopy();
        this.difficultyLabel.setText("Mode: " + difficulty.getLabel());
        this.statusLabel.setText("Isi semua kotak lalu cek jawaban.");
        this.playerLabel.setText("Pemain: " + playerName);
        this.elapsedSeconds = 0;
        this.lives = 3;
        this.selectedCell = null;
        updateTimerLabel();
        updateLivesLabel();
        rebuildGrid();
        startTimer();
    }

    private void rebuildGrid() {
        gridPanel.removeAll();
        int size = board.getSize();
        int base = board.getBase();
        JPanel grid = new JPanel(new GridLayout(size, size, 0, 0));
        grid.setBackground(GRID_LINE);
        grid.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GRID_LINE, 4),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        cells = new SudokuGameCell[size][size];
        int[][] puzzle = board.getPuzzleCopy();
        Font cellFont = new Font("Segoe UI", Font.BOLD, size == 16 ? 18 : 22);
        Dimension cellSize = size == 16 ? new Dimension(36, 36) : new Dimension(48, 48);
        char[] symbols = board.getSymbols();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int value = puzzle[row][col];
                boolean given = value != 0;
                String text = board.valueToSymbol(value);
                SudokuGameCell cell = new SudokuGameCell(
                    row,
                    col,
                    base,
                    given,
                    text,
                    cellFont,
                    cellSize,
                    GRID_LINE
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
        label.setForeground(TEXT_SUBTLE);
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
        clearButton.setBackground(new Color(235, 228, 216));
        clearButton.setForeground(new Color(70, 62, 52));
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
        button.setBackground(new Color(250, 246, 239));
        button.setForeground(new Color(70, 62, 52));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(198, 177, 151)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
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

    private void handleCellSelection(SudokuGameCell cell) {
        if (lives <= 0) {
            return;
        }

        selectCell(cell);
        if (cell.isGiven()) {
            return;
        }

        if (selectedSymbol == null) {
            return;
        }

        if (selectedSymbol.isEmpty()) {
            cell.setText("");
            cell.setError(false);
            updateHighlights();
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
            updateHighlights();
            return;
        }

        cell.setError(false);
        updateHighlights();
        if (isSolved()) {
            handleWin();
        }
    }

    private void selectCell(SudokuGameCell cell) {
        selectedCell = cell;
        updateHighlights();
    }

    private void updateHighlights() {
        if (cells == null) {
            return;
        }
        int size = board.getSize();
        int base = board.getBase();
        int row = selectedCell == null ? -1 : selectedCell.getRow();
        int col = selectedCell == null ? -1 : selectedCell.getCol();
        String selectedValue = selectedCell == null ? "" : selectedCell.getText();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                SudokuGameCell cell = cells[r][c];
                boolean selected = r == row && c == col;
                boolean peer = false;
                if (row >= 0 && col >= 0) {
                    boolean sameRow = r == row;
                    boolean sameCol = c == col;
                    boolean sameBox = (r / base) == (row / base) && (c / base) == (col / base);
                    peer = sameRow || sameCol || sameBox;
                }
                boolean sameSymbol = !selectedValue.isEmpty() && selectedValue.equals(cell.getText());
                cell.setSelected(selected);
                cell.setPeerHighlight(peer);
                cell.setSameSymbol(sameSymbol);
            }
        }
    }

    private boolean isSolved() {
        int size = board.getSize();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                SudokuGameCell cell = cells[row][col];
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
                SudokuGameCell cell = cells[row][col];
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
        updateHighlights();
    }

    private void resetPuzzle() {
        int size = board.getSize();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                SudokuGameCell cell = cells[row][col];
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
        updateHighlights();
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
                SudokuGameCell cell = cells[row][col];
                cell.setText(board.valueToSymbol(board.getSolutionValue(row, col)));
                cell.setEditable(false);
                cell.setError(false);
            }
        }
        stopTimer();
        statusLabel.setText("Solusi ditampilkan.");
        updateHighlights();
    }

    private void lockBoard() {
        int size = board.getSize();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                SudokuGameCell cell = cells[row][col];
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

class SudokuMenuPanel extends SudokuGradientPanel {
    private final JTextField nameField;
    private final JRadioButton easyButton;
    private final JRadioButton mediumButton;
    private final JRadioButton highButton;
    private final JButton startButton;
    private final JButton exitButton;

    SudokuMenuPanel() {
        super(new Color(62, 41, 26), new Color(147, 97, 52));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Sudoku Classic");
        title.setFont(new Font("Georgia", Font.BOLD, 42));
        title.setForeground(new Color(252, 245, 233));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Tantang logikamu dan kejar waktu terbaik.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(new Color(230, 219, 201));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel card = new JPanel();
        card.setBackground(new Color(255, 249, 240, 235));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(204, 181, 153)),
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
            BorderFactory.createLineBorder(new Color(200, 187, 170)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        JLabel difficultyLabel = new JLabel("Pilih Kesulitan");
        difficultyLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        difficultyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel difficultyPanel = new JPanel(new GridLayout(1, 3, 12, 12));
        difficultyPanel.setOpaque(false);
        difficultyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        easyButton = new JRadioButton(SudokuDifficulty.EASY.getLabel());
        mediumButton = new JRadioButton(SudokuDifficulty.MEDIUM.getLabel());
        highButton = new JRadioButton(SudokuDifficulty.HIGH.getLabel());

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
        startButton.setBackground(new Color(70, 140, 118));
        startButton.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        startButton.setFocusPainted(false);
        startButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        exitButton = new JButton("Keluar");
        exitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exitButton.setForeground(new Color(70, 62, 52));
        exitButton.setBackground(new Color(235, 228, 216));
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

    public SudokuDifficulty getSelectedDifficulty() {
        if (mediumButton.isSelected()) {
            return SudokuDifficulty.MEDIUM;
        }
        if (highButton.isSelected()) {
            return SudokuDifficulty.HIGH;
        }
        return SudokuDifficulty.EASY;
    }

    private void styleRadio(JRadioButton button) {
        button.setOpaque(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(new Color(66, 58, 49));
    }
}

class SudokuGameBoard {
    private static final String SYMBOL_BANK = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final int size;
    private final int base;
    private final char[] symbols;
    private final Map<Character, Integer> symbolToValue;
    private final Random random;
    private int[][] solution;
    private int[][] puzzle;

    public SudokuGameBoard(SudokuDifficulty difficulty) {
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

class SudokuGameCell extends JTextField {
    private static final Color GIVEN_BG = new Color(230, 219, 201);
    private static final Color INPUT_BG = new Color(252, 248, 242);
    private static final Color SELECTED_BG = new Color(255, 235, 192);
    private static final Color PEER_BG = new Color(245, 236, 221);
    private static final Color SAME_BG = new Color(220, 236, 247);
    private static final Color ERROR_BG = new Color(255, 213, 213);
    private static final Color GIVEN_FG = new Color(83, 72, 60);
    private static final Color INPUT_FG = new Color(49, 49, 49);

    private final boolean given;
    private final int row;
    private final int col;
    private final int base;
    private final Color gridLine;
    private boolean error;
    private boolean selected;
    private boolean peerHighlight;
    private boolean sameSymbol;

    public SudokuGameCell(
        int row,
        int col,
        int base,
        boolean given,
        String text,
        Font font,
        Dimension size,
        Color gridLine
    ) {
        super(text);
        this.given = given;
        this.row = row;
        this.col = col;
        this.base = base;
        this.gridLine = gridLine;

        setHorizontalAlignment(SwingConstants.CENTER);
        setFont(font);
        setPreferredSize(size);
        setEditable(false);
        setFocusable(true);
        setCaretColor(new Color(20, 20, 20));
        updateStyle();
        setBorder(buildBorder());
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        updateStyle();
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
        this.error = error;
        updateStyle();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        updateStyle();
    }

    public void setPeerHighlight(boolean peerHighlight) {
        this.peerHighlight = peerHighlight;
        updateStyle();
    }

    public void setSameSymbol(boolean sameSymbol) {
        this.sameSymbol = sameSymbol;
        updateStyle();
    }

    private void updateStyle() {
        Color baseBg = given ? GIVEN_BG : INPUT_BG;
        Color fg = given ? GIVEN_FG : INPUT_FG;
        Color background = baseBg;
        if (peerHighlight) {
            background = PEER_BG;
        }
        if (sameSymbol) {
            background = SAME_BG;
        }
        if (selected) {
            background = SELECTED_BG;
        }
        if (error && !given) {
            background = ERROR_BG;
        }
        setBackground(background);
        setForeground(fg);
    }

    private javax.swing.border.Border buildBorder() {
        int thick = 3;
        int thin = 1;
        int top = row % base == 0 ? thick : thin;
        int left = col % base == 0 ? thick : thin;
        int bottom = (row + 1) % base == 0 ? thick : thin;
        int right = (col + 1) % base == 0 ? thick : thin;
        return BorderFactory.createMatteBorder(top, left, bottom, right, gridLine);
    }
}

class SudokuGradientPanel extends JPanel {
    private final Color start;
    private final Color end;

    SudokuGradientPanel(Color start, Color end) {
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
