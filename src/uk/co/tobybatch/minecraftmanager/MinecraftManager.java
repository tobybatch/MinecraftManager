package uk.co.tobybatch.minecraftmanager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author tobias
 */
public class MinecraftManager extends JPanel implements Runnable, ActionListener, DebugEnabled {
    
    public static final String VERSION = "1.7.2";

    protected JTextArea txtDebug;
    protected JTextField runText;

    protected static File home = null;
    protected static File config = null;
    protected static File appdata = null;
    protected static File version = null;
    protected static File natives = null;

    private Thread thread = null;
    
    public MinecraftManager() {
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(10,10,10,10));
        
        JPanel top = new JPanel(new BorderLayout(15,15));
        
        JLabel topLabel = new JLabel("Minecraft Manager", JLabel.CENTER);
        topLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        top.add(topLabel, BorderLayout.NORTH);
        
        JPanel runPanel = new JPanel(new BorderLayout());
        runPanel.add(new JLabel("Run minecraft as:"), BorderLayout.WEST);
        this.runText = new JTextField();
        JButton runButton = new JButton("Run");
        this.runText.addActionListener(this);
        runButton.addActionListener(this);
        runPanel.add(this.runText, BorderLayout.CENTER);
        runPanel.add(runButton, BorderLayout.EAST);
        
        JPanel innerTopPanel = new JPanel(new BorderLayout());
        innerTopPanel.add(runPanel, BorderLayout.NORTH);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        this.txtDebug = new JTextArea();
        // txtDebug.setEnabled(false);
        bottomPanel.add(new JScrollPane(this.txtDebug), BorderLayout.CENTER);
        
        JPanel bottomWrapper = new JPanel(new BorderLayout(10,10));
        bottomWrapper.add(innerTopPanel, BorderLayout.NORTH);
        bottomWrapper.add(bottomPanel, BorderLayout.CENTER);
                
        this.add(top, BorderLayout.NORTH);
        this.add(bottomWrapper, BorderLayout.CENTER);
    }
    
    public void actionPerformed(ActionEvent e) {
        runMinecraft();
    }
    
    public static File getHome() {
        if (MinecraftManager.home == null) {
            MinecraftManager.home = new File(System.getProperty("user.home"));
        }

        return MinecraftManager.home;
    }

    public static File getConfig(String filename) {
        if (MinecraftManager.config == null) {
            File appData = MinecraftManager.getAppData();
            File configDir = new File(appData, filename);
            if (!configDir.isDirectory()) {
                configDir.mkdir();
            }
            File config = new File(configDir, filename);
            MinecraftManager.config = config;
        }

        return MinecraftManager.config;
    }

    public static File getAppData() {
        if (MinecraftManager.appdata == null) {
            File appdata = MinecraftManager.getHome();
            if (System.getProperty("os.name").toLowerCase().indexOf("win") != -1) {
                appdata = new File(appdata, "AppData\\Roaming");
            }
            MinecraftManager.appdata = appdata;
        }
        
        return MinecraftManager.appdata;
    }

    public static File getVersionDir() {
        if (MinecraftManager.version == null) {
            MinecraftManager.version = new File(
                MinecraftManager.getHome(),
                MinecraftManager.getAppData()
                + File.separator + ".minecraft"
                + File.separator + "versions"
                + File.separator + MinecraftManager.VERSION
            );
        }

        return MinecraftManager.version;
    }

    public static File getNativesDir() {
        if (MinecraftManager.natives == null) {
            MinecraftManager.natives = new File(
                MinecraftManager.getVersionDir(),
                MinecraftManager.VERSION + "-natives"
            );
        }

        return MinecraftManager.natives;
    }
    
    protected void runMinecraft() {
        File home = MinecraftManager.getHome();
        this.debug("Home space found at " + home);
        File appdata = MinecraftManager.getAppData();
        this.debug("AppData found at " + appdata);
        File version = MinecraftManager.getVersionDir();
        this.debug("Versions dir found at " + version);
        File natives = MinecraftManager.getNativesDir();
        this.debug("Looking for natives at " + version);
        
        if (!natives.exists() || !natives.isDirectory()) {
            this.debug("No natives found");
            this.start();
            return;
        }

        this.execMC();
    }
        
    public void execMC() {
        String cli = this.getCli();
        this.debug(cli);
        
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(cli);
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(MinecraftManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean install() {
        File natives = MinecraftManager.getNativesDir();

        Component frame = this;
        while (!(frame instanceof Frame)) {
            frame = frame.getParent();
        };
        Installer inst = new Installer((Frame)frame, natives, this);
        
        int x = frame.getX();
        int y = frame.getY();
        int width = frame.getWidth();
        int height = frame.getHeight();
        
        int xoff = x + ((width - inst.getWidth())/2);
        int yoff = y + ((height - inst.getHeight())/2);
        
        inst.setLocation(xoff, yoff);
        inst.setVisible(true);
        
        return inst.isSucess();
    }
   
    public void debug(String message) {
        String text = this.txtDebug.getText();
        this.txtDebug.setText(text + message + "\n");
    }
    
    public String getCli() {
        File home = MinecraftManager.getHome();
        File appdata = MinecraftManager.getAppData();
        File version = MinecraftManager.getVersionDir();
        File natives = MinecraftManager.getNativesDir();
        
        String OS = System.getProperty("os.name").toLowerCase();
        
        String jarpath = this.walk(
                new File(home, ".minecraft" + File.separator + "libraries")
        );
        jarpath += File.pathSeparator + new File(
                home,
                ".minecraft"
                        + File.separator + "versions/"
                        + VERSION
                        + File.separator + VERSION + ".jar"
        );
        this.debug(jarpath);
                
        return System.getProperty("java.home") 
                + "/bin/java "
                + "-Xmx1G "
                + "-Djava.library.path=" + natives.toString() + " "
                + "-cp " + jarpath
                + " net.minecraft.client.main.Main "
                + "--username " + this.runText.getText() + " "
                + "--version " + VERSION + " "
                + "--gameDir " + home + "/.minecraft "
                + "--assetsDir " + home + "/.minecraft/assets "
                + " --uuid " + UUID.randomUUID() + " "
                + "--accessToken invalid";
    }
    
    public String walk(File path) {
        String jarpath = "";
        
        Path _path;
        try {
            _path = Paths.get(path.getCanonicalPath());
            String [] jars = Finder.find(_path, "*.jar");
            jarpath = StringUtils.join(jars, File.pathSeparator);
        } catch (IOException ex) {
            this.debug(ex.getMessage());
            ex.printStackTrace();
            Logger.getLogger(
                    MinecraftManager.class.getName()).log(Level.SEVERE, null, ex
            );
        }   
        
        return jarpath;
    }
    
    public void start() {
        this.thread = new Thread(this);
        this.debug("Starting search for natives");
        this.thread.start();
    }

    public void run() {
        Thread thisThread = Thread.currentThread();

        while (this.thread == thisThread) {
            if (this.install()) {
                thisThread = null;
                this.execMC();
            } else {
                this.debug("Main thread waiting.");
                try {
                    thisThread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Logger.getLogger(Installer.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Minecraft Manager");
        MinecraftManager main = new MinecraftManager();
        frame.getRootPane().setLayout(new BorderLayout());
        frame.getRootPane().add(main, BorderLayout.CENTER);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 480);
        frame.setVisible(true);
    }
}
