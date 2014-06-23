package dragdrop.jtree;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import sqltool.common.MD5Encryption;
import xmlutil.XmlTagName;


/**
 * Factory methods for saving a "DefaultTreeModel" to XML, and reading an XML
 * file to create a "DefaultTreeModel.
 * 
 * @author wjohnson000
 */
public class TreeModelFactory {
	/**
	 * Process an XML file and generate a "DefaultTreeModel" based on the
	 * node structure (path) and values
	 * 
	 * @param path full path of the XML file
	 * @param passcode optional pass-code for decrypting values
	 * @return DefaultTreeModel
	 */
	public static DefaultTreeModel readFromXML(String path, String passcode) {
		DefaultTreeModel dtm = null;
		
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
			TreeXMLHandler treeHandler = new TreeXMLHandler(passcode);
			parser.setContentHandler(treeHandler);
			parser.setErrorHandler(new ErrorHandler() {
				public void warning(SAXParseException spe) {
					System.out.println("Parse warning: " + spe);
				}
				public void error(SAXParseException spe) {
					System.out.println("Parse error: " + spe);
				}
				public void fatalError(SAXParseException spe) {
					System.out.println("Parse fatal: " + spe);
				}
			});
			parser.parse(new InputSource(new FileInputStream(path)));
			dtm = treeHandler.getModel();
		} catch (IOException ioex) {
			System.out.println("IOEX(Extract): " + ioex);
		} catch (SAXException saxex) {
			System.out.println("SAXEX(Extract): " + saxex);
		}
		
		return dtm;
	}

	/**
	 * Navigate the "folderModel" tree and convert into an XML document;
	 * then save that document.
	 * 
	 * @param filePath full file path where the data is to be saved
	 * @param folderModel a "DefaultTreeModel" instance that contains
	 *        the folders and items to convert
	 * @param passcode optional pass-code for encrypting passwords
	 */
	public static void saveAsXML(String filePath, Object folderModel, String passcode) {
		if (folderModel == null  ||  ! (folderModel instanceof DefaultTreeModel)) {
			return;
		}

		MD5Encryption md5 = new MD5Encryption();
		
		DefaultTreeModel model = (DefaultTreeModel) folderModel;
		DndTreeNode treeRoot = (DndTreeNode) model.getRoot();
		Element cNode;
		Node cData;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element docRoot = doc.createElement(XmlTagName.TAG_TREE_ROOT);
			docRoot.setAttribute(XmlTagName.ATTR_NAME, treeRoot.getLabel());
			doc.appendChild(docRoot);

			// Only save the "leaf" nodes; since we are storing the full path
			// of the item, the root folder and sub-folders can be recreated
			// auto-magically
			DefaultMutableTreeNode treeNode = treeRoot.getFirstLeaf();
			while (treeNode != null) {
				Object userObject = treeNode.getUserObject();
				FolderItem item = (FolderItem)userObject;
				TreeNode[] path = treeNode.getPath();
				Element pNode = doc.createElement(XmlTagName.TAG_NODE);
				pNode.setAttribute(XmlTagName.ATTR_NAME, ((DndTreeNode)treeNode).getLabel());

				// Store the item class name
				cNode = doc.createElement(XmlTagName.TAG_CLASS);
				cData = doc.createCDATASection("" + item.getClass().getName());
				cNode.appendChild(cData);
				pNode.appendChild(cNode);

				// Save the full path of the item
				Element pathNode = doc.createElement(XmlTagName.TAG_PATH);
				for (int i=0;  i<path.length;  i++) {
					cNode = doc.createElement(XmlTagName.TAG_PATH_ELEMENT);
					cData = doc.createCDATASection(path[i].toString());
					cNode.appendChild(cData);
					pathNode.appendChild(cNode);
				}
				pNode.appendChild(pathNode);

				// Store the item's values, which will be used to re-create the
				// details later
				Map<String,String> values = item.getValues();
				Iterator<String> keys = values.keySet().iterator();
				while (keys.hasNext()) {
					String key = keys.next();
					String val = values.get(key);
					if (passcode != null  &&  "password".equalsIgnoreCase(key)) {
						try {
							val = md5.encrypt(passcode, val);
						} catch (Exception ex) {
							val = "Unknown";
						}
					}
					cNode = doc.createElement(XmlTagName.TAG_VARIABLE);
					cNode.setAttribute(XmlTagName.ATTR_NAME, key);
					cData = doc.createCDATASection(val);
					cNode.appendChild(cData);
					pNode.appendChild(cNode);
				}

				docRoot.appendChild(pNode);
				treeNode = treeNode.getNextLeaf();
			}

			// Save the XML file in a relatively pretty format
			TransformerFactory tfact = TransformerFactory.newInstance();
			Transformer        trans = tfact.newTransformer();
			trans.setOutputProperty(OutputKeys.METHOD, "xml");
			trans.setOutputProperty(OutputKeys.ENCODING, "iso-8859-1");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			trans.transform(new DOMSource(doc),
					new StreamResult(new FileOutputStream(filePath)));
		} catch (ParserConfigurationException pcex) {
			System.out.println("PCEX(Save): " + pcex);
		} catch (FileNotFoundException fnfex) {
			System.out.println("FNFEX(Save): " + fnfex);
		} catch (TransformerException trxex) {
			System.out.println("TRXEX(Save): " + trxex);
		} catch (DOMException domex) {
			System.out.println("DOMEX(Save): " + domex);
		}
	}
}

