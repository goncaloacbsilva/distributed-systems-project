package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;


import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfessorService extends ProfessorServiceGrpc.ProfessorServiceImplBase {

    private ClassObject class_state;
    private static final Logger LOGGER = Logger.getLogger(AdminService.class.getName());

    public ProfessorService(ClassObject classObj, boolean enableDebug) {

        class_state = classObj;
        if (!enableDebug) {
            LOGGER.setLevel(Level.OFF);
        }

        LOGGER.info("Started with debug mode enabled");
    }

    @Override
    public synchronized void openEnrollments(ProfessorClassServer.OpenEnrollmentsRequest request, StreamObserver<ProfessorClassServer.OpenEnrollmentsResponse> responseObserver) {
        LOGGER.info("Received openEnrollments request");
        ProfessorClassServer.OpenEnrollmentsResponse.Builder response = ProfessorClassServer.OpenEnrollmentsResponse.newBuilder();
        if (this.class_state.getClassState().getOpenEnrollments()) {
            response.setCodeValue(ClassesDefinitions.ResponseCode.ENROLLMENTS_ALREADY_OPENED_VALUE);
            LOGGER.info("Set response as Enrollments already opened");
        } else if (this.class_state.getClassState().getEnrolledList().size() >= request.getCapacity()) {
            response.setCodeValue(ClassesDefinitions.ResponseCode.FULL_CLASS_VALUE);
            LOGGER.info("Set response as Full class");
        } else {
            LOGGER.info("Building new class state");
            ClassesDefinitions.ClassState.Builder classStateBuilder = ClassesDefinitions.ClassState.newBuilder();
            classStateBuilder.setCapacity(request.getCapacity());
            classStateBuilder.setOpenEnrollments(true);
            classStateBuilder.addAllEnrolled(this.class_state.getClassState().getEnrolledList());
            classStateBuilder.addAllDiscarded(this.class_state.getClassState().getDiscardedList());
            this.class_state.setClassState(classStateBuilder.build());
            LOGGER.info("class state built");

            response.setCodeValue(ClassesDefinitions.ResponseCode.OK_VALUE);
            LOGGER.info("Set response as OK");
        }

        LOGGER.info("Sending openEnrollments response");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public synchronized void closeEnrollments(ProfessorClassServer.CloseEnrollmentsRequest request, StreamObserver<ProfessorClassServer.CloseEnrollmentsResponse> responseObserver) {
        LOGGER.info("Received closeEnrollments request");
        LOGGER.info("Building new class state");
        ClassesDefinitions.ClassState.Builder classStateBuilder = ClassesDefinitions.ClassState.newBuilder();
        classStateBuilder.setCapacity(this.class_state.getClassState().getCapacity());
        classStateBuilder.setOpenEnrollments(false);
        classStateBuilder.addAllEnrolled(this.class_state.getClassState().getEnrolledList());
        classStateBuilder.addAllDiscarded(this.class_state.getClassState().getDiscardedList());
        this.class_state.setClassState(classStateBuilder.build());
        LOGGER.info("class state built");

        ProfessorClassServer.CloseEnrollmentsResponse.Builder response = ProfessorClassServer.CloseEnrollmentsResponse.newBuilder();
        response.setCodeValue(ClassesDefinitions.ResponseCode.OK_VALUE);
        LOGGER.info("Set response as OK");

        LOGGER.info("Sending closeEnrollments response");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void listClass(ProfessorClassServer.ListClassRequest request, StreamObserver<ProfessorClassServer.ListClassResponse> responseObserver) {
        LOGGER.info("Received listClass request");
        ProfessorClassServer.ListClassResponse.Builder response = ProfessorClassServer.ListClassResponse.newBuilder();
        response.setClassState(this.class_state.getClassState());
        LOGGER.info("Sending list response with class state: \n" + Stringify.format(response.getClassState()));
        response.setCodeValue(ClassesDefinitions.ResponseCode.OK_VALUE);
        LOGGER.info("Set response as OK");

        LOGGER.info("Sending listClass response");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public synchronized void cancelEnrollment(ProfessorClassServer.CancelEnrollmentRequest request, StreamObserver<ProfessorClassServer.CancelEnrollmentResponse> responseObserver) {
        LOGGER.info("Received cancelEnrollment request");
        ProfessorClassServer.CancelEnrollmentResponse.Builder response = ProfessorClassServer.CancelEnrollmentResponse.newBuilder();
        String studentToRemoveId = request.getStudentId();

        LOGGER.info("Building new class state");
        ClassesDefinitions.ClassState.Builder classStateBuilder = ClassesDefinitions.ClassState.newBuilder();
        classStateBuilder.setCapacity(this.class_state.getClassState().getCapacity());
        classStateBuilder.setOpenEnrollments(this.class_state.getClassState().getOpenEnrollments());

        LOGGER.info("Searching for student");
        int studentToRemoveIndex = -1;
        for (int i = 0; i < this.class_state.getClassState().getEnrolledCount(); i++) {
            if (this.class_state.getClassState().getEnrolled(i).getStudentId().equals(studentToRemoveId)) {
                studentToRemoveIndex = i;
            }
            classStateBuilder.addEnrolled(i, this.class_state.getClassState().getEnrolled(i));
        }
        LOGGER.info("Searching over");

        classStateBuilder.addAllDiscarded(this.class_state.getClassState().getDiscardedList());
        ClassesDefinitions.Student studentToDiscard;

        if (studentToRemoveIndex != -1) {
            LOGGER.info("Removing student from enrolled  and adding to discarded");
            studentToDiscard = classStateBuilder.getEnrolled(studentToRemoveIndex);
            classStateBuilder.removeEnrolled(studentToRemoveIndex);
            classStateBuilder.addDiscarded(studentToDiscard);
            this.class_state.setClassState(classStateBuilder.build());
            LOGGER.info("class state built");
            response.setCodeValue(ClassesDefinitions.ResponseCode.OK_VALUE);
            LOGGER.info("Set response as OK");

        } else {
            response.setCodeValue(ClassesDefinitions.ResponseCode.NON_EXISTING_STUDENT_VALUE);
            LOGGER.info("Set response as non existing student");
        }
        LOGGER.info("Sending cancelEnrollment response");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();

    }
}
