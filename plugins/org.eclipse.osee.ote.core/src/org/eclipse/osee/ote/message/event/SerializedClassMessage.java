/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ote.message.event;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.eclipse.osee.ote.message.elements.ArrayElement;

public class SerializedClassMessage<T> extends OteEventMessage {

	public static final int _BYTE_SIZE = 0;

	public ArrayElement OBJECT;

	public SerializedClassMessage(String topic) {
		super(SerializedClassMessage.class.getSimpleName(), topic, _BYTE_SIZE);
		OBJECT = new ArrayElement(this, "CLAZZ", getDefaultMessageData(), 0, 0, 0);
		addElements(OBJECT);
	}
	

	public SerializedClassMessage(String topic, Serializable object) throws IOException {
		this(topic);
		setObject(object);
	}
	
	public SerializedClassMessage(byte[] bytes) {
		super(bytes);
		OBJECT = new ArrayElement(this, "CLAZZ", getDefaultMessageData(), 0, 0, 0);
		addElements(OBJECT);
	}

	public void setObject(Serializable obj) throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		byte[] data = bos.toByteArray();
		int offset = OBJECT.getByteOffset() + getHeaderSize();
		byte[] newData = new byte[data.length + offset];
		System.arraycopy(getData(), 0, newData, 0, offset);
		System.arraycopy(data, 0, newData, offset, data.length);
		getDefaultMessageData().setNewBackingBuffer(newData);
	}
	
	public T getObject() throws IOException, ClassNotFoundException{
		int offset = OBJECT.getByteOffset() + getHeaderSize();
		ByteArrayInputStream bis = new ByteArrayInputStream(getData(), offset, getData().length - offset);
		ObjectInputStream ois = new ObjectInputStream(bis);
		return (T)ois.readObject();
	}
	
}  
