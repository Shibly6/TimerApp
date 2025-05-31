package org.TimerApp;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppUtilities {
    public static void createStartupShortcut() {
        String username = System.getProperty("user.name");
        Path startupPath = Paths.get("C:", "Users", username, "AppData", "Roaming",
                "Microsoft", "Windows", "Start Menu", "Programs", "Startup");
        Path shortcutPath = startupPath.resolve("TimerApp.bat");

        try {
            String jarPath = new File(TimerApp.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).getPath();
            String batchContent = "@echo off\n" +
                    "start javaw -jar \"" + jarPath + "\"";

            Files.write(shortcutPath, batchContent.getBytes());
        } catch (Exception e) {
            System.err.println("Error creating startup shortcut: " + e.getMessage());
        }
    }
}