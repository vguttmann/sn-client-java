package de.tiwa.snclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TagMapper {

    public static Tag map(Item item) throws IOException {
        if (!item.content_type.equals(Constants.TAG_TYPE)) {
            throw new IllegalArgumentException();
        }
        Tag tag = new Tag();
        List<String> noteUuid = new ArrayList<>();
        Content content = Utils.jsonToContent(item);
        tag.title = content.title;
        tag.created_at = item.created_at;
        tag.updated_at = item.updated_at;
        tag.uuid = item.uuid;
        for (Reference reference : content.references) {
            if (reference.content_type.equals("Note")) {
                noteUuid.add(reference.uuid);
            }
        }
        tag.referenceNoteUuid = noteUuid;
        return tag;
    }

    public static Item map(Tag tag) throws IOException {
        Item item = new Item();
        item.content_type = Constants.TAG_TYPE;
        item.created_at = tag.created_at;
        item.updated_at = tag.updated_at;

        if (tag.uuid == null || tag.uuid.equals("")) {
            tag.uuid = UUID.randomUUID().toString();
        }
        item.uuid = tag.uuid;
        Content content = new Content();
        List<Reference> references = new ArrayList<>();
        if (tag.referenceNoteUuid != null) {
            for (String uuid : tag.referenceNoteUuid) {
                Reference reference = new Reference();
                reference.uuid = uuid;
                reference.content_type = Constants.NOTE_TYPE;
                references.add(reference);
            }
        }
        content.title = tag.title;
        content.references = references;

        item.decryptet_content = Utils.ContentToJson(content);
        return item;
    }
}
