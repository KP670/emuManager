package com.kristianpestano.emuManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Config {
    private static HashMap<String, String> configMap;
    private static Config instance;

    protected final File CONFIG_FILE = new File("etc/config.txt");
    private final String COMMENTED_LINE_REGEX = "^//(.*?)$";
    private final Pattern COMMMENTED_LINE_PATTERN = Pattern.compile(COMMENTED_LINE_REGEX, Pattern.MULTILINE);

    // Defaults
    public final String DEFAULT_PARENT_DIRECTORY_WIN = System.getenv("ProgramFiles(x86)") + File.separator;
    public final String DEFAULT_MACHINES_DIRECTORY_WIN = System.getenv("SystemDrive") + System.getenv("HOMEPATH") + File.separator + "Machines" + File.separator;

    public static HashMap<String, String> getConfigMap() {
        return configMap;
    }

    protected void createDefaultConfig() {
        configMap.put("binPath", DEFAULT_PARENT_DIRECTORY_WIN + "BasiliskII.exe");
        configMap.put("configuratorPath", DEFAULT_PARENT_DIRECTORY_WIN + "BasiliskIIGUI.exe");
        configMap.put("machinesPath", DEFAULT_MACHINES_DIRECTORY_WIN);
    }

    protected void loadConfig() {
        String keyVal;
        String[] keyValArr;

        try {

            if (!CONFIG_FILE.exists()) {
                // Set Default
                configurator();

            } else {
                Scanner configScanner = new Scanner(CONFIG_FILE.getCanonicalFile());
                while (configScanner.hasNextLine()) {

                    keyVal = configScanner.nextLine();
                    if (keyVal.matches(String.valueOf(COMMMENTED_LINE_PATTERN))) {
                        continue;
                    }
                    keyValArr = keyVal.split(" : ");
                    configMap.put(keyValArr[0].strip(), keyValArr[1].strip());

                }
            }

        } catch (IOException e ){
            System.out.println("Error: Unable to load config file");
        }
    }


    public void storeConfig(){

        File configParent = new File(CONFIG_FILE.getParent());

        try {
            if (configMap.isEmpty()) {
                createDefaultConfig();
            }

            if (configParent.mkdir() && CONFIG_FILE.createNewFile()) {
                System.out.println("File seems to not exist. Creating...");
            }

            FileWriter fileWriter = new FileWriter(CONFIG_FILE.getCanonicalFile());
            for (String key: configMap.keySet()) {

                fileWriter.write(String.format("%-24s %s\n", key, " : " + configMap.get(key)));
            }

            fileWriter.close();

        } catch (SecurityException e) {
            System.out.printf("%s: Check your permissions settings\n", e);
        } catch (IOException e) {
            System.out.println("Failed to write to config file");
        }


    }

    public void configurator() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the location of the emulator executable >>> ");
        configMap.put("binPath", scanner.nextLine());

        System.out.print("Enter the location of the emulator's configurator executable >>> ");
        configMap.put("configuratorPath", scanner.nextLine());

        System.out.print("Enter location of the Emulated Machines >> ");
        configMap.put("machinesPath",scanner.nextLine());

        storeConfig();
        loadConfig();
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    private Config() {
        configMap = new HashMap<>();
        if (!CONFIG_FILE.exists()) {
            configurator();
        } else {
            loadConfig();
        }
    }
}
