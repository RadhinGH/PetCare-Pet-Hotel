package util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

public class ImageLoader {

    private static Image cacheKucing;

    private ImageLoader() {}

    public static Image kucing() {
        if (cacheKucing !=null ) {
            return cacheKucing;
        }

        // 1. lewat classpath /resources/
        URL url = ImageLoader.class.getResource("/resources/kucing.png");
        if (url != null) {
            cacheKucing = new ImageIcon(url).getImage();
            return cacheKucing;
        }

        // 2) jalankan lewat terminal dari folder proyek: ./resources/kucing.png
        File file = new File("resources/kucing.png");
        if (file.exists()) {
            cacheKucing = new ImageIcon(file.getAbsolutePath()).getImage();
        } else {
            System.out.println("image tidak ditemukan!");
        }
        return cacheKucing;
    }
}
