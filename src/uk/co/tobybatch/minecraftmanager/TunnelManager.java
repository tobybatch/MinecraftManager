package uk.co.tobybatch.minecraftmanager;

import java.io.File;
import javax.swing.*;

public class TunnelManager extends JPanel implements Runnable {

    private long interval = 500;
    private long maxinter = 10000;

    protected JLabel status;

    private Thread thread = null;

    public TunnelManager() {
        // make sure we have keys and a ssh tunnel executable
        this.status = new JLabel();
        
        // JMenu menu = new JMenu("File");
        // menu.addItem(new JMenuItem("Exit"));
        // JMenu edit = new JMenu("Edit");
    }

    protected boolean checkPpk() {
        return false;
    }

    public void start() {
        this.thread = new Thread(this);
        this.thread.start();
    }

    public void run() {
        Thread thisThread = Thread.currentThread();

        while (this.thread == thisThread) {
            // is the tunnel running?
            // if not start it
            // thisThread.sleep(500);
        }
    }

    private long intervalUp(long interval) {
        if (interval <= maxinter) {
            return interval * 2;
        }
        
        return interval;
    }
}
