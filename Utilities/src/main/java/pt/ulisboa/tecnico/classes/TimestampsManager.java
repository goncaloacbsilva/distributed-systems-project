package pt.ulisboa.tecnico.classes;

import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer;

import java.util.HashMap;
import java.util.Map;

/** This class manages the timestamp of each individual server */
public class TimestampsManager {
  private final HashMap<String, Integer> _timestamps;

  public TimestampsManager(HashMap<String, Integer> _timestamps) {
    this._timestamps = _timestamps;
  }

  public TimestampsManager(HashMap<String, Integer> timestamps, NameServerFrontend nameServer) {
    this._timestamps = timestamps;
    for (ClassServerNamingServer.ServerEntry entry : nameServer.list()) {
      this._timestamps.put(entry.getAddress(), 0);
    }
  }

  public TimestampsManager(NameServerFrontend nameServer) {
    this._timestamps = new HashMap<String, Integer>();

    // Initialize timestamps
    for (ClassServerNamingServer.ServerEntry entry : nameServer.list()) {
      this._timestamps.put(entry.getAddress(), 0);
    }
  }

  public HashMap<String, Integer> getTimestamps() {
    return _timestamps;
  }

  public void setTimestamps(Map<String, Integer> timestamps) {
    this._timestamps.putAll(timestamps);
  }

  /**
   * Compares incoming timestamps to current ones and updates only the out of date timestamps
   *
   * @param newTimestamps incoming timestamps
   */
  public void updateTimestamps(Map<String, Integer> newTimestamps) {
    for (String replica : newTimestamps.keySet()) {
      int newTimestampValue = newTimestamps.get(replica);

      if (this._timestamps.containsKey(replica)) {
        if (this._timestamps.get(replica) < newTimestampValue) {
          this._timestamps.put(replica, newTimestampValue);
        }
      } else {
        this._timestamps.put(replica, newTimestampValue);
      }
    }
  }

  public void putTimestamp(String address, int value) {
    this._timestamps.put(address, value);
  }

  /**
   * Checks whether local state timestamps are updated
   *
   * @param newTimestamps timestamps from the incoming state
   * @return boolean that dictates whether timestamps are up-to-date or not
   */
  public boolean isTimestampMostUptoDate(Map<String, Integer> newTimestamps) {
    Map<String, Integer> incomingTimestamps = new HashMap<>(newTimestamps);

    for (String address : this._timestamps.keySet()) {

      int currentValue = this._timestamps.get(address);

      if (!incomingTimestamps.containsKey(address)) {
        incomingTimestamps.put(address, 0);
      }

      int newValue = incomingTimestamps.get(address);

      if (newValue > currentValue) {
        return true;
      }

      incomingTimestamps.remove(address);
    }

    // If the new timestamps have new replicas that we don't have (and they have a value > 0) we
    // also need to update
    for (Integer value : incomingTimestamps.values()) {
      if (value > 0) {
        return true;
      }
    }

    return false;
  }
}
