package pt.ulisboa.tecnico.classes.student;

import io.grpc.ManagedChannel;
import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer;
import pt.ulisboa.tecnico.classes.contract.student.StudentServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;

/** This class abstracts all the stub calls executed by the Student client */
public class StudentFrontend {

  private final StudentServiceGrpc.StudentServiceBlockingStub _stub;
  private final ClassesDefinitions.Student _student;

  /**
   * creates an instance of StudentFrontend
   *
   * @param channel
   * @param studentID
   * @param studentName
   */
  public StudentFrontend(ManagedChannel channel, String studentID, String studentName) {

    this._stub = StudentServiceGrpc.newBlockingStub(channel);
    this._student =
        ClassesDefinitions.Student.newBuilder()
            .setStudentId(studentID)
            .setStudentName(studentName)
            .build();
  }

  /**
   * Sends an enroll request to the server and enrolls a student if is possible * prints the
   * response code
   */
  public void EnrollStudent() {
    try {
      StudentClassServer.EnrollRequest request =
          StudentClassServer.EnrollRequest.newBuilder().setStudent(this._student).build();
      StudentClassServer.EnrollResponse response = this._stub.enroll(request);
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

    StudentClassServer.ListClassResponse response =
        this._stub.listClass(StudentClassServer.ListClassRequest.newBuilder().build());

    if (response.getCode().getNumber() == ClassesDefinitions.ResponseCode.OK_VALUE)
      return response.getClassState();
    else {
      throw new ResponseException(response.getCode());
    }
  }
}
