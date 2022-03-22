package pt.ulisboa.tecnico.classes.professor;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;

import java.util.List;
import java.util.Scanner;

public class Professor {


    public static void main(String[] args) {
        System.out.println(Professor.class.getSimpleName());
        System.out.printf("Received %d Argument(s)%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("args[%d] = %s%n", i, args[i]);
        }

        if (args.length < 1) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s  host %n port%n", Professor.class.getName());
            return;
        }

        if (args.length < 2) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s port%n", Professor.class.getName());
            return;
        }

        String host = String.valueOf(args[0]);
        Integer port = Integer.valueOf(args[1]);


        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

        ProfessorServiceGrpc.ProfessorServiceBlockingStub professorServiceBlockingStub = ProfessorServiceGrpc.newBlockingStub(channel);

        Scanner lineReader = new Scanner(System.in);

        while (true) {
            System.out.printf("%n> ");
            String command = lineReader.nextLine();

            if (command.contains(ProfessorConstants.LIST_COMMAND)) {
                ProfessorClassServer.ListClassResponse response = professorServiceBlockingStub.listClass(
                        ProfessorClassServer
                                .ListClassRequest
                                .newBuilder()
                                .build());
                ClassesDefinitions.ClassState classState = response.getClassState();

                if(response.getCode().getNumber() == ClassesDefinitions.ResponseCode.OK_VALUE){
                    System.out.println(Stringify.format(classState));
                }
                else {
                    System.out.println(Stringify.format(response.getCode()));
                }



            }

            if (command.contains(ProfessorConstants.OPEN_ENROLLMENTS_COMMAND)) {
                String variables[] = command.split(" ", 2);
                ProfessorClassServer.OpenEnrollmentsRequest request = ProfessorClassServer.OpenEnrollmentsRequest.newBuilder()
                        .setCapacity(Integer.parseInt(variables[1]))
                        .build();
                ProfessorClassServer.OpenEnrollmentsResponse response = professorServiceBlockingStub.openEnrollments(request);

            }

            if (command.contains(ProfessorConstants.CLOSE_ENROLLMENTS_COMMAND)) {
                ProfessorClassServer.CloseEnrollmentsResponse response = professorServiceBlockingStub.closeEnrollments(
                        ProfessorClassServer
                                .CloseEnrollmentsRequest
                                .newBuilder()
                                .build());
            }

            if (command.contains(ProfessorConstants.CANCEL_ENROLLMENTS_COMMAND)) {
            }

            if (command.contains(ProfessorConstants.EXIT_COMMAND)) {
                channel.shutdown();
                lineReader.close();
                System.exit(0);
            }

        }


    }
}
