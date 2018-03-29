package com.ynr.parser;

public class EVUSParser extends ParserBase implements IExcelParser {

	public String parseDemo(){
		String data = generateDemoData("excelparseservice_data/evus_demo_json_data.txt");
		return data;
	}
	
	public String parseTask(String task) {
		System.out.println("EVUSParser task : " + task);
		return generateJSONData("excelparseservice_data/evus.jsonkey", task);		
	}
	
	public static void main(String[] args){
		EVUSParser p = new EVUSParser();
		System.out.println(p.parseTask("C:\\Users\\Administrator\\Desktop\\20180203\\EVUS_20180203.xls"));
	}
}