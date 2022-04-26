package pt.ulisboa.tecnico.classes.professor;

import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.RPCFrontendCall;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;

import java.util.List;

/**This class abstracts the professor closeEnrollments RPC call*/
public class ProfessorRPCCloseEnrollments extends RPCFrontendCall {

    private ProfessorServiceGrpc.ProfessorServiceBlockingStub _stub;
    private final NameServerFrontend _nameServer;
    private ProfessorClassServer.CloseEnrollmentsResponse _response;


    public ProfessorRPCCloseEnrollments(List<String> qualifiers, NameServerFrontend nameServer) {
        super(qualifiers);
        _nameServer = nameServer;
    }

    public void createStubForRequest(List<String> qualifiers, boolean previousIsInactive) {
        this._stub = ProfessorServiceGrpc.newBlockingStub(_nameServer.getChannel(ProfessorServiceGrpc.SERVICE_NAME, qualifiers, previousIsInactive));
    }

    public ClassesDefinitions.ResponseCode requestCall() {
        ProfessorClassServer.CloseEnrollmentsRequest request = ProfessorClassServer.CloseEnrollmentsRequest.getDefaultInstance();
        this._response = _stub.closeEnrollments(request);

        return this._response.getCode();
    }

    public ProfessorClassServer.CloseEnrollmentsResponse getResponse() {
        return _response;
    }
}
