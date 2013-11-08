package uk.co.tobybatch.minecraftmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
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
    
    private File natives;
    
    private boolean sucess = false;
    
    public Installer(Frame owner, File natives) {
        super(owner, "Set up Minecraft", true);
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
                if (nativesExist ) {
                    this.thread = null;
                    this.feedback.setText("<html>Natives exist, you can close minecraft and this window.</html>");
                } else {
                    mcnatives = this.findMCNatives(dir);
                    if (mcnatives != null) {
                        try {
                            this.feedback.setText("<html>Copying files into place.</html>");
                            this.copy(new File(dir, mcnatives.toString()), natives);
                            this.sucess = true;
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            Logger.getLogger(Installer.class.getName()).log(Level.SEVERE, null, ex);
                            this.thread = null;
                        }
                    }
                }
                thisThread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Logger.getLogger(Installer.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

}
