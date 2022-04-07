package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.classserver.ReplicaManagerGrpc;
import pt.ulisboa.tecnico.classes.contract.classserver.ReplicaManagerClassServer;

import java.util.HashMap;
import java.util.logging.Logger;

public class ReplicaManagerService extends ReplicaManagerGrpc.ReplicaManagerImplBase {
    private ClassStateWrapper _classObj;
    private HashMap<String, Integer> _timestamps;
    private static final Logger LOGGER = Logger.getLogger(AdminService.class.getName());

    public ReplicaManagerService(ClassStateWrapper classObj, HashMap<String, Integer> timestamps) {
        this._classObj = classObj;
        this._timestamps = timestamps;
    }

    @Override
    public void propagateStatePush(ReplicaManagerClassServer.PropagateStatePushRequest request, StreamObserver<ReplicaManagerClassServer.PropagateStatePushResponse> responseObserver) {
        LOGGER.info("Received propagateStatePush request");
        this._classObj.setClassState(request.getClassState());
        this._timestamps.putAll(request.getTimestampsMap());
        responseObserver.onNext(ReplicaManagerClassServer.PropagateStatePushResponse.newBuilder().setCode(ClassesDefinitions.ResponseCode.OK).build());
        LOGGER.info("Set response as OK");
        LOGGER.info("Sending propagateStatePush response");
        responseObserver.onCompleted();
    }

    @Override
    public void propagateStatePull(ReplicaManagerClassServer.PropagateStatePullRequest request, StreamObserver<ReplicaManagerClassServer.PropagateStatePullResponse> responseObserver) {
        LOGGER.info("Received propagateStatePull request");
        ReplicaManagerClassServer.PropagateStatePullResponse.Builder response = ReplicaManagerClassServer.PropagateStatePullResponse.newBuilder();
        response.setClassState(this._classObj.getClassState());
        response.getTimestampsMap().putAll(this._timestamps);
        responseObserver.onNext(response.setCode(ClassesDefinitions.ResponseCode.OK).build());
        LOGGER.info("Set response as OK");
        LOGGER.info("Sending propagateStatePull response");
        responseObserver.onCompleted();
    }
}
