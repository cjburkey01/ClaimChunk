package com.cjburkey.claimchunk.data;

import java.util.List;

public interface IDataStorage<T> {
	
	List<T> getData();
	void addData(T data);
	void saveData() throws Exception;
	void reloadData() throws Exception;
	void clearData();
	
}