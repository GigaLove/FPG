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
	 * 读取事务数据，将事务数据放到二维List中
	 * 
	 * @param fileName
	 *            事务文件名
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
	 * 构建FP增长树
	 * 
	 * @param transRecords
	 *            事务数据
	 * @param headTable
	 *            项头表
	 * @return
	 */
	public TreeNode buildFPTree(List<List<String>> transRecords,
			ArrayList<TreeNode> headTable) {
		TreeNode root = new TreeNode(); // 创建树的根节点

		for (List<String> transRecord : transRecords) {
			LinkedList<String> record = sortRecordByTable(transRecord,
					headTable);

			insertTree(record, root, headTable);
		}

		return root;
	}

	/**
	 * 把record作为ancestor的后代插入树中，同时完善项头表的指针
	 * @param ancestor
	 *            父节点
	 * @param record
	 *            待插入的记录
	 * @param headTable
	 *            项头表
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
		
		// 儿子节点没找到或者根节点的儿子为空
		// 则创建新节点，添加到父节点中
		if (!flag) {
			newTreeNode = new TreeNode(itemName);
			newTreeNode.setCount(1);
			ancestor.addChild(newTreeNode);
			newTreeNode.setParent(ancestor);

			for (TreeNode f1Item : headTable) {
				if (f1Item.getName().equals(newTreeNode.getName())) {
					// 迭代项头表中节点链指针
					while (f1Item.getNextHomonym() != null) {
						f1Item = f1Item.getNextHomonym();
					}
					f1Item.setNextHomonym(newTreeNode);
					break;
				}
			}
		}
		
		// 如果记录不为空，继续递归调用
		if (record.size() > 0) {
			insertTree(record, newTreeNode, headTable);
		}
	}

	/**
	 * 把交易记录按项头表中项的频繁程序降序排列
	 * 
	 * @param transRecord
	 *            事务数据
	 * @param headTable
	 *            项头表
	 * @return
	 */
	public LinkedList<String> sortRecordByTable(List<String> transRecord,
			ArrayList<TreeNode> headTable) {
		LinkedList<String> sortedRecord = null;

		if (transRecord != null && headTable != null) {
			sortedRecord = new LinkedList<String>();
			// 降序迭代项头表，判断原始数据中是否包含表项，包含则压入到新的list中
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
	 * 构建项头表，即降序排序的频繁一项集
	 * 
	 * @param transRecords
	 *            原始事务数据
	 * @return 项头表
	 */
	public ArrayList<TreeNode> buildHeaderTable(List<List<String>> transRecords) {
		ArrayList<TreeNode> headTable = null;

		if (transRecords != null && transRecords.size() > 0) {
			headTable = new ArrayList<TreeNode>();
			Map<String, TreeNode> map = new HashMap<String, TreeNode>();
			// 计算事务数据库中各项的支持度
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
			// 把支持度大于（或等于）minSup的项加入到F1中
			Set<String> names = map.keySet();
			for (String name : names) {
				TreeNode tnode = map.get(name);
				if (tnode.getCount() >= MIN_SUP_COUNT) {
					headTable.add(tnode);
				}
			}
			// 基于comparable接口进行降序排序
			Collections.sort(headTable);
		}

		return headTable;
	}

	/**
	 * FP增长算法的递归实现
	 * 
	 * @param transRecords
	 *            事务数据
	 * @param postPattern
	 *            后缀模式
	 */
	public void fpGrowth(TreeNode root, List<String> postPattern,
			ArrayList<TreeNode> headerTable, List<FrePattern> frepatternList) {
		// 判断是否是单一路径，是则收集频繁模式的结果	
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

				// 寻找header的条件模式基CPB，放入newTransRecords中
				List<List<String>> newTransRecords = new LinkedList<List<String>>();
				TreeNode backnode = header.getNextHomonym();

				int tmpCount = 0;
				List<String> itemList = null;

				while (backnode != null) {
					int counter = backnode.getCount();
					tmpCount += counter;
					List<String> prenodes = new LinkedList<String>();
					TreeNode parent = backnode;
					// 遍历backnode的祖先节点，放到prenodes中
					while ((parent = parent.getParent()).getName() != null) {
						prenodes.add(0, (parent.getName()));
					}

					// 根据节点链指针指向节点的count数，添加多条数据到事务数据集中
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
				// 构建FP-Tree
				TreeNode treeRoot = buildFPTree(newTransRecords, newHeaderTable);
				
				// 递归调用FPGrowth算法
				fpGrowth(treeRoot, newPostPattern, newHeaderTable,
						frepatternList);
			}
		}
	}

	/**
	 * 遍历前缀模式的所有组合，与后缀模式构成频繁项
	 * @param root	根节点
	 * @param postPattern	后缀模式
	 * @param frepatternList	挖掘结果集
	 */
	private void gatherRes(TreeNode root, List<String> postPattern,
			List<FrePattern> frepatternList) {
		if (root == null) {
			return;
		}
		
		// 外层循环，迭代根节点
		while (root.getChildren() != null) {
			root = root.getChildren().get(0);
			TreeNode tmpRoot = root;
			int minCount = tmpRoot.getCount();
			List<String> tmpList = new ArrayList<String>();

			tmpList.add(tmpRoot.getName());
			addResult(tmpList, minCount, postPattern, frepatternList);

			// 内循环，不断延长前缀模式，每扩充一次，添加一次频繁模式
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
	 * 将挖掘到的频繁模式添加到结果集中、
	 * @param tmpList
	 *            频繁模式前缀
	 * @param count
	 *            计数
	 * @param postPattern
	 *            后缀模式
	 * @param frepatternList
	 *            结果集
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
	 * 递归当前树，判断当前树是否仅包含单个路径
	 * @param root
	 *            树根节点
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
	 * FP算法run方法，进行初始FP树迭代
	 */
	public void run() {
		// 读取原始事务数据集
		List<List<String>> transRecords = readTransRecord("data.txt");
		// 构建项头表，同时也是频繁1项集
		ArrayList<TreeNode> headerTable = buildHeaderTable(transRecords);
		// 构建FP-Tree
		TreeNode treeRoot = buildFPTree(transRecords, headerTable);

		if (treeRoot != null && treeRoot.getChildren() != null) {
			// 初始化结果集
			List<FrePattern> frepatternList = new ArrayList<FrePattern>();
			fpGrowth(treeRoot, null, headerTable, frepatternList);

			printResult(frepatternList);
		} else {
		}
	}

	/**
	 * 输出频繁模式挖掘结果
	 * 
	 * @param frepatternList
	 */
	private void printResult(List<FrePattern> frepatternList) {
		for (FrePattern frePattern : frepatternList) {
			System.out.println(frePattern);
		}
	}

	public static void main(String[] args) {
		System.out.println("FP算法频繁模式挖掘结果为：");
		new FP().run();
	}
}
