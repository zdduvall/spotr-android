package com.csun.spotr.skeleton;

public interface IAsyncTask<T> {
	public void attach(T a);
	public void detach();
}
