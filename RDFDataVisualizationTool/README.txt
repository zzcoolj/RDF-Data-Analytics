To use this RDF data visualization tool, you have to:

0. Make sure you have installed PostgreSQL server in your computer. (For mac users, if you don't have PostgerSQL server, you could go to http://postgresapp.com)

1. Open psql(the interactive terminal for working with Postgres) and in psql:
	1.1 Create a table by entering:
			CREATE TABLE your_table_name(
				subject text,
				property text,
				object text
 			);
 	1.2 Load your RDF data into your_table_name table entering:
 			COPY your_table_name FROM 'your_RDF_data_path';
 	1.3 Install the additional module tablefunc by entering:
 			CREATE EXTENSION tablefunc;

2. Open config.properties file in the folder Java and set the PostgreSQL server properties.

3. Open terminal and in terminal:
	3.1 Start the d3 server by entering (in the RDFDataVisualizationTool folder):
			cd d3 && python -m SimpleHTTPServer 8888
	3.2 Open another tab in the terminal and enter:
			cd ../Java && java -jar test.jar your_table_name
	3.3 If your browser does not show any graph, please enter:
			open http://localhost:8888/bar_chart/ in your browser