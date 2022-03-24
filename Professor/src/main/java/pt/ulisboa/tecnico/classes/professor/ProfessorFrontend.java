package pt.ulisboa.tecnico.classes.professor;

import io.grpc.ManagedChannel;
import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;

public class ProfessorFrontend {

    private ProfessorServiceGrpc.ProfessorServiceBlockingStub _professorServiceBlockingStub;

    public ProfessorFrontend(ManagedChannel channel) {
        this._professorServiceBlockingStub = ProfessorServiceGrpc.newBlockingStub(channel);
    }

    public ClassesDefinitions.ClassState listCommand() throws ResponseException {
        ProfessorClassServer.ListClassResponse response = this._professorServiceBlockingStub.listClass(
                ProfessorClassServer
                        .ListClassRequest
                        .newBuilder()
                        .build());
        if (response.getCode() == ClassesDefinitions.ResponseCode.OK) {
            return  response.getClassState();
        } else {
            throw new ResponseException(response.getCode());
        }

    }

    public void openEnrollmentsCommand(String command) {

        ProfessorClassServer.OpenEnrollmentsRequest request = ProfessorClassServer.OpenEnrollmentsRequest.newBuilder()
                .setCapacity(Integer.parseInt(command))
                .build();
        ProfessorClassServer.OpenEnrollmentsResponse response = this._professorServiceBlockingStub.openEnrollments(request);
        System.out.println(Stringify.format(response.getCode()));
    }

    public void closeEnrollmentsCommand() {
        ProfessorClassServer.CloseEnrollmentsResponse response = this._professorServiceBlockingStub.closeEnrollments(
                ProfessorClassServer
                        .CloseEnrollmentsRequest
                        .newBuilder()
                        .build());
        System.out.println(Stringify.format(response.getCode()));
    }

    public void cancelEnrollmentCommand(String studentId) {

        ProfessorClassServer.CancelEnrollmentRequest request = ProfessorClassServer.CancelEnrollmentRequest.newBuilder()
                .setStudentId(studentId)
                .build();
        ProfessorClassServer.CancelEnrollmentResponse response = this._professorServiceBlockingStub.cancelEnrollment(request);
        System.out.println(Stringify.format(response.getCode()));

    }

}
