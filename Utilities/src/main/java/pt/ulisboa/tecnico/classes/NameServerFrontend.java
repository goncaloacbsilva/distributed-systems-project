package pt.ulisboa.tecnico.classes;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer;
import pt.ulisboa.tecnico.classes.contract.naming.NamingServerServiceGrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class NameServerFrontend {

    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub _stub;
    private ManagedChannel _cachedChannel;
    private ClassServerNamingServer.ServerEntry _cachedServer;
    private HashMap<String, Integer> _cachedInactiveServers;

    private static final Integer TRY_AGAIN_COOLDOWN = 2;

    public NameServerFrontend() {
        ManagedChannel channel =
                ManagedChannelBuilder.forAddress("localhost", 5000).usePlaintext().build();
        this._stub = NamingServerServiceGrpc.newBlockingStub(channel);
        this._cachedInactiveServers = new HashMap<String, Integer>();
    }

    /**
     * requests for  a new service for a given server identified by its address to be registered
     *
     * @param serviceName
     * @param address
     * @param qualifiers
     */
    public void registerServer(String serviceName, String address, List<String> qualifiers) {
        ClassServerNamingServer.RegisterRequest request =
                ClassServerNamingServer.RegisterRequest.newBuilder()
                        .setServiceName(serviceName)
                        .setAddress(address)
                        .addAllQualifiers(qualifiers)
                        .build();

        this._stub.register(request);
    }

    /**
     * deletes a given server for a given service
     *
     * @param serviceName
     * @param address
     */
    public void deleteServer(String serviceName, String address) {
        ClassServerNamingServer.DeleteRequest request =
                ClassServerNamingServer.DeleteRequest.newBuilder()
                        .setServiceName(serviceName)
                        .setAddress(address)
                        .build();

        this._stub.delete(request);
    }

    private void updateInactiveServersList() {
        for (String key : new ArrayList<>(_cachedInactiveServers.keySet())) {
            int prev = _cachedInactiveServers.get(key);
            if (prev + 1 > TRY_AGAIN_COOLDOWN) {
                _cachedInactiveServers.remove(key);
            } else {
                _cachedInactiveServers.put(key, prev + 1);
            }
        }
    }

    /**
     * requests all servers registered on name server
     *
     * @return
     */
    public List<ClassServerNamingServer.ServerEntry> list() {
        return this._stub.list(ClassServerNamingServer.ListRequest.getDefaultInstance()).getServersList();
    }

    public boolean isPrimary(String address) {
        for (ClassServerNamingServer.ServerEntry server : this.list()) {
            if (server.getAddress().equals(address) && server.getQualifiersList().contains("P")) {
                return true;
            }
        }
        return false;
    }

    private List<ClassServerNamingServer.ServerEntry> getLookupServerList(String serviceName, List<String> qualifiers) {
        ClassServerNamingServer.LookupRequest request =
                ClassServerNamingServer.LookupRequest.newBuilder()
                        .setServiceName(serviceName)
                        .addAllQualifiers(qualifiers)
                        .build();

        ClassServerNamingServer.LookupResponse response = this._stub.lookup(request);

        List<ClassServerNamingServer.ServerEntry> servers = response.getServersList().stream()
                .filter(server -> !_cachedInactiveServers.containsKey(server.getAddress()))
                .toList();

        if (servers.isEmpty()) {
            throw new RuntimeException("No servers available");
        }

        return servers;
    }

    /**
     * request all eligible servers given required server qualifiers
     *
     * @param serviceName
     * @param qualifiers
     * @return
     */
    public ClassServerNamingServer.ServerEntry lookup(
            String serviceName, List<String> qualifiers, String serverId) {

        List<ClassServerNamingServer.ServerEntry> servers = this.getLookupServerList(serviceName, qualifiers);

        int parsedServerId = Integer.parseInt(serverId);

        if (parsedServerId >= servers.size()) {
            throw new RuntimeException("No servers available");
        }

        return servers.get(parsedServerId);
    }

    public ClassServerNamingServer.ServerEntry lookup(
            String serviceName, List<String> qualifiers) {

        List<ClassServerNamingServer.ServerEntry> servers = this.getLookupServerList(serviceName, qualifiers);

        Random rand = new Random();

        return servers.get(rand.nextInt(servers.size()));
    }

    /**
     * Returns a Managed Channel connected to a service of a server with the requested qualifiers If
     * the qualifiers don't change it returns a cached channel instead of performing new connection
     *
     * @param serviceName
     * @param qualifiers
     * @return ManagedChannel
     */
    public ManagedChannel getChannel(
            String serviceName, List<String> qualifiers, boolean previousIsInactive) {
        if (_cachedChannel == null || (!_cachedServer.getQualifiersList().containsAll(qualifiers) && !qualifiers.isEmpty()) || previousIsInactive) {
            if (_cachedChannel != null) {
                if (previousIsInactive) {
                    _cachedInactiveServers.put(_cachedServer.getAddress(), 0);
                }
                _cachedChannel.shutdown();
            }

            _cachedServer = this.lookup(serviceName, qualifiers);
            _cachedChannel = ManagedChannelBuilder.forTarget(_cachedServer.getAddress()).usePlaintext().build();
        }

        this.updateInactiveServersList();

        return _cachedChannel;
    }
}
