package me.kutuzov.server.python.pmodule;

import me.kutuzov.server.client.Client;
import me.kutuzov.server.python.PythonClient;

public interface PythonModuleInstallable {
    String getPath();
    boolean isInstalled(PythonClient client);
    boolean install(PythonClient client);
    boolean uninstall(PythonClient client);
}