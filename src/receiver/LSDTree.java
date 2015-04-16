package receiver;

import java.awt.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import weka.core.matrix.Matrix;

public class LSDTree {

	private Node root;
	private Node minNode;
	final int S_values[] = { -7, -5, -3, -1, 1, 3, 5, 7 };

	private double[] error;
	private double R;// Sphere r value
	private int K;
	private int precision = 1000000;// upto which number of decimal places

	private ArrayList<Double[]> level_values_next;
	private ArrayList<Node> currentlevel;
	private ArrayList<Node> nextlevel;
	Matrix R_mat;

	LSDTree(double R, int K) {

		this.root = new Node(0, null);
		this.root.setvalue(0);
		this.minNode = new Node(0, null, 9999999999999.9);
		currentlevel = new ArrayList<>(32);
		nextlevel = new ArrayList<>(32);

		level_values_next = new ArrayList<>();
		this.error = new double[64];
		this.K = K;
		this.R = R;
	}

	void printTree() {

		root.printNodes();
	}

	void generateFirstlevel(double y) {

		int count = 0;
		double temp = 0;
		int index = 0;
		Double[] index_val = { 0.0, 0.0, 0.0 };
		level_values_next.clear();
		/*
		 * currentlevel.clear(); nextlevel.clear();
		 */
		for (int i = 0; i < 8; i++) {

			level_values_next.add(count,
					new Double[] { (calcValue(i, y, 0, root)), (double) i,
							(double) 0 });
			count++;

		}

		// Collections.sort(level_values);
		
		Collections.sort(level_values_next, new Comparator<Double[]>() {

			@Override
			public int compare(Double[] arg0, Double[] arg1) {
				// TODO Auto-generated method stubnt

				if (Double.compare(arg0[0],arg1[0])>0) {
					return -1;
				} else {
					return 1;
				}

			}
		});

		for (int i = level_values_next.size() - 1; i >= 0; i--) {
			if (i >= (count - K)) {
				index_val = level_values_next.get(i);
				index = index_val[1].intValue();
				temp = index_val[0];
				// System.out.println(temp);
				// System.out.println(index);
				// System.out.println(temp);

				// System.out.println(node_index);

				root.addNodetonextlevel(index, root, temp);

			}

		}

		int count_cl = 0;

		for (Iterator<Node> iterator = root.getnextlevel().iterator(); iterator
				.hasNext();) {
			Node node = (Node) iterator.next();
			currentlevel.add(count_cl, new Node(0, null));
			currentlevel.get(count_cl).copyNode(node.getNode_S(),
					node.getvalue(), node.getparent(), node.getnextlevel());
			count_cl++;
		}

		/*
		 * System.out.println("************************");
		 * System.out.println("Y : " + y); for (Iterator<Node> iterator =
		 * currentlevel.iterator(); iterator .hasNext();) { Node node = (Node)
		 * iterator.next(); node.printNumValue(); }
		 */

		/*
		 * for (Iterator<Double[]> iterator = level_values_next.iterator();
		 * iterator .hasNext();) { Double[] val = iterator.next();
		 * System.out.println(val[0] + " : " + val[1] + " " + val[2]); }
		 */

	}

	void setRMatrix(Matrix R) {
		R_mat = R;
	}

	void generateNextlevel(double y, int level) {
		int count = 0;
		double temp = 0;
		int index = 0;
		int node_index = 0;
		Double[] index_val = { 0.0, 0.0, 0.0 };
		double current_node_val = 0;

		level_values_next.clear();
		/*
		 * for (int i = 0; i < 8; i++) { error[i] = calcValue(i, y, level);
		 * 
		 * System.out.println("error = " +real[i]+" + "+complex[i]+"i -- "+in
		 * [0]+" + "+in[1]+"i : "+error[i]);
		 * 
		 * }
		 */

		/*
		 * System.out.println("************************" + level);
		 * System.out.println("Y : " + y);
		 */
		for (int j = 0; j < currentlevel.size() ; j++) {
			current_node_val = currentlevel.get(j).getvalue();
			for (int i = 0; i < 8; i++) {

			//	System.out.print(" sent node : ");
				/*
				 * currentlevel.get(j).getparent()
				 * .findNode_with_S(currentlevel.get(j).getNode_S())
				 * .printNumValue();
				 */
				level_values_next
						.add(count,
								new Double[] {
										(calcValue(
												i,
												y,
												level,
												currentlevel
														.get(j)
														.getparent()
														.findNode_with_S(
																currentlevel
																		.get(j)
																		.getNode_S())) + current_node_val),
										(double) i, (double) j });
			
				count++;

			}

		}
	//	System.out.println("level_values_next"+level_values_next.size());
		
		Collections.sort(level_values_next, new Comparator<Double[]>() {

			@Override
			public int compare(Double[] arg0, Double[] arg1) {
				// TODO Auto-generated method stubnt

				return arg1[0].compareTo(arg0[0]);


			}
		});

		/*
		 * for (Iterator<Double[]> iterator = level_values_next.iterator();
		 * iterator .hasNext();) { Double[] val = iterator.next();
		 * System.out.println(val[0]+" : "+val[1]+" "+val[2]); }
		 */
		int count_k = 0;
		for (int i = level_values_next.size() - 1; i >= 0; --i) {
			if (count_k < (K)) {

				count_k++;
				index_val = level_values_next.get(i);

				index = index_val[1].intValue();
				temp = index_val[0];
				node_index = index_val[2].intValue();
				// System.out.println(index+" "+node_index+" "+temp);
				// System.out.println(node_index);
				/*
				 * currentlevel.get(node_index).addNodetonextlevel(index,
				 * currentlevel.get(node_index), temp);
				 */

				currentlevel
						.get(node_index)
						.getparent()
						.findNode_with_S(
								currentlevel.get(node_index).getNode_S())
						.addNodetonextlevel(
								index,
								currentlevel
										.get(node_index)
										.getparent()
										.findNode_with_S(
												currentlevel.get(node_index)
														.getNode_S()), temp);
				// System.out.println(count_k+" : "+temp);
			}

		}
		nextlevel.clear();
		minNode.copyNode(0, 99999999.9, null);
		// minNode.copyNode(node1.getNode_S(), node1.getvalue(),
		// node1.getparent());
		// minNode.setvalue(999999999999999.9);

		int count_nl = 0;
		for (Iterator<Node> iterator = currentlevel.iterator(); iterator
				.hasNext();) {
			Node node = (Node) iterator.next();
			for (Iterator<Node> iterator2 = node.getnextlevel().iterator(); iterator2
					.hasNext();) {
				Node node1 = (Node) iterator2.next();
				if (minNode.getvalue() > node1.getvalue()) {
					minNode.copyNode(node1.getNode_S(), node1.getvalue(),
							node1.getparent());
				}
				// nextlevel.add(node1);
				nextlevel.add(count_nl, new Node(0, null));
				nextlevel.get(count_nl).copyNode(node1.getNode_S(),
						node1.getvalue(), node1.getparent(),
						node1.getnextlevel());
				count_nl++;

			}

		}
		// Node temp_val=new Node(0, null);
		// temp_val=minNode;
		// System.out.print(" this level best  value : ");
		// System.out.println(minNode.getvalue());
		currentlevel.clear();
		int count_cl = 0;

		for (Iterator<Node> iterator = nextlevel.iterator(); iterator
				.hasNext();) {
			Node node = (Node) iterator.next();
			currentlevel.add(count_cl, new Node(0, null));
			currentlevel.get(count_cl).copyNode(node.getNode_S(),
					node.getvalue(), node.getparent(), node.getnextlevel());
			
			count_cl++;
		}
		// int count_level = 1;
		// System.out.println("**************");
		/*
		 * for (Iterator<Node> iterator = currentlevel.iterator(); iterator
		 * .hasNext();) { Node node = (Node) iterator.next();
		 * System.out.print(count_level+" : ");
		 * //node.getparent().printNumValue(); node.printNumValue();
		 * count_level++; }
		 */

	}

	public Node getMinnode() {
		return minNode;
	}
	public void printcurrentlevel(){
		int count=1;
		for (Iterator<Node> iterator = currentlevel.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			System.out.print(count+" : ");
			node.printNumValue();
			count++;
		}
	}

	private double calcValue(int i, double y, int level, Node node) {
		// TODO Auto-generated method stub
		double value = 0;
		double sum = 0.0;
		int S_temp = S_values[i];
		Node temp = new Node(1, null);
		temp.copyNode(node.getNode_S(), node.getvalue(), node.getparent());
		if (level == 0) {
			value = (y - S_values[i] * R_mat.get(31, 31));
			value = value * value;
		} else {
			//System.out.print("  level " + (level) + " : ");
			
			sum = S_temp * R_mat.get(31 - level, 31-level);
			
			//System.out.println("S_value " + S_temp + "\t j:-1");
		//	System.out.print("          : ");
			S_temp = temp.getNode_S();
			for (int j = 0; j < level; j++) {
				sum = S_temp * R_mat.get(31 - level, 31 -level+ (j + 1)) + sum;

			//	System.out.println("S_value " + S_temp + "\t j: " + j + " \t "
				//		+ temp.getNode_S() + " " + temp.getvalue());

				//if (j != 0 && j < level) {
				if ( j < level) {
					temp.copyNode(temp.getparent().getNode_S(), temp
							.getparent().getvalue(), temp.getparent()
							.getparent());
					
					S_temp = temp.getNode_S();
				}/* else if (j == 0) {

					S_temp = temp.getNode_S();
				}*/
			//	System.out.print("          : ");
			}
		//	System.out.println();
			value = (y - sum) * (y - sum);
		}

		return (value);
	}

	private boolean testSphere(int i, double r, double im) {

		return true;

	}

}

class Node {
	final int S_values[] = { -7, -5, -3, -1, 1, 3, 5, 7 };
	private int S;

	private double value;
	private Node parent;
	private ArrayList<Node> nextlevel;

	// constructor
	Node(int i, Node parent) {
		this.S = S_values[i];
		this.parent = parent;
		this.nextlevel = new ArrayList<>();
		this.value = 0;
	}

	Node(int i, Node parent, double value) {
		this.S = S_values[i];
		this.parent = parent;
		this.nextlevel = new ArrayList<>();
		this.value = value;
	}

	// //////////////////////////////////////////////////
	public void setvalue(double value) {
		this.value = value;
	}

	public void copyNode(int S, double value, Node Parent) {
		this.S = S;
		this.value = value;
		this.parent = Parent;
	}

	public void copyNode(int S, double value, Node Parent, ArrayList<Node> next) {
		this.S = S;
		this.value = value;
		this.parent = Parent;
		this.nextlevel = next;
	}

	public Node findNode_with_S(int s) {

		for (Iterator<Node> iterator = nextlevel.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			if (node.S == s) {
				return node;
			}
		}
		return null;

	}

	// //////////////////////////////////////////////////
	public double getvalue() {
		return value;
	}

	public int getNode_S() {
		return S;
	}

	public Node getparent() {
		return parent;
	}

	public ArrayList<Node> getnextlevel() {
		return nextlevel;
	}

	public void addNodetonextlevel(int i, Node parent, double value) {
		this.nextlevel.add(new Node(i, parent, value));
	}

	// //////////////////////////////////////////////////
	public void printnum() {
		System.out.println(S + " : ");

	}

	public void printvalue() {
		System.out.println("value : " + value);

	}

	public void printNumValue() {
		System.out.println(S + " : " + value);
	}
	
	void printParents(){
		Node temp=new Node(1,null);
		temp=parent;
		while(temp!=null){
			System.out.print("("+parent.getNode_S()+" : "+parent.getvalue()+") \t");
			temp=temp.getparent();
		}
	}
	


	void printNodes() {

		if (nextlevel.size() == 0) {
			System.out.println("(" + S + " : " + value + ") ! ");
		} else {
			
			for (Iterator<Node> iterator = nextlevel.iterator(); iterator
					.hasNext();) {
				Node node = (Node) iterator.next();
				System.out.print("  ---   ");
				node.printNodes();
			}

			System.out.println("-->(" + S + " : " + value + ") ");
		}

	}

}
