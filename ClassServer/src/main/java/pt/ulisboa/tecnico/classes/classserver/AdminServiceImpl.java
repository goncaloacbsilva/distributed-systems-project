package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;

public class AdminServiceImpl extends AdminServiceGrpc.AdminServiceImplBase {

  private final ClassObject _classObj;

  public AdminServiceImpl(ClassObject classObj) {
    super();
    this._classObj = classObj;
  }

  @Override
  public void dump(
      AdminClassServer.DumpRequest request,
      StreamObserver<AdminClassServer.DumpResponse> responseObserver) {
    AdminClassServer.DumpResponse response =
        AdminClassServer.DumpResponse.newBuilder()
            .setClassState(_classObj.getClassState())
            .setCode(ClassesDefinitions.ResponseCode.OK)
            .build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
