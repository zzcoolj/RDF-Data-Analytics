package exploratoryRDFAnalytics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class Visualization_D3 {
	
	
	/**
	 * Transfer the property combination(ArrayList<String>) & its count(Integer)
	 * result into a csv file to get the sequences sunburst graph.
	 * 
	 * For the whole pivot table, numPropertiesFrequencyNotZero equals to numProperties;
	 * For part of the pivot table of different types, numPropertiesFrequencyNotZero may smaller than numProperties, because some of the properties' frequency is 0. 
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public void resultTranslatorForSequencesSunburst(HashMap<String, Integer> propertyCombinationCount, HashMap<String, Integer> propertyOrder, String csvFileName, int numPropertiesFrequencyNotZero, boolean considerTypeAsProperty, int typePropertyPosition) 
			throws IOException, URISyntaxException {
		String filePath = GlobalSettings.D3_SEQUENCES_SUNBURST_FILE_FOLDER_PATH + csvFileName + ".csv";
		// Open the file
		File file = new File(filePath);
		file.createNewFile();
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		System.out.println("################# Generate " + csvFileName + ".csv file for data visualization #################");
		
		for (Entry<String, Integer> entry : propertyCombinationCount.entrySet()) {
			// Attention: properties here is a string like "t	null	null" rather than property names
			String properties = entry.getKey();
			Integer count = entry.getValue();
			//System.out.println("Result Translate Start*********"+ properties + count + "*********");
			String[] translatedProperties = Visualization_D3.translatePropertiesResult(properties, propertyOrder);
			
			/* Deprecated solution: For one combination of properties, get all permutations of this combination.
			ArrayList<ArrayList<String>> allPermutationsOfTranslatedProperties = Visualization_D3.permutation(translatedProperties, 0, translatedProperties.length-1);
			for(ArrayList<String> permutation: allPermutationsOfTranslatedProperties) {
				String line = "";
				for(String translatedProperty: permutation) {
					line += translatedProperty + "-";
				}
				line += "end," + count;
				out.write(line+"\n");
				//System.out.println(line);
			} */
			
			String line = "";
			int counter = 0;
			for (String translatedProperty : translatedProperties) {
				counter++;
				// The frequency of translatedProperties after numPropertiesFrequencyNotZero is 0, there is no need to add them in the csv file. 
				if(counter > numPropertiesFrequencyNotZero) {
					break;
				}
				
				if(considerTypeAsProperty) {
					line += translatedProperty + "-";
				// If considerTypeAsProperty is false, don't add type in the csv file.
				} else if(!translatedProperty.equals("p" + typePropertyPosition)) {
					line += translatedProperty + "-";
				}
			}
			line += "end," + count;
			out.write(line + "\n");
			//System.out.println(line);
			//System.out.println("Result Translate End******************");
		}
		// Close the file
		out.flush();
		out.close();
//		// Launch the default browser to display the web page
//		System.out.println("################# Launch the browser to display the sequences sunburst graph #################");
//		java.awt.Desktop.getDesktop().browse(new URI(GlobalSettings.D3_SEQUENCES_SUNBURST_URL));
	}
	
	public void resultTranslatorForBarChart(HashMap<String, Integer> propertyCombinationCount, String csvFileName)
			throws IOException, URISyntaxException {
		String filePath = GlobalSettings.D3_BAR_CHART_FILE_FOLDER_PATH + csvFileName + ".csv";
		// Open the file
		File file = new File(filePath);
		file.createNewFile();
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		System.out.println("################# Generate " + csvFileName + ".csv file for data visualization #################");
		HashMap<Integer, Integer> numNotNullProperties_count = new HashMap<Integer, Integer>();

		for (Entry<String, Integer> entry : propertyCombinationCount.entrySet()) {
			// Attention: properties here is a string like "t	null	null" rather than property names
			String properties = entry.getKey();
			Integer count = entry.getValue();
			//System.out.println("*********"+ properties + count + "*********");
			String[] s = properties.split("\t");
			int numNotNullProperties = 0;
			for (int i = 0; i < s.length; i++) {
				if (s[i].equals("t")) {
					numNotNullProperties++;
				}
			}
			if (numNotNullProperties_count.containsKey(numNotNullProperties)) {
				numNotNullProperties_count.put(numNotNullProperties, numNotNullProperties_count.get(numNotNullProperties) + count);
			} else {
				numNotNullProperties_count.put(numNotNullProperties, count);
			}
			//System.out.println(numNotNullProperties + "--->" +numNotNullProperties_count.get(numNotNullProperties));
		}

		out.write("number_of_subjects,number_of_properties\n");
		for (Entry<Integer, Integer> entry : numNotNullProperties_count
				.entrySet()) {
			Integer numNotNullProperties = entry.getKey();
			Integer count = entry.getValue();
			String line = numNotNullProperties + "," + count + "\n";
			out.write(line);
		}
		// Close the file
		out.flush();
		out.close();
	}
	
	// TODO now
	public void resultTranslatorForBarChart_forPropertyCombinationCount(HashMap<String, Integer> propertyCombinationCount, ArrayList<String> distinctProperties, int barNumber, String csvFileName) throws IOException {
		// Check barNumber is no bigger than the number of the property combinations
		if(barNumber > propertyCombinationCount.size()) {
			barNumber = propertyCombinationCount.size();
		}
				
		// Open the file
		String filePath = GlobalSettings.D3_BAR_CHART_FOR_PROPERTY_COMBINATION_COUNT_FILE_FOLDER_PATH + csvFileName + ".csv";
		File file = new File(filePath);
		file.createNewFile();
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		System.out.println("################# Generate " + csvFileName + ".csv file for data visualization #################");
		out.write("property_combination,count\n");
		
		LinkedHashMap<String, Integer> sortedPropertyCombinationCount = PostgresDatabaseHandler_Plus.sortHashMapByValues(propertyCombinationCount);
		LinkedList<String> list = new LinkedList<String>(sortedPropertyCombinationCount.keySet());
		// Make sortedPropertyCombinationCount in descending order.
		Collections.reverse(list);
		
		int counter = 0;
		for (String entry : list) {
			// Attention: properties here is a string like "t	null	null" rather than property names
			String properties = entry;
			Integer count = propertyCombinationCount.get(entry);
			//System.out.println("*********"+ properties + count + "*********");
			String[] s = properties.split("\t");
			String propertyCombination = "";
			for (int i = 0; i < s.length; i++) {
				if (s[i].equals("t")) {
					propertyCombination += StatementGenerator.getShorterTypeName(distinctProperties.get(i)) + "-";
				}
			}
			String line = propertyCombination.substring(0, propertyCombination.length()-1) + "," + count + "\n";
			out.write(line);
			
			counter++;
			if(counter >= barNumber) {
				break;
			}
		}

		// Close the file
		out.flush();
		out.close();
	}
	
	public void writeTypeFrequencyFile(HashMap<String, Integer> typeFrequency) throws IOException {
		// Open the file
		File file = new File(GlobalSettings.D3_PIE_CHART_FILE_PATH);
		file.createNewFile();
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		System.out.println("################# Generate pie-data.csv file #################");
		out.write("typeName,frequency\n");
		for (Entry<String, Integer> entry : typeFrequency.entrySet()) {
			String type = entry.getKey();
			Integer frequency = entry.getValue();
			if(type != null) {
				out.write(type + "," + frequency + "\n");
			} else {
				// In pie chart graph, display "None" rather than "null" 
				out.write("None," + frequency + "\n");
			}
		}
		// Close the file
		out.flush();
		out.close();
	}
	
	public void launchBrowserToShowPieChart() throws IOException, URISyntaxException {
		// Launch the default browser to display the web page
		System.out.println("################# Launch the browser to display the pie chart graph #################");
		java.awt.Desktop.getDesktop().browse(new URI(GlobalSettings.D3_PIE_CHART_URL));
	}
	
	public void launchBrowserToShowSequencesSunburstOfAllSubjects() throws IOException, URISyntaxException {
		// Launch the default browser to display the web page
		System.out.println("################# Launch the browser to display the sequences sunburst graph of all subjects #################");
		java.awt.Desktop.getDesktop().browse(new URI(GlobalSettings.D3_SEQUENCES_SUNBURST_URL + "?type=AllSubjects"));
	}

	/**
	 * INPUT example:	"t	null	t"
	 * OUTPUT example:	{"p1", "noProperty", "p3"}
	 */
	@SuppressWarnings("unused")
	private static String[] translatePropertiesResult(String properties, HashMap<String, Integer> propertyOrder) {
		String[] s = properties.split("\t");
		ArrayList<String> result = new ArrayList<String>();
		for(int i=0; i<s.length; i++) {
			if(!s[i].equals("f")) {
				result.add(new String("p" + (i+1)));
			} else {
				result.add(new String("noProperty"));
			}
		}
		if(GlobalSettings.SQL_COUNT_DISTINCT_PROPERTIES_STRATEGY == 1) {
			return result.toArray(new String[result.size()]);
		} else {
			String[] orderedResult = new String[result.size()];
			for(String property: result) {
				if(!property.equals("noProperty")) {
					int position = propertyOrder.get(property);
					orderedResult[position] = property;
				}
			}
			for(int i=0; i<orderedResult.length; i++) {
				if(orderedResult[i] == null) {
					orderedResult[i] = new String("noProperty");
				}
			}
//			System.out.print("old-->");
//			Visualization_D3.showStringArray(result.toArray(new String[result.size()]));
//			System.out.print("new-->");
//			Visualization_D3.showStringArray(orderedResult);
			return orderedResult;
		}
	}
	
	/**
	 * INPUT example:	{"p1", "p3"}
	 * OUTPUT example:	[[p1, p3], [p3, p1]]
	 */
	private static ArrayList<ArrayList<String>> permutation(String[] str, int first, int end) {
		//ArrayList<String> result = new ArrayList<String>();
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		if (first == end) {
			//String oneCombination = "";
			ArrayList<String> oneCombination = new ArrayList<String>();
			for (int j = 0; j <= end; j++) {
				//System.out.print(str[j]);
				//oneCombination += str[j];
				oneCombination.add(str[j]);
			}
			//System.out.println();
			result.add(oneCombination);
		}

		for (int i = first; i <= end; i++) {
			swap(str, i, first);
			ArrayList<ArrayList<String>> resultRestPart = permutation(str, first + 1, end);
			result.addAll(resultRestPart);
			swap(str, i, first);
		}
		return result;
	}

	// For permutation() function
	private static void swap(String[] str, int i, int first) {
		String tmp;
		tmp = str[first];
		str[first] = str[i];
		str[i] = tmp;
	}
	
	private static void showStringArray(String[] st) {
		String result = new String();
		for(String s: st) {
			result += s + "\t";
		}
		System.out.println(result);
	}
}
