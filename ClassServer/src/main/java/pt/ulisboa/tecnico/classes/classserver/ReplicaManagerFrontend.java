package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;
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

    private void getNewStubWithAddress(String qualifier) {
        if (_channel != null) {
            _channel.shutdown();
        }

        List<ClassServerNamingServer.ServerEntry> servers = this._nameServer.list().stream().filter(serverEntry -> serverEntry.getQualifiersList().contains(qualifier)).toList();
        Random rand = new Random();
        String target;

        if (qualifier.equals("P")) {
            target = servers.get(rand.nextInt(servers.size())).getAddress();
        }

        _channel = ManagedChannelBuilder.forTarget().usePlaintext().build();
        this._stub = AdminServiceGrpc.newBlockingStub(_channel);
    }
}
