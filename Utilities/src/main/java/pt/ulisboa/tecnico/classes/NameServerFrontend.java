package pt.ulisboa.tecnico.classes;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer;
import pt.ulisboa.tecnico.classes.contract.naming.NamingServerServiceGrpc;

import java.util.List;
import java.util.Optional;

public class NameServerFrontend {

  private final NamingServerServiceGrpc.NamingServerServiceBlockingStub _stub;

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

  public ClassServerNamingServer.ServerEntry lookup(String serviceName, List<String> qualifiers) {
    ClassServerNamingServer.LookupRequest request =
        ClassServerNamingServer.LookupRequest.newBuilder()
            .setServiceName(serviceName)
            .addAllQualifiers(qualifiers)
            .build();

    ClassServerNamingServer.LookupResponse response = this._stub.lookup(request);

    return Optional.ofNullable(response.getServersList())
        .orElseThrow(
            () -> new RuntimeException("Could not find a server that match the supplied args"))
        .get(0);
  }
}
