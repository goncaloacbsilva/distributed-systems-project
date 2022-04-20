package pt.ulisboa.tecnico.classes.student;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.TimestampsManager;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.Student;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer;
import pt.ulisboa.tecnico.classes.contract.student.StudentServiceGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** This class abstracts all the stub calls executed by the Student client */
public class StudentFrontend extends TimestampsManager {

  private final NameServerFrontend _nameServer;
  private StudentServiceGrpc.StudentServiceBlockingStub _stub;
  private final Student _student;

  /**
   * creates an instance of StudentFrontend
   *
   * @param studentID
   * @param studentName
   */
  public StudentFrontend(String studentID, String studentName) {
    super(new NameServerFrontend());
    this._nameServer = new NameServerFrontend();
    this._student = Student.newBuilder().setStudentId(studentID).setStudentName(studentName).build();
  }

  private void getNewStubWithQualifiers(List<String> qualifiers, boolean previousIsInactive) {
    this._stub = StudentServiceGrpc.newBlockingStub(_nameServer.getChannel(StudentServiceGrpc.SERVICE_NAME, qualifiers, previousIsInactive));
  }

  /**
   * Sends an enroll request to the server and enrolls a student if is possible * prints the
   * response code
   */
  public ResponseCode enrollStudent() throws StatusRuntimeException, ResponseException {
    getNewStubWithQualifiers(new ArrayList<>(), false);

    StudentClassServer.EnrollRequest request = StudentClassServer.EnrollRequest.newBuilder().setStudent(this._student).build();
    StudentClassServer.EnrollResponse response = _stub.enroll(request);

    if (response.getCode() == ResponseCode.INACTIVE_SERVER) {
      getNewStubWithQualifiers(new ArrayList<>(), true);
      return this.enrollStudent();
    }

    return response.getCode();
  }

  /**
   * Sends a list request to the server and returns the internal class state. In case of error,
   * throws the ResponseCode as a ResponseException
   *
   * @return
   * @throws ResponseException
   */
  public ClassState list() throws StatusRuntimeException, ResponseException {
    getNewStubWithQualifiers(new ArrayList<>(), false);

    StudentClassServer.ListClassRequest.Builder request = StudentClassServer.ListClassRequest.newBuilder();

    request.putAllTimestamps(this.getTimestamps());

    StudentClassServer.ListClassResponse response = _stub.listClass(request.build());

    if (response.getCode() == ResponseCode.INACTIVE_SERVER) {
      getNewStubWithQualifiers(new ArrayList<>(), true);
      return this.list();
    }
    else if (response.getCode() == ResponseCode.OK) {
      this.setTimestamps(response.getTimestampsMap());
      return response.getClassState();
    }
    else {
      throw new ResponseException(response.getCode());
    }
  }
}
