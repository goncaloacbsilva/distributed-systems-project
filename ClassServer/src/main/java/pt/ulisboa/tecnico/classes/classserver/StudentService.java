package pt.ulisboa.tecnico.classes.classserver;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer;
import pt.ulisboa.tecnico.classes.contract.student.StudentServiceGrpc;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Student Service remote procedure calls
 */
public class StudentService extends StudentServiceGrpc.StudentServiceImplBase {

    private ClassStateWrapper _classObj;
    private final ReplicaManagerFrontend _replicaManager;
    private final HashMap<String, Boolean> _properties;
    private static final Logger LOGGER = Logger.getLogger(StudentService.class.getName());

    /**
     * Creates an instance of StudentService
     *
     * @param obj         shared object
     * @param enableDebug debug flag
     */
    public StudentService(ClassStateWrapper obj, boolean enableDebug, HashMap<String, Boolean> properties, ReplicaManagerFrontend replicaManager) {
        super();
        this._classObj = obj;
        _replicaManager = replicaManager;
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
    public void enroll(StudentClassServer.EnrollRequest request, StreamObserver<StudentClassServer.EnrollResponse> responseObserver) {
        StudentClassServer.EnrollResponse.Builder response = StudentClassServer.EnrollResponse.newBuilder();

        if (!_properties.get("isActive")) {
            response.setCode(ResponseCode.INACTIVE_SERVER);

        } else {
            synchronized (this._classObj) {
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

                    // Set timestamp of the last change
                    Timestamp lastChange = Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build();
                    student = student.toBuilder().setLastChange(lastChange).build();

                    ClassesDefinitions.ClassState.Builder classStateBuilder = currentClassState.toBuilder();
                    classStateBuilder.addEnrolled(student);
                    this._classObj.setClassState(classStateBuilder.build());
                    LOGGER.info("Class state built");

                    response.setCode(ResponseCode.OK);
                    LOGGER.info("Set response as OK");
                    _replicaManager.updateLocalTimestamp();
                }

                LOGGER.info("Sending enroll response");
            }
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
            while (this._replicaManager.getTimestampsManager().isTimestampMostUptoDate(request.getTimestampsMap())) {
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            LOGGER.info("Received list request");
            response.setClassState(this._classObj.getClassState());
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
