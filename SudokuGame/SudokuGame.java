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
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

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
    private static final Color BACKGROUND = new Color(227, 224, 204);
    private static final Color HEADER_START = new Color(92, 108, 60);
    private static final Color HEADER_END = new Color(171, 178, 112);
    private static final Color ACCENT = new Color(84, 120, 77);
    private static final Color TEXT_ON_DARK = new Color(250, 246, 233);
    private static final Color TEXT_SUBTLE = new Color(86, 80, 68);
    private static final Color GRID_LINE = new Color(108, 95, 73);
    private static final Color WOOD = new Color(176, 136, 92);

    private SudokuGameBoard board;
    private SudokuGameState state;
    private SudokuGameState initialState;
    private final SudokuHistory history = new SudokuHistory();
    private final SudokuLeaderboard leaderboard = new SudokuLeaderboard(10);
    private final SudokuSoundPlayer soundPlayer = new SudokuSoundPlayer();
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
    private JLabel attemptsLabel;
    private Timer timer;
    private int elapsedSeconds;
    private int attemptsRemaining;
    private String playerName = "Pemain";
    private String selectedSymbol = "";
    private final List<JButton> symbolButtons = new ArrayList<>();
    private SudokuGameCell selectedCell;
    private boolean boardLocked;
    private boolean fullScreenActive;

    public SudokuGameFrame() {
        setTitle("Sudoku Classic");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(960, 720));

        // Prevent accidental closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // Only allow close from menu exit button
                if (isShowingMenu()) {
                    shutdownApplication();
                }
            }
        });

        viewLayout = new CardLayout();
        rootPanel = new JPanel(viewLayout);
        menuPanel = new SudokuMenuPanel();
        menuPanel.setStartAction(event -> startFromMenu());
        menuPanel.setExitAction(event -> shutdownApplication());
        gamePanel = buildGamePanel();

        rootPanel.add(menuPanel, "menu");
        rootPanel.add(gamePanel, "game");
        setContentPane(rootPanel);

        viewLayout.show(rootPanel, "menu");
        if (!enterFullScreen()) {
            pack();
            setLocationRelativeTo(null);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        installEscapeShortcut();
    }

    private boolean enterFullScreen() {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (!device.isFullScreenSupported()) {
            return false;
        }
        setUndecorated(true);
        setResizable(false);
        device.setFullScreenWindow(this);
        fullScreenActive = true;
        return true;
    }

    private void exitFullScreen() {
        if (!fullScreenActive) {
            return;
        }
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        device.setFullScreenWindow(null);
        fullScreenActive = false;
        boolean wasVisible = isVisible();
        setUndecorated(false);
        setResizable(true);
        if (wasVisible) {
            setVisible(true);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    private void installEscapeShortcut() {
        JComponent target = getRootPane();
        target.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "exitFullscreen");
        target.getActionMap().put("exitFullscreen", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                exitFullScreen();
            }
        });
    }

    private JPanel buildGamePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        JPanel header = buildHeader();
        gridPanel = new JPanel(new BorderLayout());
        gridPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
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
        header.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("Sudoku Classic");
        title.setFont(new Font("Georgia", Font.BOLD, 30));
        title.setForeground(TEXT_ON_DARK);

        difficultyLabel = new JLabel();
        difficultyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        difficultyLabel.setForeground(new Color(244, 238, 212));

        statusLabel = new JLabel("Isi semua kotak sesuai aturan Sudoku.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(246, 240, 221));

        playerLabel = new JLabel();
        playerLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        playerLabel.setForeground(new Color(255, 248, 235));

        timerLabel = new JLabel("Waktu: 00:00");
        timerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        timerLabel.setForeground(new Color(246, 240, 221));

        attemptsLabel = new JLabel("Kesempatan: 3/3");
        attemptsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        attemptsLabel.setForeground(new Color(246, 240, 221));

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
        infoPanel.add(playerLabel);
        infoPanel.add(difficultyLabel);
        infoPanel.add(timerLabel);
        infoPanel.add(attemptsLabel);

        header.add(textPanel, BorderLayout.WEST);
        header.add(infoPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new GridLayout(2, 4, 10, 10));
        footer.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        footer.setBackground(BACKGROUND);

        JButton undoButton = createButton("Undo", new Color(237, 231, 214), new Color(74, 66, 56));
        undoButton.addActionListener(event -> undoMove());

        JButton redoButton = createButton("Redo", new Color(237, 231, 214), new Color(74, 66, 56));
        redoButton.addActionListener(event -> redoMove());

        JButton hintButton = createButton("Hint", ACCENT, Color.WHITE);
        hintButton.addActionListener(event -> hintMove());

        JButton checkButton = createButton("Cek", new Color(214, 176, 114), new Color(58, 48, 38));
        checkButton.addActionListener(event -> checkPuzzle());

        JButton saveButton = createButton("Simpan", new Color(235, 229, 213), new Color(74, 66, 56));
        saveButton.addActionListener(event -> saveGame());

        JButton loadButton = createButton("Muat", new Color(235, 229, 213), new Color(74, 66, 56));
        loadButton.addActionListener(event -> loadGame());

        JButton menuButton = createButton("Menu", WOOD, Color.WHITE);
        menuButton.addActionListener(event -> showMenu());

        JButton resetButton = createButton("Reset", new Color(235, 229, 213), new Color(74, 66, 56));
        resetButton.addActionListener(event -> resetPuzzle());

        footer.add(undoButton);
        footer.add(redoButton);
        footer.add(hintButton);
        footer.add(checkButton);
        footer.add(saveButton);
        footer.add(loadButton);
        footer.add(menuButton);
        footer.add(resetButton);
        return footer;
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(166, 144, 112)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
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
        gamePanel.setEnabled(true);
        viewLayout.show(rootPanel, "menu");
    }

    private boolean isShowingMenu() {
        return rootPanel.getComponent(0) == menuPanel && menuPanel.isVisible();
    }

    private void shutdownApplication() {
        stopTimer();
        exitFullScreen();
        dispose();
    }

    private void startNewGame(SudokuDifficulty difficulty) {
        board = new SudokuGameBoard(difficulty);
        state = board.createState(playerName, difficulty);
        initialState = state.copy();
        elapsedSeconds = 0;
        history.clear();
        applyState(state, true, "Mulai permainan baru.");
    }

    private void applyState(SudokuGameState nextState, boolean resetHistory, String status) {
        if (nextState == null) {
            return;
        }
        state = nextState;
        board = SudokuGameBoard.fromState(state);
        playerName = state.playerName == null || state.playerName.isEmpty() ? "Pemain" : state.playerName;
        elapsedSeconds = state.elapsedSeconds;
        attemptsRemaining = state.attemptsRemaining;
        selectedCell = null;
        boardLocked = false;
        if (resetHistory) {
            history.clear();
        }
        difficultyLabel.setText("Mode: " + state.difficulty.getLabel());
        playerLabel.setText("Pemain: " + playerName);
        statusLabel.setText(status);
        rebuildGrid();
        updateTimerLabel();
        updateAttemptsLabel();
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
        int[][] values = state.board;
        boolean[][] fixed = state.fixed;
        Font cellFont = new Font("Segoe UI", Font.BOLD, size == 16 ? 18 : 22);
        Dimension cellSize = size == 16 ? new Dimension(36, 36) : new Dimension(48, 48);
        char[] symbols = board.getSymbols();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int value = values[row][col];
                boolean given = fixed[row][col];
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
        wrapper.add(buildBottomPanel(symbols), BorderLayout.SOUTH);
        gridPanel.add(wrapper, BorderLayout.CENTER);
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel buildBottomPanel(char[] symbols) {
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(Box.createVerticalStrut(6));
        bottom.add(buildNumberPad(symbols));
        return bottom;
    }

    private JPanel buildNumberPad(char[] symbols) {
        symbolButtons.clear();
        JPanel pad = new JPanel(new BorderLayout());
        pad.setBackground(BACKGROUND);
        pad.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));

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
        clearButton.setBackground(new Color(232, 224, 208));
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
        button.setBackground(new Color(248, 244, 235));
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
        if (boardLocked) {
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
            applyMove(cell, 0);
            return;
        }
        int value = board.symbolToValue(selectedSymbol);
        applyMove(cell, value);
    }

    private void applyMove(SudokuGameCell cell, int value) {
        int row = cell.getRow();
        int col = cell.getCol();
        int current = state.board[row][col];
        if (current == value) {
            return;
        }

        if (value != 0 && !SudokuValidator.isValidMove(state.board, row, col, value, state.base)) {
            attemptsRemaining = Math.max(0, attemptsRemaining - 1);
            state.attemptsRemaining = attemptsRemaining;
            updateAttemptsLabel();
            statusLabel.setText("Angka melanggar aturan Sudoku.");
            flashInvalid(cell);
            cell.animateError();
            soundPlayer.play(SoundEffect.ERROR);
            if (attemptsRemaining == 0) {
                handleLose();
            }
            return;
        }

        history.push(state);
        state.board[row][col] = value;
        cell.setText(board.valueToSymbol(value));
        cell.setError(false);
        if (value != 0) {
            cell.animateSuccess();
            soundPlayer.play(SoundEffect.CORRECT);
        }
        updateHighlights();

        if (SudokuValidator.isSolved(state.board, state.solution)) {
            handleWin();
        }
    }

    private void flashInvalid(SudokuGameCell cell) {
        cell.setError(true);
        Timer flash = new Timer(450, event -> cell.setError(false));
        flash.setRepeats(false);
        flash.start();
    }

    private void selectCell(SudokuGameCell cell) {
        selectedCell = cell;
        cell.animateSelection();
        updateHighlights();
    }

    private void updateHighlights() {
        if (cells == null || state == null) {
            return;
        }
        int size = state.size;
        int base = state.base;
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

    private void checkPuzzle() {
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

                int value = state.board[row][col];
                if (value == 0) {
                    anyEmpty = true;
                    cell.setError(false);
                    continue;
                }

                int solution = state.solution[row][col];
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
            statusLabel.setText("Masih ada jawaban yang salah.");
            JOptionPane.showMessageDialog(this, "Ada jawaban yang salah.", "Periksa", JOptionPane.WARNING_MESSAGE);
        } else {
            statusLabel.setText("Masih ada kotak kosong.");
            JOptionPane.showMessageDialog(this, "Masih ada kotak kosong.", "Periksa", JOptionPane.WARNING_MESSAGE);
        }
        updateHighlights();
    }

    private void resetPuzzle() {
        if (initialState == null) {
            return;
        }
        applyState(initialState.copy(), true, "Puzzle direset ke awal.");
    }

    private void hintMove() {
        if (state == null) {
            return;
        }
        int row = -1;
        int col = -1;
        if (selectedCell != null && !selectedCell.isGiven()) {
            row = selectedCell.getRow();
            col = selectedCell.getCol();
            if (state.board[row][col] != 0) {
                row = -1;
                col = -1;
            }
        }

        if (row == -1) {
            for (int r = 0; r < state.size; r++) {
                for (int c = 0; c < state.size; c++) {
                    if (!state.fixed[r][c] && state.board[r][c] == 0) {
                        row = r;
                        col = c;
                        break;
                    }
                }
                if (row != -1) {
                    break;
                }
            }
        }

        if (row == -1) {
            statusLabel.setText("Tidak ada sel kosong untuk hint.");
            return;
        }

        SudokuGameCell cell = cells[row][col];
        int value = state.solution[row][col];
        history.push(state);
        state.board[row][col] = value;
        cell.setText(board.valueToSymbol(value));
        cell.setError(false);
        cell.animateSuccess();
        soundPlayer.play(SoundEffect.HINT);
        selectCell(cell);
        statusLabel.setText("Hint diberikan.");

        if (SudokuValidator.isSolved(state.board, state.solution)) {
            handleWin();
        }
    }

    private void undoMove() {
        SudokuGameState prev = history.undo(state);
        if (prev == null) {
            statusLabel.setText("Tidak ada undo.");
            return;
        }
        applyState(prev, false, "Undo diterapkan.");
    }

    private void redoMove() {
        SudokuGameState next = history.redo(state);
        if (next == null) {
            statusLabel.setText("Tidak ada redo.");
            return;
        }
        applyState(next, false, "Redo diterapkan.");
    }

    private void saveGame() {
        if (state == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan permainan");
        chooser.setFileFilter(new FileNameExtensionFilter("Sudoku Save (*.sdk)", "sdk"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.US).endsWith(".sdk")) {
            file = new File(file.getParentFile(), file.getName() + ".sdk");
        }

        SudokuGameState snapshot = state.copy();
        snapshot.playerName = playerName;
        snapshot.elapsedSeconds = elapsedSeconds;
        snapshot.attemptsRemaining = attemptsRemaining;

        try (ObjectOutputStream out = new ObjectOutputStream(
            new BufferedOutputStream(new FileOutputStream(file))
        )) {
            out.writeObject(snapshot);
            statusLabel.setText("Permainan disimpan.");
        } catch (IOException ex) {
            showError("Gagal menyimpan permainan.");
        }
    }

    private void loadGame() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Muat permainan");
        chooser.setFileFilter(new FileNameExtensionFilter("Sudoku Save (*.sdk)", "sdk"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try (ObjectInputStream in = new ObjectInputStream(
            new BufferedInputStream(new FileInputStream(file))
        )) {
            Object obj = in.readObject();
            if (!(obj instanceof SudokuGameState)) {
                showError("File tidak valid.");
                return;
            }
            SudokuGameState loaded = ((SudokuGameState) obj).copy();
            initialState = loaded.copy();
            applyState(loaded, true, "Permainan dimuat.");
        } catch (IOException | ClassNotFoundException ex) {
            showError("Gagal memuat permainan.");
        }
    }

    private void handleWin() {
        stopTimer();
        lockBoard();
        recordLeaderboardEntry();
        soundPlayer.play(SoundEffect.WIN);
        statusLabel.setText("Selamat! Semua jawaban benar.");
        showWinOverlay();
    }

    private void handleLose() {
        stopTimer();
        lockBoard();
        soundPlayer.play(SoundEffect.LOSE);
        statusLabel.setText("Game selesai. Kesempatan habis.");
        showLoseOverlay();
    }

    private void recordLeaderboardEntry() {
        if (state == null) {
            return;
        }
        String name = playerName == null || playerName.isEmpty() ? "Pemain" : playerName;
        leaderboard.addEntry(new SudokuLeaderboardEntry(name, elapsedSeconds, state.difficulty));
    }

    private JPanel buildLeaderboardPanel() {
        JPanel card = new JPanel();
        card.setBackground(new Color(255, 249, 240, 232));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(195, 176, 148)),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(520, 220));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Leaderboard Top 10");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(92, 78, 62));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(8));

        List<SudokuLeaderboardEntry> entries = leaderboard.getEntries();
        if (entries.isEmpty()) {
            JLabel empty = new JLabel("Belum ada skor.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            empty.setForeground(new Color(102, 92, 78));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(empty);
            return card;
        }

        int index = 1;
        for (SudokuLeaderboardEntry entry : entries) {
            String rowText = index + ". " + entry.playerName + " - " + entry.getDuration()
                + " (" + entry.difficulty.getLabel() + ")";
            JLabel row = new JLabel(rowText);
            row.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            row.setForeground(new Color(92, 78, 62));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(row);
            index++;
        }
        return card;
    }

    private void animateOverlayText(JLabel[] labels, Color[] colors) {
        // Set colors immediately without animation to avoid crash
        for (int i = 0; i < labels.length; i++) {
            labels[i].setForeground(colors[i]);
        }
    }

    private void showWinOverlay() {
        String winner = playerName == null || playerName.isEmpty() ? "Pemain" : playerName;
        String duration = formatDuration(elapsedSeconds);

        JDialog dialog = new JDialog(this, "Selesai", true);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setSize(screen);
        dialog.setLocationRelativeTo(null);
        
        // Disable parent frame interaction
        gamePanel.setEnabled(false);
        
        // Prevent accidental closing
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // Do nothing - prevent accidental close
            }
        });

        SudokuGradientPanel overlay = new SudokuGradientPanel(
            new Color(77, 90, 56),
            new Color(164, 170, 108)
        );
        overlay.setLayout(new BoxLayout(overlay, BoxLayout.Y_AXIS));
        overlay.setBorder(BorderFactory.createEmptyBorder(100, 50, 100, 50));

        JLabel title = new JLabel("🎉 SELAMAT 🎉");
        title.setFont(new Font("Blockhead", Font.BOLD, 52));
        title.setForeground(TEXT_ON_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel winnerLabel = new JLabel("Pemain: " + winner);
        winnerLabel.setFont(new Font("Blockhead", Font.BOLD, 32));
        winnerLabel.setForeground(new Color(255, 215, 0));
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Anda Berhasil Menyelesaikan SUDOKU");
        subtitle.setFont(new Font("Blockhead", Font.BOLD, 22));
        subtitle.setForeground(new Color(248, 243, 223));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel time = new JLabel("⏱️ Waktu: " + duration);
        time.setFont(new Font("Blockhead", Font.BOLD, 20));
        time.setForeground(new Color(200, 255, 200));
        time.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel leaderboardPanel = buildLeaderboardPanel();

        JButton playAgainButton = createButton("Main Lagi", ACCENT, Color.WHITE);
        playAgainButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playAgainButton.setFont(new Font("Blockhead", Font.BOLD, 18));
        playAgainButton.addActionListener(event -> {
            SudokuDifficulty difficulty = state == null ? SudokuDifficulty.MEDIUM : state.difficulty;
            gamePanel.setEnabled(true);
            dialog.dispose();
            startNewGame(difficulty);
            viewLayout.show(rootPanel, "game");
        });

        JButton menuButton = createButton("Kembali ke Menu", WOOD, Color.WHITE);
        menuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuButton.setFont(new Font("Blockhead", Font.BOLD, 18));
        menuButton.addActionListener(event -> {
            gamePanel.setEnabled(true);
            dialog.dispose();
            showMenu();
        });

        JButton exitButton = createButton("Keluar", new Color(232, 224, 208), new Color(70, 62, 52));
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setFont(new Font("Blockhead", Font.BOLD, 18));
        exitButton.addActionListener(event -> {
            gamePanel.setEnabled(true);
            dialog.dispose();
            showMenu();
        });

        overlay.add(Box.createVerticalGlue());
        overlay.add(title);
        overlay.add(Box.createVerticalStrut(12));
        overlay.add(winnerLabel);
        overlay.add(Box.createVerticalStrut(16));
        overlay.add(subtitle);
        overlay.add(Box.createVerticalStrut(12));
        overlay.add(time);
        overlay.add(Box.createVerticalStrut(24));
        JSeparator sep1 = new JSeparator();
        sep1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        overlay.add(sep1);
        overlay.add(Box.createVerticalStrut(16));
        overlay.add(leaderboardPanel);
        overlay.add(Box.createVerticalStrut(24));
        JSeparator sep2 = new JSeparator();
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        overlay.add(sep2);
        overlay.add(Box.createVerticalStrut(16));
        overlay.add(playAgainButton);
        overlay.add(Box.createVerticalStrut(10));
        overlay.add(menuButton);
        overlay.add(Box.createVerticalStrut(10));
        overlay.add(exitButton);
        overlay.add(Box.createVerticalGlue());

        animateOverlayText(
            new JLabel[] { title, subtitle, time },
            new Color[] { TEXT_ON_DARK, new Color(248, 243, 223), new Color(252, 248, 235) }
        );

        dialog.setContentPane(overlay);
        dialog.setResizable(false);
        dialog.setVisible(true);
        gamePanel.setEnabled(true);
    }

    private void showLoseOverlay() {
        JDialog dialog = new JDialog(this, "Game Selesai", true);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setSize(screen);
        dialog.setLocationRelativeTo(null);
        
        // Disable parent frame interaction
        gamePanel.setEnabled(false);
        
        // Prevent accidental closing
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // Do nothing - prevent accidental close
            }
        });

        SudokuGradientPanel overlay = new SudokuGradientPanel(
            new Color(88, 72, 56),
            new Color(169, 146, 110)
        );
        overlay.setLayout(new BoxLayout(overlay, BoxLayout.Y_AXIS));
        overlay.setBorder(BorderFactory.createEmptyBorder(100, 50, 100, 50));

        JLabel title = new JLabel("❌ GAME SELESAI ❌");
        title.setFont(new Font("Blockhead", Font.BOLD, 52));
        title.setForeground(TEXT_ON_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Maaf, Anda Gagal Menyelesaikan Game");
        subtitle.setFont(new Font("Blockhead", Font.BOLD, 24));
        subtitle.setForeground(new Color(255, 150, 150));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel message = new JLabel("Jangan Menyerah! Coba Lagi!");
        message.setFont(new Font("Blockhead", Font.BOLD, 20));
        message.setForeground(new Color(248, 243, 223));
        message.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton playAgainButton = createButton("Main Lagi", ACCENT, Color.WHITE);
        playAgainButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playAgainButton.setFont(new Font("Blockhead", Font.BOLD, 18));
        playAgainButton.addActionListener(event -> {
            SudokuDifficulty difficulty = state == null ? SudokuDifficulty.MEDIUM : state.difficulty;
            gamePanel.setEnabled(true);
            dialog.dispose();
            startNewGame(difficulty);
            viewLayout.show(rootPanel, "game");
        });

        JButton menuButton = createButton("Kembali ke Menu", WOOD, Color.WHITE);
        menuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuButton.setFont(new Font("Blockhead", Font.BOLD, 18));
        menuButton.addActionListener(event -> {
            gamePanel.setEnabled(true);
            dialog.dispose();
            showMenu();
        });

        JButton exitButton = createButton("Keluar", new Color(232, 224, 208), new Color(70, 62, 52));
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setFont(new Font("Blockhead", Font.BOLD, 18));
        exitButton.addActionListener(event -> {
            gamePanel.setEnabled(true);
            dialog.dispose();
            showMenu();
        });

        overlay.add(Box.createVerticalGlue());
        overlay.add(title);
        overlay.add(Box.createVerticalStrut(16));
        overlay.add(subtitle);
        overlay.add(Box.createVerticalStrut(12));
        overlay.add(message);
        overlay.add(Box.createVerticalStrut(40));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        overlay.add(sep);
        overlay.add(Box.createVerticalStrut(32));
        overlay.add(playAgainButton);
        overlay.add(Box.createVerticalStrut(10));
        overlay.add(menuButton);
        overlay.add(Box.createVerticalStrut(10));
        overlay.add(exitButton);
        overlay.add(Box.createVerticalGlue());

        animateOverlayText(
            new JLabel[] { title, subtitle },
            new Color[] { TEXT_ON_DARK, new Color(248, 243, 223) }
        );

        dialog.setContentPane(overlay);
        dialog.setResizable(false);
        dialog.setVisible(true);
        gamePanel.setEnabled(true);
    }

    private void lockBoard() {
        boardLocked = true;
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
            if (state != null) {
                state.elapsedSeconds = elapsedSeconds;
            }
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

    private void updateAttemptsLabel() {
        if (attemptsLabel != null) {
            attemptsLabel.setText("Kesempatan: " + attemptsRemaining + "/3");
        }
    }

    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remaining = seconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, remaining);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Peringatan", JOptionPane.WARNING_MESSAGE);
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
        super(new Color(82, 90, 56), new Color(145, 132, 80));
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
        startButton.setBackground(new Color(84, 120, 77));
        startButton.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        startButton.setFocusPainted(false);
        startButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        exitButton = new JButton("Keluar");
        exitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exitButton.setForeground(new Color(70, 62, 52));
        exitButton.setBackground(new Color(232, 224, 208));
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
    private boolean[][] fixed;

    public SudokuGameBoard(SudokuDifficulty difficulty) {
        this.size = difficulty.getSize();
        this.base = difficulty.getBase();
        this.symbols = buildSymbols(size);
        this.symbolToValue = buildSymbolMap(symbols);
        this.random = new Random();
        generate(difficulty.getRemovals());
        this.fixed = buildFixed(puzzle);
    }

    private SudokuGameBoard(
        int size,
        int base,
        int[][] solution,
        boolean[][] fixed,
        int[][] puzzle
    ) {
        this.size = size;
        this.base = base;
        this.symbols = buildSymbols(size);
        this.symbolToValue = buildSymbolMap(symbols);
        this.random = new Random();
        this.solution = copyGrid(solution);
        this.fixed = copyFixed(fixed);
        this.puzzle = copyGrid(puzzle);
    }

    public static SudokuGameBoard fromState(SudokuGameState state) {
        return new SudokuGameBoard(state.size, state.base, state.solution, state.fixed, state.board);
    }

    public SudokuGameState createState(String playerName, SudokuDifficulty difficulty) {
        SudokuGameState state = new SudokuGameState(size, base, difficulty);
        state.board = copyGrid(puzzle);
        state.fixed = copyFixed(fixed);
        state.solution = copyGrid(solution);
        state.playerName = playerName;
        state.elapsedSeconds = 0;
        state.attemptsRemaining = 3;
        return state;
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

    private boolean[][] buildFixed(int[][] puzzle) {
        boolean[][] fixedGrid = new boolean[puzzle.length][puzzle[0].length];
        for (int r = 0; r < puzzle.length; r++) {
            for (int c = 0; c < puzzle[r].length; c++) {
                fixedGrid[r][c] = puzzle[r][c] != 0;
            }
        }
        return fixedGrid;
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

    private boolean[][] copyFixed(boolean[][] grid) {
        boolean[][] copy = new boolean[grid.length][grid[0].length];
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

class SudokuGameState implements Serializable {
    private static final long serialVersionUID = 1L;

    final int size;
    final int base;
    final SudokuDifficulty difficulty;
    int[][] board;
    boolean[][] fixed;
    int[][] solution;
    String playerName;
    int elapsedSeconds;
    int attemptsRemaining;

    SudokuGameState(int size, int base, SudokuDifficulty difficulty) {
        this.size = size;
        this.base = base;
        this.difficulty = difficulty;
    }

    SudokuGameState copy() {
        SudokuGameState copy = new SudokuGameState(size, base, difficulty);
        copy.board = copyGrid(board);
        copy.fixed = copyFixed(fixed);
        copy.solution = copyGrid(solution);
        copy.playerName = playerName;
        copy.elapsedSeconds = elapsedSeconds;
        copy.attemptsRemaining = attemptsRemaining;
        return copy;
    }

    private int[][] copyGrid(int[][] grid) {
        if (grid == null) {
            return null;
        }
        int[][] copy = new int[grid.length][grid[0].length];
        for (int i = 0; i < grid.length; i++) {
            copy[i] = Arrays.copyOf(grid[i], grid[i].length);
        }
        return copy;
    }

    private boolean[][] copyFixed(boolean[][] grid) {
        if (grid == null) {
            return null;
        }
        boolean[][] copy = new boolean[grid.length][grid[0].length];
        for (int i = 0; i < grid.length; i++) {
            copy[i] = Arrays.copyOf(grid[i], grid[i].length);
        }
        return copy;
    }
}

class SudokuHistory {
    private final Deque<SudokuGameState> undoStack = new ArrayDeque<>();
    private final Deque<SudokuGameState> redoStack = new ArrayDeque<>();

    void push(SudokuGameState state) {
        if (state != null) {
            undoStack.push(state.copy());
            redoStack.clear();
        }
    }

    SudokuGameState undo(SudokuGameState current) {
        if (undoStack.isEmpty()) {
            return null;
        }
        if (current != null) {
            redoStack.push(current.copy());
        }
        return undoStack.pop();
    }

    SudokuGameState redo(SudokuGameState current) {
        if (redoStack.isEmpty()) {
            return null;
        }
        if (current != null) {
            undoStack.push(current.copy());
        }
        return redoStack.pop();
    }

    void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}

class SudokuValidator {
    static boolean isValidMove(int[][] board, int row, int col, int value, int base) {
        if (value == 0) {
            return true;
        }
        int size = board.length;
        for (int c = 0; c < size; c++) {
            if (c != col && board[row][c] == value) {
                return false;
            }
        }
        for (int r = 0; r < size; r++) {
            if (r != row && board[r][col] == value) {
                return false;
            }
        }
        int startRow = (row / base) * base;
        int startCol = (col / base) * base;
        for (int r = startRow; r < startRow + base; r++) {
            for (int c = startCol; c < startCol + base; c++) {
                if ((r != row || c != col) && board[r][c] == value) {
                    return false;
                }
            }
        }
        return true;
    }

    static boolean isSolved(int[][] board, int[][] solution) {
        if (board == null) {
            return false;
        }
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[r].length; c++) {
                if (board[r][c] == 0) {
                    return false;
                }
                if (solution != null && solution.length > r && solution[r].length > c) {
                    if (board[r][c] != solution[r][c]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}

enum SoundEffect {
    CORRECT("correct.wav"),
    ERROR("error.wav"),
    WIN("win.wav"),
    LOSE("lose.wav"),
    HINT("hint.wav");

    final String fileName;

    SoundEffect(String fileName) {
        this.fileName = fileName;
    }
}

class SudokuSoundPlayer {
    private final Map<SoundEffect, Clip> clips = new EnumMap<>(SoundEffect.class);
    private final File baseDir = new File("sounds");

    void play(SoundEffect effect) {
        Clip clip = loadClip(effect);
        if (clip == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }

    private Clip loadClip(SoundEffect effect) {
        if (clips.containsKey(effect)) {
            return clips.get(effect);
        }
        File file = new File(baseDir, effect.fileName);
        if (!file.exists()) {
            clips.put(effect, null);
            return null;
        }
        try (AudioInputStream input = AudioSystem.getAudioInputStream(file)) {
            Clip clip = AudioSystem.getClip();
            clip.open(input);
            clips.put(effect, clip);
            return clip;
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
            clips.put(effect, null);
            return null;
        }
    }
}

class SudokuLeaderboardEntry {
    final String playerName;
    final int seconds;
    final SudokuDifficulty difficulty;

    SudokuLeaderboardEntry(String playerName, int seconds, SudokuDifficulty difficulty) {
        this.playerName = playerName;
        this.seconds = seconds;
        this.difficulty = difficulty;
    }

    String getDuration() {
        int minutes = seconds / 60;
        int remaining = seconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, remaining);
    }
}

class SudokuLeaderboard {
    private final int limit;
    private final List<SudokuLeaderboardEntry> entries = new ArrayList<>();

    SudokuLeaderboard(int limit) {
        this.limit = limit;
    }

    void addEntry(SudokuLeaderboardEntry entry) {
        entries.add(entry);
        entries.sort((left, right) -> Integer.compare(left.seconds, right.seconds));
        while (entries.size() > limit) {
            entries.remove(entries.size() - 1);
        }
    }

    List<SudokuLeaderboardEntry> getEntries() {
        return new ArrayList<>(entries);
    }
}

class SudokuGameCell extends JTextField {
    private static final Color GIVEN_BG = new Color(224, 219, 200);
    private static final Color INPUT_BG = new Color(247, 244, 233);
    private static final Color SELECTED_BG = new Color(255, 220, 148);
    private static final Color SELECTION_PULSE = new Color(255, 238, 196);
    private static final Color PEER_BG = new Color(239, 233, 214);
    private static final Color SAME_BG = new Color(212, 226, 244);
    private static final Color ERROR_BG = new Color(255, 196, 196);
    private static final Color SUCCESS_BG = new Color(204, 235, 206);
    private static final Color GIVEN_FG = new Color(61, 54, 44);
    private static final Color INPUT_FG = new Color(35, 35, 35);

    private final boolean given;
    private final int row;
    private final int col;
    private final int base;
    private final Color gridLine;
    private boolean error;
    private boolean selected;
    private boolean peerHighlight;
    private boolean sameSymbol;
    private Color animationColor;

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

    public void animateSuccess() {
        animatePulse(SUCCESS_BG, 6, 30);
    }

    public void animateError() {
        animatePulse(ERROR_BG, 6, 30);
    }

    public void animateSelection() {
        animatePulse(SELECTION_PULSE, 6, 30);
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
        if (animationColor != null) {
            background = animationColor;
        }
        setBackground(background);
        setForeground(fg);
    }

    private void animatePulse(Color pulseColor, int steps, int delayMs) {
        final Color baseColor = getBackground();
        final int totalSteps = Math.max(4, steps);
        final int half = totalSteps / 2;
        final int[] step = {0};
        final Timer[] timerRef = new Timer[1];
        timerRef[0] = new Timer(delayMs, event -> {
            step[0]++;
            float t = step[0] <= half
                ? step[0] / (float) half
                : (totalSteps - step[0]) / (float) half;
            animationColor = lerpColor(baseColor, pulseColor, t);
            updateStyle();
            if (step[0] >= totalSteps) {
                animationColor = null;
                updateStyle();
                timerRef[0].stop();
            }
        });
        timerRef[0].start();
    }

    private Color lerpColor(Color start, Color end, float t) {
        int r = Math.round(start.getRed() + (end.getRed() - start.getRed()) * t);
        int g = Math.round(start.getGreen() + (end.getGreen() - start.getGreen()) * t);
        int b = Math.round(start.getBlue() + (end.getBlue() - start.getBlue()) * t);
        return new Color(r, g, b);
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
