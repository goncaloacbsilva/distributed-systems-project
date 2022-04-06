package pt.ulisboa.tecnico.classes;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer;
import pt.ulisboa.tecnico.classes.contract.naming.NamingServerServiceGrpc;

import java.util.HashMap;
import java.util.List;

public class NameServerFrontend {

  private final NamingServerServiceGrpc.NamingServerServiceBlockingStub _stub;
  private ManagedChannel _cachedChannel;
  private String _cachedAddress;
  private List<String> _cachedChannelQualifiers;
  private HashMap<String, Integer> _cachedInactiveServers;

  private static final Integer TRY_AGAIN_COOLDOWN = 4;

  public NameServerFrontend() {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", 5000).usePlaintext().build();
    this._stub = NamingServerServiceGrpc.newBlockingStub(channel);
  }

  public void registerServer(String serviceName, String address, List<String> qualifiers) {
    ClassServerNamingServer.RegisterRequest request =
        ClassServerNamingServer.RegisterRequest.newBuilder()
            .setServiceName(serviceName)
            .setAddress(address)
            .addAllQualifiers(qualifiers)
            .build();

    this._stub.register(request);
  }

  public void deleteServer(String serviceName, String address) {
    ClassServerNamingServer.DeleteRequest request =
        ClassServerNamingServer.DeleteRequest.newBuilder()
            .setServiceName(serviceName)
            .setAddress(address)
            .build();

    this._stub.delete(request);
  }

  private void updateInactiveServersList() {
    for (String key : _cachedInactiveServers.keySet()) {
      int prev = _cachedInactiveServers.get(key);
      if (prev + 1 > TRY_AGAIN_COOLDOWN) {
        _cachedInactiveServers.remove(key);
      } else {
        _cachedInactiveServers.put(key, prev + 1);
      }
    }
  }

  public ClassServerNamingServer.ServerEntry lookup(
      String serviceName, List<String> qualifiers) {
    ClassServerNamingServer.LookupRequest request =
        ClassServerNamingServer.LookupRequest.newBuilder()
            .setServiceName(serviceName)
            .addAllQualifiers(qualifiers)
            .build();

    ClassServerNamingServer.LookupResponse response = this._stub.lookup(request);

    return response.getServersList().stream()
        .filter(address -> !_cachedInactiveServers.containsKey(address))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("No servers available"));
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
    if (_cachedChannel == null || !qualifiers.containsAll(_cachedChannelQualifiers) || previousIsInactive) {
      if (_cachedChannel != null) {
        if (previousIsInactive) {
          _cachedInactiveServers.put(_cachedAddress, 0);
        }
        _cachedChannel.shutdown();
      }

      _cachedAddress = this.lookup(serviceName, qualifiers).getAddress();
      _cachedChannelQualifiers = qualifiers;
      _cachedChannel = ManagedChannelBuilder.forTarget(_cachedAddress).usePlaintext().build();
    }

    this.updateInactiveServersList();

    return _cachedChannel;
  }
}
