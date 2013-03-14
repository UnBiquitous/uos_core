package br.unb.unbiquitous.ubiquitos.uos.driverManager;

import java.util.ArrayList;
import java.util.List;

import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDriver;

public class TreeNode {

	private List<TreeNode> parent;
	private UpDriver driver;
	private List<TreeNode> children;
	
	public TreeNode(UpDriver driver) {
		if(driver == null) throw new IllegalArgumentException("Driver cannot be null.");
		this.driver = driver;
		this.parent = new ArrayList<TreeNode>();
		this.children = new ArrayList<TreeNode>();
	}
	
	public void addChild(TreeNode node) {
		if(node == null) throw new IllegalArgumentException("Node cannot be null.");
		children.add(node);
		
		for (TreeNode child : children) {
			child.addParent(this);
		}
	}
	
	public List<TreeNode> getChildren() {
		return children;
	}
	
	private void addParent(TreeNode node) {
		parent.add(node);
	}
	
	public UpDriver getUpDriver() {
		return driver;
	}
}
