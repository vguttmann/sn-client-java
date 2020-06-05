package de.tiwa.snclient;

import java.io.IOException;
import java.util.UUID;

import static de.tiwa.snclient.Utils.ContentToJson;
import static de.tiwa.snclient.Utils.jsonToContent;

public class NoteMapper {

    public static Note map(Item item) throws IOException {
        if (!item.content_type.equals(Constants.NOTE_TYPE)) {
            throw new IllegalArgumentException();
        }
        Content content = jsonToContent(item);
        Note note = new Note();
        note.uuid = item.uuid;
        note.created_at = item.created_at;
        note.updated_at = item.updated_at;
        note.text = content.text;
        note.title = content.title;

        return note;
    }

    public static Item map(Note note) throws IOException {
        Item item = new Item();
        item.content_type = Constants.NOTE_TYPE;
        item.created_at = note.created_at;
        item.updated_at = note.updated_at;
        if (note.uuid == null || note.uuid.equals("")) {
            note.uuid = UUID.randomUUID().toString();
        }
        item.uuid = note.uuid;
        Content content = new Content();
        content.text = note.text;
        content.title = note.title;
        item.decryptet_content = ContentToJson(content);
        return item;
    }
}
