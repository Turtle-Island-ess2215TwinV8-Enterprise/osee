package org.eclipse.osee.ote.core.log.record;

import java.util.logging.Level;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.osee.framework.jdk.core.type.IPropertyStore;

public class PropertyStoreRecord extends TestRecord {

	private static final long serialVersionUID = -6515147544821433102L;
	private IPropertyStore store;

	public PropertyStoreRecord(IPropertyStore store) {
		super(null, Level.INFO, "PropertyStoreRecord", false);
		this.store = store;
	}

	@Override
	public void toXml(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("PropertyStore");
		if(store != null){
			for(String key:store.keySet()){
				String message = store.get(key);
				writer.writeStartElement("Property");
				writer.writeAttribute("key", key);
				writer.writeCharacters(message);
				writer.writeEndElement();
			}
		}
		writer.writeEndElement();
	}

}