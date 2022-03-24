package pt.ulisboa.tecnico.classes.admin;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;

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

    if (args.length < 2) {
      System.out.println("Argument(s) missing!");
      System.out.printf("Usage: java %s host port%n", Admin.class.getName());
      return;
    }

    final String host = args[0];
    final int port = Integer.parseInt(args[1]);

    final ManagedChannel channel =
        ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

    final AdminFrontend frontend = new AdminFrontend(channel);

    Scanner scanner = new Scanner(System.in);

    while (true) {
      System.out.printf("%n> ");

      String line = scanner.nextLine();

      if (DUMP_CMD.equals(line)) {
        try {
          ClassesDefinitions.ClassState classState = frontend.dump();
          System.out.println(Stringify.format(classState));
        } catch (ResponseException exception) {
          System.out.println(Stringify.format(exception.getResponseCode()));
        }
      } else if (EXIT_CMD.equals(line)) {
        channel.shutdown();
        scanner.close();
        System.exit(0);
      }
    }
  }
}
