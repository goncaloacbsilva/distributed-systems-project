package pt.ulisboa.tecnico.classes.student;

import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.TimestampsManager;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.Student;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer;
import pt.ulisboa.tecnico.classes.contract.student.StudentServiceGrpc;

import java.util.ArrayList;
import java.util.List;

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

  /**
   * Sends an enroll request to the server and enrolls a student if is possible * prints the
   * response code
   */
  public ResponseCode enrollStudent() throws StatusRuntimeException, ResponseException {
    StudentRPCEnroll rpcCall = new StudentRPCEnroll(new ArrayList<>(), this._nameServer);

    rpcCall.setStudent(this._student);
    rpcCall.exec();

    return rpcCall.getResponse().getCode();
  }

  /**
   * Sends a list request to the server and returns the internal class state. In case of error,
   * throws the ResponseCode as a ResponseException
   *
   * @return
   * @throws ResponseException
   */
  public ClassState list() throws StatusRuntimeException, ResponseException {
    StudentRPCList rpcCall = new StudentRPCList(new ArrayList<>(), this._nameServer);

    rpcCall.setTimestamps(this.getTimestamps());
    rpcCall.exec();

    return rpcCall.getResponse().getClassState();
  }
}
