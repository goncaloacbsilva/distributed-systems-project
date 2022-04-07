package pt.ulisboa.tecnico.classes.classserver;


import java.util.TimerTask;
import java.util.logging.Logger;

public class GossipScheduler extends TimerTask {
    private ReplicaManagerFrontend _replicaManagerFrontend;
    private static final Logger LOGGER = Logger.getLogger(GossipScheduler.class.getName());

    public GossipScheduler(ReplicaManagerFrontend replicaManagerFrontend) {
        super();
        this._replicaManagerFrontend = replicaManagerFrontend;
    }

    @Override
    public void run() {
        LOGGER.info("Propagating State");
        _replicaManagerFrontend.propagateStatePush(true);
    }
}
