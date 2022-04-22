package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.TimestampsManager;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.classserver.ReplicaManagerClassServer;
import pt.ulisboa.tecnico.classes.contract.classserver.ReplicaManagerGrpc;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReplicaManagerFrontend {
    private ManagedChannel _channel;
    private ClassStateWrapper _classObj;
    private final NameServerFrontend _nameServer;
    private static final Logger LOGGER = Logger.getLogger(ReplicaManagerFrontend.class.getName());

    private HashMap<String, Integer> _previousTimestamps;

    private TimestampsManager _timestampsManager;

    private ReplicaManagerGrpc.ReplicaManagerBlockingStub _stub;
    private final HashMap<String, Boolean> _properties;
    private String _address;

    public ReplicaManagerFrontend(ClassStateWrapper classObj, boolean enableDebug, HashMap<String, Boolean> properties, NameServerFrontend nameServer, String address, HashMap<String, Integer> timestamps) {
        this._classObj = classObj;
        this._nameServer = nameServer;
        this._address = address;
        this._timestampsManager = new TimestampsManager(timestamps, nameServer);
        this._timestampsManager.putTimestamp(this._address,0);
        this._properties = properties;

        if (!enableDebug) {
            LOGGER.setLevel(Level.OFF);
        }



        LOGGER.info("[ReplicaManager]: Init Timestamps \n" + this._timestampsManager.getTimestamps());

        this._previousTimestamps = new HashMap<String, Integer>(this._timestampsManager.getTimestamps());
    }

    /**
     * updates the local timestamps
     */
    public void updateTimestamp() {
        this._timestampsManager.putTimestamp(this._address, this._timestampsManager.getTimestamps().get(this._address) + 1);
        LOGGER.info(
                "[ReplicaManager] Updated timestamp "
                        + this._address
                        + " "
                        + (this._timestampsManager.getTimestamps().get(this._address) - 1)
                        + " -> "
                        + this._timestampsManager.getTimestamps().get(this._address));
    }

    private void getNewStubWithAddress(String address) {
        if (_channel != null) {
            _channel.shutdown();
        }
        _channel = ManagedChannelBuilder.forTarget(address).usePlaintext().build();
        this._stub = ReplicaManagerGrpc.newBlockingStub(_channel);
    }

    /**
     * propagates the primary servers state by pushing to the secondary server
     */
    public void propagateStatePush() {
        if (!this._previousTimestamps.equals(this._timestampsManager.getTimestamps()) && _properties.get("isActive")) {
            LOGGER.info("Propagating State...");

            // Propagate for all the servers except this one
            List<ClassServerNamingServer.ServerEntry> servers =
                    this._nameServer.list().stream()
                            .filter(serverEntry -> !serverEntry.getAddress().equals(_address))
                            .toList();

            for (ClassServerNamingServer.ServerEntry server : servers) {
                    ReplicaManagerClassServer.PropagateStatePushRequest.Builder request = ReplicaManagerClassServer.PropagateStatePushRequest.newBuilder();

                    getNewStubWithAddress(server.getAddress());
                    request.setClassState(this._classObj.getClassState());

                    request.setPrimaryAddress(this._address);
                    request.putAllTimestamps(this._timestampsManager.getTimestamps());

                    ReplicaManagerClassServer.PropagateStatePushResponse response = _stub.propagateStatePush(request.build());

                    if (response.getCode() == ClassesDefinitions.ResponseCode.OK) {
                        this._previousTimestamps = new HashMap<>(this._timestampsManager.getTimestamps());
                    }

                    LOGGER.info("[ReplicaManager Frontend] Propagated Timestamps: " + this._timestampsManager.getTimestamps());
                }
            }

    }


    public HashMap<String, Boolean> getProperties() {
        return _properties;
    }

}
