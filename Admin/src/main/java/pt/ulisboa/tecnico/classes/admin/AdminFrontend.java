package pt.ulisboa.tecnico.classes.admin;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer;


public class AdminFrontend {

    private final AdminServiceGrpc.AdminServiceBlockingStub stub;

    public AdminFrontend(ManagedChannel channel) {
        stub = AdminServiceGrpc.newBlockingStub(channel);
    }

   public ClassState dump() throws StatusRuntimeException, ResponseException {
        AdminClassServer.DumpResponse response = stub.dump(AdminClassServer.DumpRequest.getDefaultInstance());
        if (response.getCode() == ResponseCode.OK) {
            return response.getClassState();
        } else {
            throw new ResponseException(response.getCode());
        }
   }


}
