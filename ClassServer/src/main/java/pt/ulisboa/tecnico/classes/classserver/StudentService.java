package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer;
import pt.ulisboa.tecnico.classes.contract.student.StudentServiceGrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Student Service remote procedure calls
 */
public class StudentService extends StudentServiceGrpc.StudentServiceImplBase {

    private ClassStateWrapper _classObj;
    private final ReplicaManagerFrontend _replicaManger;
    private final HashMap<String, Boolean> _properties;
    private static final Logger LOGGER = Logger.getLogger(StudentService.class.getName());

    /**
     * Creates an instance of StudentService
     *
     * @param obj         shared object
     * @param enableDebug debug flag
     */
    public StudentService(ClassStateWrapper obj, boolean enableDebug, HashMap<String, Boolean> properties, ReplicaManagerFrontend replicaManger) {
        super();
        this._classObj = obj;
        _replicaManger = replicaManger;
        this._properties = properties;

        if (!enableDebug) {
            LOGGER.setLevel(Level.OFF);
        }

        LOGGER.info("Started with debug mode enabled");
    }

    /**
     * "enroll" remote procedure call. Receives enrollRequest from the student client changes the
     * class state and sends the adequate response code through a StreamObserver
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public synchronized void enroll(StudentClassServer.EnrollRequest request, StreamObserver<StudentClassServer.EnrollResponse> responseObserver) {
        StudentClassServer.EnrollResponse.Builder response = StudentClassServer.EnrollResponse.newBuilder();

        if (!_properties.get("isActive")) {
            response.setCode(ResponseCode.INACTIVE_SERVER);

        }
        else if (!_properties.get("isPrimary")) {
            response.setCode(ResponseCode.WRITING_NOT_SUPPORTED);

        } else {

            LOGGER.info("Received enroll request");
            ClassesDefinitions.Student student = request.getStudent();
            ClassesDefinitions.ClassState currentClassState = this._classObj.getClassState();

            int enrolledCount = currentClassState.getEnrolledList().size();
            int capacity = currentClassState.getCapacity();

            if (!currentClassState.getOpenEnrollments()) {
                response.setCode(ResponseCode.ENROLLMENTS_ALREADY_CLOSED);

            } else if (currentClassState.getEnrolledList().contains(student)) {
                response.setCode(ResponseCode.STUDENT_ALREADY_ENROLLED);

            } else if (capacity < enrolledCount + 1) {
                response.setCode(ResponseCode.FULL_CLASS);

            } else {
                LOGGER.info("Building new class state");
                ClassesDefinitions.ClassState.Builder classStateBuilder = currentClassState.toBuilder();
                classStateBuilder.addEnrolled(student);
                this._classObj.setClassState(classStateBuilder.build());
                LOGGER.info("Class state built");

                response.setCode(ResponseCode.OK);
                LOGGER.info("Set response as OK");
                //TODO : verificar se gossip esta ativo (entrega 3)
                _replicaManger.updateTimestamp();
            }

            LOGGER.info("Sending enroll response");
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    /**
     * "list" remote procedure call. Receives listRequest from the student client and sends the
     * internal class state through a StreamObserver
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void listClass(StudentClassServer.ListClassRequest request, StreamObserver<StudentClassServer.ListClassResponse> responseObserver) {
        StudentClassServer.ListClassResponse.Builder response = StudentClassServer.ListClassResponse.newBuilder();

        if (!_properties.get("isActive")) {
            response.setCode(ResponseCode.INACTIVE_SERVER);

        } else {

            LOGGER.info("Received dump request");
            response.setClassState(this._classObj.getClassState());
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
