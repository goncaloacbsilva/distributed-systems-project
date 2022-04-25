package pt.ulisboa.tecnico.classes.admin;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;


import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class abstracts all the stub calls executed by the Admin client
 */
public class AdminFrontend {

    private ManagedChannel _channel;
    private final NameServerFrontend _nameServer;
    private AdminServiceGrpc.AdminServiceBlockingStub _stub;


    /**
     * Creates an instance of AdminFrontend
     *
     * @see AdminFrontend
     */
    public AdminFrontend() {
        this._nameServer = new NameServerFrontend();
    }

    private void getNewStubWithQualifiers(List<String> qualifiers, String serverId) {
        if (_channel != null) {
            _channel.shutdown();
        }
        _channel = ManagedChannelBuilder.forTarget(_nameServer.lookup(AdminServiceGrpc.SERVICE_NAME, qualifiers, serverId).getAddress()).usePlaintext().build();
        this._stub = AdminServiceGrpc.newBlockingStub(_channel);
    }

    /**
     * Sends a dump request to the server and returns the internal class state. In case of error,
     * throws the ResponseCode as a ResponseException
     *
     * @return ClassesDefinitions.ClassState
     * @throws StatusRuntimeException
     * @throws ResponseException
     */
    public ClassState dump(List<String> qualifiers, String serverId) throws StatusRuntimeException, ResponseException {
        AdminRPCDump rpcCall = new AdminRPCDump(qualifiers, this._nameServer);

        getNewStubWithQualifiers(qualifiers, serverId);
        rpcCall.setStub(this._stub);
        rpcCall.exec();

        return rpcCall.getResponse().getClassState();
    }

    public ResponseCode activate(List<String> qualifiers, String serverId) throws StatusRuntimeException, ResponseException {
        AdminRPCActivate rpcCall = new AdminRPCActivate(qualifiers, this._nameServer);

        getNewStubWithQualifiers(qualifiers, serverId);
        rpcCall.setStub(this._stub);
        rpcCall.exec();

        return rpcCall.getResponse().getCode();
    }

    public ResponseCode deactivate(List<String> qualifiers, String serverId) throws StatusRuntimeException, ResponseException {
        AdminRPCDeactivate rpcCall = new AdminRPCDeactivate(qualifiers, this._nameServer);

        getNewStubWithQualifiers(qualifiers, serverId);
        rpcCall.setStub(this._stub);
        rpcCall.exec();

        return rpcCall.getResponse().getCode();
    }
    public ResponseCode deactivateGossip(List<String> qualifiers, String serverId) throws StatusRuntimeException, ResponseException {
        AdminRPCDeactivateGossip rpcCall = new AdminRPCDeactivateGossip(qualifiers, this._nameServer);

        getNewStubWithQualifiers(qualifiers, serverId);
        rpcCall.setStub(this._stub);
        rpcCall.exec();

        return rpcCall.getResponse().getCode();
    }
    public ResponseCode activateGossip(List<String> qualifiers, String serverId) throws StatusRuntimeException, ResponseException {
        AdminRPCActivateGossip rpcCall = new AdminRPCActivateGossip(qualifiers, this._nameServer);

        getNewStubWithQualifiers(qualifiers, serverId);
        rpcCall.setStub(this._stub);
        rpcCall.exec();

        return rpcCall.getResponse().getCode();
    }
    public ResponseCode gossip(List<String> qualifiers, String serverId) throws StatusRuntimeException, ResponseException {
        AdminRPCGossip rpcCall = new AdminRPCGossip(qualifiers, this._nameServer);

        getNewStubWithQualifiers(qualifiers, serverId);
        rpcCall.setStub(this._stub);
        rpcCall.exec();

        return rpcCall.getResponse().getCode();
    }
}
