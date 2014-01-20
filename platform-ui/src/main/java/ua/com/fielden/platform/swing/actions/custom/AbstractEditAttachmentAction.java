package ua.com.fielden.platform.swing.actions.custom;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.blocking.IBlockingLayerProvider;
import ua.com.fielden.platform.swing.utils.Dialogs;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * This is a two-stage action:
 * <ul>
 * <li>First stage -- attachment gets downloaded into a temporary location and opened up for editing. Action icon gets changed to indicate that the next time it is invoked, it will
 * perform something different.
 * <li>Second stage -- the modified file, which was downloaded in the first stage, gets uploaded, resulting in the replacement of the original file. Action returns to the state as
 * before the first stage, including icon restoration to the default one.
 * </ul>
 *
 * @author TG Team
 *
 */
public abstract class AbstractEditAttachmentAction extends Command<File> {

    private final IAttachment coAttachment;
    private File file;
    private Attachment attachment;

    final IBlockingLayerProvider blockingLayerProvider;

    private Stage currentStage;

    private static enum Stage {
	FIRST(ResourceLoader.getIcon("images/download.png")), SECOND(ResourceLoader.getIcon("images/upload.png"));

	final ImageIcon icon;

	Stage(final ImageIcon icon) {
	    this.icon = icon;
	}
    }

    public AbstractEditAttachmentAction(final IAttachment coAttachment, final IBlockingLayerProvider blockingLayerProvider) {
	super("Download");
	this.coAttachment = coAttachment;
	this.blockingLayerProvider = blockingLayerProvider;
	currentStage = Stage.FIRST;
	putValue(Action.LARGE_ICON_KEY, currentStage.icon);
	putValue(Action.SMALL_ICON, currentStage.icon);
    }

    /**
     * Implement to obtain context dependent attachment instance.
     *
     * @return
     */
    protected abstract Attachment getAttachment();

    /**
     * Implement to better position open directory dialog.
     *
     * @return
     */
    protected abstract Component getOwningComponent();

    @Override
    protected final boolean preAction() {
	final boolean flag = super.preAction();
	if (!flag) {
	    return false;
	}

	if (Stage.FIRST == currentStage) {
	    // check if there is anything to download
	    attachment = getAttachment();
	    if (attachment == null) {
		setMessage("No attachment selected...");
		unlock();
		return false;
	    }
	    setMessage("Preparing to download attachment...");
	} else if (Stage.SECOND == currentStage) {
	    setMessage("Preparing to upload attachment...");
	    final int userChoice = Dialogs.showYesNoCancelDialog(getOwningComponent(), "Would you like to upload modified attachment " + attachment.getKey() + "?", "Modified attachment upload");
	    if (JOptionPane.NO_OPTION == userChoice) {
		return false;
	    } else if (JOptionPane.CANCEL_OPTION == userChoice) {
		file = null;
		attachment = null;
		currentStage = Stage.FIRST;
		putValue(Action.LARGE_ICON_KEY, currentStage.icon);
		putValue(Action.SMALL_ICON, currentStage.icon);
		return false;
	    }
	}

	lock();

	// check if there is anything to download.
	if (getAttachment() == null) {
	    unlock();
	    return false;
	}

	return true;
    }

    @Override
    protected File action(final ActionEvent e) throws Exception {
	if (Stage.FIRST == currentStage) {

	    setMessage("Creating temp file...");

	    final String tmp_file_prefix = "scope_";
	    final String tmp_file_sufix = ".docx";
	    final File asFile;
	    try {

		final Path scopePath = Files.createTempFile(tmp_file_prefix, tmp_file_sufix);
		System.out.println("TMP: " + scopePath.toString());

		setMessage("Downloading attachment...");
		final byte[] content = coAttachment.download(getAttachment());

		asFile = scopePath.toFile();
		asFile.deleteOnExit();

		setMessage("Saving attachment...");
		final FileOutputStream fo = new FileOutputStream(asFile);
		fo.write(content);
		fo.flush();
		fo.close();

		file = asFile;

		setMessage("Opening attachment for editing...");
		try {
		    Desktop.getDesktop().edit(asFile);
		} catch (final UnsupportedOperationException ex) {
		    Desktop.getDesktop().open(asFile);
		}

		// TODO Need to place a watcher on the file.

	    } catch (final Exception ex) {
		unlock();
		throw ex;
	    }
	} else if (Stage.SECOND == currentStage) {
	    setMessage("Uploading modified attachment...");
	    attachment.setModified(true);
	    attachment.setFile(file);
	    coAttachment.save(attachment);

	    file = null;
	    attachment = null;
	}

	return file;
    }

    @Override
    protected void postAction(final File file) {
	try {
	    if (Stage.FIRST == currentStage) {
		setMessage("Click again once changes are saved...");
		currentStage = Stage.SECOND;
		super.postAction(file);
		putValue(Action.LARGE_ICON_KEY, currentStage.icon);
		putValue(Action.SMALL_ICON, currentStage.icon);
	    } else if (Stage.SECOND == currentStage) {
		setMessage("Uploaded...");
		currentStage = Stage.FIRST;
		super.postAction(file);
		putValue(Action.LARGE_ICON_KEY, currentStage.icon);
		putValue(Action.SMALL_ICON, currentStage.icon);
	    }
	} finally {
	    unlock();
	}
    }

    protected void lock() {
	if (blockingLayerProvider.getBlockingLayer() != null) {
	    blockingLayerProvider.getBlockingLayer().setLocked(true);
	}
    }

    protected void unlock() {
	if (blockingLayerProvider.getBlockingLayer() != null) {
	    blockingLayerProvider.getBlockingLayer().setLocked(false);
	}
    }

    protected void setMessage(final String msg) {
	if (blockingLayerProvider.getBlockingLayer() != null) {
	    blockingLayerProvider.getBlockingLayer().setText(msg);
	}
    }
}
