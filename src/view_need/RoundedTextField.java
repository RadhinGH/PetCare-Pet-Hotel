package view_need;

import view.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// JTextField dengan sudut membulat; border jadi lebih tegas saat fokus.
public class RoundedTextField extends JTextField {

    private final int radius = 14;

    public RoundedTextField() {
        setOpaque(false);
        setBorder(new EmptyBorder(10, 16, 10, 16));
        setForeground(Theme.TEKS_TUA);
        setCaretColor(Theme.PRIMARY);
        setFont(Theme.font(Font.PLAIN, 14));
        setSelectedTextColor(Color.WHITE);
        setSelectionColor(Theme.PRIMARY_MUDA);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // gambar dulu latar membulat, lalu biarkan super menggambar teksnya
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, w - 1, h - 1, radius, radius);
        g2.setColor(hasFocus() ? Theme.PRIMARY : Theme.BORDER_FIELD);
        g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);
        g2.dispose();

        super.paintComponent(g);
    }
}
