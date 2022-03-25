package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

/** Class rpc server */
public class ClassServer {

  private static boolean _enableLogging = false;

  /**
   * Class server entry point
   *
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    System.out.println(ClassServer.class.getSimpleName());
    System.out.printf("Received %d Argument(s)%n", args.length);
    for (int i = 0; i < args.length; i++) {
      System.out.printf("args[%d] = %s%n", i, args[i]);
    }

    if (args.length < 1) {
      System.err.println("Argument(s) missing!");
      System.err.printf("Usage: java %s port%n", ClassServer.class.getName());
      return;
    }

    Integer port = Integer.valueOf(args[0]);
    if (args.length > 1) {
      _enableLogging = args[1].equals("-debug");
    }

    // Initialize Class Object and all the services
    ClassObject classObj = new ClassObject();

    final BindableService adminService = new AdminService(classObj, _enableLogging);
    final BindableService professorService = new ProfessorService(classObj, _enableLogging);
    final BindableService studentService = new StudentService(classObj, _enableLogging);

    Server server =
        ServerBuilder.forPort(port)
            .addService(adminService)
            .addService(professorService)
            .addService(studentService)
            .build();

    // Start the server
    server.start();
    System.out.println("Server started at " + server.getPort());

    server.awaitTermination();
  }
}
