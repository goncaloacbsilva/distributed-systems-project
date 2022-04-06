package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Admin Service remote procedure calls */
public class AdminService extends AdminServiceGrpc.AdminServiceImplBase {

  private final ClassStateWrapper _classObj;
  private static final Logger LOGGER = Logger.getLogger(AdminService.class.getName());

  /**
   * Creates an instance of AdminService
   *
   * @param classObj shared class state object
   * @param enableDebug debug flag
   */
  public AdminService(ClassStateWrapper classObj, boolean enableDebug) {
    super();
    this._classObj = classObj;

    if (!enableDebug) {
      LOGGER.setLevel(Level.OFF);
    }

    LOGGER.info("Started with debug mode enabled");
  }

  /**
   * "dump" remote procedure call. Receives DumpRequest from the admin client and sends the internal
   * class state through a StreamObserver
   *
   * @param request
   * @param responseObserver
   */
  @Override
  public void dump(AdminClassServer.DumpRequest request, StreamObserver<AdminClassServer.DumpResponse> responseObserver) {

    LOGGER.info("Received dump request");

    AdminClassServer.DumpResponse.Builder response = AdminClassServer.DumpResponse.newBuilder();
    responseObserver.onNext(response.setClassState(this._classObj.getClassState()).build());
    responseObserver.onCompleted();
  }
}
