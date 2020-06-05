package de.tiwa.snclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StandardNotesHighLevelClientImpl implements StandardNotesHighLevelClient {

    private final Client client;

    public StandardNotesHighLevelClientImpl(Client client) {
        this.client = client;
    }

    @Override
    public ClientSettings getSettings() {
        return client.getClientSettings();
    }

    @Override
    public List<Note> getNotes() {
        try {
            ResponseContainer responseContainer = sync(new RequestContainer());
            List<Item> items = new ArrayList<>();

            items.addAll(responseContainer.retrieved_items);
            items.addAll(responseContainer.saved_items);
            return extractNotes(items);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Tag> extractTags(List<Item> items) throws IOException {
        List<Tag> tags = new ArrayList<>();
        List<Item> tagItems = items.stream().filter(item -> item.content_type.equals("Tag"))
                .collect(Collectors.toList());
        for (Item item : tagItems) {
            tags.add(TagMapper.map(item));
        }
        return tags;
    }

    private List<Note> extractNotes(List<Item> items) throws IOException {
        List<Note> notes = new ArrayList<>();
        List<Item> noteItems = items.stream().filter(item -> item.content_type.equals("Note"))
                .collect(Collectors.toList());
        for (Item item : noteItems) {
            notes.add(NoteMapper.map(item));
        }
        return notes;
    }

    private ResponseContainer sync(RequestContainer requestContainer) {
        return client.syncNotes(requestContainer);
    }

    @Override
    public void deleteNotes(List<Note> notes) {
        try {
            List<Item> items = new ArrayList<>();
            for (Note note : notes) {
                Item item = NoteMapper.map(note);
                item.deleted = true;
                items.add(item);
            }
            RequestContainer requestContainer = new RequestContainer();
            requestContainer.items = items;
            requestContainer.limit = 1;
            client.syncNotes(requestContainer);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public List<Note> createOrUpdateNotes(List<Note> notes) {
        try {
            List<Item> items = new ArrayList<>();
            for (Note note : notes) {
                Item item = NoteMapper.map(note);
                items.add(item);
            }
            RequestContainer requestContainer = new RequestContainer();
            requestContainer.items = items;
            requestContainer.limit = 1;
            client.syncNotes(requestContainer);
            return notes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Tag> getTags() {
        try {
            ResponseContainer responseContainer = sync(new RequestContainer());
            List<Item> items = new ArrayList<>();

            items.addAll(responseContainer.retrieved_items);
            items.addAll(responseContainer.saved_items);
            return extractTags(items);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Tag> createOrUpdateTags(List<Tag> tags) {
        try {
            List<Item> items = new ArrayList<>();
            for (Tag tag : tags) {
                Item item = TagMapper.map(tag);
                items.add(item);
            }
            RequestContainer requestContainer = new RequestContainer();
            requestContainer.limit = 1;
            requestContainer.items = items;
            client.syncNotes(requestContainer);
            return tags;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteTags(List<Tag> tags) {
        try {
            List<Item> items = new ArrayList<>();
            for (Tag tag : tags) {
                Item item = TagMapper.map(tag);
                item.deleted = true;
                items.add(item);
            }
            RequestContainer requestContainer = new RequestContainer();
            requestContainer.items = items;
            requestContainer.limit = 1;
            client.syncNotes(requestContainer);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Note> getNotesBy(Tag tag) {
        try {
            ResponseContainer responseContainer = sync(new RequestContainer());
            List<Item> items = new ArrayList<>();

            items.addAll(responseContainer.retrieved_items);
            items.addAll(responseContainer.saved_items);
            List<Note> notes = extractNotes(items);
            List<Note> notesWithTag = new ArrayList<>();
            for (String uuid : tag.referenceNoteUuid) {
                for (Note note : notes) {
                    if (note.uuid.equals(uuid)) {
                        notesWithTag.add(note);
                    }
                }
            }

            return notesWithTag;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
