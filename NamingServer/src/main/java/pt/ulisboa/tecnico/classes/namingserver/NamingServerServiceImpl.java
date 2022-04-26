package pt.ulisboa.tecnico.classes.namingserver;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.*;
import pt.ulisboa.tecnico.classes.contract.naming.NamingServerServiceGrpc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.grpc.Status.INVALID_ARGUMENT;

public class NamingServerServiceImpl extends NamingServerServiceGrpc.NamingServerServiceImplBase {

  private static final Logger LOGGER = Logger.getLogger(NamingServerServiceImpl.class.getName());
  private final NamingServices _services;

  public NamingServerServiceImpl(NamingServices services, boolean enableDebug) {
    super();
    this._services = services;
    if (!enableDebug) {
      LOGGER.setLevel(Level.OFF);
    }

    LOGGER.info("Started with debug mode enabled");
  }

  /**
   * Registers a new service for a given server identified by its address
   *
   * @param request
   * @param responseObserver
   */
  @Override
  public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
    if (request.getAddress().contains(":")) {
      String[] addressParser = request.getAddress().split(":");
      try {
        Integer.valueOf(addressParser[1]);
      } catch (NumberFormatException ex) {
        responseObserver.onError(
            INVALID_ARGUMENT
                .withDescription("Not a valid server address: " + request.getAddress())
                .asRuntimeException());
      }

      ServerEntry server =
          ServerEntry.newBuilder()
              .setAddress(request.getAddress())
              .addAllQualifiers(request.getQualifiersList())
              .build();
      String serviceName = request.getServiceName();

      LOGGER.info(
          "Registering "
              + server.getAddress()
              + " with qualifiers: "
              + Arrays.toString(request.getQualifiersList().toArray())
              + " at "
              + serviceName);

      this._services.registerServer(serviceName, server);

      responseObserver.onNext(RegisterResponse.getDefaultInstance());
      responseObserver.onCompleted();
    } else {
      responseObserver.onError(
          INVALID_ARGUMENT
              .withDescription("Not a valid server address: " + request.getAddress())
              .asRuntimeException());
    }
  }

  /**
   * returns all eligible servers given required server qualifiers
   *
   * @param request
   * @param responseObserver
   */
  @Override
  public void lookup(LookupRequest request, StreamObserver<LookupResponse> responseObserver) {
    LOGGER.info(
        "Searching for: "
            + Arrays.toString(request.getQualifiersList().toArray())
            + " at "
            + request.getServiceName());
    List<ServerEntry> results;

    results = this._services.lookupServers(request.getServiceName(), request.getQualifiersList());

    LOGGER.info("Got " + results.size() + " records \n" + results);

    responseObserver.onNext(LookupResponse.newBuilder().addAllServers(results).build());
    responseObserver.onCompleted();
  }

  /**
   * returns all servers registered on name server
   *
   * @param request
   * @param responseObserver
   */
  @Override
  public void list(
      ClassServerNamingServer.ListRequest request,
      StreamObserver<ClassServerNamingServer.ListResponse> responseObserver) {
    LOGGER.info("Listing available servers...");

    HashSet<ServerEntry> servers = new HashSet<ServerEntry>();
    ClassServerNamingServer.ListResponse.Builder response =
        ClassServerNamingServer.ListResponse.newBuilder();

    for (ServiceEntry service : _services.getServices().values()) {
      servers.addAll(service.getServers());
    }

    response.addAllServers(servers);

    LOGGER.info("Got " + servers.size() + " records \n" + servers);

    responseObserver.onNext(response.build());
    responseObserver.onCompleted();
  }

  /**
   * deletes a given server for a given service
   *
   * @param request
   * @param responseObserver
   */
  @Override
  public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
    LOGGER.info("Removing " + request.getAddress() + " from " + request.getServiceName());

    this._services.deleteServer(request.getServiceName(), request.getAddress());

    responseObserver.onNext(DeleteResponse.getDefaultInstance());
    responseObserver.onCompleted();
  }
}
