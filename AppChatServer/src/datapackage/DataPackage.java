package datapackage;

import java.io.Serializable;

public class DataPackage implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DataType type;
	private Object data;
	
	public DataPackage(DataType type, String data) {
		this.type = type;
		this.data = formatData(data);
	}
	
	public DataType getType() {
		return this.type;
	}
	
	public Object getData() {
		return data;
	}
	
	/**
	 * @param type, data(url)
	 * 
	 * @author tamth
	 * */
	private Object formatData(String data) {
		switch(this.type) {
			case FILE:
				return new FileInfo(data);
			case IMAGE:
				return new FileInfo(data);
			case MESSAGE:
				return new String(data);
		}
		return null;
	}	
}
