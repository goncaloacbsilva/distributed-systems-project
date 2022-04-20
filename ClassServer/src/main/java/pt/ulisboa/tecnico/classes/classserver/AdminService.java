package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Admin Service remote procedure calls
 */
public class AdminService extends AdminServiceGrpc.AdminServiceImplBase {

    private final ClassStateWrapper _classObj;
    private final HashMap<String, Boolean> _properties;

    private final ReplicaManagerFrontend _replicaManager;
    private static final Logger LOGGER = Logger.getLogger(AdminService.class.getName());

    /**
     * Creates an instance of AdminService
     *
     * @param classObj    shared class state object
     * @param enableDebug debug flag
     */
    public AdminService(ClassStateWrapper classObj, boolean enableDebug, HashMap<String, Boolean> properties, ReplicaManagerFrontend replicaManager) {
        super();
        this._classObj = classObj;
        this._replicaManager = replicaManager;
        this._properties = properties;

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

    @Override
    public void activate(AdminClassServer.ActivateRequest request, StreamObserver<AdminClassServer.ActivateResponse> responseObserver) {

        LOGGER.info("SERVER IS NOW ONLINE");
        AdminClassServer.ActivateResponse.Builder response = AdminClassServer.ActivateResponse.newBuilder();
        _properties.put("isActive", true);
        responseObserver.onNext(response.setCode(ResponseCode.OK).build());
        responseObserver.onCompleted();
    }

    @Override
    public void deactivate(AdminClassServer.DeactivateRequest request, StreamObserver<AdminClassServer.DeactivateResponse> responseObserver) {

        LOGGER.info("SERVER IS NOW OFFLINE");
        AdminClassServer.DeactivateResponse.Builder response = AdminClassServer.DeactivateResponse.newBuilder();
        this._properties.put("isActive", false);
        responseObserver.onNext(response.setCode(ResponseCode.OK).build());
        responseObserver.onCompleted();
    }

    @Override
    public void activateGossip(AdminClassServer.ActivateGossipRequest request, StreamObserver<AdminClassServer.ActivateGossipResponse> responseObserver) {
        LOGGER.info("GOSSIP IS NOW ACTIVE");
        AdminClassServer.ActivateGossipResponse.Builder response = AdminClassServer.ActivateGossipResponse.newBuilder();
        this._properties.put("GossipActive", true);
        responseObserver.onNext(response.setCode(ResponseCode.OK).build());
        responseObserver.onCompleted();
    }

    @Override
    public void deactivateGossip(AdminClassServer.DeactivateGossipRequest request, StreamObserver<AdminClassServer.DeactivateGossipResponse> responseObserver) {
        LOGGER.info("GOSSIP IS NOW DEACTIVATED");
        AdminClassServer.DeactivateGossipResponse.Builder response = AdminClassServer.DeactivateGossipResponse.newBuilder();
        this._properties.put("GossipActive", false);
        responseObserver.onNext(response.setCode(ResponseCode.OK).build());
        responseObserver.onCompleted();
    }

    @Override
    public void gossip(AdminClassServer.GossipRequest request, StreamObserver<AdminClassServer.GossipResponse> responseObserver) {
        LOGGER.info("RECIEVED GOSSIP REQUEST");
        if (this._properties.get("GossipActive")) {
            this._replicaManager.propagateStatePush();
            LOGGER.info("PROPAGTED STATE");
        } else {
            LOGGER.info("GOSSIP IS INACTIVE");
        }
        AdminClassServer.GossipResponse.Builder response = AdminClassServer.GossipResponse.newBuilder();
        responseObserver.onNext(response.setCode(ResponseCode.OK).build());
        responseObserver.onCompleted();
    }
}
