package de.tiwa.snclient;

public class StandardNotesLowLevelClientImpl implements StandardNotesLowLevelClient {

    private final Client client;

    public StandardNotesLowLevelClientImpl(Client client) {
        this.client = client;
    }

    @Override
    public ClientSettings getSettings() {
        return client.getClientSettings();
    }

    @Override
    public ResponseContainer syncNotes(RequestContainer requestContainer) {
        if (requestContainer == null) {
            requestContainer = new RequestContainer();
        }
        return client.syncNotes(requestContainer);
    }
}
