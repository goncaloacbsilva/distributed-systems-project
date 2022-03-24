package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class ClassServer {

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

    // Initialize Class Object and all the services
    ClassObject classObj = new ClassObject();

    final BindableService adminService = new AdminService(classObj);
    final BindableService professorService = new ProfessorService(classObj);

    Server server =
        ServerBuilder.forPort(port).addService(adminService).addService(professorService).build();

    // Start the server
    server.start();
    System.out.println("Server started at " + server.getPort());

    server.awaitTermination();
  }
}
