package sqltool.favorites;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.util.HashMap;

import sqltool.common.SqlToolkit;

import dragdrop.jtree.FolderItem;
import dragdrop.jtree.FolderItemEditor;


/**
 * Class that provides editing capabilities for a "FavoriteSql" entry.
 * @author wjohnson000
 *
 */
public class SqlEditor implements FolderItemEditor {

//	===========================================================================
//	Keep track of "SqlEditorUI" instances so we can re-open them in the
//	same location as before ...
//	===========================================================================
	private static HashMap<String,SqlEditorUI> StickyCache = new HashMap<String,SqlEditorUI>(10);


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
		SqlEditorUI sqlEditor = StickyCache.get(key);
		if (sqlEditor == null) {
			sqlEditor = new SqlEditorUI(frame, "SQL Editor", true);
			Dimension dlgSize = sqlEditor.getPreferredSize();
			Dimension frmSize = parent == null ? new Dimension(400, 300) : parent.getSize();
			Point loc = parent == null ? new Point(300, 400) : parent.getLocation();
			sqlEditor.setLocation((frmSize.width-dlgSize.width)/2+loc.x, (frmSize.height-dlgSize.height)/2+loc.y);
			sqlEditor.setModal(true);
			StickyCache.put(key, sqlEditor);
		}

		sqlEditor.setModel((FavoriteSql)item);
		sqlEditor.setVisible(true);
		return sqlEditor.isOK;
	}
}