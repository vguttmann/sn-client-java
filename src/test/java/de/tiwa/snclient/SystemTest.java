package de.tiwa.snclient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SystemTest {

    public static String bearerToken = "";

    public static String mk = "";

    public static String ak = "";

    public static String backendUrl = "https://sync.standardnotes.org";

    public static String mail = "CHANGE ME";

    public static String pw = "CHANGE ME";

    private StandardNotesHighLevelClient sut;

    /*
        take care:
        - do not use production account for testing. All notes and tags are DELETED before each running test
        - login is locked for ~1 hour after too many calls on login API. So save your credentials (mk, ak, bearerToken) and hardcode them.
      */
    @BeforeClass
    public static void beforeClass() {
        StandardNotesHighLevelClient highLevelClient = StandardNotesClientFactory
                .createHighLevelClient(mail, pw, Constants.DEFAULT_BACKEND_URL);
        ak = highLevelClient.getSettings().ak;
        bearerToken = highLevelClient.getSettings().bearerToken;
        mk = highLevelClient.getSettings().mk;
        backendUrl = highLevelClient.getSettings().backendUrl;
        System.out.println(bearerToken);
        System.out.println(mk);
        System.out.println(ak);
        System.out.println(backendUrl);
    }


    @Before
    public void before() {
        sut = new StandardNotesHighLevelClientImpl(new Client(new ClientSettings(bearerToken, mk, ak, backendUrl)));
        sut.deleteNotes(sut.getNotes());
        sut.deleteTags(sut.getTags());
    }

    private Note getNote() {
        Note note = new Note();
        note.title = "title";
        note.text = "text";
        note.created_at = "2000-06-05T14:53:05.122Z";
        return note;
    }

    private Tag getTag() {
        Tag tag = new Tag();
        tag.title = "title";
        tag.created_at = "2000-06-05T14:53:05.122Z";
        return tag;
    }

    @Test
    public void createNote() {
        // prepare
        List<Note> notes = new ArrayList<>();
        Note note = getNote();
        notes.add(note);

        // execute
        sut.createOrUpdateNotes(notes);
        List<Note> notesResponse = sut.getNotes();

        // verify
        Assert.assertEquals(notesResponse.get(0).text, note.text);
        Assert.assertEquals(notesResponse.get(0).title, note.title);
        Assert.assertEquals(notesResponse.get(0).created_at, note.created_at);
        Assert.assertNotNull(notesResponse.get(0).updated_at);
    }

    @Test
    public void deleteNote() {
        // prepare
        List<Note> notes = new ArrayList<>();
        Note note = getNote();
        notes.add(note);
        List<Note> noteToDelte = sut.createOrUpdateNotes(notes);

        // execute
        List<Note> notesSynct1 = sut.getNotes();
        sut.deleteNotes(noteToDelte);
        List<Note> notesSynct2 = sut.getNotes();

        // verify
        Assert.assertEquals(1, notesSynct1.size());
        Assert.assertEquals(0, notesSynct2.size());
    }

    @Test
    public void updateNote() {
        // prepare
        List<Note> notes = new ArrayList<>();
        Note note = getNote();
        notes.add(note);

        // execute
        List<Note> syncedNote = sut.createOrUpdateNotes(notes);
        syncedNote.get(0).title = "new title";
        syncedNote.get(0).text = "new text";
        syncedNote.get(0).created_at = "2010-01-01T14:53:05.122Z";
        sut.createOrUpdateNotes(syncedNote);
        List<Note> noteAfterUpdate = sut.getNotes();

        // verify
        Assert.assertEquals(syncedNote.get(0).text, noteAfterUpdate.get(0).text);
        Assert.assertEquals(syncedNote.get(0).title, noteAfterUpdate.get(0).title);
        Assert.assertEquals(syncedNote.get(0).created_at, noteAfterUpdate.get(0).created_at);
        Assert.assertEquals(syncedNote.get(0).uuid, noteAfterUpdate.get(0).uuid);
        Assert.assertNotNull(noteAfterUpdate.get(0).updated_at);
        Assert.assertNotEquals(syncedNote.get(0).updated_at, noteAfterUpdate.get(0).updated_at);
    }

    @Test
    public void createTag() {
        // prepare
        List<Tag> tags = new ArrayList<>();
        Tag tag = getTag();
        tags.add(tag);

        // execute
        sut.createOrUpdateTags(tags);
        List<Tag> tagsResponse = sut.getTags();

        // verify
        Assert.assertEquals(tagsResponse.get(0).title, tag.title);
        Assert.assertEquals(tagsResponse.get(0).created_at, tag.created_at);
        Assert.assertNotNull(tagsResponse.get(0).updated_at);
    }

    @Test
    public void deleteTag() {
        // prepare
        List<Tag> tags = new ArrayList<>();
        Tag tag = getTag();
        tags.add(tag);
        List<Tag> tagToDelte = sut.createOrUpdateTags(tags);

        // execute
        List<Tag> tagsSynct1 = sut.getTags();
        sut.deleteTags(tagToDelte);
        List<Tag> tagsSynct2 = sut.getTags();

        // verify
        Assert.assertEquals(1, tagsSynct1.size());
        Assert.assertEquals(0, tagsSynct2.size());
    }

    @Test
    public void updateTag() {
        // prepare
        List<Tag> tags = new ArrayList<>();
        Tag note = getTag();
        tags.add(note);

        // execute
        List<Tag> syncedTags = sut.createOrUpdateTags(tags);
        syncedTags.get(0).title = "new title";
        syncedTags.get(0).created_at = "2010-01-01T14:53:05.122Z";
        sut.createOrUpdateTags(syncedTags);
        List<Tag> tagAfterUpdate = sut.getTags();

        // verify
        Assert.assertEquals(syncedTags.get(0).title, tagAfterUpdate.get(0).title);
        Assert.assertEquals(syncedTags.get(0).created_at, tagAfterUpdate.get(0).created_at);
        Assert.assertEquals(syncedTags.get(0).uuid, tagAfterUpdate.get(0).uuid);
        Assert.assertNotNull(tagAfterUpdate.get(0).updated_at);
        Assert.assertNotEquals(syncedTags.get(0).updated_at, tagAfterUpdate.get(0).updated_at);
    }

    @Test
    public void createTagWithNote() {
        // prepare
        List<Note> notes = new ArrayList<>();
        Note note = getNote();
        notes.add(note);
        List<Note> updateNotes = sut.createOrUpdateNotes(notes);

        List<Tag> tags = new ArrayList<>();
        Tag tag = getTag();
        tag.referenceNoteUuid = new ArrayList<>();
        tag.referenceNoteUuid.add(updateNotes.get(0).uuid);
        tags.add(tag);

        // execute
        sut.createOrUpdateTags(tags);
        List<Tag> syncedTags = sut.getTags();

        // verify
        Assert.assertEquals(syncedTags.get(0).title, tag.title);
        Assert.assertEquals(syncedTags.get(0).created_at, tag.created_at);

        Assert.assertNotNull(syncedTags.get(0).updated_at);
        Assert.assertEquals(syncedTags.get(0).referenceNoteUuid.get(0), note.uuid);
    }

    @Test
    public void getNoteByTag() {
        // prepare
        List<Note> notes = new ArrayList<>();
        Note note1 = getNote();
        Note note2 = getNote();
        Note note3 = getNote();
        Note note4 = getNote();
        notes.add(note1);
        notes.add(note2);
        notes.add(note3);
        notes.add(note4);

        List<Note> updateNotes = sut.createOrUpdateNotes(notes);

        List<Tag> tags = new ArrayList<>();
        Tag tag = getTag();
        Tag tag2 = getTag();
        tag.referenceNoteUuid = new ArrayList<>();
        tag.title = "tagWithNode";
        tag.referenceNoteUuid.add(updateNotes.get(0).uuid);
        tag.referenceNoteUuid.add(updateNotes.get(1).uuid);
        tags.add(tag);
        tags.add(tag2);
        List<Tag> synctTags = sut.createOrUpdateTags(tags);
        List<Note> notesByTag = null;
        for (Tag tagSynct : synctTags) {
            if (tagSynct.title.equals("tagWithNode")) {
                // execute
                notesByTag = sut.getNotesBy(tag);
            }
        }

        // verify
        Assert.assertEquals(2, notesByTag.size());

    }

}
