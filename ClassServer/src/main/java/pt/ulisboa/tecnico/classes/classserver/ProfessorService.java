package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Professor Service remote procedure calls
 */
public class ProfessorService extends ProfessorServiceGrpc.ProfessorServiceImplBase {

    private ClassStateWrapper _classObj;
    private static final Logger LOGGER = Logger.getLogger(AdminService.class.getName());

    /**
     * Creates an instance of ProfessorService
     *
     * @param classObj    shared class state object
     * @param enableDebug debug flag
     */
    public ProfessorService(ClassStateWrapper classObj, boolean enableDebug) {

        _classObj = classObj;
        if (!enableDebug) {
            LOGGER.setLevel(Level.OFF);
        }

        LOGGER.info("Started with debug mode enabled");
    }

    /**
     * "openEnrollments" remote procedure call. Receives openEnrollmentRequest from the professor client
     * changes the class state and sends the adequate response code through a StreamObserver
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public synchronized void openEnrollments(ProfessorClassServer.OpenEnrollmentsRequest request, StreamObserver<ProfessorClassServer.OpenEnrollmentsResponse> responseObserver) {
        LOGGER.info("Received openEnrollments request");
        ProfessorClassServer.OpenEnrollmentsResponse.Builder response = ProfessorClassServer.OpenEnrollmentsResponse.newBuilder();
        int capacity = request.getCapacity();

        if (this._classObj.getClassState().getOpenEnrollments()) {
            response.setCode(ResponseCode.ENROLLMENTS_ALREADY_OPENED);

        } else if (this._classObj.getClassState().getEnrolledList().size() >= capacity) {
            response.setCode(ResponseCode.FULL_CLASS);

        } else {
            LOGGER.info("Building new class state");
            ClassesDefinitions.ClassState.Builder classStateBuilder = this._classObj.getClassState().toBuilder();
            classStateBuilder.setOpenEnrollments(true);
            classStateBuilder.setCapacity(capacity);
            this._classObj.setClassState(classStateBuilder.build());
            LOGGER.info("Class state built");

            response.setCode(ResponseCode.OK);
            LOGGER.info("Set response as OK");
        }

        LOGGER.info("Sending openEnrollments response");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    /**
     * "closeEnrollments" remote procedure call. Receives closeEnrollmentRequest from the professor client
     * changes the class state and sends the adequate response code through a StreamObserver
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public synchronized void closeEnrollments(ProfessorClassServer.CloseEnrollmentsRequest request, StreamObserver<ProfessorClassServer.CloseEnrollmentsResponse> responseObserver) {
        LOGGER.info("Received closeEnrollments request");
        ProfessorClassServer.CloseEnrollmentsResponse.Builder response = ProfessorClassServer.CloseEnrollmentsResponse.newBuilder();

        if (!this._classObj.getClassState().getOpenEnrollments()) {
            response.setCode(ResponseCode.ENROLLMENTS_ALREADY_CLOSED);

        } else {
            LOGGER.info("Building new class state");
            ClassesDefinitions.ClassState.Builder classStateBuilder = this._classObj.getClassState().toBuilder();
            classStateBuilder.setOpenEnrollments(false);
            this._classObj.setClassState(classStateBuilder.build());

            response.setCode(ResponseCode.OK);
            LOGGER.info("Set response as OK");
        }

        LOGGER.info("Sending closeEnrollments response");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    /**
     * "list" remote procedure call. Receives listRequest from the professor client and sends the internal
     * class state through a StreamObserver
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void listClass(ProfessorClassServer.ListClassRequest request, StreamObserver<ProfessorClassServer.ListClassResponse> responseObserver) {

        LOGGER.info("Received dump request");
        ProfessorClassServer.ListClassResponse.Builder response = ProfessorClassServer.ListClassResponse.newBuilder();
        responseObserver.onNext(response.setClassState(this._classObj.getClassState()).build());
        responseObserver.onCompleted();
    }

    /**
     * "cancelEnrollment" remote procedure call. Receives cancelEnrollmentRequest from the professor client
     * changes the class state and sends the adequate response code through a StreamObserver
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public synchronized void cancelEnrollment(ProfessorClassServer.CancelEnrollmentRequest request, StreamObserver<ProfessorClassServer.CancelEnrollmentResponse> responseObserver) {
        LOGGER.info("Received cancelEnrollment request");
        ProfessorClassServer.CancelEnrollmentResponse.Builder response = ProfessorClassServer.CancelEnrollmentResponse.newBuilder();
        ClassesDefinitions.ClassState currentClassState = this._classObj.getClassState();
        String studentToRemoveId = request.getStudentId();

        LOGGER.info("Searching for student");

        ClassesDefinitions.Student studentToDiscard = currentClassState.getEnrolledList()
                .stream()
                .filter(student -> studentToRemoveId.equals(student.getStudentId()))
                .findAny()
                .orElse(null);

        if (studentToDiscard != null) {
            LOGGER.info("Removing student from enrolled and adding to discarded");
            LOGGER.info("Building new class state");
            ClassesDefinitions.ClassState.Builder classStateBuilder = currentClassState.toBuilder();

            classStateBuilder.removeEnrolled(currentClassState.getEnrolledList().indexOf(studentToDiscard));
            classStateBuilder.addDiscarded(studentToDiscard);

            this._classObj.setClassState(classStateBuilder.build());
            LOGGER.info("Class state built");

            response.setCode(ResponseCode.OK);
            LOGGER.info("Set response as OK");
        } else {
            response.setCode(ResponseCode.NON_EXISTING_STUDENT);
            LOGGER.info("Set response as non existing student");
        }

        LOGGER.info("Sending cancelEnrollment response");
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
