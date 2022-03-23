package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer;
import pt.ulisboa.tecnico.classes.contract.student.StudentServiceGrpc;

import java.util.List;

public class StudentService extends StudentServiceGrpc.StudentServiceImplBase {

    private ClassObject object;

    public StudentService(ClassObject obj) {
        this.object = obj;
    }

    @Override
    public void enroll(StudentClassServer.EnrollRequest request, StreamObserver<StudentClassServer.EnrollResponse> responseObserver) {
        ClassesDefinitions.Student student = request.getStudent();
        int student_index = this.object.getClassState().getEnrolledList().indexOf(student);
        int enrolled = this.object.getClassState().getEnrolledList().size();
        int capacity = this.object.getClassState().getCapacity();

        if(student_index != -1){
            responseObserver.onNext(StudentClassServer.EnrollResponse.newBuilder().setCode(ClassesDefinitions.ResponseCode.STUDENT_ALREADY_ENROLLED).build());
        }
        else if(capacity < enrolled + 1){
            responseObserver.onNext(StudentClassServer.EnrollResponse.newBuilder().setCode(ClassesDefinitions.ResponseCode.FULL_CLASS).build());
        }
        else{
            ClassesDefinitions.ClassState.Builder newObject = ClassesDefinitions.ClassState.newBuilder();
            newObject.setCapacity(this.object.getClassState().getCapacity());
            newObject.setOpenEnrollments(this.object.getClassState().getOpenEnrollments());
            List<ClassesDefinitions.Student> enrolled_list = this.object.getClassState().getEnrolledList();
            enrolled_list.add(student);
            newObject.addAllEnrolled(enrolled_list);
            newObject.addAllDiscarded(this.object.getClassState().getDiscardedList());

            this.object.setClassState(newObject.build());
            responseObserver.onNext(StudentClassServer.EnrollResponse.newBuilder().setCode(ClassesDefinitions.ResponseCode.OK).build());

        }
        responseObserver.onCompleted();
    }

    @Override
    public void listClass(StudentClassServer.ListClassRequest request, StreamObserver<StudentClassServer.ListClassResponse> responseObserver) {

    }



}
