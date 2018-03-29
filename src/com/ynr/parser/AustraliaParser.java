package com.ynr.parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class AustraliaParser extends ParserBase implements IExcelParser {

	public Map<String, String> btnNameMap = new HashMap<>();
	
	public AustraliaParser(){
		btnNameMap.put("photograph_passport", "Photograph - Passport");
		btnNameMap.put("national_id_card", "National Identity Document (other than Passport)");
		btnNameMap.put("travel_document", "Travel Document");
		btnNameMap.put("family_composition", "Family Composition, Evidence of");
		btnNameMap.put("financial_capacity_bankstatement", "Financial Capacity - Personal, Evidence of");
		btnNameMap.put("financial_capacity_other_1", "Financial Capacity - Personal, Evidence of");
		btnNameMap.put("financial_capacity_other_2", "Financial Capacity - Personal, Evidence of");
		btnNameMap.put("financial_capacity_other_3", "Financial Capacity - Personal, Evidence of");
		btnNameMap.put("employment_current", "Employment - Current, Evidence of");
		btnNameMap.put("employment_licence", "Employment - Current, Evidence of");
		btnNameMap.put("planned_tourism_activities", "Planned tourism activities, Evidence of");
		btnNameMap.put("invitation_sponsor", "Invitation, Evidence of");
		btnNameMap.put("invitation_business", "Invitation, Evidence of");
		btnNameMap.put("travel_history", "Travel history, Evidence of");
		btnNameMap.put("intention_to_return", "Intention to return, Evidence of");
		btnNameMap.put("business_reason", "Business reason for travel to Australia, Evidence of");
		btnNameMap.put("previous_compliant_travel", "Previous Compliant Travel, Evidence of");
		btnNameMap.put("sponsor_relationship", "Applicant\'s relationship to sponsor, Evidence of");
		btnNameMap.put("sponsor_financial_capacity", "Sponsor’s Financial Capacity, Evidence of");
		btnNameMap.put("family_visitor", "Form 1149 Application for sponsorship for sponsored family visitors");
		btnNameMap.put("study_current", "Study-Current, Evidence of");
		btnNameMap.put("change_name", "Other (specify) - ");
	}
	
	public String parseDemo(){
		String data = generateDemoData("excelparseservice_data/australia_demo_json_data.txt");
		return data;
	}
	
	public String parseTask(String task){
		String jsonStr = generateJSONData("excelparseservice_data/australia.jsonkey", task);
		List<Map<String, Object>> attachFilesList = generateAttachData(task, "excelparseservice_data/australiaAttach.jsonkey");
		JSONObject obj = JSON.parseObject(jsonStr);
		obj.put("attachFilesList", attachFilesList);
		obj.put("login_username", "261831845@qq.com");
		obj.put("login_password", "Lb3283888");
		return obj.toJSONString();
	}
	
	public List<Map<String, Object>> generateAttachData(String excelFilePath, String attchJsonKeyPath) {
		List<Map<String, Object>> attachData = new ArrayList<>();
		BufferedReader br;
		try {
			POIFSFileSystem poifsFileSystem = new POIFSFileSystem(new FileInputStream(excelFilePath));
			Workbook workbook = new HSSFWorkbook(poifsFileSystem);
			Sheet sheet = workbook.getSheetAt(0);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(attchJsonKeyPath)));
			String line = null;
			while((line = br.readLine())!=null) {
				line = line.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "").replaceAll(" ", "");
				if(line.isEmpty()) continue;
				Map<String, Object> dataMap = new HashMap<>();
				System.out.println("line : " + line);
			    dataMap.put("document_type", getSpecifiedCellStr(workbook, sheet, line+"_document_type"));
			    dataMap.put("description", getSpecifiedCellStr(workbook, sheet, line+"_description"));
			    dataMap.put("btnName", btnNameMap.get(line));
			    dataMap.put("fileIdentifier", line);
			    if(getSpecifiedCellStr(workbook, sheet, line+"_description") != null &&
			    		!getSpecifiedCellStr(workbook, sheet, line+"_description").isEmpty()){
			    	attachData.add(dataMap);
			    }
			}
			br.close();
			workbook.close();
		} catch (Exception e) {
			System.out.println("generateAttachData exception : " + e.toString());
		}
		return attachData;
	}
	
	private String getSpecifiedCellStr(Workbook workbook, Sheet sheet, String key) {
		int namedCellIdx = workbook.getNameIndex(key);
		Name aNamedCell = workbook.getNameAt(namedCellIdx);
		CellReference cellReference = new CellReference(aNamedCell.getRefersToFormula());
	    Row row = sheet.getRow(cellReference.getRow());
	    Cell cell = row.getCell(cellReference.getCol());
	    return cell.getStringCellValue().trim();
	}
	
	public static void main(String[] args){
		AustraliaParser ap = new AustraliaParser();
		System.out.println(ap.parseTask("E:\\20180301\\上传\\yuyanjing_final.xls"));
	}
}