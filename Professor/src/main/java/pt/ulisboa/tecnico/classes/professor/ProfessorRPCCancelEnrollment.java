package pt.ulisboa.tecnico.classes.professor;

import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.RPCFrontendCall;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;

import java.util.List;

/**
 * This class abstracts the professor cancelEnrollment RPC call
 */
public class ProfessorRPCCancelEnrollment extends RPCFrontendCall {

  private final NameServerFrontend _nameServer;
  private ProfessorServiceGrpc.ProfessorServiceBlockingStub _stub;
  private ProfessorClassServer.CancelEnrollmentResponse _response;
  private String _studentId;

  public ProfessorRPCCancelEnrollment(List<String> qualifiers, NameServerFrontend nameServer) {
    super(qualifiers);
    _nameServer = nameServer;
  }

  public void setStudentId(String studentId) {
    this._studentId = studentId;
  }

  public void createStubForRequest(List<String> qualifiers, boolean previousIsInactive) {
    this._stub =
        ProfessorServiceGrpc.newBlockingStub(
            _nameServer.getChannel(
                ProfessorServiceGrpc.SERVICE_NAME, qualifiers, previousIsInactive));
  }

  public ClassesDefinitions.ResponseCode requestCall() {
    ProfessorClassServer.CancelEnrollmentRequest request =
        ProfessorClassServer.CancelEnrollmentRequest.newBuilder()
            .setStudentId(this._studentId)
            .build();
    this._response = _stub.cancelEnrollment(request);

    return this._response.getCode();
  }

  public ProfessorClassServer.CancelEnrollmentResponse getResponse() {
    return _response;
  }
}
