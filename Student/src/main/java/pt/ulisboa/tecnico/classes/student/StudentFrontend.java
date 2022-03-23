package pt.ulisboa.tecnico.classes.student;


import io.grpc.ManagedChannel;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer;
import pt.ulisboa.tecnico.classes.contract.student.StudentServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;

public class StudentFrontend {

    private final StudentServiceGrpc.StudentServiceBlockingStub _stub;
    private final ClassesDefinitions.Student _student;

    public StudentFrontend(ManagedChannel channel, String studentID, String studentName){

        _stub = StudentServiceGrpc.newBlockingStub(channel);
        this._student = ClassesDefinitions.Student.newBuilder().setStudentId(studentID).setStudentName(studentName).build();

    }

    public void EnrollStudent(){
        StudentClassServer.EnrollRequest request = StudentClassServer.EnrollRequest.newBuilder().setStudent(this._student).build();
        StudentClassServer.EnrollResponse response = this._stub.enroll(request);
        System.out.println(Stringify.format(response.getCode()));

    }

}
