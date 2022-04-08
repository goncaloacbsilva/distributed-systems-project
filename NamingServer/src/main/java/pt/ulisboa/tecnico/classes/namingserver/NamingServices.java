package pt.ulisboa.tecnico.classes.namingserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.ServerEntry;

public class NamingServices {
    private ConcurrentHashMap<String, ServiceEntry> _services;

    public NamingServices() {
        this._services = new ConcurrentHashMap<String, ServiceEntry>();
    }

    public synchronized void registerServer(String serviceName, ServerEntry server) {
        if (!this._services.containsKey(serviceName)) {
            this._services.put(serviceName, new ServiceEntry(serviceName));
        }
        this._services.get(serviceName).addServerEntry(server);
    }

    public synchronized void deleteServer(String serviceName, String address) {
        if (this._services.containsKey(serviceName)) {
            this._services.get(serviceName).deleteServerEntry(address);
        }
    }

    public synchronized Collection<ServerEntry> lookupServers(String serviceName, List<String> qualifiers) {
        if (this._services.containsKey(serviceName)) {
            return this._services.get(serviceName).lookupServers(qualifiers);
        } else {
            return new ArrayList<ServerEntry>();
        }
    }

    public ConcurrentHashMap<String, ServiceEntry> getServices() {
        return _services;
    }
}
