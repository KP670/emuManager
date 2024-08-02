package com.kristianpestano.emuManager;

import java.io.File;
import java.util.Scanner;

import static com.kristianpestano.emuManager.Config.*;

public class EmuManager {
    public static final String SERIALIZED_MACHINES_EXTENSION = ".evm";
    public static final String SERIALIZED_MACHINES_EXTENSION_REGEX = ".*." + SERIALIZED_MACHINES_EXTENSION;

    // Modifiable in Config file
    public static File binPath;
    public static File configuratorPath;
    public static File machinesPath;

    public static VMManager vmManager;
    public static Config config;

    public static void menu() {
        Scanner scanner;
        String userInput;

        while (true) {
            scanner = new Scanner(System.in);
            System.out.println("Machines Path: " + machinesPath.getAbsolutePath());

            if (VMManager.getSelectedVM() != null) {
                System.out.println("Current Machine Path: " + VMManager.getSelectedVM().getDirContext().getAbsolutePath());
            }

            System.out.print("""
                           \s
                            [1] Start Selected VM
                            [2] Configure Selected VM
                            [3] Print Selected VM Details
                           \s
                            [4] Select Another VM
                           \s
                            [5] Add a VM
                            [6] Edit a VM
                           \s
                            [8] List VMs
                            [9] Delete a VM
                            [0] Configure Manager
                           \s
                            Press Q to quit
                           >>\s""");
            userInput = scanner.nextLine();
            try {
                switch (Integer.parseInt(userInput)) {
                    case 1:
                        vmManager.startVM();
                        break;
                    case 2:
                        vmManager.configureVM();
                        break;
                    case 3:
                        vmManager.printSelectedVMDetails();
                        break;
                    case 4:
                        vmManager.selectVM();
                        break;
                    case 5:
                        vmManager.addVM();
                        break;
                    case 6:
                        vmManager.editVM();
                        break;
                    case 8:
                        VMManager.printVMs();
                        break;
                    case 9:
                        vmManager.deleteVM();
                        break;
                    case 0:
                        config.configurator();
                        resetPaths();

                }

            } catch (NumberFormatException e) {
                if (userInput.equalsIgnoreCase("Q")) {
                    System.out.println("Exiting...");
                    return;

                } else {
                    System.out.println("Invalid Input");

                }
            } catch (NullPointerException e) {
                System.out.println("You did not select a VM");
            }
        }
    }

    public static void resetPaths() {
        binPath = new File(getConfigMap().get("binPath"));
        configuratorPath = new File(getConfigMap().get("configuratorPath"));
        machinesPath = new File(getConfigMap().get("machinesPath"));
    }

    public static void close() {
        vmManager.storeAllVMs();
        config.storeConfig();
    }

    public static void main(String[] args){
        config = Config.getInstance();

        binPath = new File(getConfigMap().get("binPath"));
        configuratorPath = new File(getConfigMap().get("configuratorPath"));
        machinesPath = new File(getConfigMap().get("machinesPath"));

        vmManager = VMManager.getInstance();

        VMManager.printVMs();
        menu();

        close();
    }
}
