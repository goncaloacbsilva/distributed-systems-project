package pt.ulisboa.tecnico.classes.admin;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.DumpRequest;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.DumpResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

/** This class abstracts all the stub calls executed by the Admin client */
public class AdminFrontend {

  private ManagedChannel _channel;
  private final NameServerFrontend _nameServer;


  /**
   * Creates an instance of AdminFrontend
   *
   * @see AdminFrontend
   */
  public AdminFrontend() {
    this._nameServer = new NameServerFrontend();
  }

  private AdminServiceGrpc.AdminServiceBlockingStub getNewStubWithQualifiers(List<String> qualifiers) {
    String[] address = _nameServer.lookup(AdminServiceGrpc.SERVICE_NAME, qualifiers).getAddress().split(":");
    this._channel = ManagedChannelBuilder.forAddress(address[0], Integer.valueOf(address[1])).idleTimeout(2, TimeUnit.SECONDS).usePlaintext().build();
    return AdminServiceGrpc.newBlockingStub(this._channel);
  }

  /**
   * Sends a dump request to the server and returns the internal class state. In case of error,
   * throws the ResponseCode as a ResponseException
   *
   * @return ClassesDefinitions.ClassState
   * @throws StatusRuntimeException
   * @throws ResponseException
   */
  public ClassState dump() throws StatusRuntimeException, ResponseException {
    AdminServiceGrpc.AdminServiceBlockingStub stub = getNewStubWithQualifiers(List.of("primary"));
    DumpResponse response = stub.dump(DumpRequest.getDefaultInstance());

    if (response.getCode() == ResponseCode.OK) {
      return response.getClassState();
    } else {
      throw new ResponseException(response.getCode());
    }
  }
}
