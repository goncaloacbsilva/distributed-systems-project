package pt.ulisboa.tecnico.classes.classserver;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.classes.NameServerFrontend;
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

  private HashMap<String, Integer> _timestamps;
  private HashMap<String, Integer> _previousTimestamps;

  private ReplicaManagerGrpc.ReplicaManagerBlockingStub _stub;
  private final HashMap<String, Boolean> _properties;
  private String _address;

  public ReplicaManagerFrontend(ClassStateWrapper classObj, boolean enableDebug, HashMap<String, Boolean> properties, NameServerFrontend nameServer, String address, HashMap<String, Integer> timestamps) {
    this._classObj = classObj;
    this._nameServer = nameServer;
    this._address = address;
    this._timestamps = timestamps;
    this._properties = properties;

    if (!enableDebug) {
      LOGGER.setLevel(Level.OFF);
    }

    // Initialize timestamps
    for (ClassServerNamingServer.ServerEntry entry : this._nameServer.list()) {
      this._timestamps.put(entry.getAddress(), 0);
    }

    // Insert our address manually because it's not registered yet
    this._timestamps.put(this._address, 0);


    LOGGER.info("[ReplicaManager]: Init Timestamps \n" + this._timestamps);

    this._previousTimestamps = new HashMap<String, Integer>(this._timestamps);
  }

  public void updateTimestamp() {
    this._timestamps.put(this._address, this._timestamps.get(this._address) + 1);
    LOGGER.info(
        "[ReplicaManager] Updated timestamp "
            + this._address
            + " "
            + (this._timestamps.get(this._address) - 1)
            + " -> "
            + this._timestamps.get(this._address));
  }

  private void getNewStubWithAddress(String address) {
    if (_channel != null) {
      _channel.shutdown();
    }
    _channel = ManagedChannelBuilder.forTarget(address).usePlaintext().build();
    this._stub = ReplicaManagerGrpc.newBlockingStub(_channel);
  }

  public void propagateStatePush() {
    if (!this._previousTimestamps.equals(this._timestamps) && _properties.get("isActive") && _properties.get("isPrimary")) {
      LOGGER.info("Propagating State...");
      List<ClassServerNamingServer.ServerEntry> servers =
          this._nameServer.list().stream()
              .filter(serverEntry -> serverEntry.getQualifiersList().contains("S"))
              .toList();

      for (ClassServerNamingServer.ServerEntry server : servers) {
        ReplicaManagerClassServer.PropagateStatePushRequest.Builder request = ReplicaManagerClassServer.PropagateStatePushRequest.newBuilder();

        getNewStubWithAddress(server.getAddress());
        request.setClassState(this._classObj.getClassState());

        request.setPrimaryAddress(this._address);
        request.putAllTimestamps(this._timestamps);

        ReplicaManagerClassServer.PropagateStatePushResponse response = _stub.propagateStatePush(request.build());

        if (response.getCode() == ClassesDefinitions.ResponseCode.OK) {
          this._previousTimestamps = this._timestamps;
        }

        LOGGER.info("[ReplicaManager Frontend] Propagated Timestamps: " + this._timestamps);
      }

    }
  }

  public void propagateStatePull() {
    // if primary server, class object is already up-to-date TODO: change this condition for phase 3
    if (_properties.get("isActive")) {
      LOGGER.info("Propagating State...");
      Random rand = new Random();
      List<ClassServerNamingServer.ServerEntry> servers =
              this._nameServer.list().stream()
                      .filter(serverEntry -> serverEntry.getQualifiersList().contains("P"))
                      .toList();

      getNewStubWithAddress(servers.get(rand.nextInt(servers.size())).getAddress());

      ReplicaManagerClassServer.PropagateStatePullRequest request = ReplicaManagerClassServer.PropagateStatePullRequest.newBuilder().build();
      ReplicaManagerClassServer.PropagateStatePullResponse response = _stub.propagateStatePull(request);

      if (response.getCode() == ClassesDefinitions.ResponseCode.OK) {

        this._classObj.setClassState(response.getClassState());
        this._timestamps.putAll(response.getTimestampsMap());
      }
    }
  }
}
