package com.kristianpestano.emuManager;

import java.io.File;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

import static com.kristianpestano.emuManager.Config.getConfigMap;

public class EmuManager {
    public static final String SERIALIZED_MACHINES_EXTENSION = ".evm";
    public static final String SERIALIZED_MACHINES_EXTENSION_REGEX = ".*." + SERIALIZED_MACHINES_EXTENSION;

    // Modifiable in Config file
    protected static File binPath;
    protected static File configuratorPath;
    protected static File machinesPath;

    public static VMManager vmManager;
    public static Config config;

    private static VM selectedVM;
    public static Scanner scanner;



    public static void menu() {
        String userInput;

        while (true) {
            System.out.println("Machines Path: " + machinesPath.getAbsolutePath());

            if (selectedVM != null) {
                System.out.println("Current Machine Path: " + selectedVM.getDirContext().getAbsolutePath());
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

            System.out.print("\033[H\033[2J");
            System.out.flush();

            try {
                switch (Integer.parseInt(userInput)) {
                    case 1:
                        selectedVM.start();
                        break;
                    case 2:
                        selectedVM.configure();
                        break;
                    case 3:
                        printSelectedVMDetails();
                        break;
                    case 4:
                        selectVM();
                        break;
                    case 5:
                        addVM();
                        refresh();
                        break;
                    case 6:
                        editVMs();
                        refresh();
                        break;
                    case 8:
                        printVMs();
                        System.out.print("Input Enter to go back to the menu >>> ");
                        scanner.nextLine();
                        break;
                    case 9:
                        deleteVMs();
                        refresh();
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

    // Print Details
    public static void printVMs() {

        String vmStatus;
        char isSelected;

        int count = 1;
        System.out.printf(" %s | %-3s | %-7s | %-24s | %-24s | %s\n%s\n", "*", "#", "Status", "Name", "Directory", "Description", "=".repeat(96) );

        // Return if list is empty
        if (VMManager.getVmList().isEmpty()) {
            System.out.println("List is empty\n");
            return;
        }

        // Create a table that loads VM information
        for (VM vm : VMManager.getVmList()) {

            vmStatus = switch (vm.getVmStatus()) {
                case RUNNING -> "Running";
                case HALTED -> "Halted";
                case ON_WAIT -> "Waiting";
                default -> "ERR";
            };

            if (vm.isSelected) {
                isSelected = '*';
            } else {
                isSelected = ' ';
            }

            // ID, Name, Path, Description
            System.out.printf(" %s | %-3d | %-7s | %-24s | %-24s | %s\n", isSelected, count, vmStatus, vm.getName(), vm.getDirectoryName(), vm.getDescription() );
            count++;
        }
        System.out.println("Nothing follows\n");
    }

    public static void printSelectedVMDetails() throws NullPointerException{
        if (selectedVM == null) {
            throw new NullPointerException();
        }
    }

    public static void selectVM() {
        if (VMManager.getVmList().isEmpty()) {
            System.out.println("There is no VM to select.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        String userInput;
        int selectedVMIndex;
        VM newSelectedVM;

        while (true) { // Ask user for VM to select
            System.out.print("Select a VM, Enter Q to cancel >> ");
            userInput = scanner.nextLine();

            try {
                selectedVMIndex = Integer.parseInt(userInput);

                newSelectedVM = VMManager.getVmList().get(selectedVMIndex - 1);

                if (selectedVM == null) {
                    selectedVM = newSelectedVM;
                    selectedVM.toggleSelected();
                    break;
                } else if (selectedVM.equals(newSelectedVM)){
                    selectedVM.toggleSelected();
                    selectedVM = null;
                    break;
                }

                selectedVM.toggleSelected();
                selectedVM = newSelectedVM;
                selectedVM.toggleSelected();

                break;
            } catch (NumberFormatException e) {
                if (userInput.equalsIgnoreCase("Q")) {
                    return;
                } else {
                    System.out.println("Err: Enter an integer");

                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Err: Does not exist in list");
            }
        }

        refresh();
    }

    public static void addVM() {
        String name;
        String directoryName;
        String description;

        while (true) {
            System.out.print("Enter name >> ");
            name = scanner.nextLine();

            System.out.print("Enter folder name >> ");
            directoryName = scanner.nextLine();

            System.out.println("Enter description >> ");
            description = scanner.nextLine();

            try {

                if (name.isEmpty() | directoryName.isEmpty()) {
                    throw new NullPointerException();
                }

                vmManager.addVM(name, directoryName, description);

                break;
            }  catch (InputMismatchException e) {
                System.out.println("Error: Failed to create VM");
                System.out.printf("Name: %s\nFolder Name: %s\nDescription: %s", name, directoryName, description);

            } catch (IOException e) {
                System.out.println("Error: Failed to create VM");
            } catch (NullPointerException e) {

                if (name == null & directoryName == null) {
                    System.out.println("Both name and directory must have a value");

                } else {
                    if (name == null) {
                        System.out.println("Name must have a value");
                    }

                    if (name == null) {
                        System.out.println("Name must have a value");
                    }
                }
            }

        }


    }

    public static void deleteVMs() {
        Scanner scanner = new Scanner(System.in);
        String userInput;
        int vmPos;
        VM selectedVM;

        while (true) {
            try {
                // Select VM to delete
                System.out.println("Delete a VM >> ");

                userInput = scanner.nextLine();
                vmPos = Integer.parseInt(userInput) - 1;
                selectedVM = VMManager.getVmList().get(vmPos);

                System.out.println(selectedVM);

                System.out.println("Are you sure? >> ");

                if (confirm()) {
                    vmManager.deleteVM(vmPos, selectedVM);
                    break;
                } else {
                    System.out.println("You have cancelled this operation");
                }

                System.out.println("Are you finished? >> ");
                if (confirm()){
                    System.out.println("Done. ");
                    return;
                }

            } catch (NumberFormatException e) {
                System.out.println("Err: Enter an integer");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Err: Does not exist in list");
            }
        }
    }

    public static void editVMs() {
        Scanner scanner = new Scanner(System.in);
        String userInput;

        int selectedVMIndex;
        int realVMIndex;
        VM selectedVM;

        String name;
        String directoryName;
        String description;

        while (true) {
            name = "";
            directoryName = "";
            description= "";

            try {
                // Select VM to edit
                System.out.println("Select a VM to edit, enter 0 to cancel editing >> ");
                userInput = scanner.nextLine();

                selectedVMIndex = Integer.parseInt(userInput);

                if (selectedVMIndex == 0) {
                    System.out.println("You have cancelled this operation");
                    return;
                }

            } catch (IndexOutOfBoundsException e) {
                System.out.println("Err: Does not exist in list");

                continue;
            }

            realVMIndex = selectedVMIndex - 1;
            selectedVM = VMManager.getVmList().get(realVMIndex);

            // User picks which attribute to edit
            while (true) {
                try {
                    System.out.println(selectedVM);

                    System.out.print("""
                        What would you like to edit?
                        [1] Name
                        [2] Directory Name
                        [3] Description
                        
                        [0] Finish
                        >>\s""");

                    switch (Integer.parseInt(scanner.nextLine())) {
                        case 1:
                            System.out.printf("Current name: %s\n", selectedVM.getName());
                            System.out.print("Enter new name >> ");
                            name = scanner.nextLine().trim();
                            continue;
                        case 2:
                            System.out.printf("Current Directory Name: %s\n", selectedVM.getDirectoryName());
                            System.out.print("Enter new directory path >> ");
                            directoryName = scanner.nextLine().trim();
                            continue;
                        case 3:
                            System.out.printf("Current Description: %s\n", selectedVM.getDescription());
                            System.out.print("Enter new description >>");
                            description = scanner.nextLine().trim();
                            continue;
                        case 0:
                            break;
                        default:
                            System.out.println("Invalid Input");
                            continue;
                    }

//                    if (name.isEmpty() | directoryName.isEmpty() | description.isEmpty()) {
//                        System.out.println("Input must not be empty");
//                    } else {
//                        break;
//                    }
                    break;
                } catch (NumberFormatException | InputMismatchException e) {
                    System.out.println("Input must be a number");
                }
            }

            // Confirms user's actions
            if (!name.isEmpty()){
                System.out.println("Name: " + name);
            }

            if (!directoryName.isEmpty()) {
                System.out.println("Directory Name: " + directoryName);
            }

            if (!description.isEmpty()) {
                System.out.println("Description: \n\t" + description);
            }

            System.out.print("Confirm Changes? >> ");
            try {
                if (confirm()) {
                    vmManager.editVM(realVMIndex, name, directoryName, description);

                }  else {
                    System.out.println("Cancelled this operation");
                    continue;
                }
            } catch (IOException e) {
                System.out.println("ERR: Unable to edit VM's details");
            }


            System.out.println("Are you finished? >> ");
            if (confirm()) {
                System.out.println("Done. ");
                return;

            } else {
                break;
            }
        }
    }

    public static void resetPaths() {
        binPath = new File(getConfigMap().get("binPath"));
        configuratorPath = new File(getConfigMap().get("configuratorPath"));
        machinesPath = new File(getConfigMap().get("machinesPath"));
    }

    public static boolean confirm(){
        Scanner scanner = new Scanner(System.in);
        String userInput;

        while (true) {
            userInput = scanner.nextLine().trim();

            if (userInput.equalsIgnoreCase("Yes") | userInput.equalsIgnoreCase("Y")) {
                return true;
            } else if (userInput.equalsIgnoreCase("No") | userInput.equalsIgnoreCase("N")){
                return false;
            }

            System.out.println("Invalid Input");
        }
    }

    public static void refresh(){

        printVMs(); // Loads updated list

        if (selectedVM != null) {
            printSelectedVMDetails();
        }
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

        scanner = new Scanner(System.in);
        printVMs();
        menu();
        close();
    }
}
