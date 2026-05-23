import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Sistem Antarmuka Manajemen Mahasiswa Berbasis Java Swing
 * Kelompok 6 | ITS Surabaya | Mei 2026
 *
 * Arsitektur: MVC sederhana
 *  - Model  : DefaultTableModel (data mahasiswa)
 *  - View   : JFrame + panel-panel Swing
 *  - Control: ActionListener di setiap tombol
 */
public class MahasiswaApp extends JFrame {

    // ------------------------------------------------------------------ Field
    private JTextField       tfNIM, tfNama, tfIPK;
    private JButton          btnTambah, btnHapus, btnInfo;
    private JTable           tabel;
    private DefaultTableModel model;
    private JLabel           lblTotal;

    // ------------------------------------------------------------ Constructor
    public MahasiswaApp() {
        setTitle("Sistem Mahasiswa");
        setSize(700, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        add(buatPanelInput(), BorderLayout.NORTH);   // (A)
        add(buatPanelTabel(), BorderLayout.CENTER);  // (B)
        add(buatPanelBawah(), BorderLayout.SOUTH);   // (C)
    }

    // ------------------------------------------------------ Panel Input (NORTH)
    private JPanel buatPanelInput() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

        tfNIM     = new JTextField(10);
        tfNama    = new JTextField(15);
        tfIPK     = new JTextField(5);
        btnTambah = new JButton("Tambah");
        btnHapus  = new JButton("Hapus");

        panel.add(new JLabel("NIM:"));   panel.add(tfNIM);
        panel.add(new JLabel("Nama:"));  panel.add(tfNama);
        panel.add(new JLabel("IPK:"));   panel.add(tfIPK);
        panel.add(btnTambah);
        panel.add(btnHapus);

        // TODO (1): Daftarkan ActionListener untuk btnTambah
        //           yang memanggil method tambahMahasiswa()
        btnTambah.addActionListener(e -> tambahMahasiswa());

        // TODO (2): Daftarkan ActionListener untuk btnHapus
        //           yang memanggil method hapusMahasiswa()
        btnHapus.addActionListener(e -> hapusMahasiswa());

        return panel;
    }

    // ------------------------------------------------------ Panel Tabel (CENTER)
    private JPanel buatPanelTabel() {
        // TODO (3): Buat DefaultTableModel dengan kolom "No", "NIM", "Nama", "IPK",
        //           lalu buat JTable dari model tersebut,
        //           bungkus dalam JScrollPane, dan kembalikan
        //           sebagai JPanel berisi JScrollPane

        // 3a. Model dengan 0 baris awal dan 4 kolom
        model = new DefaultTableModel(new String[]{"No", "NIM", "Nama", "IPK"}, 0) {
            // Buat semua sel tidak dapat diedit langsung dari tabel
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // 3b. Buat JTable menggunakan model
        tabel = new JTable(model);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.getTableHeader().setReorderingAllowed(false);

        // Atur lebar kolom agar proporsional
        tabel.getColumnModel().getColumn(0).setPreferredWidth(40);  // No
        tabel.getColumnModel().getColumn(1).setPreferredWidth(120); // NIM
        tabel.getColumnModel().getColumn(2).setPreferredWidth(200); // Nama
        tabel.getColumnModel().getColumn(3).setPreferredWidth(80);  // IPK

        // 3c. Bungkus JTable dalam JScrollPane agar header selalu terlihat
        //     dan mendukung scroll ketika data melebihi tinggi panel
        JScrollPane scrollPane = new JScrollPane(tabel);

        // 3d. Kembalikan sebagai JPanel berisi JScrollPane
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // ------------------------------------------------------ Panel Bawah (SOUTH)
    private JPanel buatPanelBawah() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));

        lblTotal = new JLabel("Total Mahasiswa: 0");
        btnInfo  = new JButton("Tampil Info");

        // TODO (4): Daftarkan ActionListener untuk btnInfo.
        //           Saat diklik, ambil baris yang dipilih di tabel,
        //           tampilkan NIM + Nama + IPK menggunakan
        //           JOptionPane.showMessageDialog()
        btnInfo.addActionListener(e -> {
            int row = tabel.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Silakan pilih salah satu baris terlebih dahulu!",
                        "Peringatan",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Ambil data dari model (bukan langsung dari tabel,
            // agar aman jika kolom di-reorder)
            String nim  = model.getValueAt(row, 1).toString();
            String nama = model.getValueAt(row, 2).toString();
            String ipk  = model.getValueAt(row, 3).toString();

            JOptionPane.showMessageDialog(
                    this,
                    "NIM  : " + nim  + "\n" +
                    "Nama : " + nama + "\n" +
                    "IPK  : " + ipk,
                    "Info Mahasiswa",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        panel.add(lblTotal);
        panel.add(btnInfo);
        return panel;
    }

    // --------------------------------------------------- Method tambahMahasiswa
    private void tambahMahasiswa() {
        // TODO (5): Validasi field tidak boleh kosong
        //           (tampilkan JOptionPane.showMessageDialog jika kosong),
        //           ambil nilai tfNIM, tfNama, tfIPK,
        //           tambahkan sebagai baris baru ke model,
        //           perbarui lblTotal, dan kosongkan field input

        // 5a. Baca dan bersihkan spasi di ujung
        String nim  = tfNIM.getText().trim();
        String nama = tfNama.getText().trim();
        String ipk  = tfIPK.getText().trim();

        // 5b. Validasi: field tidak boleh kosong
        if (nim.isEmpty() || nama.isEmpty() || ipk.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Semua field (NIM, Nama, IPK) harus diisi!",
                    "Peringatan",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 5c. Validasi format IPK (opsional namun baik untuk robustness)
        try {
            double ipkVal = Double.parseDouble(ipk);
            if (ipkVal < 0.0 || ipkVal > 4.0) {
                JOptionPane.showMessageDialog(
                        this,
                        "IPK harus berada di antara 0.00 dan 4.00!",
                        "Peringatan",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "IPK harus berupa angka desimal (contoh: 3.75)!",
                    "Format Salah",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 5d. Tambahkan baris baru ke model
        int no = model.getRowCount() + 1;
        model.addRow(new Object[]{no, nim, nama, ipk});

        // 5e. Perbarui label total
        perbaraiTotal();

        // 5f. Kosongkan field input
        tfNIM.setText("");
        tfNama.setText("");
        tfIPK.setText("");
        tfNIM.requestFocus(); // kembalikan fokus ke field NIM
    }

    // ---------------------------------------------------- Method hapusMahasiswa
    private void hapusMahasiswa() {
        // TODO (6): Ambil baris yang dipilih (getSelectedRow()),
        //           jika tidak ada yang dipilih tampilkan peringatan,
        //           hapus baris dari model, perbarui lblTotal

        // 6a. Cek apakah ada baris yang dipilih
        int row = tabel.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Pilih baris yang ingin dihapus terlebih dahulu!",
                    "Peringatan",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 6b. Konfirmasi sebelum hapus (UX yang baik)
        int konfirmasi = JOptionPane.showConfirmDialog(
                this,
                "Yakin ingin menghapus data mahasiswa yang dipilih?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (konfirmasi != JOptionPane.YES_OPTION) return;

        // 6c. Hapus baris dari model
        model.removeRow(row);

        // 6d. Renumber kolom "No" agar tetap berurutan
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(i + 1, i, 0);
        }

        // 6e. Perbarui label total
        perbaraiTotal();
    }

    // --------------------------------------------------------- Metode Pembantu
    private void perbaraiTotal() {
        lblTotal.setText("Total Mahasiswa: " + model.getRowCount());
    }

    // --------------------------------------------------------------- Main / EDT
    public static void main(String[] args) {
        // TODO (7): Jalankan aplikasi di EDT menggunakan
        //           SwingUtilities.invokeLater()
        SwingUtilities.invokeLater(() -> {
            MahasiswaApp app = new MahasiswaApp();
            app.setVisible(true);
        });
    }
}
