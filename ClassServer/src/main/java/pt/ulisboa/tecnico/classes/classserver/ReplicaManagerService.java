package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.classserver.ReplicaManagerGrpc;
import pt.ulisboa.tecnico.classes.contract.classserver.ReplicaManagerClassServer;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReplicaManagerService extends ReplicaManagerGrpc.ReplicaManagerImplBase {
    private ClassStateWrapper _classObj;
    private HashMap<String, Integer> _timestamps;
    private final HashMap<String, Boolean> _properties;
    private static final Logger LOGGER = Logger.getLogger(ReplicaManagerService.class.getName());
    private String _address;

    public ReplicaManagerService(ClassStateWrapper classObj, boolean enableDebug, HashMap<String, Boolean> properties, String address, HashMap<String, Integer> timestamps) {
        this._classObj = classObj;
        this._timestamps = timestamps;
        this._properties = properties;
        this._address = address;

        if (!enableDebug) {
            LOGGER.setLevel(Level.OFF);
        }

        LOGGER.info("Started with debug mode enabled");
    }

    private void updateTimestamps(Map<String, Integer> newTimestamps) {
        for (String replica : this._timestamps.keySet()) {
            if (newTimestamps.containsKey(replica) && !replica.equals(_address)) {
                int newTimestampValue = newTimestamps.get(replica);

                if (this._timestamps.get(replica) < newTimestampValue) {
                    this._timestamps.put(replica, newTimestampValue);
                }
            }
        }
    }

    /**
     * propagates the primary servers state by pushing to the secondary server
     * @param request
     * @param responseObserver
     */
    @Override
    public void propagateStatePush(ReplicaManagerClassServer.PropagateStatePushRequest request, StreamObserver<ReplicaManagerClassServer.PropagateStatePushResponse> responseObserver) {
        ReplicaManagerClassServer.PropagateStatePushResponse.Builder response = ReplicaManagerClassServer.PropagateStatePushResponse.newBuilder();

        if (!_properties.get("isActive")) {
            response.setCode(ClassesDefinitions.ResponseCode.INACTIVE_SERVER);

        } else {

            int currentValue;

            if (this._timestamps.containsKey(request.getPrimaryAddress())) {
                currentValue = this._timestamps.get(request.getPrimaryAddress());
            } else {
                this._timestamps.put(request.getPrimaryAddress(), 0);
                currentValue = 0;
            }

            int newValue = request.getTimestampsMap().get(request.getPrimaryAddress());

            LOGGER.info("Received propagateStatePush request \n" + "Current Version: " + currentValue + "\nIncoming: " + newValue);

            if (newValue > currentValue) {
                LOGGER.info("[ReplicaManager] Processing request...");

                this._classObj.setClassState(request.getClassState());

                this.updateTimestamps(request.getTimestampsMap());

                LOGGER.info("Set response as OK");
                response.setCode(ClassesDefinitions.ResponseCode.OK);

                LOGGER.info("Sending propagateStatePush response");
                LOGGER.info("[ReplicaManager] Updated State to: " + this._timestamps);
            }

        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }




    // TODO: Decide whether we should also use Pull strategy
    /**
     * propagates the primary servers state by the secondary server pulling the primary servers state
     * @param request
     * @param responseObserver
     */
    /*
    @Override
    public void propagateStatePull(ReplicaManagerClassServer.PropagateStatePullRequest request, StreamObserver<ReplicaManagerClassServer.PropagateStatePullResponse> responseObserver) {
        ReplicaManagerClassServer.PropagateStatePullResponse.Builder response = ReplicaManagerClassServer.PropagateStatePullResponse.newBuilder();

        if (!_properties.get("isActive")) {
            response.setCode(ClassesDefinitions.ResponseCode.INACTIVE_SERVER);

        } else {

            LOGGER.info("Received propagateStatePull request");

            response.setClassState(this._classObj.getClassState());
            response.putAllTimestamps(this._timestamps);

            LOGGER.info("Set response as OK");
            response.setCode(ClassesDefinitions.ResponseCode.OK);

            LOGGER.info("Sending propagateStatePull response");
            LOGGER.info("[ReplicaManager] Updated State to: " + this._timestamps);

        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    */
}
