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

        if (args.length < 2) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s student_id student_name%n", Student.class.getName());
            return;
        }

        final String studentID = args[0];
        final String studentName = args[1];

        if (!(studentName.length() >= 3)) {
            System.out.printf("Invalid student name too small ");
        }
        if (!(studentName.length() <= 30)) {
            System.out.printf("Invalid student name too big ");
        }

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
                    System.out.println(Stringify.format(frontend.enrollStudent()));

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
