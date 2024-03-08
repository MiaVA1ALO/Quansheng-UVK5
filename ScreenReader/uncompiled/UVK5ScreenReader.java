import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import static java.lang.Thread.sleep;

public class UVK5ScreenReader
{
    private static String COM_PORT;
    private static final int REFRESH_RATE = 10; // time in milliseconds between each update.

    private static final int GLOBAL_WIDTH = 500;
    private static final int GLOBAL_HEIGHT = 350;

    // some colours I like. To use them, edit the entry for the mark and space colours in decodeImage()
    private static final int ORANGE = 0xFFA500;
    private static final int BLUE = 0x0827F5;
    // note that before the Screen class there is an experimental randomColor function. It generates coloured static!
    public static void main(String[] args)
    {
        if(args.length == 1)
        {
            System.out.println("Reading from port " + COM_PORT + "...");
            COM_PORT = args[0];
        }
        else
        {
            System.out.println("No argument provided for COM port. Using COM4...\nNormal usage: java UVK5ScreenReader COMx");
            COM_PORT = "COM4";
        }
        new UVK5ScreenReader();
    }

    public UVK5ScreenReader()
    {
        while(true)
        {
            dumpScreen(); // reads the UVK5 screen buffer via to COM port, saves to buff.bin
            Screen.setImage(decodeImage());
        }
    }


    private void dumpScreen()
    {
        try {
            // uses CMD to run the .py script.
            Process process = Runtime.getRuntime().exec("cmd /c start /min python util_051f_ramreader.py " + COM_PORT + " 0x20000684 0x400 buff.bin");
            int exitCode = process.waitFor();

            if (exitCode == 0)
            {
                System.out.println("COM READ SUCCESS");
            }
            else
            {
                System.out.println("Error: likely due to incorrect COM port, COM port in use, or Python + pyserial not installed.\nCOM port: " + COM_PORT);
                System.exit(0);
            }
        }
        catch (IOException | InterruptedException e)
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private BufferedImage decodeImage()
    {
        try
        {
            try
            {
                sleep(REFRESH_RATE);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }

            byte[] raw = java.nio.file.Files.readAllBytes(new File("buff.bin").toPath());
            BufferedImage image = new BufferedImage(128, 64, BufferedImage.TYPE_INT_RGB);

            int byteIndex = 0;
            for (int row = 0; row < 8; row++)
            {
                for (int colmn = 0; colmn < 128; colmn++)
                {
                    for (int bit = 0; bit < 8; bit++)
                    {
                        int x = colmn;
                        int y = 8 * row + bit;
                        if (((raw[byteIndex] >> bit) & 0x1) == 1)
                        {
                            image.setRGB(x, y, 0x000000); // mark (text, icons, etc)
                        }
                        else
                        {
                            image.setRGB(x, y, ORANGE); // space (empty area)
                        }
                    }
                    byteIndex++;
                }
            }

            return image;
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            System.out.println("ArrayIndexOutOfBounds: This means the input file is empty. Is the radio able to be read from the COM port?");
            try
            {
                sleep(3000); // wait 3 seconds, retry
                dumpScreen();
            }
            catch (InterruptedException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return new BufferedImage(128, 64, BufferedImage.TYPE_INT_RGB);
    }

    private int randomColor()
    {
        Random random = new Random();
        return random.nextInt(0x000000, 0xFFFFFF);
    }

    private static class Screen
    {
        private static JFrame frame;
        private static JPanel panel;

        public static void setImage(BufferedImage image)
        {
            if (frame == null) // if the script was just run, create a new JFrame
            {
                frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                panel = new JPanel()
                {
                    @Override
                    protected void paintComponent(Graphics g)
                    {
                        super.paintComponent(g);
                        g.drawImage(image.getScaledInstance(GLOBAL_WIDTH, GLOBAL_HEIGHT, Image.SCALE_SMOOTH), 0, 0, null);
                    }

                    @Override
                    public Dimension getPreferredSize()
                    {
                        return new Dimension(GLOBAL_WIDTH, GLOBAL_HEIGHT);
                    }
                };

                frame.getContentPane().add(panel);
                frame.pack();
                frame.setVisible(true);
            }
            else // if the script has been running, re-initialize the JPanel
            {
                panel = new JPanel()
                {
                    @Override
                    protected void paintComponent(Graphics g)
                    {
                        super.paintComponent(g);
                        g.drawImage(image.getScaledInstance(GLOBAL_WIDTH, GLOBAL_HEIGHT, Image.SCALE_SMOOTH), 0, 0, null);

                    }

                    @Override
                    public Dimension getPreferredSize()
                    {
                        return new Dimension(GLOBAL_WIDTH, GLOBAL_HEIGHT);
                    }
                };

                frame.getContentPane().add(panel);
                frame.pack();
                frame.setVisible(true);
                panel.repaint();
            }
        }
    }
}
