package pt.ulisboa.tecnico.classes.admin;

import pt.ulisboa.tecnico.classes.RPCFrontendCall;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;

import java.util.List;

/** Deactivate command RPC Class */
public class AdminRPCDeactivate extends RPCFrontendCall {

  private AdminServiceGrpc.AdminServiceBlockingStub _stub;
  private AdminClassServer.DeactivateResponse _response;

  /**
   * Creates an instance of AdminRPCDeactivate
   *
   * @see AdminRPCDeactivate
   * @param qualifiers server qualifiers list
   */
  public AdminRPCDeactivate(List<String> qualifiers) {
    super(qualifiers);
  }

  public void createStubForRequest(List<String> qualifiers, boolean previousIsInactive) {
    // Admin has a different behavior than the 'default' clients so this method is not used
    // The service stub is set using setStub method
  }

  public ClassesDefinitions.ResponseCode requestCall() {
    this._response = _stub.deactivate(AdminClassServer.DeactivateRequest.getDefaultInstance());

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
   * @return AdminClassServer.DeactivateResponse
   */
  public AdminClassServer.DeactivateResponse getResponse() {
    return _response;
  }
}
