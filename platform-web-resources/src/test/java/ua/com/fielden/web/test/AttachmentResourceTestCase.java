package ua.com.fielden.web.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.junit.Test;
import org.restlet.Restlet;
import org.restlet.Router;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachmentController;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.rao.AttachmentRao;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.web.resources.RouterHelper;
import ua.com.fielden.platform.web.test.WebBasedTestCase;

/**
 * Provides unit tests for a set of {@link Attachment} related web resources, which includes testing of DAO nad RAO implementations.
 *
 * @author TG Team
 *
 */
public class AttachmentResourceTestCase extends WebBasedTestCase {
    private final IAttachmentController rao = new AttachmentRao(config.restClientUtil());

    private final static String ATTACHMENT_LOCATION = "src/test/resources/data-files/attachments";
    private final static String ORIGINAL_FILE_NAME = "TEST-FILE.TXT";
    private final static String NEW_ATTACHMENT_FILE_NAME = "NEW-ATTACHMENT-FILE.TXT";

    @Override
    protected String[] getDataSetPaths() {
	return new String[] { "src/test/resources/data-files/attachment-test-case.flat.xml" };
    }

    @Override
    public synchronized Restlet getRoot() {
	final Router router = new Router(getContext());

	final RouterHelper helper = new RouterHelper(DbDrivenTestCase.injector, DbDrivenTestCase.entityFactory);
	helper.registerAttachment(router, ATTACHMENT_LOCATION);

	return router;
    }

    @Test
    public void test_find_attachment_behaviour() {
	assertNotNull("Could not find attachment by id.", rao.findById(0L));
	assertNotNull("Could not find attachment by key.", rao.findByKey(ORIGINAL_FILE_NAME));
    }

    @Test
    public void test_query_attachment_behaviour() {
	final EntityResultQueryModel<Attachment> model = select(Attachment.class).where().prop("key").eq().val(ORIGINAL_FILE_NAME).model();
	final List<Attachment> attachments = rao.getAllEntities(from(model).model());
	assertEquals("Incorrect number of attachments.", 1, attachments.size());
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_non_persited_attachment_download() {
	rao.download(new Attachment());
    }

    @Test
    public void test_persited_attachment_download() throws Exception {
	final Attachment attachment = rao.findById(0L);
	final byte[] content = rao.download(attachment);
	assertNotNull("Content should be present", content);
	assertTrue("Content should not be empty", content.length > 0);

	final File downloadedFile = new File(ATTACHMENT_LOCATION + "/DOWNLOADED_FILE.TXT");
	final FileOutputStream fo = new FileOutputStream(downloadedFile);
	fo.write(content);
	fo.flush();
	fo.close();

	final File originalFile = new File(ATTACHMENT_LOCATION + "/" + ORIGINAL_FILE_NAME);
	assertEquals("Invalid size of the downloaded file.", originalFile.length(), downloadedFile.length());
    }

    @Test
    public void test_saving_changes_to_existing_attachment() {
	// FIXME needs to be resolved -- for some reason does not pass the correct type inform,ation during serialisation using XStream
	final String desc = "Changed description";
	final Attachment attachment = (Attachment) rao.findById(0L).setDesc(desc);

	assertEquals("Description changes have not been saved.", desc, rao.save(attachment).getDesc());
    }

    @Test
    public void test_saving_new_attachment_which_should_result_in_file_upload() {
	Attachment attachment = DbDrivenTestCase.entityFactory.newEntity(Attachment.class);
	attachment.setFile(new File(ATTACHMENT_LOCATION + "/new/" + NEW_ATTACHMENT_FILE_NAME));
	attachment.setDesc("new attachment to upload");
	attachment = rao.save(attachment);

	assertTrue("Failed to persist attachment", attachment.isPersisted());

	final byte[] content = rao.download(attachment);
	assertNotNull("Failed to upload file.", content);
	assertTrue("Content of the uploaded file should not be empty", content.length > 0);
    }

    @Test
    public void test_attachment_deletion() {
	Attachment attachment = DbDrivenTestCase.entityFactory.newEntity(Attachment.class);
	attachment.setFile(new File(ATTACHMENT_LOCATION + "/new/" + NEW_ATTACHMENT_FILE_NAME));
	attachment.setDesc("new attachment to upload");
	attachment = rao.save(attachment);

	rao.delete(attachment);

	assertNull("Deleted attachment should not exist", rao.findById(attachment.getId()));
	assertFalse("Deleted attachment file should not exist", new File(ATTACHMENT_LOCATION + "/" + NEW_ATTACHMENT_FILE_NAME).exists());
    }

}
