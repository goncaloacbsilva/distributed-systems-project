package pt.ulisboa.tecnico.classes.admin;

import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.classes.RPCFrontendCall;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;

import java.util.List;

/** Activate command RPC Class */
public class AdminRPCActivate extends RPCFrontendCall {
  private AdminServiceGrpc.AdminServiceBlockingStub _stub;
  private AdminClassServer.ActivateResponse _response;

  /**
   * Creates an instance of AdminRPCActivate
   *
   * @see AdminRPCActivate
   * @param qualifiers server qualifiers list
   */
  public AdminRPCActivate(List<String> qualifiers) {
    super(qualifiers);
  }

  public void createStubForRequest(List<String> qualifiers, boolean previousIsInactive) {
    // Admin has a different behavior than the 'default' clients so this method is not used
    // The service stub is set using setStub method
  }

  public ClassesDefinitions.ResponseCode requestCall() throws StatusRuntimeException {
    this._response = _stub.activate(AdminClassServer.ActivateRequest.getDefaultInstance());

    return this._response.getCode();
  }

  /**
   * Sets the stub for the RPC call
   *
   * @param stub AdminServiceBlockingStub
   */
  public void setStub(AdminServiceGrpc.AdminServiceBlockingStub stub) {
    this._stub = stub;
  }

  /**
   * Returns the response of the RPC call. It should only be called after the exec() method
   * otherwise its value is null.
   *
   * @return AdminClassServer.ActivateResponse
   */
  public AdminClassServer.ActivateResponse getResponse() {
    return _response;
  }
}
