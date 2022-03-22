package pt.ulisboa.tecnico.classes;

import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.Student;

import java.util.Comparator;
import java.util.stream.Collectors;

public class Stringify {

  public static String format(ClassState classState) {
    return String.format(
        "ClassState{\n\tcapacity=%d,\n\topenEnrollments=%s,\n\tenrolled=%s,\n\tdiscarded=%s\n}",
        classState.getCapacity(),
        classState.getOpenEnrollments(),
        classState.getEnrolledList().stream()
            .sorted(Comparator.comparing(Student::getStudentId))
            .map(Stringify::format)
            .collect(Collectors.toList()),
        classState.getDiscardedList().stream()
            .sorted(Comparator.comparing(Student::getStudentId))
            .map(Stringify::format)
            .collect(Collectors.toList()));
  }

  public static String format(Student student) {
    return String.format(
        "\n\t\tStudent{\n\t\t\tId='%s',\n\t\t\tName='%s'\n\t\t}",
        student.getStudentId(), student.getStudentName());
  }

  public static String format(ResponseCode responseCode) {
    return switch (responseCode) {
      case OK -> "The action completed successfully.";
      case NON_EXISTING_STUDENT -> "The student does not exist.";
      case FULL_CLASS -> "The class has reached its maximum capacity.";
      case STUDENT_ALREADY_ENROLLED -> "The student is already enrolled.";
      case ENROLLMENTS_ALREADY_OPENED -> "Enrollments are already open.";
      case ENROLLMENTS_ALREADY_CLOSED -> "Enrollments are already closed.";
      case INACTIVE_SERVER -> "The server is down.";
      case WRITING_NOT_SUPPORTED -> "The server you contacted does not support writes.";
      default -> "Unknown error.";
    };
  }
}
