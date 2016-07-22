package com.example.acbingo.myfinaltest10.FileManagerService;

public class ListViewLocationStack {
	int size;
	int[] location;
	int top;

	public ListViewLocationStack(int size) {
		this.size=size;
		location=new int[size];
		top=1;
	}
	public void push(int p){
		location[top++]=p;
	}
	public int pull(){
		if(top==0)
			return 0;
		return location[--top];
	}

}
