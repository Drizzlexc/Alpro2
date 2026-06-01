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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
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

/**
 * Sudoku Classic - NetBeans GUI Style
 * Kelas utama yang mengikuti konvensi struktur NetBeans JFrame Form
 */
public class SudokuGame extends javax.swing.JFrame {

    // =========================================================================
    // GAME STATE FIELDS (non-UI)
    // =========================================================================
    private static final Color BACKGROUND   = new Color(227, 224, 204);
    private static final Color HEADER_START = new Color(92,  108,  60);
    private static final Color HEADER_END   = new Color(171, 178, 112);
    private static final Color ACCENT       = new Color(84,  120,  77);
    private static final Color TEXT_ON_DARK = new Color(250, 246, 233);
    private static final Color TEXT_SUBTLE  = new Color(86,   80,  68);
    private static final Color GRID_LINE    = new Color(108,  95,  73);
    private static final Color WOOD         = new Color(176, 136,  92);
    private static final String BGM_FILE    = "sounds/mbg.mp3";
    private static final int    BGM_VOLUME  = 60;

    private SudokuGameBoard    board;
    private SudokuGameState    state;
    private SudokuGameState    initialState;
    private final SudokuHistory     history     = new SudokuHistory();
    private final SudokuLeaderboard leaderboard = new SudokuLeaderboard(10);
    private final SudokuSoundPlayer soundPlayer = new SudokuSoundPlayer();
    private final SudokuMusicPlayer musicPlayer = new SudokuMusicPlayer();

    private SudokuGameCell[][] cells;
    private Timer  gameTimer;
    private int    elapsedSeconds;
    private int    attemptsRemaining;
    private String playerName   = "Pemain";
    private String selectedSymbol = "";
    private final List<JButton> symbolButtons = new ArrayList<>();
    private SudokuGameCell selectedCell;
    private boolean boardLocked;
    private String currentView = "menu";

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public SudokuGame() {
        initComponents();
        postInit();
    }

    // =========================================================================
    // INIT COMPONENTS (NetBeans style — semua inisialisasi komponen UI di sini)
    // =========================================================================
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        // ---------- Root (CardLayout) ----------
        viewLayout  = new CardLayout();
        rootPanel   = new javax.swing.JPanel(viewLayout);

        // ---------- Menu Panel ----------
        menuNameField      = new javax.swing.JTextField();
        menuEasyRadio      = new javax.swing.JRadioButton(SudokuDifficulty.EASY.getLabel());
        menuMediumRadio    = new javax.swing.JRadioButton(SudokuDifficulty.MEDIUM.getLabel());
        menuHighRadio      = new javax.swing.JRadioButton(SudokuDifficulty.HIGH.getLabel());
        menuDifficultyGroup = new ButtonGroup();
        menuStartButton    = new javax.swing.JButton("Mulai Bermain");
        menuExitButton     = new javax.swing.JButton("Keluar");
        menuPanel          = buildMenuPanel();

        // ---------- Game Panel: Header ----------
        headerPanel        = buildHeaderGradientPanel();
        titleLabel         = new javax.swing.JLabel("Sudoku Classic");
        statusLabel        = new javax.swing.JLabel("Isi semua kotak sesuai aturan Sudoku.");
        difficultyLabel    = new javax.swing.JLabel();
        playerLabel        = new javax.swing.JLabel();
        timerLabel         = new javax.swing.JLabel("Waktu: 00:00");
        attemptsLabel      = new javax.swing.JLabel("Kesempatan: 3/3");

        // ---------- Game Panel: Grid area ----------
        gridPanel          = new javax.swing.JPanel(new BorderLayout());

        // ---------- Game Panel: Footer buttons ----------
        undoButton         = new javax.swing.JButton("Undo");
        redoButton         = new javax.swing.JButton("Redo");
        hintButton         = new javax.swing.JButton("Hint");
        checkButton        = new javax.swing.JButton("Cek");
        saveButton         = new javax.swing.JButton("Simpan");
        loadButton         = new javax.swing.JButton("Muat");
        menuButton         = new javax.swing.JButton("Menu");
        resetButton        = new javax.swing.JButton("Reset");

        // ---------- Assemble ----------
        setupFrame();
        setupHeader();
        setupFooter();
        setupGamePanel();
        assembleRoot();

    }// </editor-fold>//GEN-END:initComponents

    // =========================================================================
    // POST-INIT (dipanggil setelah initComponents)
    // =========================================================================
    private void postInit() {
        // Pasang listener menu
        menuStartButton.addActionListener(evt -> menuStartButtonActionPerformed(evt));
        menuExitButton.addActionListener(evt  -> menuExitButtonActionPerformed(evt));

        // Pasang listener footer
        undoButton.addActionListener(evt  -> undoButtonActionPerformed(evt));
        redoButton.addActionListener(evt  -> redoButtonActionPerformed(evt));
        hintButton.addActionListener(evt  -> hintButtonActionPerformed(evt));
        checkButton.addActionListener(evt -> checkButtonActionPerformed(evt));
        saveButton.addActionListener(evt  -> saveButtonActionPerformed(evt));
        loadButton.addActionListener(evt  -> loadButtonActionPerformed(evt));
        menuButton.addActionListener(evt  -> menuButtonActionPerformed(evt));
        resetButton.addActionListener(evt -> resetButtonActionPerformed(evt));

        // ESC keluar full-screen
        installEscapeShortcut();

        musicPlayer.startLoop(BGM_FILE, BGM_VOLUME);

        viewLayout.show(rootPanel, "menu");
    }

    // =========================================================================
    // SETUP HELPERS (dipanggil dari initComponents)
    // =========================================================================
    private void setupFrame() {
        setTitle("Sudoku Classic");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen);
        setLocation(0, 0);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                if ("menu".equals(currentView)) shutdownApplication();
            }
        });
    }

    private void setupHeader() {
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 30));
        titleLabel.setForeground(TEXT_ON_DARK);

        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(246, 240, 221));

        difficultyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        difficultyLabel.setForeground(new Color(244, 238, 212));

        playerLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        playerLabel.setForeground(new Color(255, 248, 235));

        timerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        timerLabel.setForeground(new Color(246, 240, 221));

        attemptsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        attemptsLabel.setForeground(new Color(246, 240, 221));

        javax.swing.JPanel textPanel = new javax.swing.JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(titleLabel,  BorderLayout.NORTH);
        textPanel.add(statusLabel, BorderLayout.SOUTH);

        javax.swing.JPanel infoPanel = new javax.swing.JPanel();
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

        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        headerPanel.add(textPanel, BorderLayout.WEST);
        headerPanel.add(infoPanel, BorderLayout.EAST);
    }

    private void setupFooter() {
        styleFooterButton(undoButton,  new Color(237, 231, 214), new Color(74, 66, 56));
        styleFooterButton(redoButton,  new Color(237, 231, 214), new Color(74, 66, 56));
        styleFooterButton(hintButton,  ACCENT, Color.WHITE);
        styleFooterButton(checkButton, new Color(214, 176, 114), new Color(58, 48, 38));
        styleFooterButton(saveButton,  new Color(235, 229, 213), new Color(74, 66, 56));
        styleFooterButton(loadButton,  new Color(235, 229, 213), new Color(74, 66, 56));
        styleFooterButton(menuButton,  WOOD, Color.WHITE);
        styleFooterButton(resetButton, new Color(235, 229, 213), new Color(74, 66, 56));

        footerPanel = new javax.swing.JPanel(new GridLayout(2, 4, 10, 10));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        footerPanel.setBackground(BACKGROUND);
        footerPanel.add(undoButton);
        footerPanel.add(redoButton);
        footerPanel.add(hintButton);
        footerPanel.add(checkButton);
        footerPanel.add(saveButton);
        footerPanel.add(loadButton);
        footerPanel.add(menuButton);
        footerPanel.add(resetButton);
    }

    private void setupGamePanel() {
        gridPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
        gridPanel.setBackground(BACKGROUND);

        gamePanel = new javax.swing.JPanel(new BorderLayout());
        gamePanel.setBackground(BACKGROUND);
        gamePanel.add(headerPanel, BorderLayout.NORTH);
        gamePanel.add(gridPanel,   BorderLayout.CENTER);
        gamePanel.add(footerPanel, BorderLayout.SOUTH);
    }

    private void assembleRoot() {
        rootPanel.add(menuPanel, "menu");
        rootPanel.add(gamePanel, "game");
        setContentPane(rootPanel);
    }

    private javax.swing.JPanel buildMenuPanel() {
        SudokuGradientPanel panel = new SudokuGradientPanel(
                new Color(82, 90, 56), new Color(145, 132, 80));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Sudoku Classic");
        title.setFont(new Font("Georgia", Font.BOLD, 42));
        title.setForeground(new Color(252, 245, 233));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Tantang logikamu dan kejar waktu terbaik.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(new Color(230, 219, 201));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Card
        javax.swing.JPanel card = new javax.swing.JPanel();
        card.setBackground(new Color(255, 249, 240, 235));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(204, 181, 153)),
            BorderFactory.createEmptyBorder(24, 28, 24, 28)));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(520, 360));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel("Nama Pemain");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        menuNameField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        menuNameField.setMaximumSize(new Dimension(480, 38));
        menuNameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 187, 170)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        JLabel diffLabel = new JLabel("Pilih Kesulitan");
        diffLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        diffLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        menuDifficultyGroup.add(menuEasyRadio);
        menuDifficultyGroup.add(menuMediumRadio);
        menuDifficultyGroup.add(menuHighRadio);
        menuEasyRadio.setSelected(true);

        styleRadioButton(menuEasyRadio);
        styleRadioButton(menuMediumRadio);
        styleRadioButton(menuHighRadio);

        javax.swing.JPanel diffPanel = new javax.swing.JPanel(new GridLayout(1, 3, 12, 12));
        diffPanel.setOpaque(false);
        diffPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        diffPanel.add(menuEasyRadio);
        diffPanel.add(menuMediumRadio);
        diffPanel.add(menuHighRadio);

        menuStartButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        menuStartButton.setForeground(Color.WHITE);
        menuStartButton.setBackground(new Color(84, 120, 77));
        menuStartButton.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        menuStartButton.setFocusPainted(false);
        menuStartButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        menuStartButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        menuExitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        menuExitButton.setForeground(new Color(70, 62, 52));
        menuExitButton.setBackground(new Color(232, 224, 208));
        menuExitButton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        menuExitButton.setFocusPainted(false);
        menuExitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        menuExitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(nameLabel);         card.add(Box.createVerticalStrut(8));
        card.add(menuNameField);     card.add(Box.createVerticalStrut(18));
        card.add(diffLabel);         card.add(Box.createVerticalStrut(10));
        card.add(diffPanel);         card.add(Box.createVerticalStrut(22));
        card.add(menuStartButton);   card.add(Box.createVerticalStrut(10));
        card.add(menuExitButton);

        panel.add(Box.createVerticalGlue());
        panel.add(title);    panel.add(Box.createVerticalStrut(6));
        panel.add(subtitle); panel.add(Box.createVerticalStrut(26));
        panel.add(card);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private SudokuGradientPanel buildHeaderGradientPanel() {
        return new SudokuGradientPanel(HEADER_START, HEADER_END);
    }

    // =========================================================================
    // ACTION PERFORMED METHODS (NetBeans style — satu method per komponen)
    // =========================================================================

    private void menuStartButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuStartButtonActionPerformed
        String inputName = menuNameField.getText().trim();
        playerName = inputName.isEmpty() ? "Pemain" : inputName;
        SudokuDifficulty selected = getSelectedDifficulty();
        startNewGame(selected);
        currentView = "game";
        viewLayout.show(rootPanel, "game");
    }//GEN-LAST:event_menuStartButtonActionPerformed

    private void menuExitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExitButtonActionPerformed
        shutdownApplication();
    }//GEN-LAST:event_menuExitButtonActionPerformed

    private void undoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoButtonActionPerformed
        SudokuGameState prev = history.undo(state);
        if (prev == null) { statusLabel.setText("Tidak ada undo."); return; }
        applyState(prev, false, "Undo diterapkan.");
    }//GEN-LAST:event_undoButtonActionPerformed

    private void redoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoButtonActionPerformed
        SudokuGameState next = history.redo(state);
        if (next == null) { statusLabel.setText("Tidak ada redo."); return; }
        applyState(next, false, "Redo diterapkan.");
    }//GEN-LAST:event_redoButtonActionPerformed

    private void hintButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hintButtonActionPerformed
        if (state == null) return;
        int row = -1, col = -1;
        if (selectedCell != null && !selectedCell.isGiven()) {
            row = selectedCell.getRow();
            col = selectedCell.getCol();
            if (state.board[row][col] != 0) { row = -1; col = -1; }
        }
        if (row == -1) {
            outer:
            for (int r = 0; r < state.size; r++)
                for (int c = 0; c < state.size; c++)
                    if (!state.fixed[r][c] && state.board[r][c] == 0) { row = r; col = c; break outer; }
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
    }//GEN-LAST:event_hintButtonActionPerformed

    private void checkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkButtonActionPerformed
        int size = board.getSize();
        boolean anyEmpty = false, anyError = false;
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
    }//GEN-LAST:event_checkButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        if (state == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan permainan");
        chooser.setFileFilter(new FileNameExtensionFilter("Sudoku Save (*.sdk)", "sdk"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.US).endsWith(".sdk"))
            file = new File(file.getParentFile(), file.getName() + ".sdk");
        SudokuGameState snapshot = state.copy();
        snapshot.playerName        = playerName;
        snapshot.elapsedSeconds    = elapsedSeconds;
        snapshot.attemptsRemaining = attemptsRemaining;
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            out.writeObject(snapshot);
            statusLabel.setText("Permainan disimpan.");
        } catch (IOException ex) {
            showError("Gagal menyimpan permainan.");
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
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
    }//GEN-LAST:event_loadButtonActionPerformed

    private void menuButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuButtonActionPerformed
        stopTimer();
        gamePanel.setEnabled(true);
        currentView = "menu";
        viewLayout.show(rootPanel, "menu");
    }//GEN-LAST:event_menuButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        if (initialState == null) return;
        applyState(initialState.copy(), true, "Puzzle direset ke awal.");
    }//GEN-LAST:event_resetButtonActionPerformed

    // Dipanggil saat sel grid diklik
    private void gridCellMousePressed(SudokuGameCell cell) {//GEN-FIRST:event_gridCellMousePressed
        if (boardLocked) return;
        selectCell(cell);
        if (cell.isGiven()) return;
        if (selectedSymbol == null) return;
        if (selectedSymbol.isEmpty()) { applyMove(cell, 0); return; }
        int value = board.symbolToValue(selectedSymbol);
        applyMove(cell, value);
    }//GEN-LAST:event_gridCellMousePressed

    // Dipanggil saat tombol simbol (angka) diklik
    private void symbolButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_symbolButtonActionPerformed
        JButton source = (JButton) evt.getSource();
        String symbol = (String) source.getClientProperty("symbol");
        setSelectedSymbol(symbol);
    }//GEN-LAST:event_symbolButtonActionPerformed

    // =========================================================================
    // GAME LOGIC METHODS
    // =========================================================================
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
        selectedCell  = null;
        boardLocked   = false;
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
            BorderFactory.createEmptyBorder(6, 6, 6, 6)));

        cells = new SudokuGameCell[size][size];
        int[][]     values = state.board;
        boolean[][] fixed  = state.fixed;
        Font      cellFont = new Font("Segoe UI", Font.BOLD, size == 16 ? 18 : 22);
        Dimension cellSize = size == 16 ? new Dimension(36, 36) : new Dimension(48, 48);
        char[]    symbols  = board.getSymbols();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int     value = values[row][col];
                boolean given = fixed[row][col];
                String  text  = board.valueToSymbol(value);
                SudokuGameCell cell = new SudokuGameCell(row, col, base, given, text,
                        cellFont, cellSize, GRID_LINE);
                cell.addMouseListener(new MouseAdapter() {
                    @Override public void mousePressed(MouseEvent e) {
                        gridCellMousePressed(cell);
                    }
                });
                cells[row][col] = cell;
                grid.add(cell);
            }
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BACKGROUND);
        wrapper.add(grid,                     BorderLayout.CENTER);
        wrapper.add(buildNumberPad(symbols),  BorderLayout.SOUTH);
        gridPanel.add(wrapper, BorderLayout.CENTER);
        gridPanel.revalidate();
        gridPanel.repaint();
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

        int columns = symbols.length <= 9 ? symbols.length + 1 : 8;
        JPanel grid = new JPanel(new GridLayout(0, columns, 8, 8));
        grid.setOpaque(false);

        for (char symbol : symbols) {
            JButton btn = createChoiceButton(String.valueOf(symbol));
            registerSymbolButton(btn, String.valueOf(symbol));
            grid.add(btn);
        }

        JButton clearBtn = createChoiceButton("Hapus");
        clearBtn.setBackground(new Color(232, 224, 208));
        clearBtn.setForeground(new Color(70, 62, 52));
        registerSymbolButton(clearBtn, "");
        grid.add(clearBtn);

        pad.add(label, BorderLayout.NORTH);
        pad.add(grid,  BorderLayout.CENTER);

        if (symbols.length > 0) setSelectedSymbol(String.valueOf(symbols[0]));
        return pad;
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
        if (value != 0) { cell.animateSuccess(); soundPlayer.play(SoundEffect.CORRECT); }
        updateHighlights();
        if (SudokuValidator.isSolved(state.board, state.solution)) handleWin();
    }

    private void flashInvalid(SudokuGameCell cell) {
        cell.setError(true);
        Timer flash = new Timer(450, e -> cell.setError(false));
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
        int size = state.size, base = state.base;
        int row  = selectedCell == null ? -1 : selectedCell.getRow();
        int col  = selectedCell == null ? -1 : selectedCell.getCol();
        String selVal = selectedCell == null ? "" : selectedCell.getText();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                SudokuGameCell cell = cells[r][c];
                boolean selected   = r == row && c == col;
                boolean peer       = row >= 0 && (r == row || c == col
                        || (r/base == row/base && c/base == col/base));
                boolean sameSymbol = !selVal.isEmpty() && selVal.equals(cell.getText());
                cell.setSelected(selected);
                cell.setPeerHighlight(peer);
                cell.setSameSymbol(sameSymbol);
            }
        }
    }

    private void handleWin() {
        stopTimer();
        lockBoard();
        leaderboard.addEntry(new SudokuLeaderboardEntry(playerName, elapsedSeconds, state.difficulty));
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

    private void showWinOverlay() {
        String winner   = playerName == null || playerName.isEmpty() ? "Pemain" : playerName;
        String duration = formatDuration(elapsedSeconds);

        JDialog dialog = new JDialog(this, "Selesai", true);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setSize(getSize());
        dialog.setLocationRelativeTo(this);
        gamePanel.setEnabled(false);

        SudokuGradientPanel overlay = new SudokuGradientPanel(
                new Color(77, 90, 56), new Color(164, 170, 108));
        overlay.setLayout(new BoxLayout(overlay, BoxLayout.Y_AXIS));
        overlay.setBorder(BorderFactory.createEmptyBorder(100, 50, 100, 50));

        JLabel lbTitle  = createOverlayLabel("🎉 SELAMAT 🎉",     52, TEXT_ON_DARK);
        JLabel lbName   = createOverlayLabel("Pemain: " + winner, 32, new Color(255, 215, 0));
        JLabel lbSub    = createOverlayLabel("Anda Berhasil Menyelesaikan SUDOKU", 22, new Color(248, 243, 223));
        JLabel lbTime   = createOverlayLabel("⏱️ Waktu: " + duration, 20, new Color(200, 255, 200));

        JPanel lbPanel = buildLeaderboardPanel();

        JButton btnPlayAgain = createFooterButton("Main Lagi", ACCENT, Color.WHITE);
        btnPlayAgain.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPlayAgain.addActionListener(e -> {
            SudokuDifficulty diff = state == null ? SudokuDifficulty.MEDIUM : state.difficulty;
            gamePanel.setEnabled(true);
            dialog.dispose();
            startNewGame(diff);
            currentView = "game";
            viewLayout.show(rootPanel, "game");
        });

        JButton btnMenu = createFooterButton("Kembali ke Menu", WOOD, Color.WHITE);
        btnMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnMenu.addActionListener(e -> {
            gamePanel.setEnabled(true);
            dialog.dispose();
            menuButtonActionPerformed(null);
        });

        overlay.add(Box.createVerticalGlue());
        overlay.add(lbTitle);  overlay.add(Box.createVerticalStrut(12));
        overlay.add(lbName);   overlay.add(Box.createVerticalStrut(16));
        overlay.add(lbSub);    overlay.add(Box.createVerticalStrut(12));
        overlay.add(lbTime);   overlay.add(Box.createVerticalStrut(24));
        addSeparator(overlay);  overlay.add(Box.createVerticalStrut(16));
        overlay.add(lbPanel);  overlay.add(Box.createVerticalStrut(24));
        addSeparator(overlay);  overlay.add(Box.createVerticalStrut(16));
        overlay.add(btnPlayAgain); overlay.add(Box.createVerticalStrut(10));
        overlay.add(btnMenu);
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
        dialog.setSize(getSize());
        dialog.setLocationRelativeTo(this);
        gamePanel.setEnabled(false);

        SudokuGradientPanel overlay = new SudokuGradientPanel(
                new Color(88, 72, 56), new Color(169, 146, 110));
        overlay.setLayout(new BoxLayout(overlay, BoxLayout.Y_AXIS));
        overlay.setBorder(BorderFactory.createEmptyBorder(100, 50, 100, 50));

        JLabel lbTitle  = createOverlayLabel("❌ GAME SELESAI ❌",                  52, TEXT_ON_DARK);
        JLabel lbSub    = createOverlayLabel("Maaf, Anda Gagal Menyelesaikan Game", 24, new Color(255, 150, 150));
        JLabel lbMsg    = createOverlayLabel("Jangan Menyerah! Coba Lagi!",         20, new Color(248, 243, 223));

        JButton btnPlayAgain = createFooterButton("Main Lagi", ACCENT, Color.WHITE);
        btnPlayAgain.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPlayAgain.addActionListener(e -> {
            SudokuDifficulty diff = state == null ? SudokuDifficulty.MEDIUM : state.difficulty;
            gamePanel.setEnabled(true);
            dialog.dispose();
            startNewGame(diff);
            currentView = "game";
            viewLayout.show(rootPanel, "game");
        });

        JButton btnMenu = createFooterButton("Kembali ke Menu", WOOD, Color.WHITE);
        btnMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnMenu.addActionListener(e -> {
            gamePanel.setEnabled(true);
            dialog.dispose();
            menuButtonActionPerformed(null);
        });

        overlay.add(Box.createVerticalGlue());
        overlay.add(lbTitle); overlay.add(Box.createVerticalStrut(16));
        overlay.add(lbSub);   overlay.add(Box.createVerticalStrut(12));
        overlay.add(lbMsg);   overlay.add(Box.createVerticalStrut(40));
        addSeparator(overlay); overlay.add(Box.createVerticalStrut(32));
        overlay.add(btnPlayAgain); overlay.add(Box.createVerticalStrut(10));
        overlay.add(btnMenu);
        overlay.add(Box.createVerticalGlue());

        dialog.setContentPane(overlay);
        dialog.setResizable(false);
        dialog.setVisible(true);
        gamePanel.setEnabled(true);
    }

    // =========================================================================
    // UI HELPER METHODS
    // =========================================================================
    private void setSelectedSymbol(String symbol) {
        selectedSymbol = symbol;
        for (JButton btn : symbolButtons) {
            String val  = (String) btn.getClientProperty("symbol");
            boolean sel = symbol.equals(val);
            Color baseBg = (Color) btn.getClientProperty("baseBg");
            Color baseFg = (Color) btn.getClientProperty("baseFg");
            btn.setBackground(sel ? ACCENT      : baseBg);
            btn.setForeground(sel ? Color.WHITE : baseFg);
        }
    }

    private void registerSymbolButton(JButton btn, String symbol) {
        btn.putClientProperty("symbol", symbol);
        btn.putClientProperty("baseBg", btn.getBackground());
        btn.putClientProperty("baseFg", btn.getForeground());
        btn.addActionListener(this::symbolButtonActionPerformed);
        symbolButtons.add(btn);
    }

    private JButton createChoiceButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(248, 244, 235));
        btn.setForeground(new Color(70, 62, 52));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(198, 177, 151)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createFooterButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(166, 144, 112)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleFooterButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(166, 144, 112)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleRadioButton(JRadioButton rb) {
        rb.setOpaque(false);
        rb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rb.setForeground(new Color(66, 58, 49));
    }

    private JLabel createOverlayLabel(String text, int size, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, size));
        lbl.setForeground(color);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    private void addSeparator(JPanel panel) {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        panel.add(sep);
    }

    private JPanel buildLeaderboardPanel() {
        JPanel card = new JPanel();
        card.setBackground(new Color(255, 249, 240, 232));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(195, 176, 148)),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)));
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
        int idx = 1;
        for (SudokuLeaderboardEntry entry : entries) {
            String rowText = idx + ". " + entry.playerName + " - " + entry.getDuration()
                    + " (" + entry.difficulty.getLabel() + ")";
            JLabel row = new JLabel(rowText);
            row.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            row.setForeground(new Color(92, 78, 62));
            row.setAlignmentX(Component.CENTER_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
            card.add(row);
            idx++;
        }
        return card;
    }

    private void lockBoard() {
        boardLocked = true;
        int size = board.getSize();
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (!cells[r][c].isGiven()) cells[r][c].setEditable(false);
    }

    private void startTimer() {
        stopTimer();
        gameTimer = new Timer(1000, e -> {
            elapsedSeconds++;
            if (state != null) state.elapsedSeconds = elapsedSeconds;
            updateTimerLabel();
        });
        gameTimer.start();
    }

    private void stopTimer() {
        if (gameTimer != null) { gameTimer.stop(); gameTimer = null; }
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

    private SudokuDifficulty getSelectedDifficulty() {
        if (menuMediumRadio.isSelected()) return SudokuDifficulty.MEDIUM;
        if (menuHighRadio.isSelected())   return SudokuDifficulty.HIGH;
        return SudokuDifficulty.EASY;
    }

    private void shutdownApplication() {
        stopTimer();
        musicPlayer.stop();
        dispose();
        System.exit(0);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }

    private void installEscapeShortcut() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "exitFS");
        getRootPane().getActionMap().put("exitFS", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (!isUndecorated()) return;
                setVisible(false);
                dispose();
                setUndecorated(false);
                setResizable(true);
                setMinimumSize(new Dimension(960, 720));
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                setVisible(true);
            }
        });
    }

    // =========================================================================
    // MAIN METHOD (NetBeans style)
    // =========================================================================
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SudokuGame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SudokuGame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SudokuGame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SudokuGame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SudokuGame().setVisible(true);
            }
        });
    }

    // =========================================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // =========================================================================

    // Layout
    private CardLayout              viewLayout;
    private javax.swing.JPanel      rootPanel;

    // Menu components
    private javax.swing.JPanel      menuPanel;
    private javax.swing.JTextField  menuNameField;
    private javax.swing.JRadioButton menuEasyRadio;
    private javax.swing.JRadioButton menuMediumRadio;
    private javax.swing.JRadioButton menuHighRadio;
    private ButtonGroup             menuDifficultyGroup;
    private javax.swing.JButton     menuStartButton;
    private javax.swing.JButton     menuExitButton;

    // Game panel
    private javax.swing.JPanel      gamePanel;

    // Header components
    private SudokuGradientPanel     headerPanel;
    private javax.swing.JLabel      titleLabel;
    private javax.swing.JLabel      statusLabel;
    private javax.swing.JLabel      difficultyLabel;
    private javax.swing.JLabel      playerLabel;
    private javax.swing.JLabel      timerLabel;
    private javax.swing.JLabel      attemptsLabel;

    // Grid area
    private javax.swing.JPanel      gridPanel;

    // Footer buttons
    private javax.swing.JPanel      footerPanel;
    private javax.swing.JButton     undoButton;
    private javax.swing.JButton     redoButton;
    private javax.swing.JButton     hintButton;
    private javax.swing.JButton     checkButton;
    private javax.swing.JButton     saveButton;
    private javax.swing.JButton     loadButton;
    private javax.swing.JButton     menuButton;
    private javax.swing.JButton     resetButton;

    // End of variables declaration//GEN-END:variables
}

// =============================================================================
// SUPPORTING CLASSES
// =============================================================================

enum SudokuDifficulty {
    EASY("Mudah 4x4", 4, 2, 6),
    MEDIUM("Sedang 9x9", 9, 3, 40),
    HIGH("Sulit 16x16", 16, 4, 160);

    private final String label;
    private final int size, base, removals;

    SudokuDifficulty(String label, int size, int base, int removals) {
        this.label = label; this.size = size; this.base = base; this.removals = removals;
    }

    public String getLabel()    { return label; }
    public int    getSize()     { return size; }
    public int    getBase()     { return base; }
    public int    getRemovals() { return removals; }
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

    public int    getSize()    { return size; }
    public int    getBase()    { return base; }
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
        int maxRemovals = Math.max(0, size * size - (int) Math.ceil(size * size * 0.35));
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

final class SudokuPaths {
    private SudokuPaths() {}

    static File findDirUp(String dirName) {
        File dir = new File(System.getProperty("user.dir"));
        for (int i = 0; i < 6 && dir != null; i++) {
            File candidate = new File(dir, dirName);
            if (candidate.isDirectory()) return candidate;
            dir = dir.getParentFile();
        }
        return new File(System.getProperty("user.dir"), dirName);
    }

    static File findFileUp(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) return null;
        File dir = new File(System.getProperty("user.dir"));
        for (int i = 0; i < 6 && dir != null; i++) {
            File candidate = new File(dir, relativePath);
            if (candidate.isFile()) return candidate;
            dir = dir.getParentFile();
        }
        return null;
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
    private final File                   baseDir = SudokuPaths.findDirUp("sounds");

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

class SudokuMusicPlayer {
    private final File baseDir = SudokuPaths.findDirUp("sounds");
    private volatile ClassLoader jlayerLoader;
    private volatile boolean running;
    private volatile Object currentPlayer;
    private Thread worker;

    void startLoop(String filePath, int volumePercent) {
        stop();
        File file = resolveFile(filePath);
        if (file == null || !file.exists()) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        if (!isJLayerAvailable()) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        running = true;
        worker = new Thread(() -> loop(file, volumePercent), "SudokuBgm");
        worker.setDaemon(true);
        worker.start();
    }

    void stop() {
        running = false;
        closeCurrentPlayer();
        if (worker != null) {
            worker.interrupt();
            worker = null;
        }
    }

    private void loop(File file, int volumePercent) {
        while (running) {
            playOnce(file, volumePercent);
        }
    }

    private void playOnce(File file, int volumePercent) {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            Object audioDevice = createAudioDevice(volumePercent);
            Object player = createAdvancedPlayer(in, audioDevice);
            currentPlayer = player;
            invokeNoArgs(player, "play");
        } catch (Exception ex) {
            running = false;
        } finally {
            currentPlayer = null;
        }
    }

    private File resolveFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) return null;
        File file = new File(filePath);
        if (file.isAbsolute() && file.exists()) return file;
        File candidate = new File(baseDir, filePath);
        if (candidate.exists()) return candidate;
        File fallback = SudokuPaths.findFileUp(filePath);
        return fallback != null ? fallback : file;
    }

    private boolean isJLayerAvailable() {
        return getJLayerLoader() != null;
    }

    private Object createAdvancedPlayer(InputStream in, Object audioDevice) throws Exception {
        ClassLoader loader = getJLayerLoader();
        if (loader == null) throw new ClassNotFoundException("JLayer not found");
        Class<?> audioDeviceType = Class.forName("javazoom.jl.player.AudioDevice", true, loader);
        Class<?> playerClass = Class.forName("javazoom.jl.player.advanced.AdvancedPlayer", true, loader);
        try {
            return playerClass.getConstructor(InputStream.class, audioDeviceType)
                              .newInstance(in, audioDevice);
        } catch (NoSuchMethodException ex) {
            return playerClass.getConstructor(InputStream.class).newInstance(in);
        }
    }

    private Object createAudioDevice(int volumePercent) throws Exception {
        ClassLoader loader = getJLayerLoader();
        if (loader == null) throw new ClassNotFoundException("JLayer not found");
        Class<?> deviceClass = Class.forName("javazoom.jl.player.JavaSoundAudioDevice", true, loader);
        Object device = deviceClass.getDeclaredConstructor().newInstance();
        applyVolume(device, volumePercent);
        return device;
    }

    private ClassLoader getJLayerLoader() {
        ClassLoader loader = jlayerLoader;
        if (loader != null) return loader;

        ClassLoader context = Thread.currentThread().getContextClassLoader();
        if (canLoadJLayer(context)) {
            jlayerLoader = context;
            return context;
        }

        File jar = SudokuPaths.findFileUp("lib/jlayer-1.0.1.jar");
        if (jar != null) {
            try {
                java.net.URL jarUrl = jar.toURI().toURL();
                ClassLoader urlLoader = new java.net.URLClassLoader(new java.net.URL[] { jarUrl }, context);
                if (canLoadJLayer(urlLoader)) {
                    jlayerLoader = urlLoader;
                    return urlLoader;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private boolean canLoadJLayer(ClassLoader loader) {
        if (loader == null) return false;
        try {
            Class.forName("javazoom.jl.player.advanced.AdvancedPlayer", true, loader);
            Class.forName("javazoom.jl.player.JavaSoundAudioDevice", true, loader);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    private void applyVolume(Object audioDevice, int volumePercent) {
        float gain = volumeToGain(volumePercent);
        try {
            Method method = audioDevice.getClass().getMethod("setLineGain", float.class);
            method.invoke(audioDevice, gain);
        } catch (Exception ignored) {
        }
    }

    private float volumeToGain(int volumePercent) {
        int clamped = Math.max(0, Math.min(100, volumePercent));
        if (clamped == 0) return -80.0f;
        double linear = clamped / 100.0;
        double db = 20.0 * Math.log10(linear);
        if (db < -80.0) db = -80.0;
        if (db > 6.0) db = 6.0;
        return (float) db;
    }

    private void closeCurrentPlayer() {
        Object player = currentPlayer;
        if (player == null) return;
        invokeIfPresent(player, "close");
        invokeIfPresent(player, "stop");
    }

    private void invokeNoArgs(Object target, String name) throws Exception {
        Method method = target.getClass().getMethod(name);
        method.invoke(target);
    }

    private void invokeIfPresent(Object target, String name) {
        try {
            Method method = target.getClass().getMethod(name);
            method.invoke(target);
        } catch (Exception ignored) {
        }
    }
}

class SudokuLeaderboardEntry {
    final String playerName;
    final int    seconds;
    final SudokuDifficulty difficulty;

    SudokuLeaderboardEntry(String n, int s, SudokuDifficulty d) {
        this.playerName = n; this.seconds = s; this.difficulty = d;
    }

    String getDuration() {
        return String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60);
    }
}

class SudokuLeaderboard {
    private final int limit;
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
    private static final Color GIVEN_BG        = new Color(224, 219, 200);
    private static final Color INPUT_BG        = new Color(247, 244, 233);
    private static final Color SELECTED_BG     = new Color(255, 220, 148);
    private static final Color SELECTION_PULSE = new Color(255, 238, 196);
    private static final Color PEER_BG         = new Color(239, 233, 214);
    private static final Color SAME_BG         = new Color(212, 226, 244);
    private static final Color ERROR_BG        = new Color(255, 196, 196);
    private static final Color SUCCESS_BG      = new Color(204, 235, 206);
    private static final Color GIVEN_FG        = new Color(61, 54, 44);
    private static final Color INPUT_FG        = new Color(35, 35, 35);

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

    public void setError(boolean e)         { if (!given) { error = e; updateStyle(); } }
    public void setSelected(boolean s)      { selected = s; updateStyle(); }
    public void setPeerHighlight(boolean p) { peerHighlight = p; updateStyle(); }
    public void setSameSymbol(boolean s)    { sameSymbol = s; updateStyle(); }
    public void animateSuccess()            { animatePulse(SUCCESS_BG, 6, 30); }
    public void animateError()              { animatePulse(ERROR_BG, 6, 30); }
    public void animateSelection()          { animatePulse(SELECTION_PULSE, 6, 30); }

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
            Math.round(s.getBlue()  + (e.getBlue()  - s.getBlue())  * t));
    }

    private javax.swing.border.Border buildBorder() {
        int thick = 3, thin = 1;
        return BorderFactory.createMatteBorder(
            row % base == 0       ? thick : thin,
            col % base == 0       ? thick : thin,
            (row + 1) % base == 0 ? thick : thin,
            (col + 1) % base == 0 ? thick : thin,
            gridLine);
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