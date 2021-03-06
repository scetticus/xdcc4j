package it.biffi.jirc.bot.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class GenericEvent extends Observable {
	public static final int CONNECTION_EVENT = 0;
	public static final int JOIN_EVENT = 1;
	public static final int MESSAGE_EVENT = 2;
	public static final int FILE_TRASNFER_START_EVENT = 3;
	public static final int FILE_TRASNFER_END_EVENT = 4;
	public static final int CHANNEL_INFO_EVENT = 5;
	public static final int XDCC_EVENT = 6;

	protected int type;

	private Map<String, Object> data;

	public GenericEvent() {
		data = new HashMap<String, Object>();
	}

	public void putData(String dataName, Object dataValue) {
		data.put(dataName, dataValue);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public Object getDataByName(String name) {
		return this.data.get(name);
	}

	@Override
	public void notifyObservers() {
		setChanged();
		super.notifyObservers();
	}

	@Override
	public void notifyObservers(Object arg) {
		setChanged();
		super.notifyObservers(arg);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + type;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GenericEvent other = (GenericEvent) obj;
		if (type != other.type)
			return false;
		return true;
	}

}
