import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class Main {
    private static final DecimalFormat FORMAT = createFormatter();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowUi);
    }

    private static void createAndShowUi() {
        JFrame frame = new JFrame("Kalkulator Java");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(320, 480));

        JTextField display = new JTextField("0");
        display.setEditable(false);
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setFont(new Font("Segoe UI", Font.BOLD, 32));
        display.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));
        display.setBackground(Color.WHITE);

        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        displayPanel.setBackground(new Color(242, 242, 242));
        displayPanel.add(display, BorderLayout.CENTER);

        JPanel grid = new JPanel(new GridLayout(5, 4, 10, 10));
        grid.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
        grid.setBackground(new Color(242, 242, 242));

        double[] storedValue = {0.0};
        String[] pendingOp = {null};
        boolean[] startNewNumber = {true};

        String[][] rows = new String[][] {
            {"C", "DEL", "%", "/"},
            {"7", "8", "9", "*"},
            {"4", "5", "6", "-"},
            {"1", "2", "3", "+"},
            {"+/-", "0", ".", "="}
        };

        Font buttonFont = new Font("Segoe UI", Font.BOLD, 18);
        Color numberBg = Color.WHITE;
        Color opBg = new Color(255, 149, 0);
        Color opFg = Color.WHITE;
        Color controlBg = new Color(226, 226, 226);

        for (String[] row : rows) {
            for (String label : row) {
                JButton button = new JButton(label);
                button.setFont(buttonFont);
                button.setFocusPainted(false);
                button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                if (label.matches("[0-9]")) {
                    button.setBackground(numberBg);
                } else if (label.equals("=") || label.equals("+") || label.equals("-") || label.equals("*")
                    || label.equals("/") || label.equals("%")) {
                    button.setBackground(opBg);
                    button.setForeground(opFg);
                } else {
                    button.setBackground(controlBg);
                }

                button.addActionListener(event -> {
                    handleButton(
                        label,
                        display,
                        storedValue,
                        pendingOp,
                        startNewNumber,
                        frame
                    );
                });

                grid.add(button);
            }
        }

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(242, 242, 242));
        content.add(displayPanel, BorderLayout.NORTH);
        content.add(grid, BorderLayout.CENTER);

        frame.setContentPane(content);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void handleButton(
        String label,
        JTextField display,
        double[] storedValue,
        String[] pendingOp,
        boolean[] startNewNumber,
        JFrame frame
    ) {
        if (label.matches("[0-9]")) {
            appendDigit(display, label, startNewNumber);
            return;
        }

        switch (label) {
            case ".":
                appendDecimal(display, startNewNumber);
                return;
            case "+/-":
                toggleSign(display, startNewNumber);
                return;
            case "C":
                display.setText("0");
                storedValue[0] = 0.0;
                pendingOp[0] = null;
                startNewNumber[0] = true;
                return;
            case "DEL":
                deleteLast(display, startNewNumber);
                return;
            case "=":
                try {
                    applyEquals(display, storedValue, pendingOp, startNewNumber);
                } catch (IllegalArgumentException ex) {
                    showError(frame, ex.getMessage());
                    display.setText("0");
                    storedValue[0] = 0.0;
                    pendingOp[0] = null;
                    startNewNumber[0] = true;
                }
                return;
            default:
                if (label.equals("+") || label.equals("-") || label.equals("*")
                    || label.equals("/") || label.equals("%")) {
                    try {
                        applyOperator(label, display, storedValue, pendingOp, startNewNumber);
                    } catch (IllegalArgumentException ex) {
                        showError(frame, ex.getMessage());
                        display.setText("0");
                        storedValue[0] = 0.0;
                        pendingOp[0] = null;
                        startNewNumber[0] = true;
                    }
                }
                return;
        }
    }

    private static void appendDigit(JTextField display, String digit, boolean[] startNewNumber) {
        String current = display.getText();
        if (startNewNumber[0]) {
            display.setText(digit.equals("0") ? "0" : digit);
            startNewNumber[0] = false;
            return;
        }

        if (current.equals("0")) {
            display.setText(digit);
        } else {
            display.setText(current + digit);
        }
    }

    private static void appendDecimal(JTextField display, boolean[] startNewNumber) {
        String current = display.getText();
        if (startNewNumber[0]) {
            display.setText("0.");
            startNewNumber[0] = false;
            return;
        }

        if (!current.contains(".")) {
            display.setText(current + ".");
        }
    }

    private static void toggleSign(JTextField display, boolean[] startNewNumber) {
        String current = display.getText();
        if (current.equals("0")) {
            return;
        }

        if (current.startsWith("-")) {
            display.setText(current.substring(1));
        } else {
            display.setText("-" + current);
        }
        startNewNumber[0] = false;
    }

    private static void deleteLast(JTextField display, boolean[] startNewNumber) {
        String current = display.getText();
        if (startNewNumber[0] || current.length() <= 1) {
            display.setText("0");
            startNewNumber[0] = true;
            return;
        }

        String next = current.substring(0, current.length() - 1);
        if (next.equals("-") || next.isEmpty()) {
            display.setText("0");
            startNewNumber[0] = true;
        } else {
            display.setText(next);
        }
    }

    private static void applyOperator(
        String op,
        JTextField display,
        double[] storedValue,
        String[] pendingOp,
        boolean[] startNewNumber
    ) {
        double current = parseDisplay(display.getText());
        if (pendingOp[0] != null && !startNewNumber[0]) {
            storedValue[0] = applyBinary(storedValue[0], current, pendingOp[0]);
            display.setText(formatValue(storedValue[0]));
        } else if (pendingOp[0] == null) {
            storedValue[0] = current;
        }

        pendingOp[0] = op;
        startNewNumber[0] = true;
    }

    private static void applyEquals(
        JTextField display,
        double[] storedValue,
        String[] pendingOp,
        boolean[] startNewNumber
    ) {
        if (pendingOp[0] == null) {
            return;
        }

        double current = parseDisplay(display.getText());
        storedValue[0] = applyBinary(storedValue[0], current, pendingOp[0]);
        display.setText(formatValue(storedValue[0]));
        pendingOp[0] = null;
        startNewNumber[0] = true;
    }

    private static double parseDisplay(String value) {
        String trimmed = value == null ? "0" : value.trim();
        if (trimmed.isEmpty() || trimmed.equals("-")) {
            return 0.0;
        }
        return Double.parseDouble(trimmed.replace(',', '.'));
    }

    private static double applyBinary(double a, double b, String op) {
        switch (op) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                if (b == 0.0) {
                    throw new IllegalArgumentException("Pembagian oleh 0");
                }
                return a / b;
            case "%":
                if (b == 0.0) {
                    throw new IllegalArgumentException("Modulus oleh 0");
                }
                return a % b;
            default:
                throw new IllegalArgumentException("Operasi tidak didukung");
        }
    }

    private static String formatValue(double value) {
        return FORMAT.format(value);
    }

    private static void showError(JFrame frame, String message) {
        JOptionPane.showMessageDialog(
            frame,
            message,
            "Input tidak valid",
            JOptionPane.WARNING_MESSAGE
        );
    }

    private static DecimalFormat createFormatter() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat format = new DecimalFormat("0.##########", symbols);
        format.setGroupingUsed(false);
        return format;
    }
}
