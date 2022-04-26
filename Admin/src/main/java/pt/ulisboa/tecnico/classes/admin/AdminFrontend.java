package pt.ulisboa.tecnico.classes.admin;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.classes.NameServerFrontend;
import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;

import java.util.List;

/** This class abstracts all the stub calls executed by the Admin client */
public class AdminFrontend {

  private final NameServerFrontend _nameServer;
  private ManagedChannel _channel;
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
    _channel =
        ManagedChannelBuilder.forTarget(
                _nameServer
                    .lookup(AdminServiceGrpc.SERVICE_NAME, qualifiers, serverId)
                    .getAddress())
            .usePlaintext()
            .build();
    this._stub = AdminServiceGrpc.newBlockingStub(_channel);
  }

  /**
   * Sends a dump request to the server and returns the internal class state. In case of error,
   * throws the ResponseCode as a ResponseException
   *
   * @param qualifiers target server qualifiers
   * @param serverId target server id (0 if qualifier is P)
   * @return ClassesDefinitions.ClassState
   * @throws StatusRuntimeException
   * @throws ResponseException
   */
  public ClassState dump(List<String> qualifiers, String serverId)
      throws StatusRuntimeException, ResponseException {
    AdminRPCDump rpcCall = new AdminRPCDump(qualifiers);

    getNewStubWithQualifiers(qualifiers, serverId);
    rpcCall.setStub(this._stub);
    rpcCall.exec();

    return rpcCall.getResponse().getClassState();
  }

  /**
   * Sends an activate request for the specified server. In case of error, throws the ResponseCode
   * as a ResponseException
   *
   * @param qualifiers target server qualifiers
   * @param serverId target server id (0 if qualifier is P)
   * @return ResponseCode
   * @throws StatusRuntimeException
   * @throws ResponseException
   */
  public ResponseCode activate(List<String> qualifiers, String serverId)
      throws StatusRuntimeException, ResponseException {
    AdminRPCActivate rpcCall = new AdminRPCActivate(qualifiers);

    getNewStubWithQualifiers(qualifiers, serverId);
    rpcCall.setStub(this._stub);
    rpcCall.exec();

    return rpcCall.getResponse().getCode();
  }

  /**
   * Sends a deactivate request for the specified server. In case of error, throws the ResponseCode
   * as a ResponseException
   *
   * @param qualifiers target server qualifiers
   * @param serverId target server id (0 if qualifier is P)
   * @return ResponseCode
   * @throws StatusRuntimeException
   * @throws ResponseException
   */
  public ResponseCode deactivate(List<String> qualifiers, String serverId)
      throws StatusRuntimeException, ResponseException {
    AdminRPCDeactivate rpcCall = new AdminRPCDeactivate(qualifiers);

    getNewStubWithQualifiers(qualifiers, serverId);
    rpcCall.setStub(this._stub);
    rpcCall.exec();

    return rpcCall.getResponse().getCode();
  }

  /**
   * Sends a deactivateGossip request for the specified server. In case of error, throws the
   * ResponseCode as a ResponseException
   *
   * @param qualifiers target server qualifiers
   * @param serverId target server id (0 if qualifier is P)
   * @return ResponseCode
   * @throws StatusRuntimeException
   * @throws ResponseException
   */
  public ResponseCode deactivateGossip(List<String> qualifiers, String serverId)
      throws StatusRuntimeException, ResponseException {
    AdminRPCDeactivateGossip rpcCall = new AdminRPCDeactivateGossip(qualifiers);

    getNewStubWithQualifiers(qualifiers, serverId);
    rpcCall.setStub(this._stub);
    rpcCall.exec();

    return rpcCall.getResponse().getCode();
  }

  /**
   * Sends an activateGossip request for the specified server. In case of error, throws the
   * ResponseCode as a ResponseException
   *
   * @param qualifiers target server qualifiers
   * @param serverId target server id (0 if qualifier is P)
   * @return ResponseCode
   * @throws StatusRuntimeException
   * @throws ResponseException
   */
  public ResponseCode activateGossip(List<String> qualifiers, String serverId)
      throws StatusRuntimeException, ResponseException {
    AdminRPCActivateGossip rpcCall = new AdminRPCActivateGossip(qualifiers);

    getNewStubWithQualifiers(qualifiers, serverId);
    rpcCall.setStub(this._stub);
    rpcCall.exec();

    return rpcCall.getResponse().getCode();
  }

  /**
   * Sends a gossip request for the specified server. In case of error, throws the ResponseCode as a
   * ResponseException
   *
   * @param qualifiers target server qualifiers
   * @param serverId target server id (0 if qualifier is P)
   * @return ResponseCode
   * @throws StatusRuntimeException
   * @throws ResponseException
   */
  public ResponseCode gossip(List<String> qualifiers, String serverId)
      throws StatusRuntimeException, ResponseException {
    AdminRPCGossip rpcCall = new AdminRPCGossip(qualifiers);

    getNewStubWithQualifiers(qualifiers, serverId);
    rpcCall.setStub(this._stub);
    rpcCall.exec();

    return rpcCall.getResponse().getCode();
  }
}
