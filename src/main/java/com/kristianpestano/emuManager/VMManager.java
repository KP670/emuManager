package com.kristianpestano.emuManager;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

public class VMManager {
    private static VMManager instance;
    protected final File VM_LIST_FILES_ROOT = new File("data");
    protected final File VM_LIST_FILES_BACKUP = new File("backup");

    private static ArrayList<VM> vmList;

    public static ArrayList<VM> getVmList() {
        return vmList;
    }

    public static void setVmList(ArrayList<VM> vmList) {
        VMManager.vmList = vmList;
    }

    public void addVM(String name, String directoryName, String description) throws IOException{

        VM vm = new VM(name, directoryName, description);
        File file = vm.getDirContext();

        System.out.println("Creating VM file...");

//        if (file.mkdir()) {
//            System.out.println("Successfully created VM directory");
//        } else {
//            throw new IOException();
//        }

        Files.createDirectories(file.toPath());

        storeVM(vm);
        VMManager.vmList.add(vm);
    }

    public void deleteVM(int index, VM vm) {
        File serializedVM = new File(VM_LIST_FILES_ROOT + File.separator + vm.getSerializedFileName());

        if (serializedVM.delete()) {
            vmList.remove(index);
        } else {
            System.out.println("Err: Unable to delete the specified VM");
        }
    }

    public void editVM(int vmIndex, String name, String directoryName, String description) throws IOException {
        VM originalVM = vmList.get(vmIndex);

        if (directoryName.isBlank()) {

            if (!name.equals(originalVM.getName()) | !name.isBlank()){
                originalVM.setName(name);
            }

            if (!description.equals(originalVM.getDescription()) | !name.isBlank()) {
                originalVM.setDescription(description);
            }

            storeVM(originalVM);
            return;
        }

        File oldSerializedVM = new File(VM_LIST_FILES_ROOT + File.separator + originalVM.getSerializedFileName());
        VM editedVM;
        File newSerializedVM;

        if (originalVM.getVmStatus() == VMStatus.RUNNING) {
            throw new IOException();
        }

        if (name.isEmpty()) {
            name = originalVM.getName();
        }

        if (description.isEmpty()) {
            description = originalVM.getDescription();
        }

        // Delete original VM Serialized file
        Files.deleteIfExists(oldSerializedVM.toPath());

        // Create revised VM files
        editedVM = new VM(name, directoryName, description);
        newSerializedVM = new File(VM_LIST_FILES_ROOT + File.separator + editedVM.getSerializedFileName());

        Files.createFile(newSerializedVM.toPath());


        // Rename original dir
        if (originalVM.getDirContext().renameTo(editedVM.getDirContext())) {
            // Replace old VM in list
            vmList.set(vmIndex, editedVM);
        } else {

            Files.deleteIfExists(newSerializedVM.toPath());
            throw new IOException();
        }

    }


    public void createBackup(VM vm) {
        File originalLocation;
        File backupLocation;

        originalLocation = new File(VM_LIST_FILES_ROOT + File.separator + vm.getDirectoryName() + EmuManager.SERIALIZED_MACHINES_EXTENSION);
        backupLocation = new File(VM_LIST_FILES_BACKUP + File.separator + vm.getDirectoryName() + EmuManager.SERIALIZED_MACHINES_EXTENSION);

        try {
            Files.createDirectory(Path.of(VM_LIST_FILES_BACKUP.getAbsolutePath()));
            Files.copy(originalLocation.toPath(), backupLocation.toPath());
        } catch (IOException e) {
            System.out.println("Err: Failed to create directory");
        }

    }

    public void loadAllVMs() throws NullPointerException{
        File serializedVM;

        if (vmList != null) {
            vmList = new ArrayList<>();
        }

        for (String fileName: Objects.requireNonNull(VM_LIST_FILES_ROOT.list())) {
            if (fileName.matches(EmuManager.SERIALIZED_MACHINES_EXTENSION_REGEX)){
                serializedVM = new File(VM_LIST_FILES_ROOT + File.separator + fileName);
                try {
                    loadVM(serializedVM);
                } catch (IOException e) {
                    System.out.println("Err: failed to read file");
                } catch (ClassNotFoundException e) {
                    System.out.println("Err: File's detected class is not compatible");
                }
            }
        }
    }

    public void storeAllVMs() {
        if (VM_LIST_FILES_ROOT.mkdir()) {

            for (VM vm: vmList) {
                storeVM(vm);
            }
        }
    }

    public void loadVM(File serializedVM) throws IOException, ClassNotFoundException {
        VM vm;

        if (!serializedVM.toString().matches(EmuManager.SERIALIZED_MACHINES_EXTENSION_REGEX)) {
            throw new IOException("Err: Invalid file extension");
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

    public void storeVM(VM vm) {
        FileOutputStream vmFileOutput;
        ObjectOutputStream vmObjOutput;
        File vmFile;

        vmFile = new File(VM_LIST_FILES_ROOT + File.separator + vm.getSerializedFileName());

        // Set filestream
        try {
            Files.createDirectories(VM_LIST_FILES_ROOT.toPath());

            Files.createFile(vmFile.toPath());
            vmFileOutput = new FileOutputStream(vmFile.getAbsoluteFile());
            vmObjOutput = new ObjectOutputStream(vmFileOutput);
            vmObjOutput.writeObject(vm);

            // Write to file
        } catch (FileNotFoundException e) {
            System.out.println("Err: Can't find VM file");
            return;

        } catch (FileAlreadyExistsException e ) {
            System.out.println("Err: File of the same name already exists");
            return;

        } catch (IOException e) {
            System.out.println("Err: Can't write into file");
            return;

        } catch (SecurityException e) {
            System.out.println("Err: You don't have permission to write into this file");
            return;
        }

        // Close
        try {
            vmFileOutput.close();
            vmObjOutput.close();
        } catch (IOException e) {
            System.out.println("Err: Unable to close file");
        }
    }

    private void sortVMListByName() {
        vmList.sort((o1, o2) -> 0);
    }

    private void sortVMListByStatus() {
        vmList.sort(VM::compareStatus);
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
        try {
            vmList = new ArrayList<>();
            loadAllVMs();
        } catch (NullPointerException e) {
            System.out.println("Skipping as there are no detected VMs");
        }
    }
}
