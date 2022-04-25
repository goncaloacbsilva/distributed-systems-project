package pt.ulisboa.tecnico.classes.admin;

import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.RPCFrontendCall;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;

import java.util.HashMap;
import java.util.List;

public class AdminRPCActivateGossip extends RPCFrontendCall {

    private AdminServiceGrpc.AdminServiceBlockingStub _stub;
    private final NameServerFrontend _nameServer;
    private AdminClassServer.ActivateGossipResponse _response;


    public AdminRPCActivateGossip(List<String> qualifiers, NameServerFrontend nameServer) {
        super(qualifiers);
        _nameServer = nameServer;
    }

    public void createStubForRequest(List<String> qualifiers, boolean previousIsInactive) {
        // Admin has a different behavior
    }

    public void setStub(AdminServiceGrpc.AdminServiceBlockingStub _stub) {
        this._stub = _stub;
    }

    public ClassesDefinitions.ResponseCode requestCall() {
        this._response = _stub.activateGossip(AdminClassServer.ActivateGossipRequest.getDefaultInstance());

        return this._response.getCode();
    }

    public AdminClassServer.ActivateGossipResponse getResponse() {
        return _response;
    }
}
