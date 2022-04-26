package pt.ulisboa.tecnico.classes.namingserver;

import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.ServerEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class NamingServices {
  private final ConcurrentHashMap<String, ServiceEntry> _services;

  public NamingServices() {
    this._services = new ConcurrentHashMap<String, ServiceEntry>();
  }

  public void registerServer(String serviceName, ServerEntry server) {
    this._services.putIfAbsent(serviceName, new ServiceEntry(serviceName));
    this._services.get(serviceName).addServerEntry(server);
  }

  public void deleteServer(String serviceName, String address) {
    if (this._services.containsKey(serviceName)) {
      this._services.get(serviceName).deleteServerEntry(address);
    }
  }

  public List<ServerEntry> lookupServers(String serviceName, List<String> qualifiers) {
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
