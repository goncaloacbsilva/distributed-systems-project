package pt.ulisboa.tecnico.classes.student;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer;
import pt.ulisboa.tecnico.classes.contract.student.StudentServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;

import java.util.List;
import java.util.concurrent.TimeUnit;

/** This class abstracts all the stub calls executed by the Student client */
public class StudentFrontend {

  private final NameServerFrontend _nameServer;
  private ManagedChannel _channel;
  private final ClassesDefinitions.Student _student;

  /**
   * creates an instance of StudentFrontend
   *
   * @param channel
   * @param studentID
   * @param studentName
   */
  public StudentFrontend(String studentID, String studentName) {

    this._nameServer = new NameServerFrontend();
    this._student =
        ClassesDefinitions.Student.newBuilder()
            .setStudentId(studentID)
            .setStudentName(studentName)
            .build();
  }

  private StudentServiceGrpc.StudentServiceBlockingStub getNewStubWithQualifiers(List<String> qualifiers) {
    String[] address = _nameServer.lookup(StudentServiceGrpc.SERVICE_NAME, qualifiers).getAddress().split(":");
    this._channel = ManagedChannelBuilder.forAddress(address[0], Integer.valueOf(address[1])).idleTimeout(2, TimeUnit.SECONDS).usePlaintext().build();
    return StudentServiceGrpc.newBlockingStub(this._channel);
  }

  /**
   * Sends an enroll request to the server and enrolls a student if is possible * prints the
   * response code
   */
  public void EnrollStudent() {
    StudentServiceGrpc.StudentServiceBlockingStub stub = getNewStubWithQualifiers(List.of("primary"));
    try {
      StudentClassServer.EnrollRequest request =
          StudentClassServer.EnrollRequest.newBuilder().setStudent(this._student).build();
      StudentClassServer.EnrollResponse response = stub.enroll(request);
      System.out.println(Stringify.format(response.getCode()));
    } catch (RuntimeException e) {
      e.printStackTrace();
    }
  }

  /**
   * Sends a list request to the server and returns the internal class state. In case of error,
   * throws the ResponseCode as a ResponseException
   *
   * @return
   * @throws ResponseException
   */
  public ClassesDefinitions.ClassState List() throws ResponseException {
    StudentServiceGrpc.StudentServiceBlockingStub stub = getNewStubWithQualifiers(List.of("primary"));
    StudentClassServer.ListClassResponse response =
        stub.listClass(StudentClassServer.ListClassRequest.newBuilder().build());

    if (response.getCode().getNumber() == ClassesDefinitions.ResponseCode.OK_VALUE)
      return response.getClassState();
    else {
      throw new ResponseException(response.getCode());
    }
  }
}
