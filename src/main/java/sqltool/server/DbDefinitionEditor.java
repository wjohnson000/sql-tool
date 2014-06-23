package sqltool.server;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.util.HashMap;

import sqltool.common.SqlToolkit;

import dragdrop.jtree.FolderItem;
import dragdrop.jtree.FolderItemEditor;


/**
 * Implement the {@link FolderItemEditor} for a {@link DbDefinition} instance.
 * 
 * @author wjohnson000
 *
 */
public class DbDefinitionEditor implements FolderItemEditor {

//	===========================================================================
//	Keep track of "DbDefintionEditorUI" instances so we can re-open them
//	in the same location as before ...
//	===========================================================================
	private static HashMap<String,DbDefinitionEditorUI> StickyCache = new HashMap<String,DbDefinitionEditorUI>(10);


	/* (non-Javadoc)
	 * @see dragdrop.jtree.FolderItemEditor#edit(dragdrop.jtree.FolderItem)
	 */
	@Override
	public boolean edit(FolderItem item) {
		return edit(null, item);
	}
	
	/* (non-Javadoc)
	 * @see dragdrop.jtree.FolderItemEditor#edit(java.awt.Container, dragdrop.jtree.FolderItem)
	 */
	@Override
	public boolean edit(Container parent, FolderItem item) {
		Frame frame = (parent == null) ? null : SqlToolkit.getParentFrame(parent);

		String key = "" + frame;
		DbDefinitionEditorUI dbEditor = StickyCache.get(key);
		if (dbEditor == null) {
			dbEditor = new DbDefinitionEditorUI(frame, "Database Definition Editor", true);
			Dimension dlgSize = dbEditor.getPreferredSize();
			Dimension frmSize = parent == null ? new Dimension(400, 300) : parent.getSize();
			Point loc = parent == null ? new Point(300, 400) : parent.getLocation();
			dbEditor.setLocation((frmSize.width-dlgSize.width)/2+loc.x, (frmSize.height-dlgSize.height)/2+loc.y);
			dbEditor.setModal(true);
			StickyCache.put(key, dbEditor);
		}

		dbEditor.setModel((DbDefinition)item);
		dbEditor.setVisible(true);
		return dbEditor.isOK;
	}
}