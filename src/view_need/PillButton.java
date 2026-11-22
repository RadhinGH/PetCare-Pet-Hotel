package view_need;

import view.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Tombol "pill" pink gradasi seperti desain.
 * Cara pakai: new PillButton("Masuk") + setIcon(new PawIcon(18, Color.WHITE)).
 */
public class PillButton extends JButton {

    private boolean hover;
    private boolean ditekan;

    public PillButton(String teks) {
        super(teks);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(Theme.font(Font.BOLD, 16));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hover = false; ditekan = false; repaint(); }
            @Override public void mousePressed(MouseEvent e) { ditekan = true; repaint(); }
            @Override public void mouseReleased(MouseEvent e) { ditekan = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        Color cAtas, cBawah;
        if (ditekan) {
            cAtas = Theme.PRIMARY_TUA;  cBawah = Theme.PRIMARY;
        } else if (hover) {
            cAtas = Theme.PRIMARY_MUDA; cBawah = Theme.PRIMARY;
        } else {
            cAtas = new Color(0xF97BB3);
            cBawah = new Color(0xF2549A);
        }
        g2.setPaint(new GradientPaint(0, 0, cAtas, 0, h, cBawah));
        g2.fillRoundRect(0, 0, w - 1, h - 1, h, h); // radius = tinggi -> bentuk pill
        g2.dispose();

        super.paintComponent(g); // menggambar teks + ikon di atas gradasi
    }
}
