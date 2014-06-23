package dragdrop.jtree;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;


/**
 * Drag-and-drop JTree, supporting dragging and dropping of folders and
 * folder items.
 * @author wjohnson000
 *
 */
public class DndJTree extends JTree
		implements TreeSelectionListener, DragGestureListener,
					DropTargetListener, DragSourceListener {

	private static final long serialVersionUID = -7943051253227598463L;

	// Create new DROP and NO-DROP cursors for this application
	private static Cursor dropCursor;
	private static Cursor noDropCursor;
	static {
		Toolkit tk = Toolkit.getDefaultToolkit();
		ImageIcon image = new ImageIcon(DndJTree.class.getResource("no-drop.gif"));
		noDropCursor = tk.createCustomCursor(image.getImage(), new Point(12, 12), "no-drop-cursor");
		image = new ImageIcon(DndJTree.class.getResource("can-drop.gif"));
		dropCursor = tk.createCustomCursor(image.getImage(), new Point(12, 12), "can-drop-cursor");
	}
	
	// Store the selected node info
	protected TreePath    selectedTreePath = null;
	protected DndTreeNode selectedNode = null;
	
	// Variables needed for Drag-n-Drop
	private DragSource dragSource = null;

	
	/**
	 *Default Constructor
	 */
	@SuppressWarnings("unused")
	public DndJTree() {
		super();
		addTreeSelectionListener(this);
		dragSource = DragSource.getDefaultDragSource();
		
		DragGestureRecognizer dgr =
			dragSource.createDefaultDragGestureRecognizer(
					this,                      // DragSource
					DnDConstants.ACTION_MOVE,  // specifies valid actions
					this                       // DragGestureListener
			);
		
		// Eliminate right mouse clicks as valid actions (especially useful
		// if you implements a JPopupMenu for the JTree ...)
		dgr.setSourceActions(dgr.getSourceActions() & ~InputEvent.BUTTON3_MASK);

		// DO NOT DELETE:
		// Even though "dropTarget" isn't referenced, this must be left
		// here in order to register "this" as the drop target listener.  
		DropTarget dropTarget = new DropTarget(this, this);

//		putClientProperty("JTree.lineStyle", "Angled");
		DefaultTreeCellRenderer dtcr = new DefaultTreeCellRenderer();
		setCellRenderer(dtcr);
	}
	
	// Return the selected node
	public DndTreeNode getSelectedNode() {
		return selectedNode;
	}
	
	
//	=============================================================================
//	DragGestureListener methods:
//     public void dragGestureRecognized(DragGestureEvent e)
//	=============================================================================

	/**
	 * User has done something that tells the system she/he just started to
	 * drag something.  How exciting!  Set the default cursor to "NO DRAG"
	 * and start tracking the drag event.
	 * 
	 * @param e The event that triggered this excitement
	 */
	public void dragGestureRecognized(DragGestureEvent e) {
		//Get the selected node
		DndTreeNode dragNode = getSelectedNode();
		if (dragNode != null) {
			//Get the Transferable Object
			Transferable transferable = (Transferable)dragNode;
			
			//Select the appropriate cursor;
			Cursor cursor = noDropCursor;   //DragSource.DefaultMoveNoDrop;
			
			//begin the drag
			dragSource.startDrag(e, cursor, transferable, this);
		}
	}


//	=============================================================================
//	DragSourceListener methods: determine which cursor to show
//     public void dragDropEnd(DragSourceDropEvent dsde)
//     public void dragEnter(DragSourceDropEvent dsde)
//     public void dragOver(DragSourceDropEvent dsde)
//     public void dropActionChanged(DragSourceDropEvent dsde)
//     public void dragExit(DragSourceDropEvent dsde)
//	=============================================================================

	/**
	 * The drag/drop is about to end ... no need to do anything.
	 * 
	 * @param dsde the event
	 */
	public void dragDropEnd(DragSourceDropEvent dsde) {
	}
	
	/**
	 * The cursor has entered a potential drop site.  Show the
	 * appropriate cursor to indicate this is a valid drop site.
	 * 
	 * @param dsde the event
	 */
	public void dragEnter(DragSourceDragEvent dsde) {
		DragSourceContext context = dsde.getDragSourceContext();
//		context.setCursor(DragSource.DefaultMoveDrop);
		context.setCursor(dropCursor);
	}
	
	/**
	 * The cursor is hovering over a potential drop site.  Show the
	 * appropriate cursor to indicate this is a valid drop site.
	 * 
	 * @param dsde the event
	 */
	public void dragOver(DragSourceDragEvent dsde) {
		DragSourceContext context = dsde.getDragSourceContext();
//		context.setCursor(DragSource.DefaultMoveDrop);
		context.setCursor(dropCursor);
	}
	
	/**
	 * The event (copy, move, link) has changed.  We don't support copy
	 * or link so this event can be ignored.
	 * 
	 * @param dsde the event
	 */
	public void dropActionChanged(DragSourceDragEvent dsde) {
	}
	
	/**
	 * The cursor has left a potential drop site.  Show the appropriate
	 * cursor to indicate this is NOT a valid drop site.
	 * 
	 * @param dsde the event
	 */
	public void dragExit(DragSourceEvent dse) {
		DragSourceContext context = dse.getDragSourceContext();
//		context.setCursor(DragSource.DefaultMoveNoDrop);
		context.setCursor(noDropCursor);
	}
	
	
//	=============================================================================
//	DropTargetListener methods: determine which cursor to show
//     public void drop(DropTargetDropEvent e)
//     public void dragOver(DropTargetDropEvent e)
//     public void dragEnter(DropTargetDropEvent e)
//     public void dragExit(DropTargetDropEvent e)
//     public void dropActionChanged(DropTargetDropEvent e)
//	=============================================================================
	
	/**
	 * Method called when the user has actually done the drop.  This is where
	 * most of the processing happens.
	 *   -- See what object wants to drop and make sure we support one or
	 *      more of its "flavors"
	 *   -- Test once more to see if the drop operation is acceptable based
	 *      on the node to move and the location where it's to be moved
	 *   -- If the move is acceptable, remove the source object from
	 *      its current location and add it to then new parent
	 *   -- reload and re-expand the tree where the drag/drop occurred
	 * 
	 * @param e drop event
	 */
	public void drop(DropTargetDropEvent e) {
		try {
			Transferable tr = e.getTransferable();
			
			// flavor not supported, reject drop
			if (! tr.isDataFlavorSupported(DndTreeNode.FOLDER_FLAVOR)) {
				e.rejectDrop();
			}
			
			//cast into appropriate data type
			DndTreeNode node = (DndTreeNode)tr.getTransferData(DndTreeNode.FOLDER_FLAVOR);
			
			//get new parent node
			Point loc = e.getLocation();
			TreePath destinationPath = getPathForLocation(loc.x, loc.y);
			
			final String msg = testDropTarget(destinationPath, selectedTreePath);
			final Frame  parent = getFrame();
			if (msg != null) {
				e.rejectDrop();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(
								parent, msg, "Error Dialog", JOptionPane.ERROR_MESSAGE);
					}
				});
				return;
			}
			
			// Retrieve the new and old parent nodes
			DndTreeNode newParent = (DndTreeNode)destinationPath.getLastPathComponent();
			DndTreeNode oldParent = (DndTreeNode)getSelectedNode().getParent();
			
			try {
				oldParent.remove(getSelectedNode());
				newParent.add(node);
				e.acceptDrop(DnDConstants.ACTION_MOVE);
			} catch (java.lang.IllegalStateException ils) {
				e.rejectDrop();
			}
			e.getDropTargetContext().dropComplete(true);
			
			//expand nodes appropriately - this probably isnt the best way...
			DefaultTreeModel model = (DefaultTreeModel) getModel();
			model.reload(oldParent);
			model.reload(newParent);
			TreePath parentPath = new TreePath(newParent.getPath());
			expandPath(parentPath);
		}
		catch (IOException io) { e.rejectDrop(); }
		catch (UnsupportedFlavorException ufe) {e.rejectDrop();}
	}
	
	
	
	/**
	 * Method called when the user has dragged over an object we have to
	 * determine if this is a valid drop location or not.  This is done
	 * by getting the tree element at the cursor location and testing to
	 * see if the selected node (determined at the beginning of the
	 * operation) can be dropped onto the element at the current cursor
	 * location.  Inform the system via the event what the outcome is.
	 * 
	 * @param e drop event
	 */
	public void dragOver(DropTargetDragEvent e) {
		//set cursor location. Needed in setCursor method
		Point cursorLocationBis = e.getLocation();
		TreePath destinationPath =
			getPathForLocation(cursorLocationBis.x, cursorLocationBis.y);
		
		// if destination path is okay accept drop...
		if (testDropTarget(destinationPath, selectedTreePath) == null) {
			e.acceptDrag(DnDConstants.ACTION_MOVE) ;
		// ...otherwise reject drop
		} else {
			e.rejectDrag() ;
		}
	}
	
	/**
	 * The cursor has entered a potential drop site.  Do nothing.
	 * 
	 * @param e the event
	 */
	public void dragEnter(DropTargetDragEvent e) {
	}
	
	/**
	 * The cursor has left a potential drop site.  Do nothing.
	 * 
	 * @param e the event
	 */
	public void dragExit(DropTargetEvent e) {
	}
	
	/**
	 * The user has changed the action (move, copy, link): since we only support
	 * the move operation, do nothing.		this.getCellRenderer().

	 * 
	 * @param e the event
	 */
	public void dropActionChanged(DropTargetDragEvent e) {
	}


	/**
	 * Set the default image (icon) for the leaf nodes
	 * @param icon
	 */
	public void setLeafIcon(ImageIcon icon) {
		DefaultTreeCellRenderer dtcr = new DefaultTreeCellRenderer();
		dtcr.setLeafIcon(icon);
		setCellRenderer(dtcr);
	}

//	=============================================================================
//	TreeSelectionListener methods:
//     public void valueChanged(TreeSelectionEvent evt)
//	=============================================================================

	/**
	 * User has changed which node (folder or leaf) is selected: get the path
	 * from the event and save the node that is selected
	 * 
	 * @param evt the TreeSelection event that triggered this commotion
	 */
	public void valueChanged(TreeSelectionEvent evt) {
		selectedTreePath = evt.getNewLeadSelectionPath();
		if (selectedTreePath == null) {
			selectedNode = null;
		} else {
			selectedNode = (DndTreeNode)selectedTreePath.getLastPathComponent();
		}
	}
	
	
	/**
	 *  Convenience method to test whether drop location is valid
	 *    @param destination The destination path
	 *    @param dropper The path for the node to be dropped
	 *    @return null if no problems, otherwise an explanation
	 */
	private String testDropTarget(TreePath destination, TreePath dropper) {

		//Test 1.
		if (destination == null) {
			return "Invalid drop location.";
		}

		//Test 2.
		DndTreeNode node = (DndTreeNode) destination.getLastPathComponent();
		if (! node.getAllowsChildren()) {
			return "This node does not allow children";
		}

		//Test 3.
		if (destination.equals(dropper)) {
			return "Destination cannot be same as source";
		}

		//Test 4.
		if (dropper.isDescendant(destination)) {
			return "Destination node cannot be a descendant.";
		}

		//Test 5.
		if (dropper.getParentPath().equals(destination)) {
			return "Destination node cannot be a parent.";
		}

		return null;
	}
	
	
	/**
	 *  Convenience method to get the parent frame for putting up an error message
	 */
	private Frame getFrame() {
		Component frame = getParent();
		while (! (frame == null  ||  frame instanceof Frame)) {
			frame = frame.getParent();
		}
		return (frame == null) ? null : (Frame)frame;
		
	}
}
