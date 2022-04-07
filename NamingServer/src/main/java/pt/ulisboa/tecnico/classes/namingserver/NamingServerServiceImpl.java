package pt.ulisboa.tecnico.classes.namingserver;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.ResponseException;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterResponse;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterRequest;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.ServerEntry;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.DeleteRequest;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.DeleteResponse;
import pt.ulisboa.tecnico.classes.contract.naming.NamingServerServiceGrpc;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NamingServerServiceImpl extends NamingServerServiceGrpc.NamingServerServiceImplBase {

    private NamingServices _services;
    private static final Logger LOGGER = Logger.getLogger(NamingServerServiceImpl.class.getName());

    public NamingServerServiceImpl(NamingServices services, boolean enableDebug) {
        super();
        this._services = services;
        if (!enableDebug) {
            LOGGER.setLevel(Level.OFF);
        }

        LOGGER.info("Started with debug mode enabled");
    }

    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        if (request.getAddress().contains(":")) {
            String[] addresParser = request.getAddress().split(":");
            try{
                Integer number = Integer.valueOf(addresParser[1]);
            }
            catch (NumberFormatException ex){
                throw new RuntimeException("Not a valid server address");
            }
            ServerEntry server = ServerEntry.newBuilder().setAddress(request.getAddress()).addAllQualifiers(request.getQualifiersList()).build();
            String serviceName = request.getServiceName();

            LOGGER.info("Registering " + server.getAddress() + " with qualifiers: " + Arrays.toString(request.getQualifiersList().toArray()) + " at " + serviceName);

            this._services.registerServer(serviceName, server);

            responseObserver.onNext(RegisterResponse.getDefaultInstance());
            responseObserver.onCompleted();
        }else {
            throw new RuntimeException("Not a valid server address");
        }

    }

    @Override
    public void lookup(LookupRequest request, StreamObserver<LookupResponse> responseObserver) {
        LOGGER.info("Searching for: " + Arrays.toString(request.getQualifiersList().toArray()) + " at " + request.getServiceName());

        Collection<ServerEntry> results = this._services.lookupServers(request.getServiceName(), request.getQualifiersList());

        LOGGER.info("Got " + results.size() + " records \n" + results);

        responseObserver.onNext(LookupResponse.newBuilder().addAllServers(results).build());
        responseObserver.onCompleted();
    }

    @Override
    public void list(ClassServerNamingServer.ListRequest request, StreamObserver<ClassServerNamingServer.ListResponse> responseObserver) {
        LOGGER.info("Listing available servers...");

        HashSet<ServerEntry> servers = new HashSet<ServerEntry>();
        ClassServerNamingServer.ListResponse.Builder response = ClassServerNamingServer.ListResponse.newBuilder();

        for (ServiceEntry service : _services.getServices().values()) {
            servers.addAll(service.getServers());
        }

        response.addAllServers(servers);

        LOGGER.info("Got " + servers.size() + " records \n" + servers);

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
        LOGGER.info("Removing " + request.getAddress() + " from " + request.getServiceName());

        this._services.deleteServer(request.getServiceName(), request.getAddress());

        responseObserver.onNext(DeleteResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
