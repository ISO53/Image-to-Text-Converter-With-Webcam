package com.mycompany.textimagecamera;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
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
        StringBuilder textImage = new StringBuilder();
        char[] line = new char[image.getWidth()];

        for (int h = 0; h < image.getHeight(); h++) {
            for (int w = 0; w < image.getWidth(); w++) {
                int RGB = image.getRGB(w, h);
                int blue = RGB & 0xff;

                if (blue < 31) {
                    line[w] = '█';
                } else if (blue < 92) {
                    line[w] = '▓';
                } else if (blue < 143) {
                    line[w] = '▒';
                } else if (blue < 194) {
                    line[w] = '░';
                } else if (blue < 256) {
                    line[w] = ' ';
                } else {
                    //wtf
                    line[w] = ' ';
                }
            }
            textImage.append('\n').append(line);
        }

        txtPane.setText(textImage.toString());
    }

    public BufferedImage scale(BufferedImage image, int width, int height) {
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(image, 0, 0, width, height, null);
        g2d.dispose();
        return scaledImage;
    }

    public BufferedImage convertMonochrome(BufferedImage image) {
        if (image == null) {
            return null;
        }
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);

        return op.filter(image, null);
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
