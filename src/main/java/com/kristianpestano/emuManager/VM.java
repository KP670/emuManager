package com.kristianpestano.emuManager;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

import static com.kristianpestano.emuManager.EmuManager.*;

public class VM implements Serializable, Comparable<VM> {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private String directoryName;
    private String description;
    private String serializedFileName;


    transient private VMStatus vmStatus = VMStatus.HALTED;
    transient private Process process;
    transient private Process configProcess;

    transient protected boolean isSelected = false;
    transient protected File dirContext;


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
        this.vmStatus = VMStatus.HALTED;

        try {
            if (this.process.isAlive()) {
                vmStatus = VMStatus.RUNNING;
            }

        } catch (NullPointerException e ) {
            vmStatus = VMStatus.HALTED;
        }

        try {
            if (this.configProcess.isAlive()) {
                vmStatus = VMStatus.ON_WAIT;
            }

        } catch (NullPointerException e ) {
            vmStatus = VMStatus.HALTED;
        }

        return this.vmStatus;
    }

    public String getSerializedFileName() {
        return serializedFileName;
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

    public void setSerializedFileName() {
        this.serializedFileName = this.directoryName + SERIALIZED_MACHINES_EXTENSION;
    }

    public void toggleSelected() {
        isSelected = !isSelected;
    }

    public void resetVMInfo() {

        vmStatus = VMStatus.HALTED;
        dirContext = new File( machinesPath + File.separator + this.directoryName);

    }

    public void start() {
        try {
            ProcessBuilder binPb = new ProcessBuilder(binPath.getAbsolutePath());
            binPb.directory(dirContext);

            process = binPb.start();
            vmStatus = VMStatus.RUNNING;
        } catch (IOException e )  {
            System.out.println("Unable to start VM");
        }
    }

    public void kill() {
        System.out.println("Confirm >> ");
        if (confirm()) {
            this.process.destroy();
        }
    }

    public void configure() {
        try {
            ProcessBuilder configuratorPb = new ProcessBuilder(configuratorPath.getAbsolutePath());

            configuratorPb.directory(dirContext);
            configProcess = configuratorPb.start();
        } catch (IOException e) {
            System.out.println("Unable to start VM's configurator");
        }
    }

    public double getPid() throws UnsupportedOperationException {
        return process.pid();
    }

    public VM(String name, String directoryName, String description) {
        this.name = name;
        this.directoryName = directoryName;
        this.description = description;
        this.dirContext = new File( machinesPath + File.separator + directoryName);
        this.serializedFileName = directoryName + SERIALIZED_MACHINES_EXTENSION;
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

    @Override
    public int compareTo(VM o) {
        return o.getName().compareTo(this.name);
    }

    public int compareStatus(VM o) {
        return o.getVmStatus().compareTo(this.vmStatus);
    }
 }