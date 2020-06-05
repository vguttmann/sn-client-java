package de.tiwa.snclient;

public interface StandardNotesLowLevelClient {

    ClientSettings getSettings();

    ResponseContainer syncNotes(RequestContainer requestContainer);
}
