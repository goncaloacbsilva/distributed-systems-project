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
            String command = lineReader.nextLine();
            String commandArgs[] = command.split(" ", 2);

            try {
                if (commandArgs[0].equals(LIST_COMMAND)) {
                    ClassesDefinitions.ClassState classState = frontend.list();
                    System.out.println(Stringify.format(classState));

                } else if (commandArgs[0].equals(OPEN_ENROLLMENTS_COMMAND)) {
                    System.out.println(Stringify.format(frontend.openEnrollmentsCommand(Integer.parseInt(commandArgs[1]))));

                } else if (commandArgs[0].equals(CLOSE_ENROLLMENTS_COMMAND)) {
                    System.out.println(Stringify.format(frontend.closeEnrollmentsCommand()));

                } else if (commandArgs[0].equals(CANCEL_ENROLLMENTS_COMMAND)) {
                    System.out.println(Stringify.format(frontend.cancelEnrollmentCommand(commandArgs[1])));

                } else if (commandArgs[0].equals(EXIT_COMMAND)) {
                    lineReader.close();
                    System.exit(0);

                }

            } catch (ResponseException exception) {
                System.out.println(Stringify.format(exception.getResponseCode()));
            }

        }

    }
}
