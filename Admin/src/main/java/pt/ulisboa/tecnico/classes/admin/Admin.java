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
  private static final String ACTIVATE_CMD = "activate";
  private static final String DEACTIVATE_CMD = "deactivate";

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
      String commandArgs[] = line.split(" ", 2);
      String command = commandArgs[0];
      String qualifier;

      if (args.length < 2) {
        // If no qualifier is provided assume P as default
        qualifier = "P";
      } else {
        qualifier = commandArgs[1];
      }

      try {

        switch (command) {

          case DUMP_CMD -> {
            ClassesDefinitions.ClassState classState = frontend.dump(List.of(qualifier));
            System.out.println(Stringify.format(classState));
          }

          case ACTIVATE_CMD -> {
            System.out.println(Stringify.format(frontend.activate(List.of(qualifier))));
          }

          case DEACTIVATE_CMD -> {
            System.out.println(Stringify.format(frontend.deactivate(List.of(qualifier))));
          }

          case EXIT_CMD -> {
            scanner.close();
            System.exit(0);
          }

        }

      } catch (ResponseException exception) {
        System.out.println(Stringify.format(exception.getResponseCode()));
      }
    }
  }
}
