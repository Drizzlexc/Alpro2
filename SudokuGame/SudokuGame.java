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
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

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

    public String getLabel() { return label; }
    public int getSize()     { return size; }
    public int getBase()     { return base; }
    public int getRemovals() { return removals; }
}

class SudokuGameFrame extends JFrame {
    private static final Color BACKGROUND    = new Color(227, 224, 204);
    private static final Color HEADER_START  = new Color(92,  108,  60);
    private static final Color HEADER_END    = new Color(171, 178, 112);
    private static final Color ACCENT        = new Color(84,  120,  77);
    private static final Color TEXT_ON_DARK  = new Color(250, 246, 233);
    private static final Color TEXT_SUBTLE   = new Color(86,   80,  68);
    private static final Color GRID_LINE     = new Color(108,  95,  73);
    private static final Color WOOD          = new Color(176, 136,  92);

    private SudokuGameBoard    board;
    private SudokuGameState    state;
    private SudokuGameState    initialState;
    private final SudokuHistory     history     = new SudokuHistory();
    private final SudokuLeaderboard leaderboard = new SudokuLeaderboard(10);
    private final SudokuSoundPlayer soundPlayer = new SudokuSoundPlayer();

    private SudokuGameCell[][] cells;
    private JPanel             gridPanel;
    private final JPanel       gamePanel;
    private final SudokuMenuPanel menuPanel;
    private final CardLayout   viewLayout;
    private final JPanel       rootPanel;

    private JLabel statusLabel;
    private JLabel difficultyLabel;
    private JLabel playerLabel;
    private JLabel timerLabel;
    private JLabel attemptsLabel;

    private Timer  timer;
    private int    elapsedSeconds;
    private int    attemptsRemaining;
    private String playerName   = "Pemain";
    private String selectedSymbol = "";

    private final List<JButton> symbolButtons = new ArrayList<>();
    private SudokuGameCell selectedCell;
    private boolean boardLocked;

    // ── FIX 3: track current view with a proper field ─────────────────────────
    private String currentView = "menu";

    public SudokuGameFrame() {
        setTitle("Sudoku Classic");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // ── FULL SCREEN: setUndecorated HARUS dipanggil sebelum window ditampilkan.
        //    Memanggilnya setelah setVisible(true) menyebabkan error native.
        //    Kita tidak pakai device.setFullScreenWindow() (exclusive mode) karena
        //    itu yang menyebabkan "layar berpindah" / GPU takeover.
        //    Sebagai gantinya: hilangkan dekorasi, lalu penuhi seluruh layar secara manual.
        setUndecorated(true);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen);
        setLocation(0, 0);
        setResizable(false);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if ("menu".equals(currentView)) {
                    shutdownApplication();
                }
            }
        });

        viewLayout = new CardLayout();
        rootPanel  = new JPanel(viewLayout);
        menuPanel  = new SudokuMenuPanel();
        menuPanel.setStartAction(event -> startFromMenu());
        menuPanel.setExitAction(event -> shutdownApplication());
        gamePanel  = buildGamePanel();

        rootPanel.add(menuPanel, "menu");
        rootPanel.add(gamePanel, "game");
        setContentPane(rootPanel);

        viewLayout.show(rootPanel, "menu");

        // Pasang ESC untuk keluar full screen → kembali ke window biasa (opsional)
        installEscapeShortcut();
    }

    /**
     * ESC → keluar dari full-screen (hapus dekorasi & kembalikan ukuran normal).
     * Dipanggil hanya sekali dari konstruktor.
     */
    private void installEscapeShortcut() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "toggleFullscreen");
        getRootPane().getActionMap().put("toggleFullscreen", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exitFullScreen();
            }
        });
    }

    /** Keluar dari full-screen tanpa crash: sembunyikan dulu, ubah dekorasi, tampilkan lagi. */
    private void exitFullScreen() {
        if (!isUndecorated()) return;   // sudah bukan full-screen
        // Harus hide dulu sebelum mengubah undecorated pada window yang sudah tampil
        setVisible(false);
        dispose();                      // lepas native peer lama
        setUndecorated(false);
        setResizable(true);
        setMinimumSize(new Dimension(960, 720));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private JPanel buildGamePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        JPanel header = buildHeader();
        gridPanel = new JPanel(new BorderLayout());
        gridPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
        gridPanel.setBackground(BACKGROUND);
        JPanel footer = buildFooter();

        panel.add(header,   BorderLayout.NORTH);
        panel.add(gridPanel, BorderLayout.CENTER);
        panel.add(footer,   BorderLayout.SOUTH);
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
        textPanel.add(title,       BorderLayout.NORTH);
        textPanel.add(statusLabel, BorderLayout.SOUTH);

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        playerLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        difficultyLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        timerLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        attemptsLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
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

        JButton undoButton  = createButton("Undo",  new Color(237, 231, 214), new Color(74, 66, 56));
        undoButton.addActionListener(event -> undoMove());

        JButton redoButton  = createButton("Redo",  new Color(237, 231, 214), new Color(74, 66, 56));
        redoButton.addActionListener(event -> redoMove());

        JButton hintButton  = createButton("Hint",  ACCENT, Color.WHITE);
        hintButton.addActionListener(event -> hintMove());

        JButton checkButton = createButton("Cek",   new Color(214, 176, 114), new Color(58, 48, 38));
        checkButton.addActionListener(event -> checkPuzzle());

        JButton saveButton  = createButton("Simpan", new Color(235, 229, 213), new Color(74, 66, 56));
        saveButton.addActionListener(event -> saveGame());

        JButton loadButton  = createButton("Muat",  new Color(235, 229, 213), new Color(74, 66, 56));
        loadButton.addActionListener(event -> loadGame());

        JButton menuButton  = createButton("Menu",  WOOD, Color.WHITE);
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
        this.playerName  = inputName.isEmpty() ? "Pemain" : inputName;
        SudokuDifficulty selected = menuPanel.getSelectedDifficulty();
        startNewGame(selected);
        currentView = "game";                       // FIX 3
        viewLayout.show(rootPanel, "game");
    }

    private void showMenu() {
        stopTimer();
        gamePanel.setEnabled(true);
        currentView = "menu";                       // FIX 3
        viewLayout.show(rootPanel, "menu");
    }

    private void shutdownApplication() {
        stopTimer();
        dispose();
        System.exit(0);
    }

    private void startNewGame(SudokuDifficulty difficulty) {
        board        = new SudokuGameBoard(difficulty);
        state        = board.createState(playerName, difficulty);
        initialState = state.copy();
        elapsedSeconds = 0;
        history.clear();
        applyState(state, true, "Mulai permainan baru.");
    }

    private void applyState(SudokuGameState nextState, boolean resetHistory, String status) {
        if (nextState == null) return;
        state      = nextState;
        board      = SudokuGameBoard.fromState(state);
        playerName = (state.playerName == null || state.playerName.isEmpty()) ? "Pemain" : state.playerName;
        elapsedSeconds    = state.elapsedSeconds;
        attemptsRemaining = state.attemptsRemaining;
        selectedCell      = null;
        boardLocked       = false;
        if (resetHistory) history.clear();

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
        int size  = board.getSize();
        int base  = board.getBase();
        JPanel grid = new JPanel(new GridLayout(size, size, 0, 0));
        grid.setBackground(GRID_LINE);
        grid.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GRID_LINE, 4),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        cells = new SudokuGameCell[size][size];
        int[][]     values = state.board;
        boolean[][] fixed  = state.fixed;
        Font      cellFont = new Font("Segoe UI", Font.BOLD, size == 16 ? 18 : 22);
        Dimension cellSize = size == 16 ? new Dimension(36, 36) : new Dimension(48, 48);
        char[]    symbols  = board.getSymbols();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int    value = values[row][col];
                boolean given = fixed[row][col];
                String  text  = board.valueToSymbol(value);
                SudokuGameCell cell = new SudokuGameCell(row, col, base, given, text,
                        cellFont, cellSize, GRID_LINE);
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
        wrapper.add(grid,                    BorderLayout.CENTER);
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

        int size    = symbols.length;
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
        pad.add(grid,  BorderLayout.CENTER);

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
            String value    = (String) button.getClientProperty("symbol");
            boolean isSel   = symbol.equals(value);
            Color baseBg    = (Color) button.getClientProperty("baseBg");
            Color baseFg    = (Color) button.getClientProperty("baseFg");
            button.setBackground(isSel ? ACCENT          : baseBg);
            button.setForeground(isSel ? Color.WHITE     : baseFg);
        }
    }

    private void handleCellSelection(SudokuGameCell cell) {
        if (boardLocked) return;
        selectCell(cell);
        if (cell.isGiven()) return;
        if (selectedSymbol == null) return;
        if (selectedSymbol.isEmpty()) {
            applyMove(cell, 0);
            return;
        }
        int value = board.symbolToValue(selectedSymbol);
        applyMove(cell, value);
    }

    private void applyMove(SudokuGameCell cell, int value) {
        int row     = cell.getRow();
        int col     = cell.getCol();
        int current = state.board[row][col];
        if (current == value) return;

        if (value != 0 && !SudokuValidator.isValidMove(state.board, row, col, value, state.base)) {
            attemptsRemaining = Math.max(0, attemptsRemaining - 1);
            state.attemptsRemaining = attemptsRemaining;
            updateAttemptsLabel();
            statusLabel.setText("Angka melanggar aturan Sudoku.");
            flashInvalid(cell);
            cell.animateError();
            soundPlayer.play(SoundEffect.ERROR);
            if (attemptsRemaining == 0) handleLose();
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

        if (SudokuValidator.isSolved(state.board, state.solution)) handleWin();
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
        if (cells == null || state == null) return;
        int size = state.size;
        int base = state.base;
        int row  = selectedCell == null ? -1 : selectedCell.getRow();
        int col  = selectedCell == null ? -1 : selectedCell.getCol();
        String selectedValue = selectedCell == null ? "" : selectedCell.getText();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                SudokuGameCell cell = cells[r][c];
                boolean selected    = r == row && c == col;
                boolean peer        = false;
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
        int size       = board.getSize();
        boolean anyEmpty = false;
        boolean anyError = false;

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                SudokuGameCell cell = cells[row][col];
                if (cell.isGiven()) { cell.setError(false); continue; }
                int value = state.board[row][col];
                if (value == 0)  { anyEmpty = true; cell.setError(false); continue; }
                if (value != state.solution[row][col]) { anyError = true; cell.setError(true); }
                else cell.setError(false);
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
        if (initialState == null) return;
        applyState(initialState.copy(), true, "Puzzle direset ke awal.");
    }

    private void hintMove() {
        if (state == null) return;
        int row = -1, col = -1;
        if (selectedCell != null && !selectedCell.isGiven()) {
            row = selectedCell.getRow();
            col = selectedCell.getCol();
            if (state.board[row][col] != 0) { row = -1; col = -1; }
        }
        if (row == -1) {
            outer:
            for (int r = 0; r < state.size; r++) {
                for (int c = 0; c < state.size; c++) {
                    if (!state.fixed[r][c] && state.board[r][c] == 0) {
                        row = r; col = c; break outer;
                    }
                }
            }
        }
        if (row == -1) { statusLabel.setText("Tidak ada sel kosong untuk hint."); return; }

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
        if (SudokuValidator.isSolved(state.board, state.solution)) handleWin();
    }

    private void undoMove() {
        SudokuGameState prev = history.undo(state);
        if (prev == null) { statusLabel.setText("Tidak ada undo."); return; }
        applyState(prev, false, "Undo diterapkan.");
    }

    private void redoMove() {
        SudokuGameState next = history.redo(state);
        if (next == null) { statusLabel.setText("Tidak ada redo."); return; }
        applyState(next, false, "Redo diterapkan.");
    }

    private void saveGame() {
        if (state == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan permainan");
        chooser.setFileFilter(new FileNameExtensionFilter("Sudoku Save (*.sdk)", "sdk"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.US).endsWith(".sdk"))
            file = new File(file.getParentFile(), file.getName() + ".sdk");

        SudokuGameState snapshot = state.copy();
        snapshot.playerName       = playerName;
        snapshot.elapsedSeconds   = elapsedSeconds;
        snapshot.attemptsRemaining = attemptsRemaining;

        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
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
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            Object obj = in.readObject();
            if (!(obj instanceof SudokuGameState)) { showError("File tidak valid."); return; }
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
        if (state == null) return;
        String name = (playerName == null || playerName.isEmpty()) ? "Pemain" : playerName;
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
            row.setHorizontalAlignment(SwingConstants.CENTER);
            row.setAlignmentX(Component.CENTER_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
            card.add(row);
            index++;
        }
        return card;
    }

    private void showWinOverlay() {
        String winner   = (playerName == null || playerName.isEmpty()) ? "Pemain" : playerName;
        String duration = formatDuration(elapsedSeconds);

        JDialog dialog = new JDialog(this, "Selesai", true);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        // ── FIX 1: size the overlay to fill this window, not the whole screen.
        //    The original code used Toolkit.getDefaultToolkit().getScreenSize() which
        //    on multi-monitor setups could position the dialog on the wrong screen.
        dialog.setSize(getSize());
        dialog.setLocationRelativeTo(this);

        gamePanel.setEnabled(false);

        SudokuGradientPanel overlay = new SudokuGradientPanel(
            new Color(77, 90, 56), new Color(164, 170, 108));
        overlay.setLayout(new BoxLayout(overlay, BoxLayout.Y_AXIS));
        overlay.setBorder(BorderFactory.createEmptyBorder(100, 50, 100, 50));

        JLabel title    = createOverlayLabel("🎉 SELAMAT 🎉",     52, TEXT_ON_DARK);
        JLabel nameLabel = createOverlayLabel("Pemain: " + winner, 32, new Color(255, 215, 0));
        JLabel subtitle = createOverlayLabel("Anda Berhasil Menyelesaikan SUDOKU", 22,
                new Color(248, 243, 223));
        JLabel time     = createOverlayLabel("⏱️ Waktu: " + duration, 20, new Color(200, 255, 200));

        JPanel lb = buildLeaderboardPanel();

        JButton playAgainButton = createButton("Main Lagi", ACCENT, Color.WHITE);
        playAgainButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playAgainButton.addActionListener(event -> {
            SudokuDifficulty diff = state == null ? SudokuDifficulty.MEDIUM : state.difficulty;
            gamePanel.setEnabled(true);
            dialog.dispose();
            startNewGame(diff);
            currentView = "game";
            viewLayout.show(rootPanel, "game");
        });

        JButton menuButton = createButton("Kembali ke Menu", WOOD, Color.WHITE);
        menuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuButton.addActionListener(event -> {
            gamePanel.setEnabled(true);
            dialog.dispose();
            showMenu();
        });

        overlay.add(Box.createVerticalGlue());
        overlay.add(title);      overlay.add(Box.createVerticalStrut(12));
        overlay.add(nameLabel);  overlay.add(Box.createVerticalStrut(16));
        overlay.add(subtitle);   overlay.add(Box.createVerticalStrut(12));
        overlay.add(time);       overlay.add(Box.createVerticalStrut(24));
        addSeparator(overlay);   overlay.add(Box.createVerticalStrut(16));
        overlay.add(lb);         overlay.add(Box.createVerticalStrut(24));
        addSeparator(overlay);   overlay.add(Box.createVerticalStrut(16));
        overlay.add(playAgainButton); overlay.add(Box.createVerticalStrut(10));
        overlay.add(menuButton);
        overlay.add(Box.createVerticalGlue());

        dialog.setContentPane(overlay);
        dialog.setResizable(false);
        dialog.setVisible(true);
        gamePanel.setEnabled(true);
    }

    private void showLoseOverlay() {
        JDialog dialog = new JDialog(this, "Game Selesai", true);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        // ── FIX 1: same fix — size relative to this window, not the screen
        dialog.setSize(getSize());
        dialog.setLocationRelativeTo(this);

        gamePanel.setEnabled(false);

        SudokuGradientPanel overlay = new SudokuGradientPanel(
            new Color(88, 72, 56), new Color(169, 146, 110));
        overlay.setLayout(new BoxLayout(overlay, BoxLayout.Y_AXIS));
        overlay.setBorder(BorderFactory.createEmptyBorder(100, 50, 100, 50));

        JLabel title    = createOverlayLabel("❌ GAME SELESAI ❌",                    52, TEXT_ON_DARK);
        JLabel subtitle = createOverlayLabel("Maaf, Anda Gagal Menyelesaikan Game",   24, new Color(255, 150, 150));
        JLabel message  = createOverlayLabel("Jangan Menyerah! Coba Lagi!",           20, new Color(248, 243, 223));

        JButton playAgainButton = createButton("Main Lagi", ACCENT, Color.WHITE);
        playAgainButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playAgainButton.addActionListener(event -> {
            SudokuDifficulty diff = state == null ? SudokuDifficulty.MEDIUM : state.difficulty;
            gamePanel.setEnabled(true);
            dialog.dispose();
            startNewGame(diff);
            currentView = "game";
            viewLayout.show(rootPanel, "game");
        });

        JButton menuButton = createButton("Kembali ke Menu", WOOD, Color.WHITE);
        menuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuButton.addActionListener(event -> {
            gamePanel.setEnabled(true);
            dialog.dispose();
            showMenu();
        });

        overlay.add(Box.createVerticalGlue());
        overlay.add(title);    overlay.add(Box.createVerticalStrut(16));
        overlay.add(subtitle); overlay.add(Box.createVerticalStrut(12));
        overlay.add(message);  overlay.add(Box.createVerticalStrut(40));
        addSeparator(overlay); overlay.add(Box.createVerticalStrut(32));
        overlay.add(playAgainButton); overlay.add(Box.createVerticalStrut(10));
        overlay.add(menuButton);
        overlay.add(Box.createVerticalGlue());

        dialog.setContentPane(overlay);
        dialog.setResizable(false);
        dialog.setVisible(true);
        gamePanel.setEnabled(true);
    }

    // ── helpers for overlay creation ──────────────────────────────────────────
    private JLabel createOverlayLabel(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, size));
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private void addSeparator(JPanel panel) {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        panel.add(sep);
    }

    private void lockBoard() {
        boardLocked = true;
        int size = board.getSize();
        for (int row = 0; row < size; row++)
            for (int col = 0; col < size; col++)
                if (!cells[row][col].isGiven()) cells[row][col].setEditable(false);
    }

    private void startTimer() {
        stopTimer();
        timer = new Timer(1000, event -> {
            elapsedSeconds++;
            if (state != null) state.elapsedSeconds = elapsedSeconds;
            updateTimerLabel();
        });
        timer.start();
    }

    private void stopTimer() {
        if (timer != null) { timer.stop(); timer = null; }
    }

    private void updateTimerLabel() {
        if (timerLabel != null) timerLabel.setText("Waktu: " + formatDuration(elapsedSeconds));
    }

    private void updateAttemptsLabel() {
        if (attemptsLabel != null) attemptsLabel.setText("Kesempatan: " + attemptsRemaining + "/3");
    }

    private String formatDuration(int seconds) {
        return String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  All classes below are unchanged from the original
// ─────────────────────────────────────────────────────────────────────────────

class SudokuMenuPanel extends SudokuGradientPanel {
    private final JTextField  nameField;
    private final JRadioButton easyButton;
    private final JRadioButton mediumButton;
    private final JRadioButton highButton;
    private final JButton      startButton;
    private final JButton      exitButton;

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

        easyButton   = new JRadioButton(SudokuDifficulty.EASY.getLabel());
        mediumButton = new JRadioButton(SudokuDifficulty.MEDIUM.getLabel());
        highButton   = new JRadioButton(SudokuDifficulty.HIGH.getLabel());

        ButtonGroup group = new ButtonGroup();
        group.add(easyButton); group.add(mediumButton); group.add(highButton);
        easyButton.setSelected(true);

        styleRadio(easyButton); styleRadio(mediumButton); styleRadio(highButton);

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

        card.add(nameLabel);       card.add(Box.createVerticalStrut(8));
        card.add(nameField);       card.add(Box.createVerticalStrut(18));
        card.add(difficultyLabel); card.add(Box.createVerticalStrut(10));
        card.add(difficultyPanel); card.add(Box.createVerticalStrut(22));
        card.add(startButton);     card.add(Box.createVerticalStrut(10));
        card.add(exitButton);

        add(Box.createVerticalGlue());
        add(title);    add(Box.createVerticalStrut(6));
        add(subtitle); add(Box.createVerticalStrut(26));
        add(card);
        add(Box.createVerticalGlue());
    }

    public void setStartAction(java.awt.event.ActionListener l) { startButton.addActionListener(l); }
    public void setExitAction(java.awt.event.ActionListener l)  { exitButton.addActionListener(l);  }
    public String getPlayerName() { return nameField.getText().trim(); }

    public SudokuDifficulty getSelectedDifficulty() {
        if (mediumButton.isSelected()) return SudokuDifficulty.MEDIUM;
        if (highButton.isSelected())   return SudokuDifficulty.HIGH;
        return SudokuDifficulty.EASY;
    }

    private void styleRadio(JRadioButton b) {
        b.setOpaque(false);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setForeground(new Color(66, 58, 49));
    }
}

class SudokuGameBoard {
    private static final String SYMBOL_BANK = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final int size, base;
    private final char[] symbols;
    private final Map<Character, Integer> symbolToValue;
    private final Random random;
    private int[][] solution, puzzle;
    private boolean[][] fixed;

    public SudokuGameBoard(SudokuDifficulty d) {
        this.size = d.getSize(); this.base = d.getBase();
        this.symbols = buildSymbols(size);
        this.symbolToValue = buildSymbolMap(symbols);
        this.random = new Random();
        generate(d.getRemovals());
        this.fixed = buildFixed(puzzle);
    }

    private SudokuGameBoard(int size, int base, int[][] sol, boolean[][] fix, int[][] puz) {
        this.size = size; this.base = base;
        this.symbols = buildSymbols(size);
        this.symbolToValue = buildSymbolMap(symbols);
        this.random = new Random();
        this.solution = copyGrid(sol);
        this.fixed    = copyFixed(fix);
        this.puzzle   = copyGrid(puz);
    }

    public static SudokuGameBoard fromState(SudokuGameState s) {
        return new SudokuGameBoard(s.size, s.base, s.solution, s.fixed, s.board);
    }

    public SudokuGameState createState(String playerName, SudokuDifficulty d) {
        SudokuGameState s = new SudokuGameState(size, base, d);
        s.board   = copyGrid(puzzle);
        s.fixed   = copyFixed(fixed);
        s.solution = copyGrid(solution);
        s.playerName        = playerName;
        s.elapsedSeconds    = 0;
        s.attemptsRemaining = 3;
        return s;
    }

    public int getSize()    { return size; }
    public int getBase()    { return base; }
    public char[] getSymbols() { return Arrays.copyOf(symbols, symbols.length); }

    public int symbolToValue(String text) {
        if (text == null) return 0;
        String t = text.trim();
        if (t.isEmpty()) return 0;
        char c = Character.toUpperCase(t.charAt(0));
        return symbolToValue.getOrDefault(c, 0);
    }

    public String valueToSymbol(int v) {
        return (v <= 0 || v > symbols.length) ? "" : String.valueOf(symbols[v - 1]);
    }

    private void generate(int removals) {
        List<Integer> rowOrder = buildShuffledIndices();
        List<Integer> colOrder = buildShuffledIndices();
        List<Integer> numbers  = new ArrayList<>();
        for (int i = 1; i <= size; i++) numbers.add(i);
        Collections.shuffle(numbers, random);

        solution = new int[size][size];
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++) {
                int pattern = (base * (rowOrder.get(r) % base) + rowOrder.get(r) / base
                        + colOrder.get(c)) % size;
                solution[r][c] = numbers.get(pattern);
            }

        puzzle = copyGrid(solution);
        int totalCells  = size * size;
        int minGivens   = (int) Math.ceil(totalCells * 0.35);
        int maxRemovals = Math.max(0, totalCells - minGivens);
        removeCells(Math.min(removals, maxRemovals));
    }

    private List<Integer> buildShuffledIndices() {
        List<Integer> result = new ArrayList<>();
        List<Integer> group  = range(base);
        Collections.shuffle(group, random);
        for (int g : group) {
            List<Integer> row = range(base);
            Collections.shuffle(row, random);
            for (int r : row) result.add(g * base + r);
        }
        return result;
    }

    private void removeCells(int count) {
        int removed = 0;
        while (removed < count) {
            int r = random.nextInt(size), c = random.nextInt(size);
            if (puzzle[r][c] != 0) { puzzle[r][c] = 0; removed++; }
        }
    }

    private boolean[][] buildFixed(int[][] p) {
        boolean[][] f = new boolean[p.length][p[0].length];
        for (int r = 0; r < p.length; r++)
            for (int c = 0; c < p[r].length; c++)
                f[r][c] = p[r][c] != 0;
        return f;
    }

    private List<Integer> range(int max) {
        List<Integer> v = new ArrayList<>();
        for (int i = 0; i < max; i++) v.add(i);
        return v;
    }

    private int[][] copyGrid(int[][] g) {
        int[][] copy = new int[g.length][g[0].length];
        for (int i = 0; i < g.length; i++) copy[i] = Arrays.copyOf(g[i], g[i].length);
        return copy;
    }

    private boolean[][] copyFixed(boolean[][] g) {
        boolean[][] copy = new boolean[g.length][g[0].length];
        for (int i = 0; i < g.length; i++) copy[i] = Arrays.copyOf(g[i], g[i].length);
        return copy;
    }

    private char[] buildSymbols(int sz) {
        if (sz > SYMBOL_BANK.length()) throw new IllegalArgumentException("Ukuran papan terlalu besar");
        return SYMBOL_BANK.substring(0, sz).toCharArray();
    }

    private Map<Character, Integer> buildSymbolMap(char[] syms) {
        Map<Character, Integer> map = new HashMap<>();
        for (int i = 0; i < syms.length; i++) map.put(syms[i], i + 1);
        return map;
    }
}

class SudokuGameState implements Serializable {
    private static final long serialVersionUID = 1L;
    final int size, base;
    final SudokuDifficulty difficulty;
    int[][] board, solution;
    boolean[][] fixed;
    String playerName;
    int elapsedSeconds, attemptsRemaining;

    SudokuGameState(int size, int base, SudokuDifficulty difficulty) {
        this.size = size; this.base = base; this.difficulty = difficulty;
    }

    SudokuGameState copy() {
        SudokuGameState c = new SudokuGameState(size, base, difficulty);
        c.board             = copyGrid(board);
        c.fixed             = copyFixed(fixed);
        c.solution          = copyGrid(solution);
        c.playerName        = playerName;
        c.elapsedSeconds    = elapsedSeconds;
        c.attemptsRemaining = attemptsRemaining;
        return c;
    }

    private int[][] copyGrid(int[][] g) {
        if (g == null) return null;
        int[][] copy = new int[g.length][g[0].length];
        for (int i = 0; i < g.length; i++) copy[i] = Arrays.copyOf(g[i], g[i].length);
        return copy;
    }

    private boolean[][] copyFixed(boolean[][] g) {
        if (g == null) return null;
        boolean[][] copy = new boolean[g.length][g[0].length];
        for (int i = 0; i < g.length; i++) copy[i] = Arrays.copyOf(g[i], g[i].length);
        return copy;
    }
}

class SudokuHistory {
    private final Deque<SudokuGameState> undoStack = new ArrayDeque<>();
    private final Deque<SudokuGameState> redoStack = new ArrayDeque<>();

    void push(SudokuGameState s)  { if (s != null) { undoStack.push(s.copy()); redoStack.clear(); } }

    SudokuGameState undo(SudokuGameState cur) {
        if (undoStack.isEmpty()) return null;
        if (cur != null) redoStack.push(cur.copy());
        return undoStack.pop();
    }

    SudokuGameState redo(SudokuGameState cur) {
        if (redoStack.isEmpty()) return null;
        if (cur != null) undoStack.push(cur.copy());
        return redoStack.pop();
    }

    void clear() { undoStack.clear(); redoStack.clear(); }
}

class SudokuValidator {
    static boolean isValidMove(int[][] board, int row, int col, int value, int base) {
        if (value == 0) return true;
        int size = board.length;
        for (int c = 0; c < size; c++) if (c != col && board[row][c] == value) return false;
        for (int r = 0; r < size; r++) if (r != row && board[r][col] == value) return false;
        int sr = (row / base) * base, sc = (col / base) * base;
        for (int r = sr; r < sr + base; r++)
            for (int c = sc; c < sc + base; c++)
                if ((r != row || c != col) && board[r][c] == value) return false;
        return true;
    }

    static boolean isSolved(int[][] board, int[][] solution) {
        if (board == null) return false;
        for (int r = 0; r < board.length; r++)
            for (int c = 0; c < board[r].length; c++) {
                if (board[r][c] == 0) return false;
                if (solution != null && solution.length > r && solution[r].length > c)
                    if (board[r][c] != solution[r][c]) return false;
            }
        return true;
    }
}

enum SoundEffect {
    CORRECT("correct.wav"), ERROR("error.wav"), WIN("win.wav"),
    LOSE("lose.wav"), HINT("hint.wav");
    final String fileName;
    SoundEffect(String f) { this.fileName = f; }
}

class SudokuSoundPlayer {
    private final Map<SoundEffect, Clip> clips   = new EnumMap<>(SoundEffect.class);
    private final File                   baseDir = new File("sounds");

    void play(SoundEffect effect) {
        Clip clip = loadClip(effect);
        if (clip == null) { Toolkit.getDefaultToolkit().beep(); return; }
        if (clip.isRunning()) clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    private Clip loadClip(SoundEffect effect) {
        if (clips.containsKey(effect)) return clips.get(effect);
        File file = new File(baseDir, effect.fileName);
        if (!file.exists()) { clips.put(effect, null); return null; }
        try (AudioInputStream in = AudioSystem.getAudioInputStream(file)) {
            Clip clip = AudioSystem.getClip();
            clip.open(in);
            clips.put(effect, clip);
            return clip;
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
            clips.put(effect, null); return null;
        }
    }
}

class SudokuLeaderboardEntry {
    final String           playerName;
    final int              seconds;
    final SudokuDifficulty difficulty;

    SudokuLeaderboardEntry(String n, int s, SudokuDifficulty d) {
        this.playerName = n; this.seconds = s; this.difficulty = d;
    }

    String getDuration() {
        return String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60);
    }
}

class SudokuLeaderboard {
    private final int                        limit;
    private final List<SudokuLeaderboardEntry> entries = new ArrayList<>();

    SudokuLeaderboard(int limit) { this.limit = limit; }

    void addEntry(SudokuLeaderboardEntry entry) {
        entries.add(entry);
        entries.sort((a, b) -> Integer.compare(a.seconds, b.seconds));
        while (entries.size() > limit) entries.remove(entries.size() - 1);
    }

    List<SudokuLeaderboardEntry> getEntries() { return new ArrayList<>(entries); }
}

class SudokuGameCell extends JTextField {
    private static final Color GIVEN_BG      = new Color(224, 219, 200);
    private static final Color INPUT_BG      = new Color(247, 244, 233);
    private static final Color SELECTED_BG   = new Color(255, 220, 148);
    private static final Color SELECTION_PULSE = new Color(255, 238, 196);
    private static final Color PEER_BG       = new Color(239, 233, 214);
    private static final Color SAME_BG       = new Color(212, 226, 244);
    private static final Color ERROR_BG      = new Color(255, 196, 196);
    private static final Color SUCCESS_BG    = new Color(204, 235, 206);
    private static final Color GIVEN_FG      = new Color(61, 54, 44);
    private static final Color INPUT_FG      = new Color(35, 35, 35);

    private final boolean given;
    private final int row, col, base;
    private final Color gridLine;
    private boolean error, selected, peerHighlight, sameSymbol;
    private Color animationColor;

    public SudokuGameCell(int row, int col, int base, boolean given, String text,
                          Font font, Dimension size, Color gridLine) {
        super(text);
        this.given = given; this.row = row; this.col = col;
        this.base = base; this.gridLine = gridLine;
        setHorizontalAlignment(SwingConstants.CENTER);
        setFont(font);
        setPreferredSize(size);
        setEditable(false);
        setFocusable(true);
        setCaretColor(new Color(20, 20, 20));
        updateStyle();
        setBorder(buildBorder());
    }

    @Override public void setText(String t) { super.setText(t); updateStyle(); }
    public boolean isGiven() { return given; }
    public int getRow()      { return row; }
    public int getCol()      { return col; }

    public void setError(boolean e)            { if (!given) { error = e; updateStyle(); } }
    public void setSelected(boolean s)         { selected = s; updateStyle(); }
    public void setPeerHighlight(boolean p)    { peerHighlight = p; updateStyle(); }
    public void setSameSymbol(boolean s)       { sameSymbol = s; updateStyle(); }
    public void animateSuccess()               { animatePulse(SUCCESS_BG, 6, 30); }
    public void animateError()                 { animatePulse(ERROR_BG, 6, 30); }
    public void animateSelection()             { animatePulse(SELECTION_PULSE, 6, 30); }

    private void updateStyle() {
        Color bg = given ? GIVEN_BG : INPUT_BG;
        if (peerHighlight) bg = PEER_BG;
        if (sameSymbol)    bg = SAME_BG;
        if (selected)      bg = SELECTED_BG;
        if (error && !given) bg = ERROR_BG;
        if (animationColor != null) bg = animationColor;
        setBackground(bg);
        setForeground(given ? GIVEN_FG : INPUT_FG);
    }

    private void animatePulse(Color pulse, int steps, int delayMs) {
        final Color base0 = getBackground();
        final int total = Math.max(4, steps), half = total / 2;
        final int[] step = {0};
        final Timer[] ref = {null};
        ref[0] = new Timer(delayMs, e -> {
            step[0]++;
            float t = step[0] <= half ? step[0] / (float) half
                                      : (total - step[0]) / (float) half;
            animationColor = lerpColor(base0, pulse, t);
            updateStyle();
            if (step[0] >= total) { animationColor = null; updateStyle(); ref[0].stop(); }
        });
        ref[0].start();
    }

    private Color lerpColor(Color s, Color e, float t) {
        return new Color(
            Math.round(s.getRed()   + (e.getRed()   - s.getRed())   * t),
            Math.round(s.getGreen() + (e.getGreen() - s.getGreen()) * t),
            Math.round(s.getBlue()  + (e.getBlue()  - s.getBlue())  * t)
        );
    }

    private javax.swing.border.Border buildBorder() {
        int thick = 3, thin = 1;
        return BorderFactory.createMatteBorder(
            row % base == 0       ? thick : thin,
            col % base == 0       ? thick : thin,
            (row + 1) % base == 0 ? thick : thin,
            (col + 1) % base == 0 ? thick : thin,
            gridLine
        );
    }
}

class SudokuGradientPanel extends JPanel {
    private final Color start, end;

    SudokuGradientPanel(Color start, Color end) {
        this.start = start; this.end = end; setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}