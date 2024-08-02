package com.kristianpestano.emuManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Scanner;

public class VMManager {
    private static VMManager instance;
    protected final File VM_LIST_FILES_ROOT = new File("data/");
    protected final File VM_LIST_FILES_BACKUP = new File("backup/");

    private static LinkedList<VM> vmList;
    private static VM selectedVM;

    public static VM getSelectedVM() {
        return selectedVM;
    }

    public static void setVmList(LinkedList<VM> vmList) {
        VMManager.vmList = vmList;
    }

    public static void printVMs() {

        String vmStatus;
        char isSelected;

        int count = 1;
        System.out.printf("%s | %-3s| %-7s| %-24s| %-24s| %s\n%s\n", "*", "#", "Status", "Name", "Directory", "Description", "=".repeat(96) );

        // Return if list is empty
        if (vmList.isEmpty()) {
            System.out.println("List is empty\n");
            return;
        }

        // Create a table that loads VM information
        for (VM vm : vmList) {

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
            System.out.printf("%s | %-3d| %-7s| %-24s| %-24s| %s\n", isSelected, count, vmStatus, vm.getName(), vm.getDirectoryName(), vm.getDescription() );
            count++;
        }
        System.out.println("Nothing follows\n");
    }

    public void selectVM() {
        if (vmList.isEmpty()) {
            System.out.println("There is no VM to select.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        String userInput;
        int selectedVMNum;
        VM newSelectedVM;

        while (true) { // Ask user for VM to select
            System.out.print("Select a VM, Enter Q to cancel >> ");
            userInput = scanner.nextLine();

            try {
                selectedVMNum = Integer.parseInt(userInput);

                newSelectedVM = vmList.get(selectedVMNum - 1);



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

    public void addVM() {
        String name;
        String directoryName;
        String description;
        File vmPath;

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter name >> ");
            name = scanner.nextLine();

            System.out.print("Enter folder name >> ");
            directoryName = scanner.nextLine();

            System.out.println("Enter description >> ");
            description = scanner.nextLine();

            try {
                VM vm = new VM(name, directoryName,description);
                vmPath = new File(EmuManager.machinesPath + File.separator + vm.getDirectoryName());

                Files.createDirectories(vmPath.toPath());

                VMManager.vmList.add(vm);
                storeVM(vm);

                break;
            } catch (InputMismatchException e) {
                System.out.println("Error: Failed to create VM");
                System.out.printf("Name: %s\nFolder Name: %s\nDescription: %s", name, directoryName, description);
            } catch (IOException e) {
                System.out.println("Error: Failed to create VM");
            }
        }

        refresh();
    }

    public void startVM() throws NullPointerException {
        try {
            selectedVM.start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    public void deleteVM() {
        Scanner scanner = new Scanner(System.in);
        String userInput;
        int numVM;
        VM selectedVM;
        File serializedVM;

        while (true) {
            try {
                // Select VM to delete
                System.out.println("Delete a VM >> ");

                userInput = scanner.nextLine();
                numVM = Integer.parseInt(userInput);
                selectedVM = vmList.get(numVM - 1 );

                serializedVM = new File(VM_LIST_FILES_ROOT + selectedVM.getDirectoryName());
                System.out.println(selectedVM);

                System.out.println("Are you sure? >> ");
                userInput = scanner.nextLine().toUpperCase();

                if (userInput.equalsIgnoreCase("YES") | userInput.equalsIgnoreCase("Y")) {
                        if (serializedVM.delete()) {
                            vmList.remove(numVM);
                            refresh();
                        } else {
                            System.out.println("Error: Can't delete");
                        }
                    break;

                } else if (userInput.equalsIgnoreCase("NO") | userInput.equalsIgnoreCase("N")) {
                    System.out.println("You have cancelled this operation");

                } else {
                    System.out.println("Try Again\n");
                    continue;
                }


                while (true) { // User confirmation
                    System.out.println("Are you finished? >> ");
                    userInput = scanner.nextLine().toUpperCase();

                    if (userInput.equalsIgnoreCase("YES") | userInput.equalsIgnoreCase("Y")) {
                        System.out.println("Done. ");

                        scanner.close();
                        return;
                    }

                    if (userInput.equalsIgnoreCase("NO") | userInput.equalsIgnoreCase("N")) {
                        break;
                    }

                    System.out.println("Invalid Input");
                }

            } catch (NumberFormatException e) {
                System.out.println("Err: Enter an integer");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Err: Does not exist in list");
            }
        }
        scanner.close();
    }

    // Can be revised
    public void editVM() {
        Scanner scanner = new Scanner(System.in);
        String userInput;
        int numVM;
        VM selectedVM;
        File serializedVM;

        String name;
        String directoryName;
        String description;

        // Ensures that that user can restart for a different machine
        while (true) {
            name = "";
            directoryName = "";
            description= "";

            try {
                // Select VM to edit
                System.out.println("Select a VM to edit, enter 0 to cancel editing >> ");

                userInput = scanner.nextLine();



                numVM = Integer.parseInt(userInput);

                if (numVM == 0) {
                    System.out.println("You have cancelled this operation");

                    scanner.close();
                    return;
                }

                selectedVM = vmList.get(numVM - 1);


            } catch (IndexOutOfBoundsException e) {
                System.out.println("Err: Does not exist in list");

                continue;
            }

            try {
                serializedVM = new File(VM_LIST_FILES_ROOT + selectedVM.getDirectoryName());
            } catch (NullPointerException e) {
                System.out.println("Selected machines seems to be its data file, generating");
                try {
                    storeVM(selectedVM);
                } catch (IOException ex) {
                    System.out.println("Unable to create file");

                    continue;
                }
                serializedVM = new File(VM_LIST_FILES_ROOT + selectedVM.getDirectoryName());
            }


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
                            System.out.printf("Current name: %s", selectedVM.getName());
                            System.out.print("Enter new name >> ");
                            name = scanner.nextLine();
                            continue;
                        case 2:
                            System.out.printf("Current Directory Name: %s", selectedVM.getDirectoryName());
                            System.out.print("Enter new directory path >> ");
                            directoryName = scanner.nextLine();
                            continue;
                        case 3:
                            System.out.printf("Current Description: %s", selectedVM.getDescription());
                            System.out.print("Enter new description >>");
                            description = scanner.nextLine();
                            continue;
                        case 0:
                            break;
                        default:
                            System.out.println("Invalid Input");
                            continue;
                    }
                    break;

                } catch (NumberFormatException | InputMismatchException e) {
                    System.out.println("Input must be a number");
                }
            }

            // Confirms user's actions
            try {
                System.out.printf("""
                        Name: %s
                        DirectoryName: %s
                        Description:
                            %s
                       \s
                       \s""", name, directoryName, description);
                System.out.print("Confirm Changes? >> ");

                userInput = scanner.nextLine().toUpperCase();
                if (userInput.equals("YES") | userInput.equals("Y")) {

                    createBackup(selectedVM);

                    if (!name.equals(selectedVM.getName()) & !name.isEmpty()) {
                        selectedVM.setName(name);
                    }
                    if (!directoryName.equals(selectedVM.getName()) & !name.isEmpty()){
                        selectedVM.setDirectoryName(directoryName);
                    }
                    if (!description.equals(selectedVM.getName()) & !description.isEmpty()){
                        selectedVM.setDescription(description);
                    }

                    // Replace serialized file
                    if (serializedVM.delete()) {

                        try {
                            System.out.println("Successfully deleted old Serialized File");

                            // Generates a new serial file
                            System.out.println("Generating new serialized file");
                            storeVM(selectedVM);
                            refresh();
                            return;

                        } catch (IOException e) {
                            System.out.println("An error has occured, restoring to a previous state");
                            loadAllVMs();
                            break;
                        }

                    } else {
                        System.out.println("Something went wrong with deleting the old Serialized File");
                        System.out.println("Reverting Changes...");
                        // restore
                    }
                } else if (userInput.equals("NO") | userInput.equals("N")) {
                    System.out.println("Cancelled this operation");
                    continue;

                } else {
                    System.out.println("Not a valid output");
                    continue;
                }


                while (true) { // User confirmation
                    try {
                        System.out.println("Are you finished? >> ");
                        userInput = scanner.nextLine().toUpperCase();

                        if (userInput.equals("YES") | userInput.equals("Y")) {
                            System.out.println("Done. ");

                            scanner.close();
                            return;

                        } else if (userInput.equals("NO") | userInput.equals("N")) {
                            break;

                        }
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid Input");
                    }
                }

            } catch (NumberFormatException e) {
                System.out.println("Err: Enter an integer");
            }
        }
    scanner.close();
    }


    public void configureVM() throws NullPointerException {
        try {
            selectedVM.configure();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void refresh() throws NullPointerException{

        printVMs(); // Loads updated list
        printSelectedVMDetails();
    }

    public void printSelectedVMDetails() {
        if (selectedVM == null) {
            System.out.println("You have not selected a VM.");
        } else {
            System.out.println(selectedVM);
        }
    }



    public void createBackup(VM vm) {
        File originalLocation;
        File backupLocation;

        originalLocation = new File(VM_LIST_FILES_ROOT + File.separator + vm.getDirectoryName() + EmuManager.SERIALIZED_MACHINES_EXTENSION);
        backupLocation = new File(VM_LIST_FILES_BACKUP +File.separator + vm.getDirectoryName() + EmuManager.SERIALIZED_MACHINES_EXTENSION);

        try {
            Files.createDirectory(Path.of(VM_LIST_FILES_BACKUP.getAbsolutePath()));
            Files.copy(originalLocation.toPath(), backupLocation.toPath());
        } catch (IOException e) {
            System.out.println("Error: Failed to create directory");
        }

    }

    public void loadAllVMs() throws NullPointerException{
        File serializedVM;

        for (String fileName: Objects.requireNonNull(VM_LIST_FILES_ROOT.list())) {
            if (fileName.matches(EmuManager.SERIALIZED_MACHINES_EXTENSION_REGEX)){
                serializedVM = new File(VM_LIST_FILES_ROOT + File.separator + fileName);
                try {
                    loadVM(serializedVM);
                } catch (IOException e) {
                    System.out.println("Error: failed to read file");
                } catch (ClassNotFoundException e) {
                    System.out.println("File's class may not be compatible");
                }
            }
        }
    }

    public void storeAllVMs() {
        if (VM_LIST_FILES_ROOT.mkdir()) {

            int count = 1;
            for (VM vm: vmList) {
                try {
                    storeVM(count, vm);
                    count++;
                } catch (IOException e) {
                    System.out.println("Can't close file");
                }

            }
        }
    }

    public void loadVM(File serializedVM) throws IOException, ClassNotFoundException {
        VM vm;

        if (!serializedVM.toString().matches(EmuManager.SERIALIZED_MACHINES_EXTENSION_REGEX)) {
            throw new IOException("Invalid file extension");
        }

        FileInputStream vmFileInput = new FileInputStream(serializedVM.getAbsolutePath());
        try (ObjectInputStream vmObjInput = new ObjectInputStream(vmFileInput)) {

            vm = (VM) vmObjInput.readObject();
            vm.resetVMInfo();
            vmList.add(vm);

            vmFileInput.close();
        }
    }

    public void reloadVM(int index, File serializedVM) throws IOException, ClassNotFoundException {
        FileInputStream vmFileInput = new FileInputStream(serializedVM.getAbsoluteFile());
        ObjectInputStream vmObjInput = new ObjectInputStream(vmFileInput);

        vmList.set(index, (VM) vmObjInput.readObject());

        vmFileInput.close();
        vmObjInput.close();
    }

    public void storeVM(VM vm) throws IOException{
        FileOutputStream vmFileOutput;
        ObjectOutputStream vmObjOutput;
        File vmFile;

        vmFile = new File(VM_LIST_FILES_ROOT + File.separator + vm.getDirectoryName() + EmuManager.SERIALIZED_MACHINES_EXTENSION);
        // Set filestream
        try {
            Files.createDirectories(VM_LIST_FILES_ROOT.toPath());

            Files.createFile(vmFile.toPath());
            vmFileOutput = new FileOutputStream(vmFile.getAbsoluteFile());
            vmObjOutput = new ObjectOutputStream(vmFileOutput);
            vmObjOutput.writeObject(vm);

            // Write to file
        } catch (FileNotFoundException e) {
            System.out.println("Can't find VM file");
            return;

        } catch (IOException e) {
            System.out.println("Can't write into file");
            return;

        } catch (SecurityException e) {
            System.out.println("You don't have permission to write into this file");
            return;
        }

        // Close
        vmFileOutput.close();
        vmObjOutput.close();
    }

    public void storeVM(int index, VM vm) throws IOException{
        FileOutputStream vmFileOutput;
        ObjectOutputStream vmObjOutput;
        File vmFile;

        vmFile = new File(VM_LIST_FILES_ROOT + File.separator + index + "." + vm.getDirectoryName() + EmuManager.SERIALIZED_MACHINES_EXTENSION);
        // Set filestream
        try {
            Files.createDirectories(VM_LIST_FILES_ROOT.toPath());

            Files.createFile(vmFile.toPath());
            vmFileOutput = new FileOutputStream(vmFile.getAbsoluteFile());
            vmObjOutput = new ObjectOutputStream(vmFileOutput);
            vmObjOutput.writeObject(vm);

            // Write to file
        } catch (FileNotFoundException e) {
            System.out.println("Can't find VM file");
            return;

        } catch (IOException e) {
            System.out.println("Can't write into file");
            return;

        } catch (SecurityException e) {
            System.out.println("You don't have permission to write into this file");
            return;
        }

        // Close
        vmFileOutput.close();
        vmObjOutput.close();
    }

    // Import VM

    // Export VM

    public static VMManager getInstance() {
        if (instance == null) {
            instance = new VMManager();
        }
        return instance;
    }

    private VMManager() {
        selectedVM = null;

        try {
            vmList = new LinkedList<>();
            loadAllVMs();
        } catch (NullPointerException e) {
            System.out.println("Skipping as there are no detected VMs");
        }
    }
}
