package pt.ulisboa.tecnico.classes.professor;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class abstracts all the stub calls executed by the Admin client
 */
public class ProfessorFrontend {

    private final NameServerFrontend _nameServer;
    private ManagedChannel _channel;


    /**
     * creates an instance of ProfessorFrontend
     *
     * @param channel gRPC channel
     */
    public ProfessorFrontend() {
        this._nameServer = new NameServerFrontend();
    }


    private ProfessorServiceGrpc.ProfessorServiceBlockingStub getNewStubWithQualifiers(List<String> qualifiers) {
        String[] address = _nameServer.lookup(ProfessorServiceGrpc.SERVICE_NAME, qualifiers).getAddress().split(":");
        this._channel = ManagedChannelBuilder.forAddress(address[0], Integer.valueOf(address[1])).idleTimeout(2, TimeUnit.SECONDS).usePlaintext().build();
        return ProfessorServiceGrpc.newBlockingStub(this._channel);
    }

    /**
     * Sends a list request to the server and returns the internal class state. In case of error,
     * throws the ResponseCode as a ResponseException
     *
     * @return ClassesDefinitions.ClassState
     * @throws ResponseException
     */
    public ClassesDefinitions.ClassState listCommand() throws ResponseException {
        ProfessorServiceGrpc.ProfessorServiceBlockingStub stub = getNewStubWithQualifiers(List.of("primary"));

        ProfessorClassServer.ListClassResponse response =
                stub.listClass(
                        ProfessorClassServer.ListClassRequest.newBuilder().build());
        if (response.getCode() == ClassesDefinitions.ResponseCode.OK) {
            return response.getClassState();
        } else {
            throw new ResponseException(response.getCode());
        }
    }

    /**
     * Sends a openEnrollment request to the server and changes the class state to allow enrollments
     * prints the response code
     *
     * @param command
     */
    public void openEnrollmentsCommand(String command) {
        ProfessorServiceGrpc.ProfessorServiceBlockingStub stub = getNewStubWithQualifiers(List.of("primary"));

        ProfessorClassServer.OpenEnrollmentsRequest request =
                ProfessorClassServer.OpenEnrollmentsRequest.newBuilder()
                        .setCapacity(Integer.parseInt(command))
                        .build();
        ProfessorClassServer.OpenEnrollmentsResponse response =
                stub.openEnrollments(request);
        System.out.println(Stringify.format(response.getCode()));
    }

    /**
     * Sends a closeEnrollments request to the server and changes the class state to not
     * allow further enrollments, prints the response code
     */
    public void closeEnrollmentsCommand() {
        ProfessorServiceGrpc.ProfessorServiceBlockingStub stub = getNewStubWithQualifiers(List.of("primary"));
        ProfessorClassServer.CloseEnrollmentsResponse response =
                stub.closeEnrollments(
                        ProfessorClassServer.CloseEnrollmentsRequest.newBuilder().build());
        System.out.println(Stringify.format(response.getCode()));
    }

    /**
     * Sends a cancelEnrollment request to the server and changes the class state
     * un enrolling a student in the class, prints the response code
     *
     * @param studentId
     */
    public void cancelEnrollmentCommand(String studentId) {
        ProfessorServiceGrpc.ProfessorServiceBlockingStub stub = getNewStubWithQualifiers(List.of("primary"));
        ProfessorClassServer.CancelEnrollmentRequest request =
                ProfessorClassServer.CancelEnrollmentRequest.newBuilder().setStudentId(studentId).build();
        ProfessorClassServer.CancelEnrollmentResponse response =
                stub.cancelEnrollment(request);
        System.out.println(Stringify.format(response.getCode()));
    }
}
