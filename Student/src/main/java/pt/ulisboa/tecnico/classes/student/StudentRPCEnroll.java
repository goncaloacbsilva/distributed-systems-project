package pt.ulisboa.tecnico.classes.student;

import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.RPCFrontendCall;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer;
import pt.ulisboa.tecnico.classes.contract.student.StudentServiceGrpc;

import java.util.HashMap;
import java.util.List;

/** This class abstracts the enroll RPC call */
public class StudentRPCEnroll extends RPCFrontendCall {

    private StudentServiceGrpc.StudentServiceBlockingStub _stub;
    private final NameServerFrontend _nameServer;
    private StudentClassServer.EnrollResponse _response;

    private ClassesDefinitions.Student _student;


    public StudentRPCEnroll(List<String> qualifiers, NameServerFrontend nameServer) {
        super(qualifiers);
        _nameServer = nameServer;
    }

    public void setStudent(ClassesDefinitions.Student student) {
        this._student = student;
    }

    public void createStubForRequest(List<String> qualifiers, boolean previousIsInactive) {
        this._stub = StudentServiceGrpc.newBlockingStub(_nameServer.getChannel(StudentServiceGrpc.SERVICE_NAME, qualifiers, previousIsInactive));
    }

    public ClassesDefinitions.ResponseCode requestCall() {
        StudentClassServer.EnrollRequest request = StudentClassServer.EnrollRequest.newBuilder().setStudent(this._student).build();

        this._response = _stub.enroll(request);

        return this._response.getCode();
    }

    public StudentClassServer.EnrollResponse getResponse() {
        return _response;
    }
}
