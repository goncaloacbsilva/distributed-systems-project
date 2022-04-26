package pt.ulisboa.tecnico.classes.professor;

import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.RPCFrontendCall;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;

import java.util.HashMap;
import java.util.List;

/**This class abstracts the professor list RPC call*/
public class ProfessorRPCList extends RPCFrontendCall {

    private ProfessorServiceGrpc.ProfessorServiceBlockingStub _stub;
    private final NameServerFrontend _nameServer;
    private ProfessorClassServer.ListClassResponse _response;
    private HashMap<String, Integer> _timestamps;


    public ProfessorRPCList(List<String> qualifiers, NameServerFrontend nameServer) {
        super(qualifiers);
        _nameServer = nameServer;
    }

    public void setTimestamps(HashMap<String, Integer> _timestamps) {
        this._timestamps = new HashMap<>(_timestamps);
    }

    public void createStubForRequest(List<String> qualifiers, boolean previousIsInactive) {
        this._stub = ProfessorServiceGrpc.newBlockingStub(_nameServer.getChannel(ProfessorServiceGrpc.SERVICE_NAME, qualifiers, previousIsInactive));
    }

    public ClassesDefinitions.ResponseCode requestCall() {
        ProfessorClassServer.ListClassRequest.Builder request = ProfessorClassServer.ListClassRequest.newBuilder();
        request.putAllTimestamps(this._timestamps);

        this._response = _stub.listClass(request.build());

        return this._response.getCode();
    }

    public ProfessorClassServer.ListClassResponse getResponse() {
        return _response;
    }
}
