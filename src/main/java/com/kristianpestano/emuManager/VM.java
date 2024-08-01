package com.kristianpestano.emuManager;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Scanner;

public class VM implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private String directoryName;
    private String description;

    transient private VMStatus vmStatus = VMStatus.HALTED;
    transient private Process process;

    transient protected boolean isSelected = false;

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

    public void setName(String name) {
        this.name = name;
    }

    public void setDirectoryName(ProcessBuilder launchApp, String directoryName) throws SecurityException, NullPointerException{
        File vmDirectory = new File(launchApp.directory().getAbsolutePath() + File.separator + this.directoryName);
        File newVMDirectory = new File(launchApp.directory().getAbsolutePath() + File.separator + directoryName);


        if(vmDirectory.renameTo(newVMDirectory)) {
            System.out.println("VM directory has been successfully renamed");
        } else {
            System.out.println("There was an error renaming VM directory");
            return;
        }

        this.directoryName = directoryName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public void resetVMStatus() {
        vmStatus = VMStatus.HALTED;
    }

    public void start(ProcessBuilder launchApp) throws IOException {
        process = launchApp.start();
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

    public void configure(ProcessBuilder launchApp) throws IOException {
        launchApp.directory(new File(launchApp.directory().getAbsolutePath() + File.separator + this.directoryName));
        Process configProcess = launchApp.start();
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