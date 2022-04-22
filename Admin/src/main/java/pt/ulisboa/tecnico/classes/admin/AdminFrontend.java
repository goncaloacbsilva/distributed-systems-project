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
        getNewStubWithQualifiers(qualifiers, serverId);
        AdminClassServer.DumpResponse response = _stub.dump(AdminClassServer.DumpRequest.getDefaultInstance());

        if (response.getCode() == ResponseCode.OK) {
            return response.getClassState();
        } else {
            throw new ResponseException(response.getCode());
        }
    }

    public ResponseCode activate(List<String> qualifiers, String serverId) throws StatusRuntimeException {
        getNewStubWithQualifiers(qualifiers,serverId);
        AdminClassServer.ActivateResponse response = _stub.activate(AdminClassServer.ActivateRequest.getDefaultInstance());

        return response.getCode();
    }

    public ResponseCode deactivate(List<String> qualifiers, String serverId) throws StatusRuntimeException {
        getNewStubWithQualifiers(qualifiers,serverId);
        AdminClassServer.DeactivateResponse response = _stub.deactivate(AdminClassServer.DeactivateRequest.getDefaultInstance());

        return response.getCode();
    }
    public ResponseCode deactivateGossip(List<String> qualifiers, String serverId) throws StatusRuntimeException {
        getNewStubWithQualifiers(qualifiers,serverId);
        AdminClassServer.DeactivateGossipResponse response = _stub.deactivateGossip(AdminClassServer.DeactivateGossipRequest.getDefaultInstance());
        return response.getCode();
    }
    public ResponseCode activateGossip(List<String> qualifiers, String serverId) throws StatusRuntimeException {
        getNewStubWithQualifiers(qualifiers,serverId);
        AdminClassServer.ActivateGossipResponse response = _stub.activateGossip(AdminClassServer.ActivateGossipRequest.getDefaultInstance());
        return response.getCode();
    }
    public ResponseCode gossip(List<String> qualifiers, String serverId) throws StatusRuntimeException {
        getNewStubWithQualifiers(qualifiers,serverId);
        AdminClassServer.GossipResponse response = _stub.gossip(AdminClassServer.GossipRequest.getDefaultInstance());
        return response.getCode();
    }
}
