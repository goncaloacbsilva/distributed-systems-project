package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer;

public class ProfessorService extends ProfessorServiceGrpc.ProfessorServiceImplBase {

    private ClassObject class_state;

    public ProfessorService(ClassObject classObj) {
        class_state = classObj;
    }

    @Override
    public synchronized void openEnrollments(ProfessorClassServer.OpenEnrollmentsRequest request, StreamObserver<ProfessorClassServer.OpenEnrollmentsResponse> responseObserver) {
        ProfessorClassServer.OpenEnrollmentsResponse.Builder response = ProfessorClassServer.OpenEnrollmentsResponse.newBuilder();
        if (this.class_state.getClassState().getOpenEnrollments()) {
            response.setCodeValue(ClassesDefinitions.ResponseCode.ENROLLMENTS_ALREADY_OPENED_VALUE);
        } else if (this.class_state.getClassState().getEnrolledList().size() >= request.getCapacity()) {
            response.setCodeValue(ClassesDefinitions.ResponseCode.FULL_CLASS_VALUE);
        } else {
            ClassesDefinitions.ClassState.Builder classStateBuilder = ClassesDefinitions.ClassState.newBuilder();
            classStateBuilder.setCapacity(request.getCapacity());
            classStateBuilder.setOpenEnrollments(true);
            classStateBuilder.addAllEnrolled(this.class_state.getClassState().getEnrolledList());
            classStateBuilder.addAllDiscarded(this.class_state.getClassState().getDiscardedList());
            this.class_state.set_classState(classStateBuilder.build());

            response.setCodeValue(ClassesDefinitions.ResponseCode.OK_VALUE);

        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public synchronized void closeEnrollments(ProfessorClassServer.CloseEnrollmentsRequest request, StreamObserver<ProfessorClassServer.CloseEnrollmentsResponse> responseObserver) {

        ClassesDefinitions.ClassState.Builder classStateBuilder = ClassesDefinitions.ClassState.newBuilder();
        classStateBuilder.setCapacity(this.class_state.getClassState().getCapacity());
        classStateBuilder.setOpenEnrollments(false);
        classStateBuilder.addAllEnrolled(this.class_state.getClassState().getEnrolledList());
        classStateBuilder.addAllDiscarded(this.class_state.getClassState().getDiscardedList());
        this.class_state.set_classState(classStateBuilder.build());

        ProfessorClassServer.CloseEnrollmentsResponse.Builder response = ProfessorClassServer.CloseEnrollmentsResponse.newBuilder();
        response.setCodeValue(ClassesDefinitions.ResponseCode.OK_VALUE);

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void listClass(ProfessorClassServer.ListClassRequest request, StreamObserver<ProfessorClassServer.ListClassResponse> responseObserver) {
        ProfessorClassServer.ListClassResponse.Builder response = ProfessorClassServer.ListClassResponse.newBuilder();
        response.setClassState(this.class_state.getClassState());
        response.setCodeValue(ClassesDefinitions.ResponseCode.OK_VALUE);

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public synchronized void cancelEnrollment(ProfessorClassServer.CancelEnrollmentRequest request, StreamObserver<ProfessorClassServer.CancelEnrollmentResponse> responseObserver) {
        ProfessorClassServer.CancelEnrollmentResponse.Builder response = ProfessorClassServer.CancelEnrollmentResponse.newBuilder();
        String studentToRemoveId = request.getStudentId();

        ClassesDefinitions.ClassState.Builder classStateBuilder = ClassesDefinitions.ClassState.newBuilder();
        classStateBuilder.setCapacity(this.class_state.getClassState().getCapacity());
        classStateBuilder.setOpenEnrollments(this.class_state.getClassState().getOpenEnrollments());

        int studentToRemoveIndex = -1;
        for (int i = 0; i < this.class_state.getClassState().getEnrolledList().size(); i++) {
            if (this.class_state.getClassState().getEnrolled(i).getStudentId().equals(studentToRemoveId))
                studentToRemoveIndex = i;
            classStateBuilder.setEnrolled(i, this.class_state.getClassState().getEnrolled(i));
        }

        classStateBuilder.addAllDiscarded(this.class_state.getClassState().getDiscardedList());
        ClassesDefinitions.Student studentToDiscard;

        if (studentToRemoveIndex != -1) {
            studentToDiscard = classStateBuilder.getEnrolled(studentToRemoveIndex);
            classStateBuilder.removeEnrolled(studentToRemoveIndex);
            classStateBuilder.addDiscarded(studentToDiscard);
            this.class_state.set_classState(classStateBuilder.build());
            response.setCodeValue(ClassesDefinitions.ResponseCode.OK_VALUE);
        } else {
            response.setCodeValue(ClassesDefinitions.ResponseCode.NON_EXISTING_STUDENT_VALUE);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();

    }
}
