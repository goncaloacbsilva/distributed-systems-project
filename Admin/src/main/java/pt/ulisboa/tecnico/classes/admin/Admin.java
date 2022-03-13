package pt.ulisboa.tecnico.classes.admin;

public class Admin {

  public static void main(String[] args) {
    System.out.println(Admin.class.getSimpleName());
    System.out.printf("Received %d Argument(s)%n", args.length);
    for (int i = 0; i < args.length; i++) {
      System.out.printf("args[%d] = %s%n", i, args[i]);
    }
  }
}
