package dragdrop.jtree.action;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import dragdrop.jtree.DndJTree;
import dragdrop.jtree.DndTreeNode;


/**
 * Create a new folder and place it in the specified location in a tree
 * 
 * @author wjohnson000
 *
 */
public class CreateFolderAction extends AbstractUndoableEdit {

    private static final long serialVersionUID = -3771224904479607219L;

    /** Folder tree that we're working on */
    DndJTree folderTree;

    /**
     * Constructor takes the folder tree that we're working on
     * @param tree
     */
    public CreateFolderAction(DndJTree tree) {
        folderTree = tree;
    }

    /* (non-Javadoc)
     * @see javax.swing.undo.AbstractUndoableEdit#undo()
     */
    @Override
    public void undo() throws CannotUndoException {
        // TODO Auto-generated method stub
        super.undo();
    }

    /* (non-Javadoc)
     * @see javax.swing.undo.AbstractUndoableEdit#redo()
     */
    @Override
    public void redo() throws CannotRedoException {
        super.redo();

        // Prompt the user for a folder name
        String name = JOptionPane.showInputDialog(null, "Folder Name:",
                "New Folder", JOptionPane.QUESTION_MESSAGE);

        // Create a new folder and insert it into the proper place
        if (name != null) {
            DndTreeNode folder = new DndTreeNode(name, null);
            DndTreeNode parent = getSelectedFolder();
            int offset = 0;
            for (int i=0;  i<parent.getChildCount();  i++) {
                DndTreeNode aNode = (DndTreeNode)parent.getChildAt(i);
                if (aNode.getAllowsChildren()) {
                    if (aNode.getLabel().compareToIgnoreCase(name) < 0) {
                        offset = i+1;
                    }
                }
            }
            DefaultTreeModel folderModel = (DefaultTreeModel)folderTree.getModel();
            folderModel.insertNodeInto(folder, parent, offset);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.undo.AbstractUndoableEdit#getPresentationName()
     */
    @Override
    public String getPresentationName() {
        return "create folder ...";
    }

    /**
     * Find the currently selected folder.  If no item is selected, default to
     * the root folder
     * @return selected folder
     */
    protected DndTreeNode getSelectedFolder() {
        TreePath parentPath = folderTree.getSelectionPath();
        if (parentPath != null) {
            for (int i=parentPath.getPathCount()-1;  i>0;  i--) {
                if (((DndTreeNode) parentPath.getPathComponent(i)).getAllowsChildren()) {
                    return (DndTreeNode)parentPath.getPathComponent(i);
                }
            }
        }
        
        return getRoot();
    }

    /**
     * Retrieve the root [top] node
     * @return root node
     */
    protected DndTreeNode getRoot() {
        DefaultTreeModel folderModel = (DefaultTreeModel)folderTree.getModel();
        return (DndTreeNode)folderModel.getRoot();
    }
}
