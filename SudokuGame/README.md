# Sudoku Classic

## Deskripsi Umum

Sudoku adalah permainan logika berbasis penempatan angka pada grid 9x9 yang terbagi menjadi 9 sub-grid 3x3. Tujuan permainan adalah mengisi seluruh sel kosong dengan angka 1-9 sehingga tidak ada angka yang berulang pada setiap baris, kolom, maupun sub-grid 3x3.

## Requirement

- Java (Netbeans / Eclipse / IntelliJ)
- Pemahaman dasar: OOP, 2D arrays, recursion, Scanner

## Menjalankan

Dari folder `SudokuGame`:

```bash
javac SudokuGame.java
java SudokuGameApp
```

## Komponen Game

Data

- Board: `SudokuGameState.board`
- fixedBox: `SudokuGameState.fixed`
- history: `SudokuHistory`

Logika

- papanPermainan: `SudokuGameBoard` (generator puzzle)
- validator: `SudokuValidator`
- gameController: `SudokuGameFrame`

Antarmuka

- createPapan: `rebuildGrid()`
- panelControl: tombol angka, hapus, undo, hint, menu, simpan, muat

## Alur Permainan

Inisialisasi

- Pemain menekan tombol Play.
- Pilih tingkat kesulitan (Mudah 4x4, Sedang 9x9, Sulit 16x16).
- Membuat papan permainan dengan puzzle acak.
- Menyimpan state awal.
- Merender UI.

GamePlay Loop

- Pemain memilih sel kosong.
- Pemain memasukkan angka.
- Sistem melakukan validasi.

Validasi

- Baris yang sama
- Kolom yang sama
- Sub-grid 3x3

Manajemen State

- State permainan disimpan pada `SudokuGameState`.

## Fitur Wajib

- Generator Puzzle
- Input dan Validasi Real-Time
- Undo/Redo
- Save & Load State
- Verifikasi Kemenangan

## Fitur Tambahan

- Kesempatan salah: 3 kali
- Leaderboard Top 10 (tersimpan selama aplikasi berjalan)
- Efek suara dan animasi sederhana (pulse input benar/salah, fade teks kemenangan)

## Suara

Letakkan file .wav di folder `SudokuGame/sounds` dengan nama berikut:

- `correct.wav`
- `error.wav`
- `win.wav`
- `lose.wav`
- `hint.wav`

## Catatan

- File utama: `SudokuGame.java`
- Entry point: `SudokuGameApp`
