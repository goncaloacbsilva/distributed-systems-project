package pt.ulisboa.tecnico.classes.admin;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;

/** This class abstracts all the stub calls executed by the Admin client */
public class AdminFrontend {

  private final AdminServiceGrpc.AdminServiceBlockingStub stub;

  /**
   * Creates an instance of AdminFrontend
   *
   * @param channel gRPC channel
   * @see AdminFrontend
   */
  public AdminFrontend(ManagedChannel channel) {
    stub = AdminServiceGrpc.newBlockingStub(channel);
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
    AdminClassServer.DumpResponse response =
        stub.dump(AdminClassServer.DumpRequest.getDefaultInstance());
    if (response.getCode() == ResponseCode.OK) {
      return response.getClassState();
    } else {
      throw new ResponseException(response.getCode());
    }
  }
}
