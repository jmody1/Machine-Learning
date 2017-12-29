import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class Main {
	
	private ArrayList<Attribute> attributeMap = new ArrayList<>();
	private int arr[][];
	
	public static String trainingSet, validationSet, testSet;
	public static boolean toPrint = false;

	public static void main(String[] args) throws IOException {
		
		if(args.length<3) throw new IllegalArgumentException("Illegal Args");
		if(args[0] == null || args[1] == null || args[2] == null) throw new IllegalArgumentException("Arguments are not valid");
		
		trainingSet = args[0];
		validationSet = args[1];
		testSet = args[2];
		
		if(args[3] != null) toPrint = (args[3].equals("true")?true:false);
		
		
		
		
		new Main();
	}
	
	public Main() throws IOException{
		FileReader reader = new FileReader(trainingSet);
		CSVParser parser = CSVParser.parse(reader, CSVFormat.RFC4180);
		List<CSVRecord> records = parser.getRecords();
		
		CSVRecord attributeRecord = records.get(0);
		Iterator<String> attributeIterator = attributeRecord.iterator();

		int i = 0;
		while (attributeIterator.hasNext()) {
			String attribute = attributeIterator.next();
			attributeMap.add(new Attribute(attribute));
			i++;
		}
		
		arr = new int[attributeMap.size()][records.size()-1];
		
		for (i = 0; i < attributeMap.size(); i++) {
			for(int j = 1; j < records.size(); j++){
				String data = records.get(j).get(i);
				arr[i][j-1] = new Integer(data);
			}
		}
		
		/*
		 * Select the root node
		 */
		Node root = selectNode(new HashMap<Integer, Integer>(), null);
		
		if(toPrint)
			traverseTree(root, 0, -1);
		
		validate(root);
		//traverse through tree
		
	}
	
	
	private void validate(Node root) throws IOException {
		
		double countPositive = 0, accuracy;
		
		FileReader reader = new FileReader(validationSet);
		CSVParser parser = CSVParser.parse(reader, CSVFormat.RFC4180);
		List<CSVRecord> records = parser.getRecords();
		for (int j = 1; j < records.size(); j++) {
			countPositive += validateRecord(records.get(j), root);
		}
		
		accuracy = (countPositive/new Double(records.size()-1))*100d;
		System.out.println();
		System.out.println("Accuracy is : " + accuracy);
		
	}
	
	/**
	 * 
	 * @param record
	 * @param root
	 * @return 1 if positive, 0 if negative
	 */
	private int validateRecord(CSVRecord record, Node root){
		if(root == null){
			System.out.println("Code should not reach here. Counting as negative.");
			return 0;
		} else if(root.getAttribute() != null){
			if(Integer.parseInt(record.get(root.getAttribute())) == 0){
				return validateRecord(record, root.getBranchZero());
			} else {
				return validateRecord(record, root.getBranchOne());
			}
		} else {
			if(Integer.parseInt(record.get(attributeMap.size()-1)) == root.getValue()){
				return 1;
			} else return 0;
		}
	}
	
	
	/**
	 * 
	 * @param root
	 * @param string
	 * @param branch
	 * @return
	 * 
	 * 
	 * 
	 * Wesley = 0 :
	 * | Honor = 0 :
	 * | | barclay = 0 : 1
	 * | | barclay = 1 : 0
	 * | Honor = 1 :
	 * | | tea = 0 : 0
	 * | | tea = 1 : 1
	 * Wesley = 1 : 0 
	 */
	private void traverseTree(Node root, int indent, int branch){
		
		
		
		if(root == null){
			
			return;
		}
		else if(root.getAttribute()!=null){
			
			if(branch != -1){
				System.out.print(" = " + branch + " :");
			}
			System.out.println();
			for(int i=0; i<indent; i++){
				System.out.print("| ");
			}
			
			System.out.print(attributeMap.get(root.getAttribute()).getName()); //Wesely
			//see left
			traverseTree(root.getBranchZero(), indent+1, 0);
			//see right
			System.out.println();
			for(int i=0; i<indent; i++){
				System.out.print("| ");
			}

			System.out.print(attributeMap.get(root.getAttribute()).getName());
			traverseTree(root.getBranchOne(), indent+1, 1);
		} else {
			//attribute is null
			//print data
			System.out.print(" = " + branch + " : " + root.getValue());
		}
		
		
		
		
	}
	
	private double calculateClassImpurity(double classCountOne, double classCountZero, boolean method){
		double classTotal = classCountOne + classCountZero;
		if(method){ //Entropy
			double classProbabilityZero = (classCountZero / classTotal);
			double classProbabilityOne = (classCountOne) / classTotal;
			
			//find class entropy
			double classEntropy = (-classProbabilityZero * (Math.log(classProbabilityZero) / Math.log(2)))
					+ (-classProbabilityOne * (Math.log(classProbabilityOne) / Math.log(2)));
			
			return classEntropy;
		} else { //Variance
			return (classCountOne/classTotal) * (classCountZero/classTotal);
		}
	}
	
	private Node selectNode(HashMap<Integer, Integer> filters, Node root){
		
		double classCountZero = 0, classCountOne = 0, classTotal = 0;
		
		//with filters applied
		//find number of yes
		//find number of no
		for(int i = 0; i < arr[attributeMap.size()-1].length; i++){
			int value = arr[attributeMap.size()-1][i];
			if(filters.size() == 0){
				if(value == 0) classCountZero++;
				else classCountOne++;
			} else {
				Set<Integer> keySet = filters.keySet();
				Iterator<Integer> keySetIterator = keySet.iterator();
				boolean matched = true;
				while(keySetIterator.hasNext()){
					Integer key = keySetIterator.next();
					if(arr[key][i] != filters.get(key)){
						matched = false;
						break;
					}
				}
				
				if(matched && value == 0){
					classCountZero++;
				} 
				else if(matched && value == 1){
					classCountOne++;
				}
			}
		}
		
		
		classTotal = classCountOne + classCountZero;
		
		if(classTotal == 0){
			return null;
		} else if(classCountOne == classTotal){
			Node node = new Node(null, root);
			node.setValue(1);
			return node;
		} else if(classCountZero == classTotal){
			Node node = new Node(null, root);
			node.setValue(0);
			return node;
		}
		
		//find class entropy
		double classEntropy = calculateClassImpurity(classCountOne, classCountZero, true);
		//forEach in attributeMap

		double count11 = 0;
		double count10 = 0;
		double count01 = 0;
		double count00 = 0;
		int largestIndex = -1;
		boolean found = false;
		for(int i=0; i<attributeMap.size()-1; i++){
			count11 = 0;
			count10 = 0;
			count01 = 0;
			count00 = 0;
			//if notUsed
			if(!filters.containsKey(i)) {
				found = true;
				if(largestIndex == -1) largestIndex = i;
				double totalCount = 0;
				for(int j=0; j<arr[0].length; j++){
					if(filters.size() == 0){
						totalCount = arr[0].length;
						if(arr[i][j] == 0 && arr[attributeMap.size()-1][j]==0) count00++;
						if(arr[i][j] == 0 && arr[attributeMap.size()-1][j]==1) count01++;
						if(arr[i][j] == 1 && arr[attributeMap.size()-1][j]==0) count10++;
						if(arr[i][j] == 1 && arr[attributeMap.size()-1][j]==1) count11++;
					} else {
						Set<Integer> keySet = filters.keySet();
						Iterator<Integer> keySetIterator = keySet.iterator();
						boolean matched = true;
						while(keySetIterator.hasNext()){
							Integer key = keySetIterator.next();
							if(arr[key][j] != filters.get(key)){
								matched = false;
							} else 
								totalCount++;
						}
						
						if(matched){
							if(arr[i][j] == 0 && arr[attributeMap.size()-1][j]==0) count00++;
							if(arr[i][j] == 0 && arr[attributeMap.size()-1][j]==1) count01++;
							if(arr[i][j] == 1 && arr[attributeMap.size()-1][j]==0) count10++;
							if(arr[i][j] == 1 && arr[attributeMap.size()-1][j]==1) count11++;
						}
						
					}
				}
				
				
				double attributeProbability11 = ((count11 + count10)==0?0:(count11 / (count11 + count10)));
				double attributeProbability10 = ((count10 + count11)==0?0:(count10) / (count10 + count11));
				
				double hsv1 = (attributeProbability10==0?0:(-attributeProbability10*(Math.log(attributeProbability10) / Math.log(2)))) + (attributeProbability11==0?0:(-attributeProbability11*(Math.log(attributeProbability11) / Math.log(2))));
				double attributeProbability01 = ((count01 + count00)==0?0:(count01) / (count01 + count00));
				double attributeProbability00 = ((count00 + count01)==0?0:(count00) / (count00 + count01));
				double hsv0 = (attributeProbability00==0?0:(-attributeProbability00*(Math.log(attributeProbability00) / Math.log(2))))+(attributeProbability01==0?0:(-attributeProbability01*(Math.log(attributeProbability01) / Math.log(2))));
				
				double expectedEntropy = ((count11 + count10)/totalCount) * hsv1 + ((count01 + count00)/totalCount) * hsv0;
				double gain = classEntropy - expectedEntropy;
				attributeMap.get(i).gain = gain;
				if(attributeMap.get(largestIndex).gain<gain){
					largestIndex = i;
				}
			}
		}
		
		if(!found){
			return null;
		}
		
		

		

//		

		
		Attribute attribute = attributeMap.get(largestIndex);
		attribute.setUsed(true);
		Node node = new Node(largestIndex, root);
		Node nodeZero, nodeOne;

		node = new Node(largestIndex, root);
		
		
		HashMap<Integer, Integer> filter = (HashMap<Integer, Integer>) filters.clone();
		filter.put(largestIndex, 0);

		nodeZero = selectNode(filter, node);
		
		
		
		filter = (HashMap<Integer, Integer>) filters.clone();
		filter.put(largestIndex, 1);
		nodeOne = selectNode(filter, node);

		node.setBranchZero(nodeZero);
		node.setBranchOne(nodeOne);
		
		
		return node;

		
	}

}

class Attribute {
	
	private String name;
	
	private boolean isUsed = false;
	
	public double gain = 0;
	
	public Attribute(String name){
		this.name = name;
	}

	public boolean isUsed() {
		return isUsed;
	}

	public void setUsed(boolean isUsed) {
		this.isUsed = isUsed;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
	

}

class Node {
	/**
	 * When this Node is leaf, attribute is null
	 */
	private Integer attribute;
	/**
	 * When this node is root, parent is null
	 */
	private Node parent;
	private Node branchZero, branchOne;
	private Integer value;
	private HashMap<Integer, Integer> previousValues = new HashMap();
	
	public Node(Integer attribute, Node parent) {
		super();
		this.attribute = attribute;
		this.parent = parent;
	}
	
	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Integer getAttribute() {
		return attribute;
	}

	public void setAttribute(Integer attribute) {
		this.attribute = attribute;
	}

	public Node getBranchZero() {
		return branchZero;
	}

	public void setBranchZero(Node branchZero) {
		this.branchZero = branchZero;
	}

	public Node getBranchOne() {
		return branchOne;
	}

	public void setBranchOne(Node branchOne) {
		this.branchOne = branchOne;
	}

	public HashMap<Integer, Integer> getPreviousValues() {
		return previousValues;
	}

	public void setPreviousValues(HashMap<Integer, Integer> previousValues) {
		this.previousValues = previousValues;
	}

	public Node getParent() {
		return parent;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}
	
	
	
	
	
	
	
}

