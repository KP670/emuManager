import com.kristianpestano.emuManager.VM;

import java.io.File;

public class VMtest {
    public static void main(String[] args) {

        File binPath = new File("C:\\Users\\krist\\Applications\\BasiliskII\\BasiliskII.exe");
        File emuPath = new File("D:\\PC Emulators\\Basilisk II\\");



        ProcessBuilder  binPb = new ProcessBuilder(binPath.getAbsolutePath());
        ProcessBuilder configPb = new ProcessBuilder(binPath.getParent() + "\\BasiliskIIGUI.exe");

        binPb.directory(emuPath);
        configPb.directory(emuPath);


        System.out.println(binPath.getParent());

        VM vm = new VM("MacOS Sample", "MacOS Sample", "");
//        try {
//            System.out.println(vm);
//
//        } catch (IOException e){
////            System.out.println(e);
//        } catch (InterruptedException e) {
//            System.out.println(e);
//        }
    }
}
