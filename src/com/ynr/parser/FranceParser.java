package com.ynr.parser;

public class FranceParser extends ParserBase implements IExcelParser {

	public String parseDemo(){
		String data = generateDemoData("excelparseservice_data/france_demo_json_data.txt");
		return data;
	}
	
	public String parseTask(String task){
		return generateJSONData("excelparseservice_data/france.jsonkey", task);
	}
	
	public static void main(String[] args){
		FranceParser fp = new FranceParser();
		System.out.println(fp.parseTask("C:\\Users\\Administrator\\Desktop\\20180203\\FRANCE_20180206.xls"));
	}
}