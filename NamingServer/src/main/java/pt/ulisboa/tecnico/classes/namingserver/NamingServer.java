package pt.ulisboa.tecnico.classes.namingserver;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class NamingServer {

  public static void main(String[] args) throws Exception {
    System.out.println(NamingServer.class.getSimpleName());
    System.out.printf("Received %d Argument(s)%n", args.length);
    for (int i = 0; i < args.length; i++) {
      System.out.printf("args[%d] = %s%n", i, args[i]);
    }

    if (args.length < 1) {
      System.err.println("Argument(s) missing!");
      System.err.printf("Usage: java %s port%n", NamingServer.class.getName());
      return;
    }

    Integer port = Integer.valueOf(args[0]);
    boolean _enableLogging = args[args.length - 1].equals("-debug");

    NamingServices services = new NamingServices();
    final BindableService nameServerService = new NamingServerServiceImpl(services, _enableLogging);

    Server server = ServerBuilder.forPort(port).addService(nameServerService).build();

    // Start the server
    server.start();
    System.out.println("Server started at " + server.getPort());

    server.awaitTermination();
  }
}
