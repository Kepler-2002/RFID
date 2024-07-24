package util;

public class ConnectionResult {
    private boolean isConnected;
    private String connectionMessage;

    public ConnectionResult(boolean isConnected, String connectionMessage) {
        this.isConnected = isConnected;
        this.connectionMessage = connectionMessage;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getConnectionMessage() {
        return connectionMessage;
    }
}
