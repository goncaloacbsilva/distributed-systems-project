package pt.ulisboa.tecnico.classes.admin;

import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;

import java.util.List;
import java.util.Scanner;

/** Admin client class */
public class Admin {

  // Define Admin commands
  private static final String EXIT_CMD = "exit";
  private static final String DUMP_CMD = "dump";

  /**
   * Admin client entry point
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    System.out.println(Admin.class.getSimpleName());
    System.out.printf("Received %d Argument(s)%n", args.length);
    for (int i = 0; i < args.length; i++) {
      System.out.printf("args[%d] = %s%n", i, args[i]);
    }

    final AdminFrontend frontend = new AdminFrontend();

    Scanner scanner = new Scanner(System.in);

    while (true) {
      System.out.printf("%n> ");

      String line = scanner.nextLine();

      try {
        if (DUMP_CMD.equals(line)) {
          ClassesDefinitions.ClassState classState = frontend.dump();
          System.out.println(Stringify.format(classState));

        } else if (EXIT_CMD.equals(line)) {
          scanner.close();
          System.exit(0);

        }
      } catch (ResponseException exception) {
        System.out.println(Stringify.format(exception.getResponseCode()));
      }
    }
  }
}
