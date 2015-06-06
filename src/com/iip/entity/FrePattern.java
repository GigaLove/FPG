package com.iip.entity;

import java.util.List;

/**
 * ���ڱ����ھ򵽵�Ƶ��ģʽ
 * @author ��Ц�Ҵ�
 *
 */
public class FrePattern {
	private List<String> itemSets;	// Ƶ���
	private int count;	// �����

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
