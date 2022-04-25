package pt.ulisboa.tecnico.classes.professor;

import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.RPCFrontendCall;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;

import java.util.HashMap;
import java.util.List;

public class ProfessorRPCOpenEnrollments extends RPCFrontendCall {

    private ProfessorServiceGrpc.ProfessorServiceBlockingStub _stub;
    private final NameServerFrontend _nameServer;
    private ProfessorClassServer.OpenEnrollmentsResponse _response;
    private int _capacity;


    public ProfessorRPCOpenEnrollments(List<String> qualifiers, NameServerFrontend nameServer) {
        super(qualifiers);
        _nameServer = nameServer;
    }

    public void setCapacity(int capacity) {
        this._capacity = capacity;
    }

    public void createStubForRequest(List<String> qualifiers, boolean previousIsInactive) {
        this._stub = ProfessorServiceGrpc.newBlockingStub(_nameServer.getChannel(ProfessorServiceGrpc.SERVICE_NAME, qualifiers, previousIsInactive));
    }

    public ClassesDefinitions.ResponseCode requestCall() {
        ProfessorClassServer.OpenEnrollmentsRequest request = ProfessorClassServer.OpenEnrollmentsRequest.newBuilder().setCapacity(this._capacity).build();

        this._response = _stub.openEnrollments(request);

        return this._response.getCode();
    }

    public ProfessorClassServer.OpenEnrollmentsResponse getResponse() {
        return _response;
    }
}
