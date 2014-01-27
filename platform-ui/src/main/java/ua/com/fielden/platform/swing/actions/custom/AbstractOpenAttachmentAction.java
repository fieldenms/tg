package ua.com.fielden.platform.swing.actions.custom;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.blocking.IBlockingLayerProvider;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

/**
 * This action downloads an attachment into a temporary location and opens it up.
 *
 * @author TG Team
 *
 */
public abstract class AbstractOpenAttachmentAction extends Command<File> {

    private final IAttachment coAttachment;
    final IBlockingLayerProvider blockingLayerProvider;

    private final boolean largeIcons;

    public AbstractOpenAttachmentAction(final IAttachment coAttachment, final IBlockingLayerProvider blockingLayerProvider) {
	this("Edit", true, coAttachment, blockingLayerProvider);
    }

    public AbstractOpenAttachmentAction(final String title, final boolean largeIcons, final IAttachment coAttachment, final IBlockingLayerProvider blockingLayerProvider) {
	super(title);
	this.largeIcons = largeIcons;
	this.coAttachment = coAttachment;
	this.blockingLayerProvider = blockingLayerProvider;
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

	lock();

	setMessage("Preparing to download attachment...");
	if (getAttachment() == null) {
	    unlock();
	    return false;
	}

	return true;
    }

    @Override
    protected File action(final ActionEvent e) throws Exception {
	setMessage("Creating temp file...");

	final String tmp_file_prefix = "open_";
	final String tmp_file_sufix = "." + getAttachment().getFileExtension();
	final File asFile;

	try {
	    final Path scopePath = Files.createTempFile(tmp_file_prefix, tmp_file_sufix);

	    setMessage("Downloading attachment...");
	    final byte[] content = coAttachment.download(getAttachment());

	    asFile = scopePath.toFile();
	    asFile.deleteOnExit();

	    setMessage("Saving attachment...");
	    final FileOutputStream fo = new FileOutputStream(asFile);
	    fo.write(content);
	    fo.flush();
	    fo.close();

	    setMessage("Opening attachment...");
	    try {
		Desktop.getDesktop().open(asFile);
	    } catch (final UnsupportedOperationException ex) {
		Desktop.getDesktop().edit(asFile);
	    }

	} catch (final Exception ex) {
	    setMessage("Could not open...");
	    unlock();
	    throw ex;
	}
	return asFile;
    }

    @Override
    protected void postAction(final File file) {
	try {
	    setMessage("Completed...");
	    super.postAction(file);
	} finally {
	    unlock();
	}
    }

    protected void lock() {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		if (blockingLayerProvider.getBlockingLayer() != null) {
		    blockingLayerProvider.getBlockingLayer().setLocked(true);
		}
	    }
	});
    }

    protected void unlock() {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		if (blockingLayerProvider.getBlockingLayer() != null) {
		    blockingLayerProvider.getBlockingLayer().setLocked(false);
		}
	    }
	});
    }

    protected void setMessage(final String msg) {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		if (blockingLayerProvider.getBlockingLayer() != null) {
		    blockingLayerProvider.getBlockingLayer().setText(msg);
		}
	    }
	});
    }
}
