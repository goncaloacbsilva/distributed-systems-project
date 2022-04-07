package pt.ulisboa.tecnico.classes;

import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer;

import java.util.HashMap;
import java.util.Map;

public abstract class TimestampsManager {
    private HashMap<String, Integer> _timestamps;

    public TimestampsManager(NameServerFrontend nameServer) {
        this._timestamps = new HashMap<String, Integer>();

        // Initialize timestamps
        for(ClassServerNamingServer.ServerEntry entry : nameServer.list()) {
            this._timestamps.put(entry.getAddress(), 0);
        }

        System.out.println("[TIMESTAMPS INIT]: " + this._timestamps);
    }

    public HashMap<String, Integer> getTimestamps() {
        return _timestamps;
    }

    public void setTimestamps(Map<String, Integer> timestamps) {
        this._timestamps.putAll(timestamps);
    }
}
