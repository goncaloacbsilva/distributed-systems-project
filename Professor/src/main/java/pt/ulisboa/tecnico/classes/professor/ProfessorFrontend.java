package pt.ulisboa.tecnico.classes.professor;

import io.grpc.ManagedChannel;
import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;

/**
 * This class abstracts all the stub calls executed by the Admin client
 */
public class ProfessorFrontend {

    private ProfessorServiceGrpc.ProfessorServiceBlockingStub _professorServiceBlockingStub;


    /**
     * creates an instance of ProfessorFrontend
     *
     * @param channel gRPC channel
     */
    public ProfessorFrontend(ManagedChannel channel) {
        this._professorServiceBlockingStub = ProfessorServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Sends a list request to the server and returns the internal class state. In case of error,
     * throws the ResponseCode as a ResponseException
     *
     * @return ClassesDefinitions.ClassState
     * @throws ResponseException
     */
    public ClassesDefinitions.ClassState listCommand() throws ResponseException {
        ProfessorClassServer.ListClassResponse response =
                this._professorServiceBlockingStub.listClass(
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

        ProfessorClassServer.OpenEnrollmentsRequest request =
                ProfessorClassServer.OpenEnrollmentsRequest.newBuilder()
                        .setCapacity(Integer.parseInt(command))
                        .build();
        ProfessorClassServer.OpenEnrollmentsResponse response =
                this._professorServiceBlockingStub.openEnrollments(request);
        System.out.println(Stringify.format(response.getCode()));
    }

    /**
     * Sends a closeEnrollments request to the server and changes the class state to not
     * allow further enrollments, prints the response code
     */
    public void closeEnrollmentsCommand() {
        ProfessorClassServer.CloseEnrollmentsResponse response =
                this._professorServiceBlockingStub.closeEnrollments(
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

        ProfessorClassServer.CancelEnrollmentRequest request =
                ProfessorClassServer.CancelEnrollmentRequest.newBuilder().setStudentId(studentId).build();
        ProfessorClassServer.CancelEnrollmentResponse response =
                this._professorServiceBlockingStub.cancelEnrollment(request);
        System.out.println(Stringify.format(response.getCode()));
    }
}
