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
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
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
    private final boolean largeIcons;

    public static enum Stage {
	FIRST(ResourceLoader.getIcon("images/download-large.png"), ResourceLoader.getIcon("images/download-small.png")), //
	SECOND(ResourceLoader.getIcon("images/upload-large.png"), ResourceLoader.getIcon("images/upload-small.png"));

	final ImageIcon largeIcon;
	final ImageIcon smallIcon;

	Stage(final ImageIcon largeIcon, final ImageIcon smallIcon) {
	    this.largeIcon = largeIcon;
	    this.smallIcon = smallIcon;
	}
    }

    public AbstractEditAttachmentAction(final IAttachment coAttachment, final IBlockingLayerProvider blockingLayerProvider) {
	this("Edit", true, coAttachment, blockingLayerProvider);
    }

    public AbstractEditAttachmentAction(final String title, final boolean largeIcons, final IAttachment coAttachment, final IBlockingLayerProvider blockingLayerProvider) {
	super(title);
	this.largeIcons = largeIcons;
	this.coAttachment = coAttachment;
	this.blockingLayerProvider = blockingLayerProvider;
	setCurrentStage(Stage.FIRST);
    }

    private void setCurrentStage(final Stage stage) {
	currentStage = stage;

	SwingUtilitiesEx.invokeAndWaitIfPossible(new Runnable() {
	    @Override
	    public void run() {
		if (largeIcons) {
		    putValue(Action.LARGE_ICON_KEY, currentStage.largeIcon); // menu should use small icon anyway
		    putValue(Action.SMALL_ICON, currentStage.smallIcon);
		} else {
		    putValue(Action.LARGE_ICON_KEY, currentStage.smallIcon);
		    putValue(Action.SMALL_ICON, currentStage.smallIcon);
		}
	    }
	});

    }

    public Stage getCurrentStage() {
	return currentStage;
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
		cancel();
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

    /**
     * Cancels the action in progress, returning to the first state.
     */
    public void cancel() {
	file = null;
	attachment = null;
	setCurrentStage(Stage.FIRST);
    }

    @Override
    protected File action(final ActionEvent e) throws Exception {
	if (Stage.FIRST == currentStage) {

	    setMessage("Creating temp file...");

	    final String tmp_file_prefix = "editing_";
	    final String tmp_file_sufix = "." + attachment.getFileExtension();
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
		super.postAction(file);
		setCurrentStage(Stage.SECOND);
	    } else if (Stage.SECOND == currentStage) {
		setMessage("Uploaded...");
		super.postAction(file);
		setCurrentStage(Stage.FIRST);
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
