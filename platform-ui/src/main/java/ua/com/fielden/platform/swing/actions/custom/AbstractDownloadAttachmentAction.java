package ua.com.fielden.platform.swing.actions.custom;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.blocking.IBlockingLayerProvider;

/**
 * This is a convenient construct for implementing specific download attachment actions.
 *
 * @author TG Team
 *
 */
public abstract class AbstractDownloadAttachmentAction extends Command<File> {

    private final IAttachment attachmentController;
    private File prevLocation = new File(".");
    final IBlockingLayerProvider blockingLayerProvider;

    public AbstractDownloadAttachmentAction(final IAttachment attachmentController, final IBlockingLayerProvider blockingLayerProvider) {
	super("Download");
	this.attachmentController = attachmentController;
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

	setMessage("Obtaining attachment information...");
	lock();

	// check if there is anything to download.
	if (getAttachment() == null) {
	    unlock();
	    return false;
	}

	setMessage("Prompting where to store attachment...");

	// let user choose a location where to save downloaded attachment
	final JFileChooser fileChooser = new JFileChooser();
	fileChooser.setCurrentDirectory(prevLocation);
	fileChooser.setDialogTitle("Attachment");
	fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	fileChooser.setAcceptAllFileFilterUsed(false);

	// prompt for a location
	if (fileChooser.showSaveDialog(getOwningComponent()) == JFileChooser.APPROVE_OPTION) {
	    prevLocation = fileChooser.getSelectedFile();
	    final File file = new File(prevLocation.getPath() + "/" + getAttachment().getKey());
	    if (file.exists()
		    && JOptionPane.showConfirmDialog(getOwningComponent(), "The file already exists. Overwrite?", "Attachment", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
		unlock();
		return false;
	    }
	    setMessage("Downloading...");
	    return true;
	}
	unlock();
	return false;
    }

    @Override
    protected File action(final ActionEvent e) throws Exception {
	try {
	    final byte[] content = attachmentController.download(getAttachment());
	    final File file = new File(prevLocation.getPath() + "/" + getAttachment().getKey());
	    if (!file.exists()) {
		file.createNewFile();
	    }

	    final FileOutputStream fo = new FileOutputStream(file);
	    fo.write(content);
	    fo.flush();
	    fo.close();

	    return file;
	} catch (final Exception ex) {
	    unlock();
	    throw ex;
	}
    }

    @Override
    protected void postAction(final File file) {
	try {
	    setMessage("Downloaded successfully");
	    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(getOwningComponent(), "Downloaded successfully. Open?", "Attachment", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE)) {
		setMessage("Opening attachment...");
		try {
		    Desktop.getDesktop().open(file);
		} catch (final IOException e) {
		    JOptionPane.showMessageDialog(getOwningComponent(), "Could not open file. Try opening using standard facilities.\n\n" + e.getMessage(), "Export", JOptionPane.WARNING_MESSAGE);
		}
	    }
	    super.postAction(file);
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
