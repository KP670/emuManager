package com.kristianpestano.emuManager;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Scanner;

import static com.kristianpestano.emuManager.EmuManager.*;

public class VM implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private String directoryName;
    private String description;


    transient private VMStatus vmStatus = VMStatus.HALTED;
    transient private Process process;

    transient protected boolean isSelected = false;
    transient protected File dirContext;


    public boolean isSelected() {
        return this.isSelected;
    }

    public String getName() {
        return this.name;
    }

    public String getDirectoryName() {
        return this.directoryName;
    }

    public String getDescription() {
        return this.description;
    }

    public VMStatus getVmStatus() {
        return this.vmStatus;
    }

    public File getDirContext() {
        return dirContext;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDirectoryName(String directoryName) throws SecurityException, NullPointerException{
        File newVMDirectory = new File(machinesPath + File.separator + directoryName);


        if(dirContext.renameTo(newVMDirectory)) {
            System.out.println("VM directory has been successfully renamed");
            this.dirContext = new File( machinesPath + File.separator + this.directoryName);

        } else {
            System.out.println("There was an error renaming VM directory");
            return;
        }

        this.directoryName = directoryName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void toggleSelected() {
        isSelected = !isSelected;
    }

    public void resetVMInfo() {

        vmStatus = VMStatus.HALTED;
        dirContext = new File( machinesPath + File.separator + this.directoryName);

    }

    public void start() throws IOException {
        ProcessBuilder binPb = new ProcessBuilder(binPath.getAbsolutePath());
        binPb.directory(dirContext);

        process = binPb.start();
        vmStatus = VMStatus.RUNNING;
    }

    public void kill() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Confirm >> ");
        String userInput = scanner.nextLine().toUpperCase();

        while (true) {
            if (userInput.equals("YES") | userInput.equals("Y")) {
                this.process.destroy();
                break;
            } else if(userInput.equals("NO")| userInput.equals("N")) {
                break;
            } else {
                System.out.println("Try Again\n");
            }
        }
    }

    public void configure() throws IOException {
        ProcessBuilder configuratorPb = new ProcessBuilder(configuratorPath.getAbsolutePath());

        configuratorPb.directory(dirContext);
        Process configProcess = configuratorPb.start();
        while(configProcess.isAlive()) {
            vmStatus = VMStatus.ON_WAIT;
        }

        if (process != null) {
            vmStatus = VMStatus.RUNNING;
        } else {
            vmStatus = VMStatus.HALTED;
        }
    }

    public double getPid() throws UnsupportedOperationException {
        return process.pid();
    }

    public VM(String name, String directoryName, String description) {
        this.name = name;
        this.directoryName = directoryName;
        this.description = description;
        this.dirContext = new File( machinesPath + File.separator + this.directoryName);

    }

    @Override
    public String toString() {
        return String.format("""
                Name:       %s
                Directory:  %s
                
                Description:
                    %s
                """, name, directoryName, description);
    }
}