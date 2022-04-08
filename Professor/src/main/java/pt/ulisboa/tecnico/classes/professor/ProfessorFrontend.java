package pt.ulisboa.tecnico.classes.professor;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.TimestampsManager;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class abstracts all the stub calls executed by the Professor client
 */
public class ProfessorFrontend extends TimestampsManager {

    private final NameServerFrontend _nameServer;
    private ProfessorServiceGrpc.ProfessorServiceBlockingStub _stub;


    /**
     * creates an instance of ProfessorFrontend
     */
    public ProfessorFrontend() {
        super(new NameServerFrontend());
        this._nameServer = new NameServerFrontend();
    }


    private void getNewStubWithQualifiers(List<String> qualifiers, boolean previousIsInactive) {
        this._stub = ProfessorServiceGrpc.newBlockingStub(_nameServer.getChannel(ProfessorServiceGrpc.SERVICE_NAME, qualifiers, previousIsInactive));
    }

    /**
     * Sends a list request to the server and returns the internal class state. In case of error,
     * throws the ResponseCode as a ResponseException
     *
     * @return ClassesDefinitions.ClassState
     * @throws ResponseException
     */
    public ClassState list() throws StatusRuntimeException, ResponseException {
        getNewStubWithQualifiers(new ArrayList<>(), false);

        ProfessorClassServer.ListClassRequest.Builder request = ProfessorClassServer.ListClassRequest.newBuilder();

        request.putAllTimestamps(this.getTimestamps());

        ProfessorClassServer.ListClassResponse response = _stub.listClass(request.build());

        if (response.getCode() == ResponseCode.OK) {
            this.setTimestamps(response.getTimestampsMap());
            return response.getClassState();
        }
        else if (response.getCode() == ResponseCode.INACTIVE_SERVER) {
            getNewStubWithQualifiers(new ArrayList<>(), true);
            return this.list();
        }
        else {
            throw new ResponseException(response.getCode());
        }
    }

    /**
     * Sends a openEnrollment request to the server and changes the class state to allow enrollments
     * prints the response code
     *
     * @param capacity
     */
    public ResponseCode openEnrollmentsCommand(int capacity) throws StatusRuntimeException {

        getNewStubWithQualifiers(List.of("P"), false);
        ProfessorClassServer.OpenEnrollmentsRequest request = ProfessorClassServer.OpenEnrollmentsRequest.newBuilder().setCapacity(capacity).build();
        ProfessorClassServer.OpenEnrollmentsResponse response = _stub.openEnrollments(request);

        if (response.getCode() == ResponseCode.INACTIVE_SERVER) {
            getNewStubWithQualifiers(List.of("P"), true);
            return this.openEnrollmentsCommand(capacity);
        }

        return response.getCode();
    }

    /**
     * Sends a closeEnrollments request to the server and changes the class state to not
     * allow further enrollments, prints the response code
     */
    public ResponseCode closeEnrollmentsCommand() throws StatusRuntimeException {

        getNewStubWithQualifiers(List.of("P"), false);
        ProfessorClassServer.CloseEnrollmentsRequest request = ProfessorClassServer.CloseEnrollmentsRequest.getDefaultInstance();
        ProfessorClassServer.CloseEnrollmentsResponse response = _stub.closeEnrollments(request);

        if (response.getCode() == ResponseCode.INACTIVE_SERVER) {
            getNewStubWithQualifiers(List.of("P"), true);
            return this.closeEnrollmentsCommand();
        }

        return response.getCode();
    }

    /**
     * Sends a cancelEnrollment request to the server and changes the class state
     * un enrolling a student in the class, prints the response code
     *
     * @param studentId
     */
    public ResponseCode cancelEnrollmentCommand(String studentId) throws StatusRuntimeException {

        getNewStubWithQualifiers(List.of("P"), false);
        ProfessorClassServer.CancelEnrollmentRequest request = ProfessorClassServer.CancelEnrollmentRequest.newBuilder().setStudentId(studentId).build();
        ProfessorClassServer.CancelEnrollmentResponse response = _stub.cancelEnrollment(request);

        if (response.getCode() == ResponseCode.INACTIVE_SERVER) {
            getNewStubWithQualifiers(List.of("P"), true);
            return this.cancelEnrollmentCommand(studentId);
        }

        return response.getCode();
    }
}
