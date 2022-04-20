package pt.ulisboa.tecnico.classes;

import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer;

import java.util.HashMap;
import java.util.Map;

public  class TimestampsManager {
    private HashMap<String, Integer> _timestamps;

    public TimestampsManager(HashMap<String, Integer> _timestamps) {
        this._timestamps = _timestamps;
    }

    public TimestampsManager(HashMap<String, Integer> timestamps, NameServerFrontend nameServer){
        this._timestamps = timestamps;
        for(ClassServerNamingServer.ServerEntry entry : nameServer.list()) {
            this._timestamps.put(entry.getAddress(), 0);
        }
    }
    public TimestampsManager(NameServerFrontend nameServer) {
        this._timestamps = new HashMap<String, Integer>();

        // Initialize timestamps
        for(ClassServerNamingServer.ServerEntry entry : nameServer.list()) {
            this._timestamps.put(entry.getAddress(), 0);
        }
    }

    public HashMap<String, Integer> getTimestamps() {
        return _timestamps;
    }

    public void setTimestamps(Map<String, Integer> timestamps) {
        this._timestamps.putAll(timestamps);
    }
    public void updateTimestamps(Map<String, Integer> newTimestamps) {
        for (String replica : this._timestamps.keySet()) {
            if (newTimestamps.containsKey(replica)) {
                int newTimestampValue = newTimestamps.get(replica);

                if (this._timestamps.get(replica) < newTimestampValue) {
                    this._timestamps.put(replica, newTimestampValue);
                }
            }
        }
    }

    public void putTimestamp(String address, int value) {
        this._timestamps.put(address, value);
    }
}
