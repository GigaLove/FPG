package com.iip.fpg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.iip.entity.FrePattern;
import com.iip.entity.TreeNode;
import com.iip.tool.ReadData;

public class FP {
	private static final int MIN_SUP_COUNT = 2;

	/**
	 * ��ȡ�������ݣ����������ݷŵ���άList��
	 * 
	 * @param fileName
	 *            �����ļ���
	 * @return
	 */
	public List<List<String>> readTransRecord(String fileName) {
		if (fileName == null) {
			return null;
		}

		List<String[]> originData = ReadData.readCommaFile(fileName);
		List<List<String>> transRecords = new ArrayList<List<String>>();

		for (String[] strs : originData) {
			List<String> record = new LinkedList<String>();
			for (int i = 1; i < strs.length; i++) {
				record.add(strs[i]);
			}
			transRecords.add(record);
		}

		return transRecords;
	}

	/**
	 * ����FP������
	 * 
	 * @param transRecords
	 *            ��������
	 * @param headTable
	 *            ��ͷ��
	 * @return
	 */
	public TreeNode buildFPTree(List<List<String>> transRecords,
			ArrayList<TreeNode> headTable) {
		TreeNode root = new TreeNode(); // �������ĸ��ڵ�

		for (List<String> transRecord : transRecords) {
			LinkedList<String> record = sortRecordByTable(transRecord,
					headTable);

			insertTree(record, root, headTable);
		}

		return root;
	}

	/**
	 * ��record��Ϊancestor�ĺ���������У�ͬʱ������ͷ���ָ��
	 * @param ancestor
	 *            ���ڵ�
	 * @param record
	 *            ������ļ�¼
	 * @param headTable
	 *            ��ͷ��
	 */
	public void insertTree(LinkedList<String> record, TreeNode ancestor,
			ArrayList<TreeNode> headTable) {
		boolean flag = false;
		List<TreeNode> childList = ancestor.getChildren();
		TreeNode newTreeNode = null;

		String itemName = record.poll();
		if (childList != null) {
			for (TreeNode tn : childList) {
				if (tn.getName().equals(itemName)) {
					flag = true;
					tn.countIncrement(1);
					newTreeNode = tn;
					break;
				}
			}
		}
		
		// ���ӽڵ�û�ҵ����߸��ڵ�Ķ���Ϊ��
		// �򴴽��½ڵ㣬��ӵ����ڵ���
		if (!flag) {
			newTreeNode = new TreeNode(itemName);
			newTreeNode.setCount(1);
			ancestor.addChild(newTreeNode);
			newTreeNode.setParent(ancestor);

			for (TreeNode f1Item : headTable) {
				if (f1Item.getName().equals(newTreeNode.getName())) {
					// ������ͷ���нڵ���ָ��
					while (f1Item.getNextHomonym() != null) {
						f1Item = f1Item.getNextHomonym();
					}
					f1Item.setNextHomonym(newTreeNode);
					break;
				}
			}
		}
		
		// �����¼��Ϊ�գ������ݹ����
		if (record.size() > 0) {
			insertTree(record, newTreeNode, headTable);
		}
	}

	/**
	 * �ѽ��׼�¼����ͷ�������Ƶ������������
	 * 
	 * @param transRecord
	 *            ��������
	 * @param headTable
	 *            ��ͷ��
	 * @return
	 */
	public LinkedList<String> sortRecordByTable(List<String> transRecord,
			ArrayList<TreeNode> headTable) {
		LinkedList<String> sortedRecord = null;

		if (transRecord != null && headTable != null) {
			sortedRecord = new LinkedList<String>();
			// ���������ͷ���ж�ԭʼ�������Ƿ�������������ѹ�뵽�µ�list��
			for (TreeNode tn : headTable) {
				String itemName = tn.getName();
				if (transRecord.contains(itemName)) {
					sortedRecord.add(itemName);
				}
			}
		}

		return sortedRecord;
	}

	/**
	 * ������ͷ�������������Ƶ��һ�
	 * 
	 * @param transRecords
	 *            ԭʼ��������
	 * @return ��ͷ��
	 */
	public ArrayList<TreeNode> buildHeaderTable(List<List<String>> transRecords) {
		ArrayList<TreeNode> headTable = null;

		if (transRecords != null && transRecords.size() > 0) {
			headTable = new ArrayList<TreeNode>();
			Map<String, TreeNode> map = new HashMap<String, TreeNode>();
			// �����������ݿ��и����֧�ֶ�
			for (List<String> record : transRecords) {
				for (String item : record) {
					if (!map.keySet().contains(item)) {
						TreeNode node = new TreeNode(item);
						node.setCount(1);
						map.put(item, node);
					} else {
						map.get(item).countIncrement(1);
					}
				}
			}
			// ��֧�ֶȴ��ڣ�����ڣ�minSup������뵽F1��
			Set<String> names = map.keySet();
			for (String name : names) {
				TreeNode tnode = map.get(name);
				if (tnode.getCount() >= MIN_SUP_COUNT) {
					headTable.add(tnode);
				}
			}
			// ����comparable�ӿڽ��н�������
			Collections.sort(headTable);
		}

		return headTable;
	}

	/**
	 * FP�����㷨�ĵݹ�ʵ��
	 * 
	 * @param transRecords
	 *            ��������
	 * @param postPattern
	 *            ��׺ģʽ
	 */
	public void fpGrowth(TreeNode root, List<String> postPattern,
			ArrayList<TreeNode> headerTable, List<FrePattern> frepatternList) {
		// �ж��Ƿ��ǵ�һ·���������ռ�Ƶ��ģʽ�Ľ��	
		if (isSinglePath(root)) {
			gatherRes(root, postPattern, frepatternList);
		} else {
			for (int i = headerTable.size() - 1; i >= 0; i--) {
				List<String> newPostPattern = new ArrayList<String>();
				TreeNode header = headerTable.get(i);

				newPostPattern.add(header.getName());
				if (postPattern != null) {
					newPostPattern.addAll(postPattern);
				}

				// Ѱ��header������ģʽ��CPB������newTransRecords��
				List<List<String>> newTransRecords = new LinkedList<List<String>>();
				TreeNode backnode = header.getNextHomonym();

				int tmpCount = 0;
				List<String> itemList = null;

				while (backnode != null) {
					int counter = backnode.getCount();
					tmpCount += counter;
					List<String> prenodes = new LinkedList<String>();
					TreeNode parent = backnode;
					// ����backnode�����Ƚڵ㣬�ŵ�prenodes��
					while ((parent = parent.getParent()).getName() != null) {
						prenodes.add(0, (parent.getName()));
					}

					// ���ݽڵ���ָ��ָ��ڵ��count������Ӷ������ݵ��������ݼ���
					if (prenodes.size() > 0) {
						while (counter-- > 0) {
							newTransRecords.add(prenodes);
						}
					} else {
						if (newPostPattern.size() > 1) {
							itemList = new ArrayList<String>(newPostPattern);
						}
					}

					backnode = backnode.getNextHomonym();
				}

				if (itemList != null) {
					FrePattern frePattern = new FrePattern(itemList, tmpCount);
					frepatternList.add(frePattern);
				}

				ArrayList<TreeNode> newHeaderTable = buildHeaderTable(newTransRecords);
				// ����FP-Tree
				TreeNode treeRoot = buildFPTree(newTransRecords, newHeaderTable);
				
				// �ݹ����FPGrowth�㷨
				fpGrowth(treeRoot, newPostPattern, newHeaderTable,
						frepatternList);
			}
		}
	}

	/**
	 * ����ǰ׺ģʽ��������ϣ����׺ģʽ����Ƶ����
	 * @param root	���ڵ�
	 * @param postPattern	��׺ģʽ
	 * @param frepatternList	�ھ�����
	 */
	private void gatherRes(TreeNode root, List<String> postPattern,
			List<FrePattern> frepatternList) {
		if (root == null) {
			return;
		}
		
		// ���ѭ�����������ڵ�
		while (root.getChildren() != null) {
			root = root.getChildren().get(0);
			TreeNode tmpRoot = root;
			int minCount = tmpRoot.getCount();
			List<String> tmpList = new ArrayList<String>();

			tmpList.add(tmpRoot.getName());
			addResult(tmpList, minCount, postPattern, frepatternList);

			// ��ѭ���������ӳ�ǰ׺ģʽ��ÿ����һ�Σ����һ��Ƶ��ģʽ
			while (tmpRoot.getChildren() != null) {
				tmpRoot = tmpRoot.getChildren().get(0);
				if (tmpRoot.getCount() < minCount) {
					minCount = tmpRoot.getCount();
				}
				tmpList.add(tmpRoot.getName());
				addResult(tmpList, minCount, postPattern, frepatternList);
			}
		}
	}

	/**
	 * ���ھ򵽵�Ƶ��ģʽ��ӵ�������С�
	 * @param tmpList
	 *            Ƶ��ģʽǰ׺
	 * @param count
	 *            ����
	 * @param postPattern
	 *            ��׺ģʽ
	 * @param frepatternList
	 *            �����
	 */
	private void addResult(List<String> tmpList, int count,
			List<String> postPattern, List<FrePattern> frepatternList) {
		List<String> itemList = new ArrayList<String>(tmpList);

		if (postPattern != null) {
			itemList.addAll(postPattern);
		}

		FrePattern frePattern = new FrePattern(itemList, count);
		frepatternList.add(frePattern);
	}

	/**
	 * �ݹ鵱ǰ�����жϵ�ǰ���Ƿ����������·��
	 * @param root
	 *            �����ڵ�
	 * @return
	 */
	private boolean isSinglePath(TreeNode root) {
		List<TreeNode> childList = root.getChildren();

		if (childList == null) {
			return true;
		} else if (childList != null && childList.size() > 1) {
			return false;
		} else {
			return isSinglePath(root.getChildren().get(0));
		}
	}

	/**
	 * FP�㷨run���������г�ʼFP������
	 */
	public void run() {
		// ��ȡԭʼ�������ݼ�
		List<List<String>> transRecords = readTransRecord("data.txt");
		// ������ͷ��ͬʱҲ��Ƶ��1�
		ArrayList<TreeNode> headerTable = buildHeaderTable(transRecords);
		// ����FP-Tree
		TreeNode treeRoot = buildFPTree(transRecords, headerTable);

		if (treeRoot != null && treeRoot.getChildren() != null) {
			// ��ʼ�������
			List<FrePattern> frepatternList = new ArrayList<FrePattern>();
			fpGrowth(treeRoot, null, headerTable, frepatternList);

			printResult(frepatternList);
		} else {
		}
	}

	/**
	 * ���Ƶ��ģʽ�ھ���
	 * 
	 * @param frepatternList
	 */
	private void printResult(List<FrePattern> frepatternList) {
		for (FrePattern frePattern : frepatternList) {
			System.out.println(frePattern);
		}
	}

	public static void main(String[] args) {
		System.out.println("FP�㷨Ƶ��ģʽ�ھ���Ϊ��");
		new FP().run();
	}
}
