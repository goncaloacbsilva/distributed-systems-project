package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.classes.Stringify;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminService extends AdminServiceGrpc.AdminServiceImplBase {

  private final ClassObject _classObj;
  private static final Logger LOGGER = Logger.getLogger(AdminService.class.getName());

  public AdminService(ClassObject classObj, boolean enableDebug) {
    super();
    this._classObj = classObj;

    if (!enableDebug) {
      LOGGER.setLevel(Level.OFF);
    }

    LOGGER.info("Started with debug mode enabled");
  }

  @Override
  public void dump(
      AdminClassServer.DumpRequest request,
      StreamObserver<AdminClassServer.DumpResponse> responseObserver) {

    LOGGER.info("Received dump request");

    AdminClassServer.DumpResponse response =
        AdminClassServer.DumpResponse.newBuilder()
            .setClassState(_classObj.getClassState())
            .setCode(ClassesDefinitions.ResponseCode.OK)
            .build();

    LOGGER.info("Sending dump response with class state: \n" + Stringify.format(response.getClassState()));

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
