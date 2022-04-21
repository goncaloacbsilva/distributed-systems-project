package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

/** Class rpc server */
public class ClassServer {

  private static boolean _enableLogging = false;
  private static HashMap<String, Boolean> _properties;
  private static HashMap<String, Integer> _timestamps;

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
    String address = host + ":" + port;

    ArrayList<String> qualifiers = new ArrayList<String>();

    for (int i = 2; i < args.length - 1; i++) {
      qualifiers.add(args[i]);
    }

    if ("-debug".equals(args[args.length - 1])) {
      _enableLogging = true;
    } else {
      qualifiers.add(args[args.length - 1]);
    }

    _properties = new HashMap<String, Boolean>();
    _properties.put("isActive", true);
    _properties.put("isPrimary", qualifiers.contains("P"));
    _properties.put("GossipActive", true);

    _timestamps = new HashMap<>();

    final NameServerFrontend nameServer = new NameServerFrontend();

    // Initialize Class Object, replica manager frontend and all the services
    ClassStateWrapper classObj = new ClassStateWrapper();
    final ReplicaManagerFrontend replicaManagerFrontend = new ReplicaManagerFrontend(classObj, _enableLogging, _properties, nameServer, address, _timestamps);

    final BindableService adminService = new AdminService(classObj, _enableLogging, _properties, replicaManagerFrontend);
    final BindableService professorService = new ProfessorService(classObj, _enableLogging, _properties, replicaManagerFrontend);
    final BindableService replicaManagerService = new ReplicaManagerService(classObj, _enableLogging, _properties, nameServer, address, _timestamps);
    final BindableService studentService = new StudentService(classObj, _enableLogging, _properties, replicaManagerFrontend);

    Server server =
        ServerBuilder.forPort(port)
            .addService(adminService)
            .addService(professorService)
            .addService(studentService)
            .addService(replicaManagerService)
            .build();


    Timer time = new Timer();
    GossipScheduler gossipScheduler = new GossipScheduler(replicaManagerFrontend);
    time.schedule(gossipScheduler, 0, 2000); // propagates state every 2 seconds

    server.getServices().forEach(serverService -> {
      nameServer.registerServer(serverService.getServiceDescriptor().getName(), address, qualifiers);
    });

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      server.getServices().forEach(serverService -> {
        nameServer.deleteServer(serverService.getServiceDescriptor().getName(), address);
      });
    }));

    // Start the server
    server.start();
    System.out.println("Server started at " + server.getPort());

    server.awaitTermination();
  }
}
