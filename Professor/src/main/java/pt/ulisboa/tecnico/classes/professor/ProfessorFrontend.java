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

    /**
     * Sends a list request to the server and returns the internal class state. In case of error,
     * throws the ResponseCode as a ResponseException
     *
     * @return ClassesDefinitions.ClassState
     * @throws ResponseException
     */
    public ClassState list() throws StatusRuntimeException, ResponseException {
        ProfessorRPCList rpcCall = new ProfessorRPCList(new ArrayList<>(), this._nameServer);

        rpcCall.setTimestamps(this.getTimestamps());
        rpcCall.exec();

        return rpcCall.getResponse().getClassState();
    }

    /**
     * Sends a openEnrollment request to the server and changes the class state to allow enrollments
     * prints the response code
     *
     * @param capacity
     */
    public ResponseCode openEnrollmentsCommand(int capacity) throws ResponseException, StatusRuntimeException {
        ProfessorRPCOpenEnrollments rpcCall = new ProfessorRPCOpenEnrollments(List.of("P"), this._nameServer);

        rpcCall.setCapacity(capacity);
        rpcCall.exec();

        return rpcCall.getResponse().getCode();
    }

    /**
     * Sends a closeEnrollments request to the server and changes the class state to not
     * allow further enrollments, prints the response code
     */
    public ResponseCode closeEnrollmentsCommand() throws ResponseException, StatusRuntimeException {
        ProfessorRPCCloseEnrollments rpcCall = new ProfessorRPCCloseEnrollments(List.of("P"), this._nameServer);

        rpcCall.exec();

        return rpcCall.getResponse().getCode();
    }

    /**
     * Sends a cancelEnrollment request to the server and changes the class state
     * un enrolling a student in the class, prints the response code
     *
     * @param studentId
     */
    public ResponseCode cancelEnrollmentCommand(String studentId) throws ResponseException, StatusRuntimeException {
        ProfessorRPCCancelEnrollment rpcCall = new ProfessorRPCCancelEnrollment(List.of("P"), this._nameServer);

        rpcCall.exec();

        return rpcCall.getResponse().getCode();
    }
}
