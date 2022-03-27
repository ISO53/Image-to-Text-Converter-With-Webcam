package com.mycompany.textimagecamera;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 *
 * @author ISO53
 */
public class RunnableCamera implements Runnable {

    private BufferedImage image;
    private boolean exit;
    private final Thread thread;
    private final Webcam webcam;
    private final JTextPane txtPane;
    private final Document doc;

    public RunnableCamera(JTextPane txtPane) {
        webcam = Webcam.getDefault();
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        webcam.open();

        exit = false;
        this.txtPane = txtPane;
        doc = txtPane.getDocument();
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while (!exit) {
            image = webcam.getImage();
            printTextImage(scale(convertMonochrome(image), image.getWidth() / 3, image.getHeight() / 6));
            /* Scaling width to 1/3 and height to 1/6 because 1:1 image text
             * converting lowers the fps. You can scale the image to 1:1 to
             * improve quality by setting the width normal and height 1/2.
             * Height must be 1/2 because the length of the character used is
             * twice its width. So the width should always be twice the height.
             */
        }
    }

    public void stop() {
        webcam.close();
        exit = true;
    }

    public void printTextImage(BufferedImage image) {
        String textImage = "";

        for (int h = 0; h < image.getHeight(); h++) {
            textImage += "\n";
            for (int w = 0; w < image.getWidth(); w++) {
                int RGB = image.getRGB(w, h);
                int blue = RGB & 0xff;
                //int green = (RGB & 0xff00) >> 8;
                //int red = (RGB & 0xff0000) >> 16;
                //All values will be same since this is a monochrome image

                // ░▒▓█ 
                if (blue < 51) {
                    textImage += "█";
                } else if (blue < 102) {
                    textImage += "▓";
                } else if (blue < 153) {
                    textImage += "▒";
                } else if (blue < 204) {
                    textImage += "░";
                } else if (blue < 256) {
                    textImage += " ";
                } else {
                    //wtf
                    textImage += " ";
                }
            }
        }
        txtPane.setText(textImage);
    }

    /**
     * Runs slightly faster than printTextImage
     * @param image
     */
    public void printTextImageV2(BufferedImage image) {

        for (int h = 0; h < image.getHeight(); h++) {
            for (int w = 0; w < image.getWidth(); w++) {
                int RGB = image.getRGB(w, h);
                int blue = RGB & 0xff;
                //int green = (RGB & 0xff00) >> 8;
                //int red = (RGB & 0xff0000) >> 16;
                //All values will be same since this is a monochrome image

                // ░▒▓█ 
                if (blue < 51) {
                    append("█");
                } else if (blue < 102) {
                    append("▓");
                } else if (blue < 153) {
                    append("▒");
                } else if (blue < 204) {
                    append("░");
                } else if (blue < 256) {
                    append(" ");
                } else {
                    //wtf
                    append(" ");
                }
            }
            append("\n");
        }
        clean();
    }

    public void append(String str) {
        try {
            doc.insertString(doc.getLength(), str, null);
        } catch (BadLocationException ex) {
            Logger.getLogger(RunnableCamera.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void clean() {
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException ex) {
            Logger.getLogger(RunnableCamera.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public BufferedImage scale(BufferedImage image, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int x, y;
        int ww = image.getWidth();
        int hh = image.getHeight();
        int[] ys = new int[height];
        for (y = 0; y < height; y++) {
            ys[y] = y * hh / height;
        }
        for (x = 0; x < width; x++) {
            int newX = x * ww / width;
            for (y = 0; y < height; y++) {
                int col = image.getRGB(newX, ys[y]);
                img.setRGB(x, y, col);
            }
        }
        return img;
    }

    public BufferedImage convertMonochrome(BufferedImage image) {
        if (image == null) {
            return null;
        }

        BufferedImage monochromeImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                Color c = new Color(image.getRGB(j, i));
                int red = (int) (c.getRed());
                int green = (int) (c.getGreen());
                int blue = (int) (c.getBlue());

                int mix = ((red + green + blue) / 3);
                Color newColor = new Color(mix, mix, mix);

                monochromeImage.setRGB(j, i, newColor.getRGB());
            }
        }

        return monochromeImage;
    }

    public void screenShotTxt() {
        String textImage;
        BufferedImage tempImage = scale(image, image.getWidth(), image.getHeight() / 2);
        String userDefPath = "C:\\Users\\" + System.getProperty("user.name") + "\\Desktop\\text.txt";
        File log = new File(userDefPath);
        FileOutputStream fOS;
        OutputStreamWriter oSW;
        BufferedWriter bW = null;

        try {
            fOS = new FileOutputStream(log);
            oSW = new OutputStreamWriter(fOS, "UTF-8");
            bW = new BufferedWriter(oSW);
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(RunnableCamera.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int h = 0; h < tempImage.getHeight(); h++) {
            textImage = "\n";
            for (int w = 0; w < tempImage.getWidth(); w++) {
                int RGB = tempImage.getRGB(w, h);
                int blue = RGB & 0xff;
                //int green = (RGB & 0xff00) >> 8;
                //int red = (RGB & 0xff0000) >> 16;
                //All values will be same since this is a monochrome image

                // ░▒▓█ 
                if (blue < 51) {
                    textImage += "█";
                } else if (blue < 102) {
                    textImage += "▓";
                } else if (blue < 153) {
                    textImage += "▒";
                } else if (blue < 204) {
                    textImage += "░";
                } else if (blue < 255) {
                    textImage += " ";
                } else {
                    //wtf
                    textImage += " ";
                }
            }
            try {
                bW.append(textImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            bW.close();
        } catch (IOException ex) {
            Logger.getLogger(RunnableCamera.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
