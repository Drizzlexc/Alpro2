# SUDOKU GAME
Panduan Pengguna & Dokumentasi Program
Game Puzzle Logika Berbasis Java Swing
Versi 1.0  |  2026

Anggota Kelompok

No.	Nama	NRP
1	Zaskia Novita Sari	5002251007
2	Ezequiel Mulyadi	5002251035
3	Yosua Kharisma Putra	5002251038
4	Rizky Ronaldo Hutauruk	5002251085

# 1. Deskripsi Umum
Sudoku Classic adalah aplikasi permainan puzzle logika berbasis Java Swing yang dikembangkan mengikuti konvensi struktur NetBeans JFrame Form. Program ini menghadirkan pengalaman bermain Sudoku yang lengkap dengan antarmuka grafis yang intuitif, sistem manajemen state yang solid, dan berbagai fitur pendukung seperti undo/redo, hint, save/load, serta musik latar.

Permainan Sudoku menantang pemain untuk mengisi seluruh sel kosong pada grid dengan simbol (angka atau huruf) sehingga tidak ada simbol yang berulang pada setiap baris, kolom, maupun sub-grid/blok kotak.

# 2. Requirement Sistem
2.1 Perangkat Lunak
Spesifikasi Software
Java Development Kit (JDK)  :  Versi 8 atau lebih baru (direkomendasikan JDK 11+)
IDE yang Didukung            :  NetBeans IDE, Eclipse, IntelliJ IDEA
Build Tool (Opsional)        :  Apache Maven atau Gradle
Sistem Operasi               :  Windows 7+, macOS 10.12+, Linux (dengan GUI)

2.2 Library Pihak Ketiga (Opsional)
Catatan Library
JLayer 1.0.1  (jlayer-1.0.1.jar)  :  Diperlukan untuk memutar musik latar berformat MP3.
Letakkan file JAR di folder:  lib/jlayer-1.0.1.jar (relatif terhadap direktori project).
Jika JLayer tidak tersedia, program tetap berjalan normal tanpa musik latar.

2.3 Aset Suara (Opsional)
Buat folder sounds/ di direktori project dan letakkan file-file berikut di dalamnya:
•sounds/mbg.mp3  —  Musik latar permainan (format MP3, memerlukan JLayer)
•sounds/correct.wav  —  Efek suara saat angka benar dimasukkan
•sounds/error.wav  —  Efek suara saat angka salah
•sounds/hint.wav  —  Efek suara saat hint diberikan
•sounds/win.wav  —  Efek suara saat pemain menang
•sounds/lose.wav  —  Efek suara saat pemain kalah
Jika file suara tidak ditemukan, program tetap berjalan normal (fallback ke beep sistem).

# 3. Cara Menjalankan Program
3.1 Menggunakan NetBeans IDE
1.Buka NetBeans IDE.
2.Pilih menu File > Open Project, lalu arahkan ke folder project SudokuGame.
3.Pastikan semua file .java sudah terdapat dalam satu package/folder yang sama.
4.Jika menggunakan musik MP3, tambahkan jlayer-1.0.1.jar ke Libraries project (klik kanan Libraries > Add JAR/Folder).
5.Klik tombol Run (F6) atau klik kanan file SudokuGame.java > Run File.
6.Program akan terbuka dalam mode fullscreen secara otomatis.

3.2 Menggunakan Command Line (Compile & Run Manual)
# Masuk ke direktori sumber
cd /path/ke/folder/project
 
# Compile semua file Java
javac -cp .;lib/jlayer-1.0.1.jar *.java
 
# Jalankan program
java -cp .;lib/jlayer-1.0.1.jar SudokuGame
 
# Catatan untuk Linux/macOS: ganti titik koma (;) dengan titik dua (:)
javac -cp .:lib/jlayer-1.0.1.jar *.java
java  -cp .:lib/jlayer-1.0.1.jar SudokuGame

3.3 Struktur Folder yang Direkomendasikan
SudokuGame/
  +-- SudokuGame.java          (file utama - berisi semua class)
  +-- lib/
  |    +-- jlayer-1.0.1.jar     (opsional, untuk musik MP3)
  +-- sounds/
       +-- mbg.mp3              (musik latar, opsional)
       +-- correct.wav
       +-- error.wav
       +-- hint.wav
       +-- win.wav
       +-- lose.wav

# 4. Alur Permainan
4.1 Layar Menu Utama
Saat program pertama kali dijalankan, pemain akan disambut oleh layar menu utama dengan latar gradasi hijau gelap. Dari sini, pemain dapat:
•Mengisi nama pemain pada kolom teks yang tersedia.
•Memilih tingkat kesulitan (lihat bagian 5.2).
•Menekan tombol Mulai Bermain untuk memulai permainan baru.
•Menekan tombol Keluar untuk menutup aplikasi.

4.2 Layar Permainan
Setelah memulai, layar akan berganti ke area permainan yang terdiri dari tiga bagian utama:
•Header  :  Menampilkan judul, nama pemain, mode kesulitan, timer, dan sisa kesempatan.
•Grid Sudoku  :  Area utama tempat puzzle ditampilkan dan dimainkan.
•Number Pad  :  Panel pemilihan angka/simbol di bawah grid.
•Footer  :  Deretan tombol aksi (Undo, Redo, Hint, Cek, Simpan, Muat, Menu, Reset).

4.3 Cara Bermain
7.Klik salah satu sel kosong pada grid untuk memilihnya (sel akan berubah warna kuning).
8.Pilih angka/simbol pada Number Pad di bawah grid.
9.Sistem otomatis memvalidasi input:
◦Jika valid, angka tersimpan dan sel berhasil diisi.
◦Jika melanggar aturan Sudoku (baris/kolom/blok sama), satu kesempatan berkurang.
10.Lanjutkan hingga semua sel terisi dengan benar.
11.Gunakan tombol Cek untuk memverifikasi semua jawaban sekaligus.

ATURAN SUDOKU:
Setiap angka/simbol hanya boleh muncul SATU KALI di setiap:
  - Baris (horizontal)
  - Kolom (vertikal)
  - Blok kotak (sub-grid berukuran base x base)

# 5. Fitur-Fitur Program
5.1 Tiga Mode Kesulitan
Fitur	Lokasi / Tombol	Deskripsi Detail
Mudah 4x4	Radio button di menu	Grid 4x4 dengan blok 2x2. Simbol: 1-4. Cocok untuk pemula. Jumlah penghapusan: 6 sel.
Sedang 9x9	Radio button di menu	Grid 9x9 klasik dengan blok 3x3. Simbol: 1-9. Standar Sudoku umumnya. Penghapusan: 40 sel.
Sulit 16x16	Radio button di menu	Grid 16x16 dengan blok 4x4. Simbol: 1-9 dan A-G. Tantangan tinggi. Penghapusan: 160 sel.

5.2 Generator Puzzle Acak
Program menghasilkan puzzle Sudoku yang unik dan dapat diselesaikan setiap kali permainan dimulai. Algoritma generator bekerja sebagai berikut:
•Membuat solusi lengkap menggunakan permutasi baris, kolom, dan angka yang diacak secara random.
•Menggunakan pola matematika untuk memastikan solusi valid memenuhi semua aturan Sudoku.
•Menghapus sejumlah sel sesuai tingkat kesulitan untuk membentuk puzzle.
•Jumlah sel yang dihapus dibatasi maksimal 65% dari total sel agar puzzle tetap solvable.

5.3 Input & Validasi Real-Time
Setiap angka yang dimasukkan pemain langsung divalidasi secara real-time:
•Sistem memeriksa baris, kolom, dan sub-grid/blok yang bersesuaian.
•Jika angka valid, sel berhasil diisi dengan animasi hijau.
•Jika angka melanggar aturan, sel berkedip merah dan satu kesempatan berkurang.
•Sistem memberikan tiga (3) kesempatan kesalahan sebelum game berakhir (Game Over).

5.4 Sistem Highlight Interaktif
Saat pemain memilih sebuah sel, sistem otomatis menyoroti area terkait untuk membantu pemain:
•Kuning terang  : Sel yang sedang dipilih.
•Krem/Peach     : Semua sel pada baris, kolom, dan blok yang sama (peer cells).
•Biru muda      : Semua sel lain yang berisi angka/simbol yang sama.
•Merah          : Sel yang terdeteksi salah oleh tombol Cek.

5.5 Undo & Redo
Fitur	Lokasi / Tombol	Deskripsi Detail
Undo	Tombol Undo (footer)	Membatalkan langkah terakhir. Mendukung multi-level undo (seluruh riwayat sejak game dimulai atau di-reset).
Redo	Tombol Redo (footer)	Mengulangi langkah yang telah di-undo. Riwayat redo terhapus otomatis jika ada langkah baru setelah undo.

5.6 Hint (Bantuan)
Tombol Hint memberikan satu jawaban benar secara otomatis. Prioritas pengisian:
•Jika ada sel yang sedang dipilih dan masih kosong, hint diberikan pada sel tersebut.
•Jika tidak ada sel yang dipilih atau sel sudah terisi, hint diberikan pada sel kosong pertama yang ditemukan (urutan kiri-atas ke kanan-bawah).
•Setelah hint diberikan, sistem otomatis memeriksa apakah puzzle sudah selesai.

5.7 Verifikasi Jawaban (Cek)
Tombol Cek melakukan pemeriksaan menyeluruh terhadap semua sel sekaligus:
•Sel yang salah akan diberi highlight merah.
•Jika semua sel terisi dan benar, permainan dinyatakan menang (Win).
•Pop-up peringatan ditampilkan jika masih ada sel kosong atau sel yang salah.

5.8 Save & Load State
Fitur	Lokasi / Tombol	Deskripsi Detail
Simpan	Tombol Simpan (footer)	Menyimpan seluruh state permainan ke file .sdk menggunakan Java Serialization. Termasuk: posisi semua angka, jawaban benar, status fixed cells, nama pemain, waktu, dan sisa kesempatan.
Muat	Tombol Muat (footer)	Memuat kembali file .sdk yang sudah disimpan. Permainan dilanjutkan persis dari titik terakhir disimpan, lengkap dengan timer dan sisa kesempatan.

5.9 Timer
Timer otomatis berjalan sejak permainan dimulai atau dimuat dari file simpanan:
•Ditampilkan di header dalam format MM:SS (contoh: 05:42).
•Timer berhenti otomatis saat pemain menang atau kalah.
•Waktu turut disimpan bersama file permainan dan dilanjutkan saat di-load.

5.10 Sistem Kesempatan
Pemain memiliki 3 (tiga) kesempatan untuk membuat kesalahan sebelum Game Over:
•Setiap input yang melanggar aturan Sudoku mengurangi 1 kesempatan.
•Sisa kesempatan ditampilkan di header: Kesempatan: N/3.
•Ketika kesempatan habis (0), game berakhir dan overlay Game Selesai ditampilkan.

5.11 Reset Puzzle
Tombol Reset mengembalikan puzzle ke kondisi awal saat permainan pertama kali dimulai atau dimuat. Semua input pemain dihapus, timer direset ke 0, dan riwayat undo/redo dikosongkan.

5.12 Leaderboard
Setiap kali pemain menyelesaikan puzzle, skor (nama + waktu + kesulitan) dicatat ke leaderboard sesi:
•Menyimpan hingga 10 skor terbaik per sesi (urutkan dari waktu tercepat).
•Leaderboard ditampilkan pada overlay layar kemenangan.
•Leaderboard bersifat sesi - direset saat aplikasi ditutup.

5.13 Musik & Efek Suara
Program mendukung audio dua lapis:
•Musik Latar (BGM)  :  File MP3 diputar berulang (loop) selama permainan berlangsung menggunakan library JLayer.
•Efek Suara (SFX)   :  File WAV diputar untuk setiap kejadian (jawaban benar, salah, hint, menang, kalah).
•Program tetap berjalan normal jika file audio tidak ditemukan.

5.14 Tampilan Fullscreen & ESC Toggle
Program berjalan dalam mode fullscreen tanpa border secara default. Tekan tombol ESC untuk beralih ke mode window biasa (non-fullscreen, dengan border, ukuran maksimal).

5.15 Overlay Menang & Kalah
Saat permainan berakhir (menang atau kalah), layar overlay ditampilkan:
•Overlay Menang  :  Menampilkan pesan selamat, nama pemain, durasi, dan leaderboard. Pemain dapat memilih Main Lagi atau Kembali ke Menu.
•Overlay Kalah   :  Menampilkan pesan gagal dan motivasi. Pemain dapat memilih Main Lagi atau Kembali ke Menu.

# 6. Arsitektur & Komponen Program
6.1 Class Utama
Fitur	Lokasi / Tombol	Deskripsi Detail
SudokuGame	JFrame utama	Class utama yang mewarisi JFrame. Mengelola seluruh UI (CardLayout: menu & game), event handling, dan koordinasi antar komponen.
SudokuGameBoard	Model/Logic	Mengelola papan permainan: generate puzzle, konversi simbol-nilai, dan manajemen grid.
SudokuGameState	Data/Serializable	Menyimpan seluruh state permainan (board, solution, fixed, timer, attempts) dan dapat diserialisasi ke file.
SudokuGameCell	View/JTextField	Mewarisi JTextField. Merender satu sel grid dengan highlight, animasi, dan border blok.
SudokuValidator	Logic	Memvalidasi apakah suatu angka boleh diletakkan pada posisi tertentu dan apakah puzzle sudah selesai.
SudokuHistory	Data/Undo-Redo	Mengelola tumpukan undo dan redo menggunakan ArrayDeque.
SudokuLeaderboard	Data	Menyimpan dan mengurutkan skor terbaik (maks. 10 entri per sesi).
SudokuSoundPlayer	Audio	Memuat dan memainkan efek suara WAV menggunakan javax.sound.sampled.Clip.
SudokuMusicPlayer	Audio	Memainkan musik latar MP3 secara loop menggunakan library JLayer (via refleksi).
SudokuGradientPanel	View	Panel khusus dengan latar gradasi dua warna menggunakan GradientPaint.
SudokuDifficulty	Enum	Mendefinisikan tiga tingkat kesulitan (EASY, MEDIUM, HIGH) beserta parameter size, base, dan removals.
SudokuPaths	Utility	Helper untuk mencari direktori atau file secara rekursif ke atas dalam hierarki folder.

# 7. Panduan Tombol & Shortcut
7.1 Tombol Footer (Area Permainan)
Fitur	Lokasi / Tombol	Deskripsi Detail
Undo	Footer kiri atas	Batalkan satu langkah terakhir.
Redo	Footer kiri atas	Ulangi langkah yang sudah di-undo.
Hint	Footer tengah atas	Dapatkan satu jawaban benar secara otomatis.
Cek	Footer tengah atas	Periksa semua jawaban sekaligus dan tandai yang salah.
Simpan	Footer kanan atas	Simpan progress permainan ke file .sdk.
Muat	Footer kanan atas	Muat kembali permainan dari file .sdk.
Menu	Footer kiri bawah	Kembali ke layar menu utama (timer berhenti).
Reset	Footer kanan bawah	Reset puzzle ke kondisi awal.

7.2 Keyboard Shortcut
ESC  :  Toggle antara mode fullscreen dan mode window biasa.

# 8. Troubleshooting
Fitur	Lokasi / Tombol	Deskripsi Detail
Tidak ada musik	JLayer tidak ditemukan	Letakkan lib/jlayer-1.0.1.jar di folder project. Program tetap berjalan tanpa musik.
Tidak ada efek suara	File WAV tidak ada	Buat folder sounds/ dan tambahkan file .wav yang diperlukan. Program tetap berjalan tanpa SFX.
Layar hitam saat start	Driver grafis / Java versi lama	Perbarui JDK ke versi 11+. Coba jalankan dengan flag: java -Dsun.java2d.noddraw=true SudokuGame
File .sdk gagal dimuat	Versi class berbeda	File .sdk hanya kompatibel dengan versi program yang sama (serialVersionUID = 1L).
Compile error	File terpisah	Pastikan semua class ada dalam satu file SudokuGame.java atau dalam package yang sama.

# 9. Catatan Tambahan
•Program dikembangkan menggunakan Java Swing dengan konvensi struktur NetBeans JFrame Form.
•Semua class pendukung (enum, helper, model, view) didefinisikan dalam satu file .java.
•Leaderboard tidak persisten: data skor hilang setelah aplikasi ditutup.
•Puzzle yang digenerate menggunakan algoritma permutasi acak; setiap permainan menghasilkan puzzle yang berbeda.
•Ukuran minimum yang disarankan: resolusi layar 1280 x 720 piksel.


Dokumentasi ini dibuat untuk keperluan tugas Algoritma Pemograman 2.
Institut Teknologi Sepuluh Nopember  |  2026
