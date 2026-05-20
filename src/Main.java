import java.util.Scanner;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Kalkulator Java");
        System.out.println("=================");

        while (true) {
            printMenu();
            System.out.print("Pilih menu (1-6) atau q untuk keluar: ");
            String choice = SCANNER.nextLine().trim();

            if (choice.equalsIgnoreCase("q")) {
                break;
            }

            try {
                switch (choice) {
                    case "1":
                        binaryOp("+");
                        break;
                    case "2":
                        binaryOp("-");
                        break;
                    case "3":
                        binaryOp("*");
                        break;
                    case "4":
                        binaryOp("/");
                        break;
                    case "5":
                        binaryOp("%");
                        break;
                    case "6":
                        binaryOp("^");
                        break;
                    default:
                        System.out.println("Pilihan tidak dikenal. Coba lagi.");
                        break;
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("Input tidak valid: " + ex.getMessage());
            }

            System.out.println();
        }

        System.out.println("Sampai jumpa!");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("Menu:");
        System.out.println("1. Tambah");
        System.out.println("2. Kurang");
        System.out.println("3. Kali");
        System.out.println("4. Bagi");
        System.out.println("5. Modulus");
        System.out.println("6. Pangkat");
    }

    private static void binaryOp(String op) {
        double a = readDouble("Angka pertama: ");
        double b = readDouble("Angka kedua: ");
        double result;

        switch (op) {
            case "+":
                result = a + b;
                break;
            case "-":
                result = a - b;
                break;
            case "*":
                result = a * b;
                break;
            case "/":
                if (b == 0.0) {
                    throw new IllegalArgumentException("pembagian oleh 0");
                }
                result = a / b;
                break;
            case "%":
                if (b == 0.0) {
                    throw new IllegalArgumentException("modulus oleh 0");
                }
                result = a % b;
                break;
            case "^":
                result = Math.pow(a, b);
                break;
            default:
                throw new IllegalArgumentException("operasi tidak didukung");
        }

        System.out.println("Hasil: " + result);
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input kosong. Masukkan angka.");
                continue;
            }

            try {
                return Double.parseDouble(input.replace(',', '.'));
            } catch (NumberFormatException ex) {
                System.out.println("Masukkan angka yang valid.");
            }
        }
    }
}
