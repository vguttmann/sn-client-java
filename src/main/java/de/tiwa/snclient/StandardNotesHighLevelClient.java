package de.tiwa.snclient;

import java.util.List;

public interface StandardNotesHighLevelClient {

    ClientSettings getSettings();

    List<Note> getNotes();

    void deleteNotes(List<Note> notes);

    List<Note> createOrUpdateNotes(List<Note> notes);

    List<Tag> getTags();

    List<Tag> createOrUpdateTags(List<Tag> tags);

    void deleteTags(List<Tag> tag);

    List<Note> getNotesBy(Tag tag);
}
