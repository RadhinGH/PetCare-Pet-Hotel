package view_need;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import view.Theme;

/** Tombol sekunder: putih berbingkai pink (untuk Batal, Ubah, Tutup, dll). */
public class OutlineButton extends JButton {

    private boolean hover;

    public OutlineButton(String teks) {
        super(teks);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(Theme.PRIMARY_TUA);
        setFont(Theme.font(Font.BOLD, 13));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        g2.setColor(hover ? new Color(0xFFE6F1) : Color.WHITE);
        g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
        g2.setColor(hover ? Theme.PRIMARY_MUDA : Theme.BORDER_FIELD);
        g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
        g2.dispose();

        super.paintComponent(g);
    }
}
