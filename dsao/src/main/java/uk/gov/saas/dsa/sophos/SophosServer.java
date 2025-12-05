package uk.gov.saas.dsa.sophos;

import java.util.LinkedList;

public class SophosServer {
    String ipOrName = "";
    int port = 0;
    LinkedList activeConnectionList = new LinkedList();

    public SophosServer() {
    }

    public String getIpOrName() {
        return this.ipOrName;
    }

    public void setIpOrName(String ipOrName) {
        this.ipOrName = ipOrName;
    }

    public synchronized LinkedList getActiveConnectionList() {
        return this.activeConnectionList;
    }

    public synchronized int getActiveConnectionListCount() {
        return this.activeConnectionList.size();
    }

    public synchronized void addConnection(SophosConnection newSophosConnection) {
        this.activeConnectionList.add(newSophosConnection);
    }

    public synchronized SophosConnection getConnection() {
        SophosConnection sc = (SophosConnection)this.activeConnectionList.get(0);
        this.activeConnectionList.remove(0);
        return sc;
    }

    public void setPort(int portIn) {
        this.port = portIn;
    }

    public int getPort() {
        return this.port;
    }
}

