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
    protected static final File CONFIG_FILE = new File("etc/config.txt");

    private static final String COMMENTED_LINE_REGEX = "^//(.*?)$";
    private static final Pattern COMMMENTED_LINE_PATTERN = Pattern.compile(COMMENTED_LINE_REGEX, Pattern.MULTILINE);

    // Defaults
    public static final String DEFAULT_PARENT_DIRECTORY_WIN = System.getenv("ProgramFiles(x86)") + File.separator;
    public static final String DEFAULT_MACHINES_DIRECTORY_WIN = System.getenv("SystemDrive") + System.getenv("HOMEPATH") + File.separator + "Machines" + File.separator;

    public static HashMap<String, String> getConfigMap() {
        return configMap;
    }

    protected static void createDefaultConfig() {
        configMap.put("binPath", DEFAULT_PARENT_DIRECTORY_WIN + "BasiliskII.exe");
        configMap.put("configuratorPath", DEFAULT_PARENT_DIRECTORY_WIN + "BasiliskIIGUI.exe");
        configMap.put("machinesPath", DEFAULT_MACHINES_DIRECTORY_WIN);
    }

    protected static void loadConfig() {
        String tempString;
        String[] temp;

        try {

            if (!CONFIG_FILE.exists()) {
                // Set Default
                configurator();

            } else {
                Scanner configScanner = new Scanner(CONFIG_FILE.getCanonicalFile());
                while (configScanner.hasNextLine()) {

                    tempString = configScanner.nextLine();
                    if (tempString.matches(String.valueOf(COMMMENTED_LINE_PATTERN))) {
                        continue;
                    }
                    temp = tempString.split(" : ");
                    configMap.put(temp[0].strip(), temp[1].strip());

                }
            }

        } catch (IOException e ){
            System.out.println();
        }
    }


    public static void storeConfig(){

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

    public static void configurator() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the location of the emulator executable >>> ");
        configMap.put("binPath", scanner.nextLine());

        System.out.print("Enter the location of the emulator's configurator executable >>> ");
        configMap.put("configuratorPath", scanner.nextLine());

        System.out.print("Enter location of the Emulated Machines >> ");
        configMap.put("machinesPath",scanner.nextLine());
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    private Config() {
        configMap = new HashMap<>();
        loadConfig();
    }
}
