package pt.ulisboa.tecnico.classes.admin;

import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.RPCFrontendCall;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;

import java.util.List;

public class AdminRPCActivate extends RPCFrontendCall {

    private AdminServiceGrpc.AdminServiceBlockingStub _stub;

    private final NameServerFrontend _nameServer;

    private AdminClassServer.ActivateResponse _response;

    public AdminRPCActivate(List<String> qualifiers, NameServerFrontend nameServer) {
        super(qualifiers);
        _nameServer = nameServer;
    }


    public void createStubForRequest(List<String> qualifiers, boolean previousIsInactive) {
        // Admin has a different behavior
    }

    public void setStub(AdminServiceGrpc.AdminServiceBlockingStub stub) {
        this._stub = stub;
    }

    public ClassesDefinitions.ResponseCode requestCall() throws StatusRuntimeException {
        this._response = _stub.activate(AdminClassServer.ActivateRequest.getDefaultInstance());

        return this._response.getCode();
    }

    public AdminClassServer.ActivateResponse getResponse() {
        return _response;
    }
}
