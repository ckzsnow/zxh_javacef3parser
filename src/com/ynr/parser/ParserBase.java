package com.ynr.parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import com.alibaba.fastjson.JSON;

public class ParserBase {
	public String generateDemoData(String demoFilePath){
		BufferedReader br;
		StringBuilder sb = new StringBuilder();
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(demoFilePath)));
			String line = null;
			while((line = br.readLine())!=null) {
				line = line.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "");
				sb.append(line);
			}
			br.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		return sb.toString();
	}
	
	public String generateJSONData(String jsonKeyFilePath, String excelFilePath){
		String jsonData = "";
		Map<String, Object> dataMap = new HashMap<>();
		BufferedReader br;
		try {
			POIFSFileSystem poifsFileSystem = new POIFSFileSystem(new FileInputStream(excelFilePath));
			Workbook workbook = new HSSFWorkbook(poifsFileSystem);
			Sheet sheet = workbook.getSheetAt(0);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(jsonKeyFilePath)));
			String line = null;
			while((line = br.readLine())!=null) {
				line = line.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "").replaceAll(" ", "");
				if(line.isEmpty()) continue;
				if(line.endsWith("map_begin")){
					String key = line.substring(0, line.indexOf("_map_begin"));
					Map<String, String> inMap = new HashMap<>();
					while((line = br.readLine())!=null){
						if(line.endsWith("map_end")){
							break;
						}
						System.out.println("line : " + line);
						int namedCellIdx = workbook.getNameIndex(line);
						Name aNamedCell = workbook.getNameAt(namedCellIdx);
						CellReference cellReference = new CellReference(aNamedCell.getRefersToFormula());
					    Row row = sheet.getRow(cellReference.getRow());
					    Cell cell = row.getCell(cellReference.getCol());
					    CellType type = cell.getCellTypeEnum();
					    System.out.println("cell type : " + type.name());
					    if(("NUMERIC").equals(type.name())) {
					    	System.out.println("value : " + String.valueOf((int)cell.getNumericCellValue()).trim());
					    	inMap.put(line, String.valueOf((int)cell.getNumericCellValue()).trim());
					    } else {
					    	System.out.println("value : " + cell.getStringCellValue().trim());
					    	inMap.put(line, cell.getStringCellValue().trim());
					    }
					}
					dataMap.put(key, inMap);
				} else if(line.endsWith("list_begin")){
					String key = line.substring(0, line.indexOf("_list_begin"));
					List<String> keyList = new ArrayList<>();
					while((line = br.readLine())!=null){
						if(line.endsWith("list_end")){
							break;
						}
						keyList.add(line);
					}
					int index = 1;
					List<Object> inList = new ArrayList<>();
					boolean isExit = false;
					while(!isExit){
						Map<String, String> subMap = new HashMap<>();
						for(String keyName : keyList){
							String realName = keyName + "_" + index;
							int namedCellIdx = workbook.getNameIndex(realName);
							if(namedCellIdx == -1) {
								isExit = true;
						    	break;
							}
							Name aNamedCell = workbook.getNameAt(namedCellIdx);
							CellReference cellReference = new CellReference(aNamedCell.getRefersToFormula());
						    Row row = sheet.getRow(cellReference.getRow());
						    Cell cell = row.getCell(cellReference.getCol());
						    CellType type = cell.getCellTypeEnum();
						    String cellContent = "";
						    if(("NUMERIC").equals(type.name())) {
						    	System.out.println("value : " + String.valueOf((int)cell.getNumericCellValue()).trim());
						    	cellContent = String.valueOf((int)cell.getNumericCellValue()).trim();
						    } else {
						    	System.out.println("value : " + cell.getStringCellValue().trim());
						    	cellContent = cell.getStringCellValue().trim();
						    }
						    if(cellContent ==  null || cellContent.isEmpty()) {
						    	isExit = true;
						    	break;
						    } else {
						    	subMap.put(keyName, cellContent);
						    }
						}
						if(!subMap.isEmpty()) inList.add(subMap);
						index++;
					}
					dataMap.put(key, inList);
				} else {
					System.out.println("line : " + line);
					int namedCellIdx = workbook.getNameIndex(line);
					Name aNamedCell = workbook.getNameAt(namedCellIdx);
					CellReference cellReference = new CellReference(aNamedCell.getRefersToFormula());
				    Row row = sheet.getRow(cellReference.getRow());
				    Cell cell = row.getCell(cellReference.getCol());
				    CellType type = cell.getCellTypeEnum();
				    System.out.println("cell type : " + type.name());
				    if(("NUMERIC").equals(type.name())) {
				    	System.out.println("value : " + String.valueOf((int)cell.getNumericCellValue()).trim());
					    dataMap.put(line, String.valueOf((int)cell.getNumericCellValue()).trim());
				    } else {
				    	System.out.println("value : " + cell.getStringCellValue().trim());
					    dataMap.put(line, cell.getStringCellValue().trim());
				    }
				}
			}
			br.close();
			workbook.close();
			jsonData = JSON.toJSONString(dataMap);
		} catch (Exception e) {
			System.out.println("generateJSONData exception : " + e.toString());
		}
		return jsonData;
	}
}