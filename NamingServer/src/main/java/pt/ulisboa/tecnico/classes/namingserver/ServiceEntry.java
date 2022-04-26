package pt.ulisboa.tecnico.classes.namingserver;

import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.ServerEntry;

import java.util.ArrayList;
import java.util.List;

public class ServiceEntry {
  private final String _serviceName;
  private final List<ServerEntry> _servers;

  public ServiceEntry(String serviceName) {
    this._serviceName = serviceName;
    this._servers = new ArrayList<ServerEntry>();
  }

  public String getServiceName() {
    return _serviceName;
  }

  public synchronized void addServerEntry(ServerEntry server) {
    this._servers.add(server);
  }

  public synchronized void deleteServerEntry(String address) {
    this._servers.removeIf(server -> (server.getAddress().equals(address)));
  }

  public synchronized List<ServerEntry> lookupServers(List<String> qualifiers) {
    List<ServerEntry> foundServers = new ArrayList<ServerEntry>();
    for (ServerEntry server : this._servers) {
      if (server.getQualifiersList().containsAll(qualifiers) || qualifiers.isEmpty()) {
        foundServers.add(server);
      }
    }

    return foundServers;
  }

  public synchronized List<ServerEntry> getServers() {
    return new ArrayList<>(_servers);
  }
}
