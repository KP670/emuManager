package com.kristianpesatno.emuManager;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class VM {

    private String name;
    private String directoryName;
    private String description;

    private VMStatus vmStatus = VMStatus.HALTED;
    private Process process;
    private Process configProcess;

    protected boolean isSelected = false;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getName() {
        return name;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public String getDescription() {
        return description;
    }

    public VMStatus getVmStatus() {
        return vmStatus;
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
        launchApp.directory(new File(launchApp.directory().getAbsolutePath() + "\\" + this.directoryName));
        configProcess = launchApp.start();
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
        return "com.kristianpesatno.emuManager.VM{" +
                "name='" + name + '\'' +
                ", directoryName='" + directoryName + '\'' +
                ", description='" + description + '\'' +
                ", vmStatus=" + vmStatus +
                ", process=" + process +
                ", configProcess=" + configProcess +
                ", isSelected=" + isSelected +
                '}';
    }
}