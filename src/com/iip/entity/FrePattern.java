package com.iip.entity;

import java.util.List;

/**
 * 用于保存挖掘到的频繁模式
 * @author 别笑我呆
 *
 */
public class FrePattern {
	private List<String> itemSets;	// 频繁项集
	private int count;	// 项集计数

	public FrePattern() {
	}
	
	public FrePattern(List<String> itemSets, int count) {
		super();
		this.itemSets = itemSets;
		this.count = count;
	}

	public List<String> getItemSets() {
		return itemSets;
	}
	
	public void setItemSets(List<String> itemSets) {
		this.itemSets = itemSets;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = "";
		
		for (int i = 0; i <itemSets.size() - 1 ; i++) {
			str += itemSets.get(i) + ",";
		}
		
		str += itemSets.get(itemSets.size() - 1) + ": " + count;
		return str;
	}
}
