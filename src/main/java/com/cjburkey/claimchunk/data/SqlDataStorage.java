package com.cjburkey.claimchunk.data;

import java.util.ArrayList;
import java.util.List;

public class SqlDataStorage<T> implements IDataStorage<T> {
	
	private final List<T> storage = new ArrayList<>();
	
	public void saveData() {
		
	}
	
	public void reloadData() {
		
	}
	
	public void addData(T data) {
		storage.add(data);
	}
	
	public void clearData() {
		storage.clear();
	}
	
	public List<T> getData() {
		return new ArrayList<>(storage);
	}
	
}