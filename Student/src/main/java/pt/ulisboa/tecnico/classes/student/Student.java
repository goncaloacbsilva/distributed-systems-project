package pt.ulisboa.tecnico.classes.student;

public class Student {

  public static void main(String[] args) {
    System.out.println(Student.class.getSimpleName());
    System.out.printf("Received %d Argument(s)%n", args.length);
    for (int i = 0; i < args.length; i++) {
      System.out.printf("args[%d] = %s%n", i, args[i]);
    }
  }
}
