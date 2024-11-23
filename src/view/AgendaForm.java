package view;

import javax.swing.JOptionPane;
import javax.swing.*;                  // Untuk komponen GUI seperti JButton, JTable, JTextField, dll.
import javax.swing.table.DefaultTableModel; // Untuk model tabel di JTable
import java.awt.event.ActionListener; // Untuk event handling tombol
import java.awt.event.ActionEvent;    // Untuk menangani event klik tombol
import controller.AgendaController;  // Untuk menghubungkan View dengan Controller
import model.Agenda;                 // Untuk menggunakan model Agenda
import java.sql.SQLException;        // Untuk menangani exception SQL
import java.util.List;               // Untuk menangani List yang dikembalikan oleh Controller
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 *
 * @author muham
 */
public class AgendaForm extends javax.swing.JFrame {

    private AgendaController controller;

    public AgendaForm() {
        initComponents();
        controller = new AgendaController();

        // Panggil fungsi untuk mengisi data ke JTable
        loadDataToTable();

        btnTambah.addActionListener(e -> {
            try {
                String judul = txtJudul.getText();
                String tanggalInput = txtTanggal.getText();
                String deskripsi = txtDeskripsi.getText();

                // Konversi tanggal dari format DD/MM/YYYY ke YYYY-MM-DD
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date tanggalParsed = inputFormat.parse(tanggalInput); // Parsing
                String tanggal = outputFormat.format(tanggalParsed); // Format ulang

                Agenda agenda = new Agenda(0, judul, tanggal, deskripsi);
                controller.createAgenda(agenda);
                JOptionPane.showMessageDialog(this, "Agenda berhasil ditambahkan!");

                // Kosongkan input setelah menambahkan
                txtJudul.setText("");
                txtTanggal.setText("");
                txtDeskripsi.setText("");

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnExportPdf.addActionListener(e -> {
            try {
                // Tentukan lokasi file output
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Simpan sebagai");
                int userSelection = fileChooser.showSaveDialog(this);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    String filePath = fileChooser.getSelectedFile().getAbsolutePath() + ".pdf";

                    // Buat dokumen PDF
                    Document document = new Document();
                    PdfWriter.getInstance(document, new FileOutputStream(filePath));

                    // Buka dokumen
                    document.open();

                    // Tambahkan judul ke dokumen
                    Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                    Paragraph title = new Paragraph("Laporan Agenda Harian", fontTitle);
                    title.setAlignment(Element.ALIGN_CENTER);
                    document.add(title);
                    document.add(new Paragraph("\n")); // Tambahkan spasi

                    // Tambahkan tabel ke dokumen
                    PdfPTable table = new PdfPTable(tblAgenda.getColumnCount());
                    table.setWidthPercentage(100);

                    // Tambahkan header tabel
                    for (int i = 0; i < tblAgenda.getColumnCount(); i++) {
                        PdfPCell cell = new PdfPCell(new Phrase(tblAgenda.getColumnName(i)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(cell);
                    }

                    // Tambahkan data ke tabel
                    for (int row = 0; row < tblAgenda.getRowCount(); row++) {
                        for (int col = 0; col < tblAgenda.getColumnCount(); col++) {
                            table.addCell(tblAgenda.getValueAt(row, col).toString());
                        }
                    }

                    // Tambahkan tabel ke dokumen
                    document.add(table);

                    // Tutup dokumen
                    document.close();

                    // Tampilkan pesan sukses
                    JOptionPane.showMessageDialog(this, "Data berhasil diekspor ke PDF!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnHapus.addActionListener(e -> {
            try {
                // Periksa apakah ada baris yang dipilih
                int selectedRow = tblAgenda.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus!");
                    return;
                }

                // Ambil ID dari baris yang dipilih
                DefaultTableModel model = (DefaultTableModel) tblAgenda.getModel();
                int id = (int) model.getValueAt(selectedRow, 0); // Kolom 0 adalah ID

                // Konfirmasi penghapusan
                int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus data?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    // Hapus data dari database melalui controller
                    controller.deleteAgenda(id);

                    // Hapus baris dari JTable
                    model.removeRow(selectedRow);

                    JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnUbah.addActionListener(e -> {
            try {
                int selectedRow = tblAgenda.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(this, "Pilih data yang akan diubah!");
                    return;
                }

                DefaultTableModel model = (DefaultTableModel) tblAgenda.getModel();
                int id = (int) model.getValueAt(selectedRow, 0); // Ambil ID dari kolom 0

                String judul = txtJudul.getText().trim();
                String tanggalInput = txtTanggal.getText().trim();
                String deskripsi = txtDeskripsi.getText().trim();

                // Validasi input
                if (judul.isEmpty() || tanggalInput.isEmpty() || deskripsi.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
                    return;
                }

                // Konversi tanggal ke format database
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date tanggalParsed = inputFormat.parse(tanggalInput);
                String tanggal = outputFormat.format(tanggalParsed);

                // Perbarui data di database melalui controller
                Agenda agenda = new Agenda(id, judul, tanggal, deskripsi);
                controller.updateAgenda(agenda);

                // Refresh tabel
                loadDataToTable();
                JOptionPane.showMessageDialog(this, "Data berhasil diubah!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        tblAgenda.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = tblAgenda.getSelectedRow(); // Ambil baris yang diklik
                if (selectedRow != -1) {
                    DefaultTableModel model = (DefaultTableModel) tblAgenda.getModel();

                    // Ambil data dari JTable
                    txtJudul.setText(model.getValueAt(selectedRow, 1).toString()); // Kolom 1: Judul
                    txtDeskripsi.setText(model.getValueAt(selectedRow, 3).toString()); // Kolom 3: Deskripsi

                    // Ambil tanggal dan konversi jika diperlukan
                    String tanggalDatabase = model.getValueAt(selectedRow, 2).toString(); // Format YYYY-MM-DD
                    try {
                        SimpleDateFormat databaseFormat = new SimpleDateFormat("yyyy-MM-dd"); // Format sesuai database
                        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy"); // Format untuk ditampilkan
                        Date tanggalParsed = databaseFormat.parse(tanggalDatabase);
                        txtTanggal.setText(displayFormat.format(tanggalParsed)); // Tampilkan dalam format DD/MM/YYYY
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Error parsing date: " + ex.getMessage());
                    }
                }
            }
        });

    }

    private void loadDataToTable() {
        try {
            List<Agenda> agendas = controller.getAllAgenda();
            DefaultTableModel model = (DefaultTableModel) tblAgenda.getModel();

            // Reset tabel
            model.setRowCount(0);

            // Tambahkan data
            for (Agenda agenda : agendas) {
                model.addRow(new Object[]{
                    agenda.getId(),
                    agenda.getJudul(),
                    agenda.getTanggal(),
                    agenda.getDeskripsi()
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saat memuat data: " + ex.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtJudul = new javax.swing.JTextField();
        txtTanggal = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtDeskripsi = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblAgenda = new javax.swing.JTable();
        btnExportPdf = new javax.swing.JButton();
        btnTambah = new javax.swing.JButton();
        btnUbah = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        txtCari = new javax.swing.JTextField();
        btnCari = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 36)); // NOI18N
        jLabel1.setText("Aplikasi Agenda");

        jLabel2.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel2.setText("Judul Agenda");

        jLabel3.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel3.setText("Tabel Agenda");

        jLabel4.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel4.setText("Deskripsi");

        jLabel5.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel5.setText("Tanggal");

        txtDeskripsi.setColumns(20);
        txtDeskripsi.setRows(5);
        jScrollPane1.setViewportView(txtDeskripsi);

        tblAgenda.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "Judul", "Tanggal", "Deskripsi"
            }
        ));
        jScrollPane2.setViewportView(tblAgenda);

        btnExportPdf.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        btnExportPdf.setText("Eksport Data");

        btnTambah.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        btnTambah.setText("Tambah");

        btnUbah.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        btnUbah.setText("Ubah");

        btnHapus.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        btnHapus.setText("Hapus");

        btnCari.setText("Cari Data");
        btnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCariActionPerformed(evt);
            }
        });

        btnRefresh.setText("Refresh Table");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtCari, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnCari)
                                    .addComponent(btnRefresh))))
                        .addGap(23, 23, 23)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnTambah)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnHapus)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnUbah)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnExportPdf))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE)
                                .addComponent(txtTanggal, javax.swing.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE)
                                .addComponent(txtJudul)
                                .addComponent(jScrollPane1))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(259, 259, 259)
                        .addComponent(jLabel1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)
                        .addGap(117, 117, 117)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtJudul, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addGap(19, 19, 19)
                        .addComponent(txtTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(164, 164, 164)
                        .addComponent(jLabel5)))
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(48, 48, 48)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtCari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCari))
                        .addGap(9, 9, 9)
                        .addComponent(btnRefresh)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExportPdf)
                    .addComponent(btnTambah)
                    .addComponent(btnUbah)
                    .addComponent(btnHapus))
                .addContainerGap(161, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariActionPerformed
        try {
            String keyword = txtCari.getText().trim(); // Ambil input pencarian
            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Masukkan kata kunci untuk pencarian!");
                return;
            }

            // Ambil data dari database berdasarkan kata kunci pencarian
            List<Agenda> agendas = controller.searchAgenda(keyword);

            // Perbarui JTable dengan hasil pencarian
            DefaultTableModel model = (DefaultTableModel) tblAgenda.getModel();
            model.setRowCount(0); // Reset tabel

            for (Agenda agenda : agendas) {
                model.addRow(new Object[]{
                    agenda.getId(),
                    agenda.getJudul(),
                    agenda.getTanggal(),
                    agenda.getDeskripsi()
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saat mencari data: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnCariActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        try {
            // Muat ulang data dari database
            loadDataToTable();

            // Kosongkan field pencarian
            txtCari.setText("");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saat refresh data: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnRefreshActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AgendaForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AgendaForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AgendaForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AgendaForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AgendaForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCari;
    private javax.swing.JButton btnExportPdf;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnTambah;
    private javax.swing.JButton btnUbah;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tblAgenda;
    private javax.swing.JTextField txtCari;
    private javax.swing.JTextArea txtDeskripsi;
    private javax.swing.JTextField txtJudul;
    private javax.swing.JTextField txtTanggal;
    // End of variables declaration//GEN-END:variables
}
