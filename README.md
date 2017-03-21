# RDF Data Analytics Tool
## Directory layout
```
rdf_analytics/
├── PostgreSQL_customized_rdf_parser
├── RDFDataVisualizationTool
│   ├── Java
│   │   └── data
│   └── d3
│       ├── bar_chart
│       ├── d3
│       └── sequences_sunburst
│           └── data
└── exploratory-rdf-analytics
    ├── Draft
    ├── data
    ├── src
    │   ├── main
    │   │   ├── java
    │   │   │   ├── exploratoryRDFAnalytics
    │   │   │   └── lattice
    │   │   └── resources
    │   └── test
    └── target
```
## RDFDataVisualizationTool
If you just want to use this tool, all you need is this folder. Follow `README` instructions in `RDFDataVisualizationTool` folder, which contains two main steps:
* Use Java `jar` to analyse your RDF data, then generate necessary files for data visualization.
* Launch your broswer and play this data visualization tool.
### Demo
Before running this tool, you could go [**here**](https://perso.limsi.fr/zzheng/RDFDataVisualizationTool/bar_chart/pie-chart_for_website.html) to see a demo. This will give you an idea of what you could get by using this RDF data analytics tool. In this demo, I used DBLP data.
## PostgreSQL_customized_rdf_parser
Customize a RDF data parser directly in PostgreSQL server. Using common query command for RDF data keyword searching.
