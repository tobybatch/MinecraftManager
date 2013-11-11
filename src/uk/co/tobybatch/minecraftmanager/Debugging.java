package uk.co.tobybatch.minecraftmanager;

import java.io.File;

/**
 *
 * @author tobias
 */
public class Debugging {
    public static void main(String args[]) {
        MinecraftManager mm = new MinecraftManager();
        
        File home = new File(System.getProperty("user.home"));
        File versiondir = new File(
                home,
                File.separator + ".minecraft"
                + File.separator + "versions"
                + File.separator + MinecraftManager.VERSION
        );
        File natives = new File(versiondir, MinecraftManager.VERSION + "-natives");
        
//        String cli = mm.getCli(home, versiondir, natives).replace(
//                "USERNAME", "tobias"
//        );
        
//        System.out.println(cli);
    }
}
