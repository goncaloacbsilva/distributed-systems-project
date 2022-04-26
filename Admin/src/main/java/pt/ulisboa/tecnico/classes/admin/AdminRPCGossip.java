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

/**
 * Gossip command RPC Class
 */
public class AdminRPCGossip extends RPCFrontendCall {

    private AdminServiceGrpc.AdminServiceBlockingStub _stub;
    private AdminClassServer.GossipResponse _response;

    /**
     * Creates an instance of AdminRPCGossip
     *
     * @see AdminRPCGossip
     *
     * @param qualifiers server qualifiers list
     */
    public AdminRPCGossip(List<String> qualifiers) {
        super(qualifiers);
    }

    public void createStubForRequest(List<String> qualifiers, boolean previousIsInactive) {
        // Admin has a different behavior than the 'default' clients so this method is not used
        // The service stub is set using setStub method
    }

    public ClassesDefinitions.ResponseCode requestCall() {
        this._response = _stub.gossip(AdminClassServer.GossipRequest.getDefaultInstance());

        return this._response.getCode();
    }

    /**
     * Sets the stub for the RPC call
     * @param stub AdminServiceBlockingStub
     */
    public void setStub(AdminServiceGrpc.AdminServiceBlockingStub _stub) {
        this._stub = _stub;
    }

    /**
     * Returns the response of the RPC call.
     * It should only be called after the exec() method otherwise its value is null.
     *
     * @return AdminClassServer.GossipResponse
     */
    public AdminClassServer.GossipResponse getResponse() {
        return _response;
    }
}
