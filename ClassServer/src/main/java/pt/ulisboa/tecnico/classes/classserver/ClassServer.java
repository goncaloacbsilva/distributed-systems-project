package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.classes.NameServerFrontend;

import java.util.ArrayList;

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
      System.err.printf("Usage: java %s host port <qualifiers>%n", ClassServer.class.getName());
      return;
    }

    String host = args[0];
    Integer port = Integer.valueOf(args[1]);

    ArrayList<String> qualifiers = new ArrayList<String>();

    for (int i = 2; i < args.length - 1; i++) {
      qualifiers.add(args[i]);
    }

    if ("-debug".equals(args[args.length - 1])) {
      _enableLogging = true;
    } else {
      qualifiers.add(args[args.length - 1]);
    }

    // Initialize Class Object, name server frontend and all the services
    ClassStateWrapper classObj = new ClassStateWrapper();

    final NameServerFrontend nameServer = new NameServerFrontend();

    final BindableService adminService = new AdminService(classObj, _enableLogging);
    final BindableService professorService = new ProfessorService(classObj, _enableLogging);
    final BindableService studentService = new StudentService(classObj, _enableLogging);

    Server server =
        ServerBuilder.forPort(port)
            .addService(adminService)
            .addService(professorService)
            .addService(studentService)
            .build();



    server.getServices().forEach(serverService -> {
      nameServer.registerServer(serverService.getServiceDescriptor().getName(), host+":"+port, qualifiers);
    });

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      server.getServices().forEach(serverService -> {
        nameServer.deleteServer(serverService.getServiceDescriptor().getName(), host+":"+port);
      });
    }));

    // Start the server
    server.start();
    System.out.println("Server started at " + server.getPort());

    server.awaitTermination();
  }
}
