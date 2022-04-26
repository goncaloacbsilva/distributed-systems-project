package pt.ulisboa.tecnico.classes.student;

import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.RPCFrontendCall;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer;
import pt.ulisboa.tecnico.classes.contract.student.StudentServiceGrpc;

import java.util.HashMap;
import java.util.List;

/** This class abstracts the student list RPC call */
public class StudentRPCList extends RPCFrontendCall {

  private final NameServerFrontend _nameServer;
  private StudentServiceGrpc.StudentServiceBlockingStub _stub;
  private StudentClassServer.ListClassResponse _response;
  private HashMap<String, Integer> _timestamps;

  public StudentRPCList(List<String> qualifiers, NameServerFrontend nameServer) {
    super(qualifiers);
    _nameServer = nameServer;
  }

  public void setTimestamps(HashMap<String, Integer> _timestamps) {
    this._timestamps = new HashMap<>(_timestamps);
  }

  public void createStubForRequest(List<String> qualifiers, boolean previousIsInactive) {
    this._stub =
        StudentServiceGrpc.newBlockingStub(
            _nameServer.getChannel(
                StudentServiceGrpc.SERVICE_NAME, qualifiers, previousIsInactive));
  }

  public ClassesDefinitions.ResponseCode requestCall() {
    StudentClassServer.ListClassRequest.Builder request =
        StudentClassServer.ListClassRequest.newBuilder();
    request.putAllTimestamps(this._timestamps);

    this._response = _stub.listClass(request.build());

    return this._response.getCode();
  }

  public StudentClassServer.ListClassResponse getResponse() {
    return _response;
  }
}
