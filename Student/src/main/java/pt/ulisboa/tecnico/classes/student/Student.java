package pt.ulisboa.tecnico.classes.student;

import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;

import java.util.Scanner;

/**
 * Student client class
 */
public class Student {

    private static final String EXIT_CMD = "exit";
    private static final String LIST_CMD = "list";
    private static final String ENROLL_CMD = "enroll";

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println(Student.class.getSimpleName());
        System.out.printf("Received %d Argument(s)%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("args[%d] = %s%n", i, args[i]);
        }

        if (!(args[3].length() >= 3)) {
            System.out.printf("invalid student name too small ");
        }
        if (!(args[3].length() <= 30)) {
            System.out.printf("invalid student name too big ");
        }
        final String host = args[0];
        final int port = Integer.parseInt(args[1]);
        final String studentID = args[2];
        final String studentName = args[3];

        final StudentFrontend frontend = new StudentFrontend(studentID, studentName);

        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.printf("%n> ");

            String line = scanner.nextLine();

            try {
                if (EXIT_CMD.equals(line)) {
                    scanner.close();
                    System.exit(0);

                } else if (ENROLL_CMD.equals(line)) {
                    frontend.enrollStudent();

                } else if (LIST_CMD.equals(line)) {
                    ClassesDefinitions.ClassState classState = frontend.list();
                    System.out.println(Stringify.format(classState));

                }
            } catch (ResponseException exception) {
                System.out.println(Stringify.format(exception.getResponseCode()));
            }
        }
    }
}
