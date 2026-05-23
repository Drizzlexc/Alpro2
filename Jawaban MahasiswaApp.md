# Jawaban Soal Praktikum
## Penguasaan Java GUI: Dari Dasar Swing hingga JavaFX
**Kelompok 6 | ITS Surabaya | Mei 2026**

---

## Bagian A — Pemahaman Komponen & Layout Manager [20 poin]

### Tabel Komponen Swing

| No | Kebutuhan UI | Komponen | Alasan |
|----|-------------|----------|--------|
| 1 | Input satu baris untuk NIM | `JTextField` | Komponen input teks satu baris yang memungkinkan pengguna mengetik data NIM. Parameter lebar (kolom) mengatur ukuran visualnya. |
| 2 | Menampilkan data mahasiswa dalam baris dan kolom | `JTable` | Komponen tabel dua dimensi yang bekerja dengan model data (`TableModel`) untuk menampilkan data terstruktur dalam format baris-kolom. |
| 3 | Agar header tabel selalu terlihat dan mendukung scroll | `JScrollPane` | Kontainer pembungkus `JTable` yang secara otomatis menangani scrollbar dan mempertahankan header tabel (`JTableHeader`) agar selalu terlihat saat konten di-scroll. |
| 4 | Tombol aksi "Tambah" dan "Hapus" | `JButton` | Komponen tombol standar yang dapat diberi `ActionListener` untuk menjalankan logika ketika diklik pengguna. |
| 5 | Menampilkan teks statis "Total Mahasiswa:" | `JLabel` | Komponen label untuk menampilkan teks yang tidak dapat diedit. Dapat di-update secara programatik dengan `setText()`. |

---

## Bagian B — Implementasi Aplikasi [50 poin]

### TODO (1) — ActionListener btnTambah [5 poin]

```java
btnTambah.addActionListener(e -> tambahMahasiswa());
```

**Penjelasan:** Menggunakan lambda expression (Java 8+) sebagai `ActionListener`. Ketika tombol diklik, event dipanggil di EDT dan method `tambahMahasiswa()` dieksekusi.

---

### TODO (2) — ActionListener btnHapus [5 poin]

```java
btnHapus.addActionListener(e -> hapusMahasiswa());
```

---

### TODO (3) — DefaultTableModel, JTable, JScrollPane [10 poin]

```java
private JPanel buatPanelTabel() {
    // 3a. Buat model dengan header kolom dan 0 baris awal
    model = new DefaultTableModel(new String[]{"No", "NIM", "Nama", "IPK"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Cegah edit langsung di tabel
        }
    };

    // 3b. Buat JTable menggunakan model
    tabel = new JTable(model);
    tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    // 3c. Bungkus dalam JScrollPane agar header tetap terlihat
    JScrollPane scrollPane = new JScrollPane(tabel);

    // 3d. Kembalikan sebagai JPanel
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(scrollPane, BorderLayout.CENTER);
    return panel;
}
```

**Mengapa menggunakan `JScrollPane`?**
- `JTable` tidak menampilkan header-nya sendiri. Saat dibungkus `JScrollPane`, header (`JTableHeader`) otomatis ditempatkan di area *column header* viewport.
- Ketika jumlah baris melebihi tinggi tampilan, scrollbar muncul otomatis.

---

### TODO (4) — ActionListener btnInfo dengan JOptionPane [8 poin]

```java
btnInfo.addActionListener(e -> {
    int row = tabel.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this,
            "Silakan pilih salah satu baris terlebih dahulu!",
            "Peringatan", JOptionPane.WARNING_MESSAGE);
        return;
    }
    String nim  = model.getValueAt(row, 1).toString();
    String nama = model.getValueAt(row, 2).toString();
    String ipk  = model.getValueAt(row, 3).toString();

    JOptionPane.showMessageDialog(this,
        "NIM  : " + nim  + "\n" +
        "Nama : " + nama + "\n" +
        "IPK  : " + ipk,
        "Info Mahasiswa", JOptionPane.INFORMATION_MESSAGE);
});
```

**Catatan:** Data diambil dari `model` (bukan langsung `tabel.getValueAt`) agar aman jika urutan kolom di-reorder.

---

### TODO (5) — tambahMahasiswa() dengan validasi [12 poin]

```java
private void tambahMahasiswa() {
    // Baca nilai field
    String nim  = tfNIM.getText().trim();
    String nama = tfNama.getText().trim();
    String ipk  = tfIPK.getText().trim();

    // Validasi: field tidak boleh kosong
    if (nim.isEmpty() || nama.isEmpty() || ipk.isEmpty()) {
        JOptionPane.showMessageDialog(this,
            "Semua field (NIM, Nama, IPK) harus diisi!",
            "Peringatan", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Validasi: IPK harus angka desimal 0.00 – 4.00
    try {
        double ipkVal = Double.parseDouble(ipk);
        if (ipkVal < 0.0 || ipkVal > 4.0) {
            JOptionPane.showMessageDialog(this,
                "IPK harus berada di antara 0.00 dan 4.00!",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this,
            "IPK harus berupa angka desimal (contoh: 3.75)!",
            "Format Salah", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Tambahkan baris baru ke model
    int no = model.getRowCount() + 1;
    model.addRow(new Object[]{no, nim, nama, ipk});

    // Perbarui label total dan kosongkan field
    perbaraiTotal();
    tfNIM.setText(""); tfNama.setText(""); tfIPK.setText("");
    tfNIM.requestFocus();
}
```

---

### TODO (6) — hapusMahasiswa() dengan validasi [5 poin]

```java
private void hapusMahasiswa() {
    int row = tabel.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this,
            "Pilih baris yang ingin dihapus terlebih dahulu!",
            "Peringatan", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Konfirmasi sebelum hapus
    int konfirmasi = JOptionPane.showConfirmDialog(this,
        "Yakin ingin menghapus data mahasiswa yang dipilih?",
        "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
    if (konfirmasi != JOptionPane.YES_OPTION) return;

    model.removeRow(row);

    // Renumber kolom "No" agar tetap berurutan
    for (int i = 0; i < model.getRowCount(); i++) {
        model.setValueAt(i + 1, i, 0);
    }
    perbaraiTotal();
}
```

---

### TODO (7) — Jalankan aplikasi di EDT [5 poin]

```java
public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        MahasiswaApp app = new MahasiswaApp();
        app.setVisible(true);
    });
}
```

---

## Bagian C — Analisis Konseptual [30 poin]

### a. Event Dispatch Thread (EDT)

#### i. Perbedaan Kode P dan Kode Q dari sisi *thread safety*

| Aspek | Kode P | Kode Q |
|-------|--------|--------|
| **Thread eksekusi** | GUI dibuat di **main thread** (bukan EDT) | GUI dibuat di **EDT** melalui `invokeLater()` |
| **Thread safety** | ❌ **Tidak aman** — Swing bukan thread-safe | ✅ **Aman** — semua operasi UI berjalan di EDT |
| **Risiko race condition** | Ada, terutama jika ada background thread lain | Tidak ada, karena EDT adalah single thread untuk UI |

**Penjelasan Teknis:**

- **Kode P:** `new MahasiswaApp()` dan `app.setVisible(true)` dieksekusi langsung di *main thread*. Saat `setVisible(true)` dipanggil, Swing mulai memproses event di EDT. Jika main thread masih memodifikasi komponen secara bersamaan, dua thread bisa mengakses state yang sama secara bersamaan → **race condition**.

- **Kode Q:** Seluruh inisialisasi GUI dikirim ke antrian EDT menggunakan `invokeLater()`. Eksekusi bersifat asinkron — main thread langsung selesai, sementara EDT menjalankan lambda ketika siap. Semua operasi Swing berjalan pada satu thread saja → **aman**.

---

#### ii. Mengapa Kode Q benar? Risiko Kode P pada aplikasi kompleks?

**Kode Q adalah praktik yang benar karena:**

Swing didesain sebagai *single-threaded GUI framework*. Semua modifikasi dan rendering komponen Swing **harus** dilakukan dari EDT. Ini adalah aturan fundamental Swing sejak Java 1.4+, yang didokumentasikan resmi oleh Sun/Oracle.

**Risiko menggunakan Kode P pada aplikasi kompleks:**

1. **Rendering tidak konsisten (visual glitch/flickering)**
   - Komponen bisa dirender dalam keadaan setengah jadi karena main thread memodifikasi saat EDT sedang menggambar.

2. **Deadlock**
   - Jika main thread menunggu hasil dari EDT, sementara EDT menunggu lock yang dipegang main thread → **deadlock permanen**.

3. **Race condition pada model data**
   - Jika `DefaultTableModel` dimodifikasi dari main thread sementara EDT me-render tabel, data bisa korup atau aplikasi crash (`ArrayIndexOutOfBoundsException`, `ConcurrentModificationException`).

4. **Perilaku non-deterministik**
   - Bug yang muncul hanya sesekali (*heisenbugs*) dan sangat sulit direproduksi/debug.

---

#### iii. Kapan `SwingUtilities.invokeAndWait()` lebih tepat dibanding `invokeLater()`?

| | `invokeLater()` | `invokeAndWait()` |
|--|----------------|-------------------|
| **Sifat** | Asinkron — melempar task ke EDT lalu langsung lanjut | Sinkron — **memblokir** thread pemanggil sampai task EDT selesai |
| **Penggunaan** | Memperbarui UI dari background thread tanpa perlu hasilnya | Perlu memastikan UI selesai diperbarui **sebelum** melanjutkan logika berikutnya |
| **Risiko** | Urutan eksekusi tidak dijamin segera | Potensi **deadlock** jika dipanggil dari EDT itu sendiri |

**`invokeAndWait()` lebih tepat digunakan ketika:**

1. **Unit testing komponen Swing** — test runner perlu memastikan komponen sudah ter-render sebelum memeriksa state-nya.
   ```java
   SwingUtilities.invokeAndWait(() -> button.doClick());
   assert label.getText().equals("Clicked"); // dijamin sudah diupdate
   ```

2. **Inisialisasi berurutan yang kritis** — background thread perlu mendapatkan nilai dari komponen UI sebelum memulai komputasi berikutnya.
   ```java
   // Di background thread:
   final String[] result = new String[1];
   SwingUtilities.invokeAndWait(() -> result[0] = textField.getText());
   proses(result[0]); // dijamin textField sudah dibaca
   ```

3. **Saat menampilkan dialog modal** di mana thread lain harus menunggu respons pengguna sebelum melanjutkan.

> ⚠️ **Penting:** `invokeAndWait()` **tidak boleh** dipanggil dari EDT (akan menyebabkan deadlock). Selalu periksa dengan `SwingUtilities.isEventDispatchThread()` sebelum menggunakannya.

---

## Ringkasan Poin

| Bagian | Poin Maksimum | Keterangan |
|--------|--------------|------------|
| A — Tabel Komponen | 20 | 5 komponen × 4 poin |
| B — TODO 1–7 | 50 | Implementasi lengkap dengan validasi |
| C — Analisis EDT | 30 | 3 sub-pertanyaan |
| **Total** | **100** | |
