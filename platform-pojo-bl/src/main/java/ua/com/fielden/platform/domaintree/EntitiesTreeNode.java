package ua.com.fielden.platform.domaintree;

import java.util.Collections;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import ua.com.fielden.platform.algorithm.search.ITreeNode;
import ua.com.fielden.platform.utils.Pair;

public class EntitiesTreeNode extends DefaultMutableTreeNode implements ITreeNode<Pair<Class<?>, String>> {

    private static final long serialVersionUID = -1846717362518717357L;

    public EntitiesTreeNode(final Pair<Class<?>, String> userObject) {
	super(userObject);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ITreeNode<Pair<Class<?>, String>>> daughters() {
	return Collections.list(children());
    }

    @Override
    public Pair<Class<?>, String> state() {
	return getUserObject();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Pair<Class<?>, String> getUserObject() {
	return (Pair<Class<?>, String>)super.getUserObject();
    }
}
