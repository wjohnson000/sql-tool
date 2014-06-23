package xmlutil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Factory class that can save a configuration as XML, and read it back
 * from the XML.
 * 
 * @author wjohnson000
 *
 */
public class SystemConfigFactory {

	/**
	 * Read all configuration parameters from a XML file
	 * 
	 * @param path full path to the file
	 * @return Array of "ConfigParam" instances
	 */
	public static ConfigParam[] readConfigFromXML(String path) {
		List<ConfigParam> paramList = new ArrayList<ConfigParam>(10);
		
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
			ConfigHandler paramHandler = new ConfigHandler();
			parser.setContentHandler(paramHandler);
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
			paramList = paramHandler.getModel();
		} catch (IOException ioex) {
			System.out.println("IOEX(Extract): " + ioex);
		} catch (SAXException saxex) {
			System.out.println("SAXEX(Extract): " + saxex);
		}
		
		return paramList.toArray(new ConfigParam[paramList.size()]);
	}
	
	/**
	 * Save the configuration file as XML, and write it prettily to a file
	 * @param path full path of the output file
	 * @param paramList array of all configuration parameters to save
	 */
	public static void saveConfigAsXML(String path, ConfigParam[] paramList) {
		Element cNode;
		Node cData;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element root = doc.createElement(XmlTagName.ROOT_USER_CONFIG);
			doc.appendChild(root);
			for (int i=0;  i<paramList.length;  i++) {
				Element pNode = null;
				
				// For INT parameters, add the "value" tag
				if (paramList[i] instanceof IntParam) {
					IntParam iParam = (IntParam)paramList[i];
					pNode = doc.createElement(XmlTagName.TAG_INT_PARAM);
					cNode = doc.createElement(XmlTagName.TAG_VALUE);
					cData = doc.createCDATASection(""+iParam.getValue());
					cNode.appendChild(cData);
					pNode.appendChild(cNode);
					
					// For STRING parameters, add the "value" tag
				} else if (paramList[i] instanceof StringParam) {
					StringParam sParam = (StringParam)paramList[i];
					pNode = doc.createElement(XmlTagName.TAG_STRING_PARAM);
					cNode = doc.createElement(XmlTagName.TAG_VALUE);
					cData = doc.createCDATASection(sParam.getValue());
					cNode.appendChild(cData);
					pNode.appendChild(cNode);
					
					// For FONT parameters, add the "family" and "size" tags
				} else if (paramList[i] instanceof FontParam) {
					FontParam fParam = (FontParam)paramList[i];
					pNode = doc.createElement(XmlTagName.TAG_FONT_PARAM);
					cNode = doc.createElement(XmlTagName.TAG_FAMILY);
					cData = doc.createCDATASection(fParam.getFamily());
					cNode.appendChild(cData);
					pNode.appendChild(cNode);
					cNode = doc.createElement(XmlTagName.TAG_SIZE);
					cData = doc.createCDATASection(""+fParam.getSize());
					cNode.appendChild(cData);
					pNode.appendChild(cNode);
				}
				
				// For all parameters, add the "description" tag and,
				// if present, the "option" tags
				if (pNode != null) {
					pNode.setAttribute(XmlTagName.ATTR_NAME, paramList[i].getName());
					cNode = doc.createElement(XmlTagName.TAG_DESCRIPTION);
					cData = doc.createCDATASection(paramList[i].getDescription());
					cNode.appendChild(cData);
					pNode.appendChild(cNode);
					for (int j=0;  j<paramList[i].optionCount();  j++) {
						cNode = doc.createElement(XmlTagName.TAG_OPTION);
						cData = doc.createCDATASection(paramList[i].getOptionValue(j));
						cNode.setAttribute(XmlTagName.ATTR_TAG, paramList[i].getOptionTag(j));
						cNode.appendChild(cData);
						pNode.appendChild(cNode);
					}
					root.appendChild(pNode);
				}
			}
			
			TransformerFactory tfact = TransformerFactory.newInstance();
			Transformer        trans = tfact.newTransformer();
			trans.setOutputProperty(OutputKeys.METHOD, "xml");
			trans.setOutputProperty(OutputKeys.ENCODING, "iso-8859-1");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			trans.transform(new DOMSource(doc),
					new StreamResult(new FileOutputStream(path)));
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
