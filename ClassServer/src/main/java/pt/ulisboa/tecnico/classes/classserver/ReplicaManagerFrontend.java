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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ReplicaManager related frontend responsible for propagate state logic and manage local timestamps
 */
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

    /**
     * Creates an instance of ReplicaManagerFrontend
     *
     * @see ReplicaManagerFrontend
     *
     * @param classObj ClassStateWrapper instance
     * @param enableDebug debug flag (enabled if true)
     * @param properties server properties HashMap
     * @param nameServer NameServerFrontend instance
     * @param address String of local address
     * @param timestamps server local timestamps HashMap
     */
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
     * Generates a stub with the supplied server address (this generated stub is private)
     * @param address
     */
    private void getNewStubWithAddress(String address) {
        if (_channel != null) {
            _channel.shutdown();
        }
        _channel = ManagedChannelBuilder.forTarget(address).usePlaintext().build();
        this._stub = ReplicaManagerGrpc.newBlockingStub(_channel);
    }

    /**
     * Updates the local timestamps when local state is changed
     */
    public void updateLocalTimestamp() {
        this._timestampsManager.putTimestamp(this._address, this._timestampsManager.getTimestamps().get(this._address) + 1);
        LOGGER.info(
                "[ReplicaManager] Updated timestamp "
                        + this._address
                        + " "
                        + (this._timestampsManager.getTimestamps().get(this._address) - 1)
                        + " -> "
                        + this._timestampsManager.getTimestamps().get(this._address));
    }

    /**
     * Propagates state to the supplied server
     * @param server target ServerEntry
     */
    private void propagate(ClassServerNamingServer.ServerEntry server) {
        ReplicaManagerClassServer.PropagateStatePushRequest.Builder request = ReplicaManagerClassServer.PropagateStatePushRequest.newBuilder();

        getNewStubWithAddress(server.getAddress());
        request.setClassState(this._classObj.getClassState());
        request.setPrimaryAddress(this._address);
        request.putAllTimestamps(this._timestampsManager.getTimestamps());

        ReplicaManagerClassServer.PropagateStatePushResponse response = _stub.propagateStatePush(request.build());

        // If propagate was successful update previous timestamps with the current timestamps
        if (response.getCode() == ClassesDefinitions.ResponseCode.OK) {
            this._previousTimestamps = new HashMap<>(this._timestampsManager.getTimestamps());
        }

        LOGGER.info("[ReplicaManager Frontend] Propagated Timestamps: " + this._timestampsManager.getTimestamps());
    }

    /**
     * Propagates the primary servers state by pushing to the secondary server
     *
     * @param forceGossip if true forces gossip
     */
    public void propagateStatePush(boolean forceGossip) {
        if ((!this._previousTimestamps.equals(this._timestampsManager.getTimestamps()) && _properties.get("isActive")) || forceGossip) {
            LOGGER.info("Propagating State...");

            // Propagate for all the servers except this one
            List<ClassServerNamingServer.ServerEntry> servers =
                    this._nameServer.list().stream()
                            .filter(serverEntry -> !serverEntry.getAddress().equals(_address))
                            .toList();

            for (ClassServerNamingServer.ServerEntry server : servers) {
                propagate(server);
            }
        }
    }

    /**
     * Returns internal instance of TimestampsManager
     * @return TimestampsManager
     */
    public TimestampsManager getTimestampsManager() {
        return _timestampsManager;
    }

    /**
     * Returns server local timestamps HashMap
     * @return timestamps HashMap
     */
    public HashMap<String, Boolean> getProperties() {
        return _properties;
    }

}
