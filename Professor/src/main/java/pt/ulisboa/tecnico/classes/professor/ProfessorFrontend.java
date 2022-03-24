package pt.ulisboa.tecnico.classes.professor;

import io.grpc.ManagedChannel;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;

public class ProfessorFrontend {

  private ProfessorServiceGrpc.ProfessorServiceBlockingStub professorServiceBlockingStub;

  public ProfessorFrontend(ManagedChannel channel) {
    this.professorServiceBlockingStub = ProfessorServiceGrpc.newBlockingStub(channel);
  }

  public void listCommand() {
    ProfessorClassServer.ListClassResponse response =
        this.professorServiceBlockingStub.listClass(
            ProfessorClassServer.ListClassRequest.newBuilder().build());
    ClassesDefinitions.ClassState classState = response.getClassState();

    if (response.getCode().getNumber() == ClassesDefinitions.ResponseCode.OK_VALUE) {
      System.out.println(Stringify.format(classState));
    } else {
      System.out.println(Stringify.format(response.getCode()));
    }
  }

  public void openEnrollmentsCommand(String command) {

    ProfessorClassServer.OpenEnrollmentsRequest request =
        ProfessorClassServer.OpenEnrollmentsRequest.newBuilder()
            .setCapacity(Integer.parseInt(command))
            .build();
    ProfessorClassServer.OpenEnrollmentsResponse response =
        this.professorServiceBlockingStub.openEnrollments(request);
    System.out.println(Stringify.format(response.getCode()));
  }

  public void closeEnrollmentsCommand() {
    ProfessorClassServer.CloseEnrollmentsResponse response =
        this.professorServiceBlockingStub.closeEnrollments(
            ProfessorClassServer.CloseEnrollmentsRequest.newBuilder().build());
    System.out.println(Stringify.format(response.getCode()));
  }

  public void cancelEnrollmentCommand(String command) {

    ProfessorClassServer.CancelEnrollmentRequest request =
        ProfessorClassServer.CancelEnrollmentRequest.newBuilder().setStudentId(command).build();
    ProfessorClassServer.CancelEnrollmentResponse response =
        this.professorServiceBlockingStub.cancelEnrollment(request);
    System.out.println(Stringify.format(response.getCode()));
  }

  public void exitCommand() {
    System.exit(0);
  }
}
