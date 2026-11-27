package view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

import dao.UserDAO;
import model.User;
import util.AvaterLabel;
import util.ImageLoader;
import util.ToasterPopOut;
import view_need.*;

public class LoginFrame extends JFrame {

    private JTextField kotakUsername;
    private JPasswordField kotakPassword;
    private JCheckBox centangLihatPassword;
    private JButton btnMasuk;

    public LoginFrame() {

        //  1. Window Login
        setTitle("PetCare - Masuk");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 670);
        setLocationRelativeTo(null);
        setResizable(false);

        // 2. Latar Window
        PawBackgroundPanel latar = new PawBackgroundPanel();
        latar.setForeground(Theme.PAW_PINK);
        latar.setLayout(new GridBagLayout());
        setContentPane(latar);

        GridBagConstraints g = new GridBagConstraints();
        g.weightx = 1.0;
        g.weighty = 1.0;
        g.anchor = GridBagConstraints.CENTER;
        latar.add(bangunkartu(), g);

        getRootPane().setDefaultButton(null);
    }

    private RoundedPanel bangunkartu() {
        RoundedPanel kartu = new RoundedPanel(30);
        kartu.setBackground(Color.WHITE);
        kartu.setPreferredSize(new Dimension(392, 595));
        kartu.setLayout(new GridBagLayout());

        GridBagConstraints grid = new GridBagConstraints();
        grid.weightx = 1.0;
        grid.gridx = 0;
        grid.gridy = 0;
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.insets = new Insets(26, 34, 0, 34);

        // 1. avatar kucing
        JPanel barisAvatar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        barisAvatar.setOpaque(false);
        barisAvatar.add(new AvaterLabel((ImageLoader.kucing()), 110));
        kartu.add(barisAvatar, grid);

        // 2. Judul dan subjudul
        grid.gridy = 1;
        grid.insets = new Insets(10, 34, 0, 34);
        JLabel judul = new JLabel("PetCare", SwingConstants.CENTER);
        judul.setIconTextGap(8);
        judul.setFont(Theme.font(Font.BOLD, 29));
        judul.setForeground(Theme.PRIMARY);
        kartu.add(judul, grid);

        grid.gridy = 2;
        grid.insets = new Insets(2, 34,0,34);
        JLabel subjudul = new JLabel("Pet Care & Pet Hotel - masuk dulu yuk!", SwingConstants.CENTER);
        subjudul.setFont(Theme.font(Font.PLAIN, 14));
        subjudul.setForeground(Theme.TEKS_MUDA);
        kartu.add(subjudul,grid);

        // 3) USERNAME
        grid.gridy = 3;
        grid.insets = new Insets(16, 34, 0, 34);
        kartu.add(labelKecil("USERNAME"), grid);

        grid.gridy = 4;
        grid.insets = new Insets(5, 34, 0, 34);
        kotakUsername = new RoundedTextField();
        kotakUsername.setPreferredSize(new Dimension(10, 42));
        kartu.add(kotakUsername, grid);

        // 4) PASSWORD
        grid.gridy = 5;
        grid.insets = new Insets(11, 34, 0, 34);
        kartu.add(labelKecil("PASSWORD"), grid);

        grid.gridy = 6;
        grid.insets = new Insets(5, 34, 0, 34);
        kotakPassword = new RoundedPasswordField();
        kotakPassword.setPreferredSize(new Dimension(10, 42));
        kotakPassword.addActionListener(e -> cekLogin());
        kartu.add(kotakPassword, grid);

        // 5. Lihat pw dan lupa pw

        grid.gridy = 7;
        grid.insets = new Insets(8,34,0,34);
        JPanel barisOpsi = new JPanel(new BorderLayout());
        barisOpsi.setOpaque(false);

        centangLihatPassword = new JCheckBox("Lihat Password?");
        centangLihatPassword.setOpaque(false);
        centangLihatPassword.setFont(Theme.font(Font.PLAIN, 12));
        centangLihatPassword.setForeground(Theme.TEKS_MUDA);
        centangLihatPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        centangLihatPassword.addActionListener(e -> kotakPassword.setEchoChar(
                centangLihatPassword.isSelected() ? '\u0000' : '\u2022'));

        JLabel lbLupaPW = new JLabel("Lupa Password?");
        lbLupaPW.setFont(Theme.font(Font.PLAIN, 12));
        lbLupaPW.setForeground(Theme.LINK);
        lbLupaPW.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lbLupaPW.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LoginFrame.this,
                        "Yah NT, harus hubungin Admin untuk resets ya",
                        "Lupa password", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        barisOpsi.add(centangLihatPassword, BorderLayout.WEST);
        barisOpsi.add(lbLupaPW, BorderLayout.EAST);
        kartu.add(barisOpsi, grid);

        // 6. Tombol Masuk

        grid.gridy = 8;
        grid.insets = new Insets(13, 34, 0, 34);
        btnMasuk = new PillButton("Masuk");
        btnMasuk.setIcon(new PawIcon(18, Color.WHITE));
        btnMasuk.setIconTextGap(8);
        btnMasuk.setPreferredSize(new Dimension(10,52));
        btnMasuk.addActionListener(e -> cekLogin());
        kartu.add(btnMasuk,grid);

        // 7) kotak akun demo
        grid.gridy = 9;
        grid.insets = new Insets(12, 26, 0, 26);
        RoundedPanel kotakDemo = new RoundedPanel(12);
        kotakDemo.setBackground(new Color(0xFDF2F7));
        kotakDemo.setBorderColor(new Color(0xF6D8E6));
        kotakDemo.setLayout(new GridBagLayout());
        JLabel lblDemo = new JLabel(
                "<html><center><b>Admin:</b> admin / admin123 &bull; resepsionis / resepsionis123"
                + "<br><b>Pelanggan:</b> pelanggan1 / pelanggan123 &bull; pelanggan2 / pelanggan123</center></html>",
                SwingConstants.CENTER);
        lblDemo.setFont(Theme.font(Font.PLAIN, 10));
        lblDemo.setForeground(Theme.TEKS_MUDA);
        kotakDemo.add(lblDemo);
        kotakDemo.setPreferredSize(new Dimension(10, 48));
        kartu.add(kotakDemo, grid);

        // 8) tautan daftar akun pelanggan
        grid.gridy = 10;
        grid.insets = new Insets(10, 34, 14, 34);
        JLabel lblDaftar = new JLabel("Belum punya akun? Daftar sebagai Pelanggan", SwingConstants.CENTER);
        lblDaftar.setFont(Theme.font(Font.BOLD, 12));
        lblDaftar.setForeground(Theme.LINK);
        lblDaftar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblDaftar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                bukaDaftarPelanggan();
            }
        });
        kartu.add(lblDaftar, grid);


        return kartu;
    }

    // functionalitas

    private JLabel labelKecil(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(Theme.font(Font.BOLD,11));
        l.setForeground(Theme.TEKS_MUDA);
        return l;
    }

    private void cekLogin() {
        String username = kotakUsername.getText().trim();          // input username
        String password = new String(kotakPassword.getPassword());  // input pw

        // validasikan dahulu
        if (username.isEmpty() || password.isEmpty()) {         // validasii
            ToasterPopOut.error(this,"Meow? Isi  yang bener yak!",
                    "isi username dan password dulu! Meow~");
            goyang();       // kita berikan animasi shake
            return;
        }

        // panggil DAO: SELECT ... WHERE username ? lalu bandingkan hash SHA-256
        User user = new UserDAO().login(username, password);
        if (user != null) {
            //kalo suksesss -> tutup LoginFrame dan buka MainFrame
            new MainFrame(user).setVisible(true);
            dispose();
        } else {
            // login nya gagal kasih Toaster dan efek shake
            ToasterPopOut.error( this, "Meow? Username / password salah!",
                    "coba inget-inget lagi ya~");
            goyang();
        }
    }

    private void bukaDaftarPelanggan() {
        RegisterDialog dialog = new RegisterDialog(this);
        dialog.setVisible(true);
        User baru = dialog.getHasil();
        if (baru != null) {
            kotakUsername.setText(baru.getUsername());
            kotakPassword.setText("");
            ToasterPopOut.sukses(this, "Akun pelanggan dibuat!",
                    "silakan masuk sebagai " + baru.getUsername());
        }
    }

    // animasi shake

    private void goyang() {
        final Point dasar = getLocation();
        final int[] geser = {0, -10, 10, -8, 8, -6, 6, -3, 3, 0};
        final int[] i = {0};

        Timer timer = new Timer(28, null);
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setLocation(dasar.x + geser[i[0]], dasar.y);
                i[0]++;
                if (i[0] >= geser.length) {
                    timer.stop();
                    setLocation(dasar);
                }
            }
        });
        timer.start();
    }

}


