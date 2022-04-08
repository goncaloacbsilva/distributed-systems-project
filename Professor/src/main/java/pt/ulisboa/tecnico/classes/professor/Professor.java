package pt.ulisboa.tecnico.classes.professor;

import io.grpc.ManagedChannel;
import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;

import java.util.Scanner;

/**
 * Professor client class
 */
public class Professor {
    public static final String LIST_COMMAND = "list";
    public static final String OPEN_ENROLLMENTS_COMMAND = "openEnrollments";
    public static final String CLOSE_ENROLLMENTS_COMMAND = "closeEnrollments";
    public static final String CANCEL_ENROLLMENTS_COMMAND = "cancelEnrollment";
    public static final String EXIT_COMMAND = "exit";


    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println(Professor.class.getSimpleName());

        ProfessorFrontend frontend = new ProfessorFrontend();

        Scanner lineReader = new Scanner(System.in);

        while (true) {
            System.out.printf("%n> ");
            String line = lineReader.nextLine();
            String commandArgs[] = line.split(" ", 2);
            String command = commandArgs[0];

            try {

                switch (command) {

                    case LIST_COMMAND -> {
                        ClassesDefinitions.ClassState classState = frontend.list();
                        System.out.println(Stringify.format(classState));
                    }

                    case OPEN_ENROLLMENTS_COMMAND -> {
                        System.out.println(Stringify.format(frontend.openEnrollmentsCommand(Integer.parseInt(commandArgs[1]))));
                    }

                    case CLOSE_ENROLLMENTS_COMMAND -> {
                        System.out.println(Stringify.format(frontend.closeEnrollmentsCommand()));
                    }

                    case CANCEL_ENROLLMENTS_COMMAND -> {
                        System.out.println(Stringify.format(frontend.cancelEnrollmentCommand(commandArgs[1])));
                    }

                    case EXIT_COMMAND -> {
                        lineReader.close();
                        System.exit(0);
                    }

                }

            } catch (ResponseException exception) {
                System.out.println(Stringify.format(exception.getResponseCode()));
            }

        }

    }
}
