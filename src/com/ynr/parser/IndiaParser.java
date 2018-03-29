package com.ynr.parser;

public class IndiaParser extends ParserBase implements IExcelParser {

	public String parseDemo(){
		String data = generateDemoData("excelparseservice_data/france_demo_json_data.txt");
		return data;
	}
	
	public String parseTask(String task){
		return generateJSONData("excelparseservice_data/india.jsonkey", task);
	}
	
	public static void main(String[] args){
		IndiaParser fp = new IndiaParser();
		System.out.println(fp.parseTask("C:\\Users\\lb\\Desktop\\INDIA.xls"));
	}
}