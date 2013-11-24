package uk.co.tobybatch.minecraftmanager;

import java.awt.Frame;
import java.awt.GridLayout;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author tobias
 */
public class Installer extends javax.swing.JDialog implements Runnable{
    
    protected volatile Thread thread = null;
    protected JLabel feedback;
    
    private final File natives;
    private final DebugEnabled debugger;
    private boolean sucess = false;
    
    public Installer(Frame owner, File natives, DebugEnabled debugger) {
        super(owner, "Set up Minecraft", true);
        this.debugger = debugger;
        this.natives = natives;
        
        this.setLayout(new GridLayout(2, 1, 10, 10));
        
        JLabel label = new JLabel("<html>I will now try and find and configure your minecraft instance to run un-authenicated against Tyrnan's server.  Just follow the instructions below.</html>");
        label.setBorder(new EmptyBorder(10,10,10,10));
        this.add(label);
        
        this.feedback = new JLabel(
                "<html>You need to start Minecraft now and start a single player game using the newest version.</html>",
                JLabel.CENTER
        );
        this.feedback.setBorder(new EmptyBorder(10,10,10,10));
        this.add(this.feedback);
        
        this.setSize(320, 240);
        this.start();
    }

    public void start() {
        this.thread = new Thread(this);
        this.debugger.debug("Starting search for natives");
        this.thread.start();
    }
    
    protected boolean checkForNatives(File dir) {
        return dir.exists() && dir.isDirectory();
    }
    
    protected File findMCNatives(File dir) {
        String list[] = dir.list(
                new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return (name.indexOf("-natives-") != -1);
                    }
                }
        );
        
        if (list.length > 0) {
            return new File(list[0]);
        }
        
        return null;
    }
    
    protected void copy(File mcnatives, File natives) throws IOException {
        this.debugger.debug("Copying natives from " + mcnatives.toString() + " to " + natives.toString());
        FileUtils.copyDirectory(mcnatives, natives);
        // return Files.copy(Paths.get(mcnatives.toString()), Paths.get(natives.toString()), REPLACE_EXISTING);
    }
    
    public boolean isSucess() {
        return this.sucess;
    }
        
    @Override
    public void run() {
        Thread thisThread = Thread.currentThread();
        File dir = this.natives.getParentFile();
        
        boolean nativesExist;
        File mcnatives;
        
        while (this.thread == thisThread) {
            try {
                nativesExist = this.checkForNatives(this.natives);
                this.debugger.debug("Natives exists: " + nativesExist);
                if (nativesExist ) {
                    this.thread = null;
                    this.debugger.debug("Natives found!");
                    this.feedback.setText("<html>Natives exist, you can close minecraft and this window.</html>");
                } else {
                    mcnatives = this.findMCNatives(dir);
                    if (mcnatives != null) {
                        try {
                            this.feedback.setText("<html>Copying files into place.</html>");
                            this.debugger.debug("Copying files into place.");
                            this.copy(new File(dir, mcnatives.toString()), natives);
                            this.sucess = true;
                            continue;
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            Logger.getLogger(Installer.class.getName()).log(Level.SEVERE, null, ex);
                            this.thread = null;
                        }
                    }
                }
                this.debugger.debug("Sleeping 5s");
                thisThread.sleep(5000);
                this.debugger.debug("Awake again");
            } catch (InterruptedException e) {
                e.printStackTrace();
                Logger.getLogger(Installer.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

}
