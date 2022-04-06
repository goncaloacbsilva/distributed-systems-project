package pt.ulisboa.tecnico.classes.student;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.Student;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer;
import pt.ulisboa.tecnico.classes.contract.student.StudentServiceGrpc;

import java.util.List;
import java.util.concurrent.TimeUnit;

/** This class abstracts all the stub calls executed by the Student client */
public class StudentFrontend {

  private final NameServerFrontend _nameServer;
  private ManagedChannel _channel;
  private final Student _student;

  /**
   * creates an instance of StudentFrontend
   *
   * @param studentID
   * @param studentName
   */
  public StudentFrontend(String studentID, String studentName) {
    this._nameServer = new NameServerFrontend();
    this._student = Student.newBuilder().setStudentId(studentID).setStudentName(studentName).build();
  }

  private StudentServiceGrpc.StudentServiceBlockingStub getNewStubWithQualifiers(List<String> qualifiers) {
    this._channel = _nameServer.getChannel(StudentServiceGrpc.SERVICE_NAME, qualifiers);
    return StudentServiceGrpc.newBlockingStub(this._channel);
  }

  /**
   * Sends an enroll request to the server and enrolls a student if is possible * prints the
   * response code
   */
  public ResponseCode enrollStudent() throws StatusRuntimeException, ResponseException {
    StudentServiceGrpc.StudentServiceBlockingStub stub = getNewStubWithQualifiers(List.of("primary"));

    StudentClassServer.EnrollRequest request = StudentClassServer.EnrollRequest.newBuilder().setStudent(this._student).build();
    StudentClassServer.EnrollResponse response = stub.enroll(request);

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
    StudentServiceGrpc.StudentServiceBlockingStub stub = getNewStubWithQualifiers(List.of("primary"));
    StudentClassServer.ListClassResponse response = stub.listClass(StudentClassServer.ListClassRequest.getDefaultInstance());

    if (response.getCode() == ResponseCode.OK) {
      return response.getClassState();
    } else {
      throw new ResponseException(response.getCode());
    }
  }
}
