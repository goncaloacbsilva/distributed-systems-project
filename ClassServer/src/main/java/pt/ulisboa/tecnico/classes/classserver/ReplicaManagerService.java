package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.contract.classserver.ReplicaManagerGrpc;
import pt.ulisboa.tecnico.classes.contract.classserver.ReplicaManagerClassServer;

import java.util.HashMap;

public class ReplicaManagerService extends ReplicaManagerGrpc.ReplicaManagerImplBase {
    private ClassStateWrapper _classObj;
    private HashMap<String, Integer> _timestamps;

    public ReplicaManagerService(ClassStateWrapper classObj, HashMap<String, Integer> timestamps) {
        this._classObj = classObj;
        this._timestamps = timestamps;
    }

    @Override
    public void propagateStatePush(ReplicaManagerClassServer.PropagateStatePushRequest request, StreamObserver<ReplicaManagerClassServer.PropagateStatePushResponse> responseObserver) {

    }

    @Override
    public void propagateStatePull(ReplicaManagerClassServer.PropagateStatePullRequest request, StreamObserver<ReplicaManagerClassServer.PropagateStatePullResponse> responseObserver) {

    }
}
