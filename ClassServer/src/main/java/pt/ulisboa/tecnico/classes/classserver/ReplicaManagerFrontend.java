package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.classserver.ReplicaManagerClassServer;
import pt.ulisboa.tecnico.classes.contract.classserver.ReplicaManagerGrpc;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ReplicaManagerFrontend {
    private ManagedChannel _channel;
    private ClassStateWrapper _classObj;
    private final NameServerFrontend _nameServer;

    private HashMap<String, Integer> _timestamps;
    private ReplicaManagerGrpc.ReplicaManagerBlockingStub _stub;

    public ReplicaManagerFrontend(ClassStateWrapper classObj, NameServerFrontend nameServer, HashMap<String, Integer> timestamps) {
        this._classObj = classObj;
        this._nameServer = nameServer;
        this._timestamps = timestamps;
    }

    private void getNewStubWithAddress(boolean isPrimary) {
        if (_channel != null) {
            _channel.shutdown();
        }

        String target;
        List<ClassServerNamingServer.ServerEntry> servers;
        if (isPrimary) {
            servers = this._nameServer.list().stream().filter(serverEntry -> serverEntry.getQualifiersList().contains("S")).toList();
        } else {
            servers = this._nameServer.list().stream().filter(serverEntry -> serverEntry.getQualifiersList().contains("P")).toList();

        }
        Random rand = new Random();
        target = servers.get(rand.nextInt(servers.size())).getAddress();

        _channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this._stub = ReplicaManagerGrpc.newBlockingStub(_channel);
    }


    public void propagateStatePush(boolean isPrimary) {
        getNewStubWithAddress(isPrimary);

        ReplicaManagerClassServer.PropagateStatePushRequest.Builder request = ReplicaManagerClassServer.PropagateStatePushRequest.newBuilder();
        request.setClassState(this._classObj.getClassState());
        request.getTimestampsMap().putAll(this._timestamps);
        ReplicaManagerClassServer.PropagateStatePushResponse response = _stub.propagateStatePush(request.build());

    }

    public void propagateStatePull(boolean isPrimary) {
        // if primary server, class object is already up-to-date TODO: change this condition for phase 3
        if (!isPrimary) {
            getNewStubWithAddress(false);
            ReplicaManagerClassServer.PropagateStatePullRequest request = ReplicaManagerClassServer.PropagateStatePullRequest.newBuilder().build();
            ReplicaManagerClassServer.PropagateStatePullResponse response = _stub.propagateStatePull(request);
            if (response.getCode() == ClassesDefinitions.ResponseCode.OK) {
                this._classObj.setClassState(response.getClassState());
                this._timestamps.putAll(response.getTimestampsMap());
            }
        }
    }
}
