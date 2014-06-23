package xmlutil;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Extend the default SAX handler for the XML configuration file.
 * 
 * @author wjohnson000
 *
 */
public class ConfigHandler extends DefaultHandler {
	private boolean     rootTagOK   = false;
	private String      charData    = null;
	private String      optionTag   = null;
	private String      value       = null;
	private String      description = null;
	private String      family      = null;
	private String      size        = null;
	private ConfigParam confParam   = null;
	private List<ConfigParam> paramList = new ArrayList<ConfigParam>(10);
	

	/**
	 * Retrieve the list of parameters that we parsed
	 * @return list of parameters
	 */
	public List<ConfigParam> getModel() {
		return paramList;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException {
		// do nothing
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {
		// do nothing
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes attr)
			throws SAXException {
		// Don't do anything until we have found the main tag.  Then we can
		// start looking for INT, STRING, FONT and OPTION tags
		if (! rootTagOK) {
			rootTagOK = XmlTagName.ROOT_USER_CONFIG.equalsIgnoreCase(localName);
		} else if (XmlTagName.TAG_INT_PARAM.equalsIgnoreCase(localName)) {
			confParam = new IntParam();
			confParam.setName(getAttrValue(attr, XmlTagName.ATTR_NAME));
		} else if (XmlTagName.TAG_STRING_PARAM.equalsIgnoreCase(localName)) {
			confParam = new StringParam();
			confParam.setName(getAttrValue(attr, XmlTagName.ATTR_NAME));
		} else if (XmlTagName.TAG_FONT_PARAM.equalsIgnoreCase(localName)) {
			confParam = new FontParam();
			confParam.setName(getAttrValue(attr, XmlTagName.ATTR_NAME));
		} else if (XmlTagName.TAG_OPTION.equalsIgnoreCase(localName)) {
			optionTag = getAttrValue(attr, XmlTagName.ATTR_TAG);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (rootTagOK) {
			if (XmlTagName.TAG_INT_PARAM.equalsIgnoreCase(localName)) {
				confParam.setDescription(description);
				((IntParam)confParam).setValue(value);
				paramList.add(confParam);
				confParam   = null;
				value       = "";
				description = "";
			} else if (XmlTagName.TAG_STRING_PARAM.equalsIgnoreCase(localName)) {
				confParam.setDescription(description);
				((StringParam)confParam).setValue(value);
				paramList.add(confParam);
				confParam   = null;
				value       = "";
				description = "";
			} else if (XmlTagName.TAG_FONT_PARAM.equalsIgnoreCase(localName)) {
				confParam.setDescription(description);
				((FontParam)confParam).setFamily(family);
				((FontParam)confParam).setSize(size);
				paramList.add(confParam);
				confParam   = null;
				size        = "";
				family      = "";
				description = "";
			} else if (XmlTagName.TAG_OPTION.equalsIgnoreCase(localName)) {
				confParam.addOption(optionTag, charData);
				charData = "";
			} else if (XmlTagName.TAG_VALUE.equalsIgnoreCase(localName)) {
				value = charData.trim();
				charData = "";
			} else if (XmlTagName.TAG_DESCRIPTION.equalsIgnoreCase(localName)) {
				description = charData.trim();
				charData = "";
			} else if (XmlTagName.TAG_FAMILY.equalsIgnoreCase(localName)) {
				family = charData.trim();
				charData = "";
			} else if (XmlTagName.TAG_SIZE.equalsIgnoreCase(localName)) {
				size = charData.trim();
				charData = "";
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length)
	throws SAXException {
		charData = new String(ch, start, length);
	}

	/**
	 * Extract a specific attribute from a list of attributes
	 * @param attr list of attributes
	 * @param attrName attribute name
	 * @return attribute value
	 */
	private String getAttrValue(Attributes attr, String attrName) {
		String attrValue = "";
		int aCnt = attr.getLength();
		for (int i=0;  i<aCnt;  i++) {
			if (attrName.equalsIgnoreCase(attr.getLocalName(i))) {
				attrValue = attr.getValue(i);
			}
		}
		return attrValue;
	}
}
