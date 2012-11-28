package org.openforis.collect.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.math.stat.DescriptiveStatistics;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.IntegerAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.RealValue;
import org.openforis.idm.model.expression.AbsoluteModelPathExpression;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.model.expression.ModelPathExpression;
import org.openforis.idm.model.expression.internal.MissingValueException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class Tract5PlotaProcessingController {

	@Autowired
	SurveyDao surveyDao;
	
	@Autowired
	RecordManager recordManager;
	
	@RequestMapping(value = "/processTract5Plota.htm", method = RequestMethod.GET)
	public void processTract5Plota(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ServletOutputStream outputStream = response.getOutputStream();
		
		try {
			testReportingNvUsingCollectSTEP1();
			synchReportTable();
		} catch (Exception e) {
			outputStream.println("Error commenching");
			e.printStackTrace();
		} 
		outputStream.println("Success processing!");
	}
	
	public List<String> extractValues(Node<?> axis,String attributeName) {
		if ( axis == null ) {
			throw new NullPointerException("Axis must be non-null");
		} else if ( axis instanceof Entity ) {
			Attribute<?,?> attr = (Attribute<?, ?>) ((Entity) axis).get(attributeName, 0);
			if ( attr == null ) {
				return Arrays.asList(""); 
			} else {
				Field<?> fld = attr.getField(0);
				Object v = fld.getValue();
				return Arrays.asList(v == null ? "" : v.toString());
			}
		} else {
			throw new UnsupportedOperationException("Axis must be an Entity");
		}
	}
	
	private void testReportingNvUsingCollectSTEP1() throws InvalidExpressionException, RecordPersistenceException, URISyntaxException, IOException
	{	
	
	    URI uriOutput;
	    FileOutputStream fileOutputStream; 
	
	    XSSFWorkbook workbook;
	    CollectSurvey survey;
	    survey = surveyDao.load("idnfi");
	  
	    {
	        workbook = new XSSFWorkbook();		
	        String sRootPath = new File("").getAbsolutePath();
	        uriOutput = new URI(sRootPath + "/ReportNV-SE.xlsx");
	        fileOutputStream = new FileOutputStream(uriOutput.getPath());
	        XSSFSheet worksheet = workbook.createSheet("Report NV");
	
	        System.out.println("Creating temporary file " + uriOutput.getPath());
	            
			String rootEntityName;
			List<CollectRecord> records;
			ModelPathExpression relativeExpression;
		
		
			HashMap<Integer, ProvinceN> hashProvince = new HashMap<Integer, ProvinceN>();
			int i=0;	
			hashProvince.put(1, new ProvinceN(1,"Daerah Istimewa Aceh"));
			hashProvince.put(2, new ProvinceN(2,"Sumatera Utara"));
			hashProvince.put(3, new ProvinceN(3,"Sumatera Barat"));
			hashProvince.put(4, new ProvinceN(4,"Riau"));
			hashProvince.put(5, new ProvinceN(5,"Jambi"));
			hashProvince.put(6, new ProvinceN(6,"Sumatera Selatan"));
			hashProvince.put(7, new ProvinceN(7,"Lampung"));
			hashProvince.put(8, new ProvinceN(8,"Bengkulu"));
			hashProvince.put(9, new ProvinceN(9,"Banten"));
			hashProvince.put(10, new ProvinceN(10,"Jawa Barat"));
			hashProvince.put(11, new ProvinceN(11,"Jawa Tengah"));
			hashProvince.put(12, new ProvinceN(12,"DIY"));
			hashProvince.put(13, new ProvinceN(13,"Jawa Timur"));
			hashProvince.put(14, new ProvinceN(14,"Bali"));
			hashProvince.put(15, new ProvinceN(15,"Nusa Tenggara Barat"));
			hashProvince.put(16, new ProvinceN(16,"Nusa Tenggara Timur"));
			hashProvince.put(17, new ProvinceN(17,"Timor Timur"));
			hashProvince.put(18, new ProvinceN(18,"Kalimantan Barat"));
			hashProvince.put(19, new ProvinceN(19,"Kalimantan Tengah"));
			hashProvince.put(20, new ProvinceN(20,"Kalimantan Selatan"));
			hashProvince.put(21, new ProvinceN(21,"Kalimantan Timur"));
			hashProvince.put(22, new ProvinceN(22,"Sulawesi Utara"));
			hashProvince.put(23, new ProvinceN(23,"Sulawesi Tengah"));
			hashProvince.put(24, new ProvinceN(24,"Sulawesi Tenggara"));
			hashProvince.put(25, new ProvinceN(25,"Sulawesi Selatan"));
			hashProvince.put(26, new ProvinceN(26,"Maluku"));
			hashProvince.put(27, new ProvinceN(27,"Irian Jaya"));
			hashProvince.put(28, new ProvinceN(28,"Kepulauan Riau"));
			hashProvince.put(29, new ProvinceN(29,"Bangka Belitung"));
			hashProvince.put(30, new ProvinceN(30,"Gorontalo"));
			hashProvince.put(31, new ProvinceN(31,"Sulawesi Barat"));
			hashProvince.put(32, new ProvinceN(32,"Maluku Utara"));
			hashProvince.put(33, new ProvinceN(33,"Irian Barat"));
			
		
			records = recordManager.loadSummaries(survey, "cluster", 0, Integer.MAX_VALUE, (List<RecordSummarySortField>) null, (String) null);
			
			// TRACT 5
			rootEntityName = "/cluster/natural_forest/tp";
			for (CollectRecord s : records) {
				ExpressionFactory expressionFactory = s.getSurveyContext().getExpressionFactory();
				AbsoluteModelPathExpression expression = expressionFactory.createAbsoluteModelPathExpression(rootEntityName);
				CollectRecord record = recordManager.load(survey, s.getId(), 1);
				List<Node<?>> rowNodes = null;
				try {
					rowNodes = expression.iterate(record);
				}catch(MissingValueException ex)
				{				
				}
				
				if(rowNodes == null) continue;
				
				for(Node<?> n :rowNodes)
				{	
					//tract 5 only
					relativeExpression = expressionFactory.createModelPathExpression("parent()/tract_no");
					IntegerAttribute tract = (IntegerAttribute) relativeExpression.evaluate(n, null);
					
					if(tract.getValue().getValue()!=5){
						continue;
					}
					
					//System.out.println("\tGetting diameter of Tract 5 tree");	
					String strD = extractValues(n, "diameter").get(0);
					double d;
					int provinceCode=-1;
					
					
					if(!"".equals(strD))
					{	
						//utm zone
						relativeExpression = expressionFactory.createModelPathExpression("parent()/parent()/utm_zone");
						IntegerAttribute utmZoneAttr = (IntegerAttribute) relativeExpression.evaluate(n, null);
						Integer utmZone = utmZoneAttr.getValue().getValue();
						
						
						//easting
						relativeExpression = expressionFactory.createModelPathExpression("parent()/parent()/easting");
						IntegerAttribute eastingAttr = (IntegerAttribute) relativeExpression.evaluate(n, null);
						Integer easting = eastingAttr.getValue().getValue();
						
						
						//northing
						relativeExpression = expressionFactory.createModelPathExpression("parent()/parent()/northing");
						IntegerAttribute northingAttr = (IntegerAttribute) relativeExpression.evaluate(n, null);
						Integer northing = northingAttr.getValue().getValue();
						
						
						String clusterKey = utmZone + "" + easting + "" + String.format("%04d", northing)  + "";
						//System.out.println(utmZone + ":" + easting + ":" + String.format("%04d", northing)  + "");
						
						//year
						relativeExpression = expressionFactory.createModelPathExpression("parent()/year");
						IntegerAttribute yearAttr = (IntegerAttribute) relativeExpression.evaluate(n, null);
						Integer year = yearAttr.getValue().getValue();
						
						
						//bole_height
						relativeExpression = expressionFactory.createModelPathExpression("lg_trees/bole_height");
						RealAttribute bole_heightAttr=null;
						try {
							bole_heightAttr = (RealAttribute) relativeExpression.evaluate(n, null);
						}catch(MissingValueException ex)
						{
							
						}
						double bole_height = 0;
						if(bole_heightAttr!=null)
						{
							RealValue x = bole_heightAttr.getValue();
							if(x!=null) {
								try {
									bole_height = x.getValue();
								}catch(NullPointerException ex)
								{	
								}
							}
						}
						
						relativeExpression = expressionFactory.createModelPathExpression("parent()/province");
						CodeAttribute code = null;
						try {
							code = (CodeAttribute ) relativeExpression.evaluate(n, null);
							provinceCode = Integer.parseInt(code.getValue().getCode());
						}catch(MissingValueException ex)
						{
							System.out.println("Province Error on Record = " + record.getId());
							continue;
						}
						
						
						
						d = Double.parseDouble(strD);
						if(d>=20){
							i++;
							hashProvince.get(provinceCode).getTract5N(clusterKey, year, "20").add(d);
							hashProvince.get(provinceCode).addTract5V(clusterKey, year, "20", d, bole_height);
						}
						if(d>=30){
							hashProvince.get(provinceCode).getTract5N(clusterKey, year, "30").add(d);
							hashProvince.get(provinceCode).addTract5V(clusterKey, year, "30", d, bole_height);
						}
						if(d>=40){
							hashProvince.get(provinceCode).getTract5N(clusterKey, year, "40").add(d);
							hashProvince.get(provinceCode).addTract5V(clusterKey, year, "40", d, bole_height);
						}
						if(d>=50){
							hashProvince.get(provinceCode).getTract5N(clusterKey, year, "50").add(d);
							hashProvince.get(provinceCode).addTract5V(clusterKey, year, "50", d, bole_height);
						}
						if(d>=60){
							hashProvince.get(provinceCode).getTract5N(clusterKey, year, "60").add(d);
							hashProvince.get(provinceCode).addTract5V(clusterKey, year, "60", d, bole_height);
						}
						if(d>=70){
							hashProvince.get(provinceCode).getTract5N(clusterKey, year, "70").add(d);
							hashProvince.get(provinceCode).addTract5V(clusterKey, year, "70", d, bole_height);
						}
						if(d>=80){
							hashProvince.get(provinceCode).getTract5N(clusterKey, year, "80").add(d);
							hashProvince.get(provinceCode).addTract5V(clusterKey, year, "80", d, bole_height);
						}
					}
				}		
			}
			
			rootEntityName = "/cluster/permanent_plot_a/plota_enum";
			for (CollectRecord s : records) {
				//clear data
				
				ExpressionFactory expressionFactory = s.getSurveyContext().getExpressionFactory();
				AbsoluteModelPathExpression expression = expressionFactory.createAbsoluteModelPathExpression(rootEntityName);
				CollectRecord record = recordManager.load(survey, s.getId(), 1);
				List<Node<?>> rowNodes = null;
				try {
					rowNodes = expression.iterate(record);
				}catch(MissingValueException ex)
				{				
				}
				
				if(rowNodes == null) continue;
				
				for(Node<?> n :rowNodes)
				{	
					String strD = extractValues(n, "dbb_or_b").get(0);
					double d;
					int provinceCode=-1;
					
					
					if(!"".equals(strD))
					{	
						//utm zone
						relativeExpression = expressionFactory.createModelPathExpression("parent()/parent()/utm_zone");
						IntegerAttribute utmZoneAttr = (IntegerAttribute) relativeExpression.evaluate(n, null);
						Integer utmZone = utmZoneAttr.getValue().getValue();
						
						
						//easting
						relativeExpression = expressionFactory.createModelPathExpression("parent()/parent()/easting");
						IntegerAttribute eastingAttr = (IntegerAttribute) relativeExpression.evaluate(n, null);
						Integer easting = eastingAttr.getValue().getValue();
						
						
						//northing
						relativeExpression = expressionFactory.createModelPathExpression("parent()/parent()/northing");
						IntegerAttribute northingAttr = (IntegerAttribute) relativeExpression.evaluate(n, null);
						Integer northing = northingAttr.getValue().getValue();
						
						
						String clusterKey = utmZone + "" + easting + "" + String.format("%04d", northing)  + "";
						//System.out.println(utmZone + ":" + easting + ":" + String.format("%04d", northing)  + "");
						
						//year
						relativeExpression = expressionFactory.createModelPathExpression("parent()/year");
						IntegerAttribute yearAttr = (IntegerAttribute) relativeExpression.evaluate(n, null);
						Integer year = yearAttr.getValue().getValue();
						
						
						//bole_height
						relativeExpression = expressionFactory.createModelPathExpression("trees_higher_than_20cm/bole_height");
						RealAttribute bole_heightAttr=null;
						try {
							bole_heightAttr = (RealAttribute) relativeExpression.evaluate(n, null);
						}catch(MissingValueException ex)
						{
							
						}
						double bole_height = 0;
						if(bole_heightAttr!=null)
						{
							RealValue x = bole_heightAttr.getValue();
							if(x!=null) {
								try {
									bole_height = x.getValue();
								}catch(NullPointerException ex)
								{	
								}
							}
						}
						
						relativeExpression = expressionFactory.createModelPathExpression("parent()/province");
						CodeAttribute code = null;
						try {
							code = (CodeAttribute ) relativeExpression.evaluate(n, null);
							provinceCode = Integer.parseInt(code.getValue().getCode());
						}catch(MissingValueException ex)
						{
							System.out.println("Province Error on Record = " + record.getId());
	                        continue;
						}
						
						
						
						d = Double.parseDouble(strD);
						if(d>=20){
							i++;
							hashProvince.get(provinceCode).getPlotaN(clusterKey, year, "20").add(d);
							hashProvince.get(provinceCode).addPlotaV(clusterKey, year, "20", d, bole_height);
						}
						if(d>=30){
							hashProvince.get(provinceCode).getPlotaN(clusterKey, year, "30").add(d);
							hashProvince.get(provinceCode).addPlotaV(clusterKey, year, "30", d, bole_height);
						}
						if(d>=40){
							hashProvince.get(provinceCode).getPlotaN(clusterKey, year, "40").add(d);
							hashProvince.get(provinceCode).addPlotaV(clusterKey, year, "40", d, bole_height);
						}
						if(d>=50){
							hashProvince.get(provinceCode).getPlotaN(clusterKey, year, "50").add(d);
							hashProvince.get(provinceCode).addPlotaV(clusterKey, year, "50", d, bole_height);
						}
						if(d>=60){
							hashProvince.get(provinceCode).getPlotaN(clusterKey, year, "60").add(d);
							hashProvince.get(provinceCode).addPlotaV(clusterKey, year, "60", d, bole_height);
						}
						if(d>=70){
							hashProvince.get(provinceCode).getPlotaN(clusterKey, year, "70").add(d);
							hashProvince.get(provinceCode).addPlotaV(clusterKey, year, "70", d, bole_height);
						}
						if(d>=80){
							hashProvince.get(provinceCode).getPlotaN(clusterKey, year, "80").add(d);
							hashProvince.get(provinceCode).addPlotaV(clusterKey, year, "80", d, bole_height);
						}
					}
				}		
			}
		
			
			//System.out.println("i =" + i);
			//System.out.println("Provinsi;Cluster;Tahun;N20;V20;N30;V30;N40;V40;N50;V50;N60;V60;N70;V70;N80;V80");
			XSSFRow rowHeader;
			XSSFCell cellHeader;
			
			rowHeader = worksheet.createRow(0);
			cellHeader = rowHeader.createCell(0);
			cellHeader.setCellValue("Provinsi");
			cellHeader = rowHeader.createCell(1);
			cellHeader.setCellValue("Klaster");
			cellHeader = rowHeader.createCell(2);
			cellHeader.setCellValue("Tahun");		
			
			//enumerasi/tract5
			cellHeader = rowHeader.createCell(3);
			cellHeader.setCellValue("TSP N20");
			cellHeader = rowHeader.createCell(4);
			cellHeader.setCellValue("TSP V20");
	        cellHeader = rowHeader.createCell(5);//18=>5
			cellHeader.setCellValue("PSP N20");
			cellHeader = rowHeader.createCell(6);//19=>6
			cellHeader.setCellValue("PSP V20");
	
	        cellHeader = rowHeader.createCell(7);//5=?7
			cellHeader.setCellValue("TSP N30");
			cellHeader = rowHeader.createCell(8);
			cellHeader.setCellValue("TSP V30");
	        cellHeader = rowHeader.createCell(9);
			cellHeader.setCellValue("PSP N30");
			cellHeader = rowHeader.createCell(10);
			cellHeader.setCellValue("PSP V30");
	
			cellHeader = rowHeader.createCell(11);
			cellHeader.setCellValue("TSP N40");
			cellHeader = rowHeader.createCell(12);
			cellHeader.setCellValue("TSP V40");
	        cellHeader = rowHeader.createCell(13);
			cellHeader.setCellValue("PSP N40");
			cellHeader = rowHeader.createCell(14);
			cellHeader.setCellValue("PSP V40");
	
			cellHeader = rowHeader.createCell(15);
			cellHeader.setCellValue("TSP N50");
			cellHeader = rowHeader.createCell(16);
			cellHeader.setCellValue("TSP V50");
	        cellHeader = rowHeader.createCell(17);
			cellHeader.setCellValue("PSP N50");
			cellHeader = rowHeader.createCell(18);
			cellHeader.setCellValue("PSP V50");
		
	
			cellHeader = rowHeader.createCell(19);
			cellHeader.setCellValue("TSP N60");
			cellHeader = rowHeader.createCell(20);
			cellHeader.setCellValue("TSP V60");
	        cellHeader = rowHeader.createCell(21);
	        cellHeader.setCellValue("PSP N60");
			cellHeader = rowHeader.createCell(22);
			cellHeader.setCellValue("PSP V60");
	
			cellHeader = rowHeader.createCell(23);
			cellHeader.setCellValue("TSP N70");
			cellHeader = rowHeader.createCell(24);
			cellHeader.setCellValue("TSP V70");
	        cellHeader = rowHeader.createCell(25);
			cellHeader.setCellValue("PSP N70");
			cellHeader = rowHeader.createCell(26);
			cellHeader.setCellValue("PSP V70");
	
			cellHeader = rowHeader.createCell(27);
			cellHeader.setCellValue("TSP N80");
			cellHeader = rowHeader.createCell(28);
			cellHeader.setCellValue("TSP V80");
	        cellHeader = rowHeader.createCell(29);
			cellHeader.setCellValue("PSP N80");
			cellHeader = rowHeader.createCell(30);
			cellHeader.setCellValue("PSP V80");
	
			cellHeader = rowHeader.createCell(31);
			cellHeader.setCellValue("Standar Deviasi TSP");
	        cellHeader = rowHeader.createCell(32);
			cellHeader.setCellValue("Standar Deviasi PSP");
	
		
			int iRow = 1; 
			XSSFRow rowData = null;
			XSSFCell cellValue = null;
			for(int p : hashProvince.keySet())
			{
				ProvinceN prov = hashProvince.get(p);
				if(prov.getHashClusterPlotaN().keySet().size()>0) 
				{
					rowData = worksheet.createRow(iRow);
					cellValue = rowData.createCell(0);
					cellValue.setCellValue(prov.getTitle());
				}
	
				// Enumerasi/Tract5
				for(String clusterKey : prov.getHashClusterTract5N().keySet())//province
				{				
					if(prov.getHashClusterTract5N().get(clusterKey).size()>0)//cluster
					{					
						for(Integer year : prov.getHashClusterTract5N().get(clusterKey).get(20).keySet())
						{
							cellValue = rowData.createCell(1);
							cellValue.setCellValue(clusterKey);
							
							cellValue = rowData.createCell(2);
							cellValue.setCellValue(year);
							
							
							VolumeStatistic vstatsTract520 = prov.getTract5Volume(clusterKey, year, "20");
							int nTract520 = prov.getHashTract5N(clusterKey, year, "20").size();
							double vTract520  = prov.getHashTract5V(clusterKey, year, "20").size()==0? 0: vstatsTract520.getTotalV();
							
							VolumeStatistic vstatsTract530 = prov.getTract5Volume(clusterKey, year, "30");
							int nTract530 = prov.getHashTract5N(clusterKey, year, "30").size();
							double vTract530  = prov.getHashTract5V(clusterKey, year, "30").size()==0? 0: vstatsTract530.getTotalV();
							
							VolumeStatistic vstatsTract540 = prov.getTract5Volume(clusterKey, year, "40");
							int nTract540 = prov.getHashTract5N(clusterKey, year, "40").size();
							double vTract540  = prov.getHashTract5V(clusterKey, year, "40").size()==0? 0: vstatsTract540.getTotalV();
							
							VolumeStatistic vstatsTract550 = prov.getTract5Volume(clusterKey, year, "50");
							int nTract550 = prov.getHashTract5N(clusterKey, year, "50").size();
							double vTract550  = prov.getHashTract5V(clusterKey, year, "50").size()==0? 0: vstatsTract550.getTotalV();
							
							VolumeStatistic vstatsTract560 = prov.getTract5Volume(clusterKey, year, "60");
							int nTract560 = prov.getHashTract5N(clusterKey, year, "60").size();
							double vTract560  = prov.getHashTract5V(clusterKey, year, "60").size()==0? 0: vstatsTract560.getTotalV();
							
							VolumeStatistic vstatsTract570 = prov.getTract5Volume(clusterKey, year, "70");
							int nTract570 = prov.getHashTract5N(clusterKey, year, "70").size();
							double vTract570  = prov.getHashTract5V(clusterKey, year, "70").size()==0? 0: vstatsTract570.getTotalV();
							
							VolumeStatistic vstatsTract580 = prov.getTract5Volume(clusterKey, year, "80");
							int nTract580 = prov.getHashTract5N(clusterKey, year, "80").size();
							double vTract580  = prov.getHashTract5V(clusterKey, year, "80").size()==0? 0: vstatsTract580.getTotalV();
							
							//System.out.println("\tCalculating Standar Deviation of Tract5..");
							VolumeStatistic vTract5Stats = prov.getTract5StandarDeviation(clusterKey, year);
							//System.out.println("\tDONE");
							
							//enumerasi/tract 5
							cellValue = rowData.createCell(3);
							cellValue.setCellValue(nTract520);
							cellValue = rowData.createCell(4);
							cellValue.setCellValue(vTract520);
							
							cellValue = rowData.createCell(7);
							cellValue.setCellValue(nTract530);
							cellValue = rowData.createCell(8);
							cellValue.setCellValue(vTract530);
							
							cellValue = rowData.createCell(11);
							cellValue.setCellValue(nTract540);
							cellValue = rowData.createCell(12);
							cellValue.setCellValue(vTract540);
							
							cellValue = rowData.createCell(15);
							cellValue.setCellValue(nTract550);
							cellValue = rowData.createCell(16);
							cellValue.setCellValue(vTract550);
							
							cellValue = rowData.createCell(19);
							cellValue.setCellValue(nTract560);
							cellValue = rowData.createCell(20);
							cellValue.setCellValue(vTract560);
							
							cellValue = rowData.createCell(23);
							cellValue.setCellValue(nTract570);
							cellValue = rowData.createCell(24);
							cellValue.setCellValue(vTract570);
							
							cellValue = rowData.createCell(27);
							cellValue.setCellValue(nTract580);
							cellValue = rowData.createCell(28);
							cellValue.setCellValue(vTract580);						
							
							cellValue = rowData.createCell(31);
							cellValue.setCellValue(vTract5Stats.getStandardDeviation());
							
							
							//CHECK whether there is PSP value here.
							if(prov.getHashClusterPlotaN().get(clusterKey)!=null)
							{
								System.out.println("PSP exist for " + clusterKey);
								// Plot A
								VolumeStatistic vstatsPlota20 = prov.getPlotaVolume(clusterKey, year, "20");
								int nPlota20 = prov.getHashPlotaN(clusterKey, year, "20").size();
								double vPlota20  = prov.getHashPlotaV(clusterKey, year, "20").size()==0? 0: vstatsPlota20.getTotalV();
								
								VolumeStatistic vstatsPlota30 = prov.getPlotaVolume(clusterKey, year, "30");
								int nPlota30 = prov.getHashPlotaN(clusterKey, year, "30").size();
								double vPlota30  = prov.getHashPlotaV(clusterKey, year, "30").size()==0? 0: vstatsPlota30.getTotalV();
								
								VolumeStatistic vstatsPlota40 = prov.getPlotaVolume(clusterKey, year, "40");
								int nPlota40 = prov.getHashPlotaN(clusterKey, year, "40").size();
								double vPlota40  = prov.getHashPlotaV(clusterKey, year, "40").size()==0? 0: vstatsPlota40.getTotalV();
								
								VolumeStatistic vstatsPlota50 = prov.getPlotaVolume(clusterKey, year, "50");
								int nPlota50 = prov.getHashPlotaN(clusterKey, year, "50").size();
								double vPlota50  = prov.getHashPlotaV(clusterKey, year, "50").size()==0? 0: vstatsPlota50.getTotalV();
								
								VolumeStatistic vstatsPlota60 = prov.getPlotaVolume(clusterKey, year, "60");
								int nPlota60 = prov.getHashPlotaN(clusterKey, year, "60").size();
								double vPlota60  = prov.getHashPlotaV(clusterKey, year, "60").size()==0? 0: vstatsPlota60.getTotalV();
								
								VolumeStatistic vstatsPlota70 = prov.getPlotaVolume(clusterKey, year, "70");
								int nPlota70 = prov.getHashPlotaN(clusterKey, year, "70").size();
								double vPlota70  = prov.getHashPlotaV(clusterKey, year, "70").size()==0? 0: vstatsPlota70.getTotalV();
								
								VolumeStatistic vstatsPlota80 = prov.getPlotaVolume(clusterKey, year, "80");
								int nPlota80 = prov.getHashPlotaN(clusterKey, year, "80").size();
								double vPlota80  = prov.getHashPlotaV(clusterKey, year, "80").size()==0? 0: vstatsPlota80.getTotalV();
								
								//System.out.println("Calculating Standar Deviation of Plot A");
								VolumeStatistic vPlotaStats = prov.getPlotaStandarDeviation(clusterKey, year);
								//System.out.println("\tDONE");
		
								//plot A
								cellValue = rowData.createCell(5);//17->5
								cellValue.setCellValue(nPlota20);
								cellValue = rowData.createCell(6);
								cellValue.setCellValue(vPlota20);
								
								cellValue = rowData.createCell(9);
								cellValue.setCellValue(nPlota30);
								cellValue = rowData.createCell(10);
								cellValue.setCellValue(vPlota30);
								
								cellValue = rowData.createCell(13);
								cellValue.setCellValue(nPlota40);
								cellValue = rowData.createCell(14);
								cellValue.setCellValue(vPlota40);
								
								cellValue = rowData.createCell(17);
								cellValue.setCellValue(nPlota50);
								cellValue = rowData.createCell(18);
								cellValue.setCellValue(vPlota50);
								
								cellValue = rowData.createCell(21);
								cellValue.setCellValue(nPlota60);
								cellValue = rowData.createCell(22);
								cellValue.setCellValue(vPlota60);
								
								cellValue = rowData.createCell(25);
								cellValue.setCellValue(nPlota70);
								cellValue = rowData.createCell(26);
								cellValue.setCellValue(vPlota70);
								
								cellValue = rowData.createCell(29);
								cellValue.setCellValue(nPlota80);
								cellValue = rowData.createCell(30);
								cellValue.setCellValue(vPlota80);						
								
								cellValue = rowData.createCell(32);
								cellValue.setCellValue(vPlotaStats.getStandardDeviation());
								prov.getHashClusterPlotaN().remove(clusterKey);
							}
							//prepare new row
							iRow++;
							rowData = worksheet.createRow(iRow);
							cellValue = rowData.createCell(0);
							cellValue.setCellValue(prov.getTitle());
						}
					}
				}
				
				// Plot A
				for(String clusterKey : prov.getHashClusterPlotaN().keySet())//province
				{				
					if(prov.getHashClusterPlotaN().get(clusterKey).size()>0)//cluster
					{					
						for(Integer year : prov.getHashClusterPlotaN().get(clusterKey).get(20).keySet())
						{
							cellValue = rowData.createCell(1);
							cellValue.setCellValue(clusterKey);
							
							cellValue = rowData.createCell(2);
							cellValue.setCellValue(year);
							
							
							// Plot A
							VolumeStatistic vstatsPlota20 = prov.getPlotaVolume(clusterKey, year, "20");
							int nPlota20 = prov.getHashPlotaN(clusterKey, year, "20").size();
							double vPlota20  = prov.getHashPlotaV(clusterKey, year, "20").size()==0? 0: vstatsPlota20.getTotalV();
							
							VolumeStatistic vstatsPlota30 = prov.getPlotaVolume(clusterKey, year, "30");
							int nPlota30 = prov.getHashPlotaN(clusterKey, year, "30").size();
							double vPlota30  = prov.getHashPlotaV(clusterKey, year, "30").size()==0? 0: vstatsPlota30.getTotalV();
							
							VolumeStatistic vstatsPlota40 = prov.getPlotaVolume(clusterKey, year, "40");
							int nPlota40 = prov.getHashPlotaN(clusterKey, year, "40").size();
							double vPlota40  = prov.getHashPlotaV(clusterKey, year, "40").size()==0? 0: vstatsPlota40.getTotalV();
							
							VolumeStatistic vstatsPlota50 = prov.getPlotaVolume(clusterKey, year, "50");
							int nPlota50 = prov.getHashPlotaN(clusterKey, year, "50").size();
							double vPlota50  = prov.getHashPlotaV(clusterKey, year, "50").size()==0? 0: vstatsPlota50.getTotalV();
							
							VolumeStatistic vstatsPlota60 = prov.getPlotaVolume(clusterKey, year, "60");
							int nPlota60 = prov.getHashPlotaN(clusterKey, year, "60").size();
							double vPlota60  = prov.getHashPlotaV(clusterKey, year, "60").size()==0? 0: vstatsPlota60.getTotalV();
							
							VolumeStatistic vstatsPlota70 = prov.getPlotaVolume(clusterKey, year, "70");
							int nPlota70 = prov.getHashPlotaN(clusterKey, year, "70").size();
							double vPlota70  = prov.getHashPlotaV(clusterKey, year, "70").size()==0? 0: vstatsPlota70.getTotalV();
							
							VolumeStatistic vstatsPlota80 = prov.getPlotaVolume(clusterKey, year, "80");
							int nPlota80 = prov.getHashPlotaN(clusterKey, year, "80").size();
							double vPlota80  = prov.getHashPlotaV(clusterKey, year, "80").size()==0? 0: vstatsPlota80.getTotalV();
							
							//System.out.println("Calculating Standar Deviation of Plot A");
							VolumeStatistic vPlotaStats = prov.getPlotaStandarDeviation(clusterKey, year);
							//System.out.println("\tDONE");
	
							//plot A
							cellValue = rowData.createCell(5);//17->5
							cellValue.setCellValue(nPlota20);
							cellValue = rowData.createCell(6);
							cellValue.setCellValue(vPlota20);
							
							cellValue = rowData.createCell(9);
							cellValue.setCellValue(nPlota30);
							cellValue = rowData.createCell(10);
							cellValue.setCellValue(vPlota30);
							
							cellValue = rowData.createCell(13);
							cellValue.setCellValue(nPlota40);
							cellValue = rowData.createCell(14);
							cellValue.setCellValue(vPlota40);
							
							cellValue = rowData.createCell(17);
							cellValue.setCellValue(nPlota50);
							cellValue = rowData.createCell(18);
							cellValue.setCellValue(vPlota50);
							
							cellValue = rowData.createCell(21);
							cellValue.setCellValue(nPlota60);
							cellValue = rowData.createCell(22);
							cellValue.setCellValue(vPlota60);
							
							cellValue = rowData.createCell(25);
							cellValue.setCellValue(nPlota70);
							cellValue = rowData.createCell(26);
							cellValue.setCellValue(vPlota70);
							
							cellValue = rowData.createCell(29);
							cellValue.setCellValue(nPlota80);
							cellValue = rowData.createCell(30);
							cellValue.setCellValue(vPlota80);						
							
							cellValue = rowData.createCell(32);
							cellValue.setCellValue(vPlotaStats.getStandardDeviation());
							
							//prepare new row
							iRow++;
							rowData = worksheet.createRow(iRow);
							cellValue = rowData.createCell(0);
							cellValue.setCellValue(prov.getTitle());
						}
					}
				}
			}
		System.out.println("Writing to file " + uriOutput.getPath());
		workbook.write(fileOutputStream);
	        }
	    }

	private void synchReportTable() throws URISyntaxException, IOException, SQLException {
		String sRootPath = new File("").getAbsolutePath();
		URI uri = new URI("file:///"+ sRootPath + "/ReportNV-SE.xlsx");
		FileInputStream fileInputStream = new FileInputStream(uri.getPath());
		XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
		XSSFSheet worksheet = workbook.getSheet("Report NV");
		int rowNum = 0;
		Iterator<Row> iter = worksheet.rowIterator();
		
		Connection con = openConnection("postgres", "adminadmin");
		Statement stmt = con.createStatement();
		stmt.execute("delete from idcalc.report");
		System.out.println("Reading from " + uri.getPath());
		while(iter.hasNext())
		{	
			rowNum++;
			Row row = iter.next();
			if(rowNum>1 && iter.hasNext())
			{
				String propinsi, klasterkey, tahun;
				String tspN20, tspV20, pspN20, pspV20;
				String tspN30, tspV30, pspN30, pspV30;
				String tspN40, tspV40, pspN40, pspV40;
				String tspN50, tspV50, pspN50, pspV50;
				String tspN60, tspV60, pspN60, pspV60;
				String tspN70, tspV70, pspN70, pspV70;
				String tspN80, tspV80, pspN80, pspV80;
				String tspSd, pspSd;
				Cell cell;
				
				propinsi = getStringValue(row, 0, "0.0");
				klasterkey = getStringValue(row, 1, "0.0");
				tahun = getStringValue(row, 2, "0.0");
				tspN20 = getStringValue(row, 3, "0.0");
				tspV20 = getStringValue(row, 4, "0.0");
				pspN20 = getStringValue(row, 5, "0.0");
				pspV20 = getStringValue(row, 6, "0.0");

				tspN30 = getStringValue(row, 7, "0.0");
				tspV30 = getStringValue(row, 8, "0.0");
				pspN30 = getStringValue(row, 9, "0.0");
				pspV30 = getStringValue(row, 10, "0.0");

				tspN40 = getStringValue(row, 11, "0.0");
				tspV40 = getStringValue(row, 12, "0.0");
				pspN40 = getStringValue(row, 13, "0.0");
				pspV40 = getStringValue(row, 14, "0.0");

				tspN50 = getStringValue(row, 15, "0.0");
				tspV50 = getStringValue(row, 16, "0.0");
				pspN50 = getStringValue(row, 17, "0.0");
				pspV50 = getStringValue(row, 18, "0.0");

				tspN60 = getStringValue(row, 19, "0.0");
				tspV60 = getStringValue(row, 20, "0.0");
				pspN60 = getStringValue(row, 21, "0.0");
				pspV60 = getStringValue(row, 22, "0.0");

				tspN70 = getStringValue(row, 22, "0.0");
				tspV70 = getStringValue(row, 23, "0.0");
				pspN70 = getStringValue(row, 24, "0.0");
				pspV70 = getStringValue(row, 25, "0.0");

				tspN80 = getStringValue(row, 26, "0.0");
				tspV80 = getStringValue(row, 27, "0.0");
				pspN80 = getStringValue(row, 28, "0.0");
				pspV80 = getStringValue(row, 29, "0.0");
				
				tspSd = getStringValue(row, 30, "0.0");
				pspSd = getStringValue(row, 31, "0.0");
				
				String sql = "insert into idcalc.report(propinsi, klasterkey, tsp_n20, tsp_v20, psp_n20, psp_v20, tsp_n30, tsp_v30, psp_n30, psp_v30, tsp_n40, tsp_v40, psp_n40, psp_v40, tsp_n50, tsp_v50, psp_n50, psp_v50, tsp_n60, tsp_v60, psp_n60, psp_v60, tsp_n70, tsp_v70, psp_n70, psp_v70, tsp_n80, tsp_v80, psp_n80, psp_v80, sd_tsp,sd_psp) values('" +
				propinsi  +"','" + klasterkey  + "','" +
						tspN20 + "','" + tspV20 + "','" + pspN20 + "','" + pspV20 + "','" + 
						tspN30 + "','" + tspV30 + "','" + pspN30 + "','" + pspV30 + "','" + 
						tspN40 + "','" + tspV40 + "','" + pspN40 + "','" + pspV40 + "','" + 
						tspN50 + "','" + tspV50 + "','" + pspN50 + "','" + pspV50 + "','" + 
						tspN60 + "','" + tspV60 + "','" + pspN60 + "','" + pspV60 + "','" + 
						tspN70 + "','" + tspV70 + "','" + pspN70 + "','" + pspV70 + "','" + 
						tspN80 + "','" + tspV80 + "','" + pspN80 + "','" + tspV80 + "','" +
						tspSd + "','" + pspSd + "')";
				
				stmt.execute(sql);
				
			}
		}
		
		if(con!=null) con.close();
		
	}

	private String getStringValue(Row row, int i, String defaultValue) {
		String propinsi;
		Cell cell;
		cell = row.getCell(i);
		if(cell==null) return defaultValue;
		cell.setCellType(Cell.CELL_TYPE_STRING);
		propinsi = cell.getStringCellValue();
		return propinsi;
	}

	private Connection openConnection(String username, String password) {
			System.out.println("-------- PostgreSQL "
					+ "JDBC Connection Testing ------------");
	 
			try {
	 
				Class.forName("org.postgresql.Driver");
	 
			} catch (ClassNotFoundException e) {
	 
				System.out.println("Where is your PostgreSQL JDBC Driver? "
						+ "Include in your library path!");
				e.printStackTrace();
				return null;
	 
			}
	 
			System.out.println("PostgreSQL JDBC Driver Registered!");
	 
			Connection connection = null;
	 
			try {
	 
				connection = DriverManager.getConnection(
						"jdbc:postgresql://localhost:5432/idcalc", username,
						password);
	 
			} catch (SQLException e) {
	 
				System.out.println("Connection Failed! Check output console");
				e.printStackTrace();
				return null;
	 
			}
	 
			if (connection != null) {
				System.out.println("You made it, take control your database now!");
			} else {
				System.out.println("Failed to make connection!");
			}
		
		return connection;
	}	
}

class ProvinceN {
	private int code;
	private String title;
	private HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<Double>>>> hashClusterPlotaV, hashClusterPlotaN;
	private HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<Double>>>> hashClusterTract5V, hashClusterTract5N;

	public ProvinceN(int code, String title) {
		this.code = code;
		this.title = title;

		hashClusterPlotaV = new HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<Double>>>>();
		hashClusterPlotaN = new HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<Double>>>>();
		
		hashClusterTract5V = new HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<Double>>>>();
		hashClusterTract5N = new HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<Double>>>>();
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

		public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	
	public void addPlotaV(String clusterKey, Integer year, String diameterRange, double d, double bole_height) {
		double newV = (0.25 * 3.14 * d * d * bole_height * 0.56) / 10000;
		//double newV = 0.25 * 3.14 * (d/100) * (d/100) * bole_height * 0.7;
		ArrayList<Double> lv = null;
		if ("20".equals(diameterRange)) {			
			lv = getHashPlotaV(clusterKey, year, "20");
		} else if ("30".equals(diameterRange)) {
			lv = getHashPlotaV(clusterKey, year, "30");
		} else if ("40".equals(diameterRange)) {
			lv = getHashPlotaV(clusterKey, year, "40");
		} else if ("50".equals(diameterRange)) {
			lv = getHashPlotaV(clusterKey, year, "50");
		} else if ("60".equals(diameterRange)) {
			lv = getHashPlotaV(clusterKey, year, "60");
		} else if ("70".equals(diameterRange)) {
			lv = getHashPlotaV(clusterKey, year, "70");
		} else if ("80".equals(diameterRange)) {
			lv = getHashPlotaV(clusterKey, year, "80");
		}
		
		/*if (lv.size() == 0) {
			lv.add(newV);
		} else {
			Double oldV = lv.get(0);
			newV += oldV;
			lv.clear();
			lv.add(newV);

                        }*/
		// add all V to be able to calculate SDV
		lv.add(newV);			
	}
	
	public void addTract5V(String clusterKey, Integer year, String diameterRange, double d, double bole_height) {
		double newV = (0.25 * 3.14 * d * d * bole_height * 0.56) / 10000;
		//double newV = 0.25 * 3.14 * (d/100) * (d/100) * bole_height * 0.7;
		ArrayList<Double> lv = null;
		if ("20".equals(diameterRange)) {			
			lv = getHashTract5V(clusterKey, year, "20");
		} else if ("30".equals(diameterRange)) {
			lv = getHashTract5V(clusterKey, year, "30");
		} else if ("40".equals(diameterRange)) {
			lv = getHashTract5V(clusterKey, year, "40");
		} else if ("50".equals(diameterRange)) {
			lv = getHashTract5V(clusterKey, year, "50");
		} else if ("60".equals(diameterRange)) {
			lv = getHashTract5V(clusterKey, year, "60");
		} else if ("70".equals(diameterRange)) {
			lv = getHashTract5V(clusterKey, year, "70");
		} else if ("80".equals(diameterRange)) {
			lv = getHashTract5V(clusterKey, year, "80");
		}
		/*if (lv.size() == 0) {
			lv.add(newV);
		} else {
			Double oldV = lv.get(0);
			newV += oldV;
			lv.clear();
			lv.add(newV);

                        }*/
		
		// add all V to be able to calculate SDV
		lv.add(newV);			
	}


	public ArrayList<Double> getHashPlotaV(String clusterKey, Integer year, String diameterRange) {
		HashMap<Integer,ArrayList<Double>> result = null;		
		HashMap<Integer, HashMap<Integer, ArrayList<Double>>> hashV;
		
		
		if(hashClusterPlotaV.get(clusterKey)==null)
		{
			hashClusterPlotaV.put(clusterKey, new HashMap<Integer, HashMap<Integer, ArrayList<Double>>>());
		}
		hashV = hashClusterPlotaV.get(clusterKey);
		
		if("20".equals(diameterRange))
		{
			if(hashV.get(20)==null)
			{
				hashV.put(20, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashV.get(20);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		} else if("30".equals(diameterRange))
		{
			if(hashV.get(30)==null)
			{
				hashV.put(30, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashV.get(30);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		} else if("40".equals(diameterRange))
		{
			if(hashV.get(40)==null)
			{
				hashV.put(40, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashV.get(40);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		}else if("50".equals(diameterRange))
		{
			if(hashV.get(50)==null)
			{
				hashV.put(50, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashV.get(50);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		}else if("60".equals(diameterRange))
		{
			if(hashV.get(60)==null)
			{
				hashV.put(60, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashV.get(60);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		}else if("70".equals(diameterRange))
		{
			if(hashV.get(70)==null)
			{
				hashV.put(70, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashV.get(70);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		}else if("80".equals(diameterRange))
		{
			if(hashV.get(80)==null)
			{
				hashV.put(80, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hashYearly = hashV.get(80);
			
			if(hashYearly.get(year)==null)
			{
				hashYearly.put(year, new ArrayList<Double>());
			}
			result = hashYearly;
		}
		
		return result.get(year);
	}
	
	public ArrayList<Double> getHashTract5V(String clusterKey, Integer year, String diameterRange) {
		HashMap<Integer,ArrayList<Double>> result = null;		
		HashMap<Integer, HashMap<Integer, ArrayList<Double>>> hashV;
		
		
		if(hashClusterTract5V.get(clusterKey)==null)
		{
			hashClusterTract5V.put(clusterKey, new HashMap<Integer, HashMap<Integer, ArrayList<Double>>>());
		}
		hashV = hashClusterTract5V.get(clusterKey);
		
		if("20".equals(diameterRange))
		{
			if(hashV.get(20)==null)
			{
				hashV.put(20, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashV.get(20);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		} else if("30".equals(diameterRange))
		{
			if(hashV.get(30)==null)
			{
				hashV.put(30, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashV.get(30);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		} else if("40".equals(diameterRange))
		{
			if(hashV.get(40)==null)
			{
				hashV.put(40, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashV.get(40);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		}else if("50".equals(diameterRange))
		{
			if(hashV.get(50)==null)
			{
				hashV.put(50, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashV.get(50);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		}else if("60".equals(diameterRange))
		{
			if(hashV.get(60)==null)
			{
				hashV.put(60, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashV.get(60);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		}else if("70".equals(diameterRange))
		{
			if(hashV.get(70)==null)
			{
				hashV.put(70, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashV.get(70);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		}else if("80".equals(diameterRange))
		{
			if(hashV.get(80)==null)
			{
				hashV.put(80, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hashYearly = hashV.get(80);
			
			if(hashYearly.get(year)==null)
			{
				hashYearly.put(year, new ArrayList<Double>());
			}
			result = hashYearly;
		}
		
		return result.get(year);
	}
	
	public ArrayList<Double> getHashPlotaN(String clusterKey, Integer year, String diameterRange) {
		HashMap<Integer,ArrayList<Double>> result = null;		
		HashMap<Integer, HashMap<Integer, ArrayList<Double>>> hashN;
		
		
		if(hashClusterPlotaN.get(clusterKey)==null)
		{
			hashClusterPlotaN.put(clusterKey, new HashMap<Integer, HashMap<Integer, ArrayList<Double>>>());
		}
		hashN = hashClusterPlotaN.get(clusterKey);
		
		if("20".equals(diameterRange))
		{
			if(hashN.get(20)==null)
			{
				hashN.put(20, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashN.get(20);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		} else if("30".equals(diameterRange))
		{
			if(hashN.get(30)==null)
			{
				hashN.put(30, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashN.get(30);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		} else if("40".equals(diameterRange))
		{
			if(hashN.get(40)==null)
			{
				hashN.put(40, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashN.get(40);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		}else if("50".equals(diameterRange))
		{
			if(hashN.get(50)==null)
			{
				hashN.put(50, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashN.get(50);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		}else if("60".equals(diameterRange))
		{
			if(hashN.get(60)==null)
			{
				hashN.put(60, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashN.get(60);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		}else if("70".equals(diameterRange))
		{
			if(hashN.get(70)==null)
			{
				hashN.put(70, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashN.get(70);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		}else if("80".equals(diameterRange))
		{
			if(hashN.get(80)==null)
			{
				hashN.put(80, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hashYearly = hashN.get(80);
			
			if(hashYearly.get(year)==null)
			{
				hashYearly.put(year, new ArrayList<Double>());
			}
			result = hashYearly;
		}
		
		return result.get(year);
	}
	
	public ArrayList<Double> getHashTract5N(String clusterKey, Integer year, String diameterRange) {
		HashMap<Integer,ArrayList<Double>> result = null;		
		HashMap<Integer, HashMap<Integer, ArrayList<Double>>> hashN;
		
		
		if(hashClusterTract5N.get(clusterKey)==null)
		{
			hashClusterTract5N.put(clusterKey, new HashMap<Integer, HashMap<Integer, ArrayList<Double>>>());
		}
		hashN = hashClusterTract5N.get(clusterKey);
		
		if("20".equals(diameterRange))
		{
			if(hashN.get(20)==null)
			{
				hashN.put(20, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashN.get(20);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		} else if("30".equals(diameterRange))
		{
			if(hashN.get(30)==null)
			{
				hashN.put(30, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashN.get(30);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		} else if("40".equals(diameterRange))
		{
			if(hashN.get(40)==null)
			{
				hashN.put(40, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashN.get(40);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		}else if("50".equals(diameterRange))
		{
			if(hashN.get(50)==null)
			{
				hashN.put(50, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashN.get(50);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		}else if("60".equals(diameterRange))
		{
			if(hashN.get(60)==null)
			{
				hashN.put(60, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashN.get(60);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		}else if("70".equals(diameterRange))
		{
			if(hashN.get(70)==null)
			{
				hashN.put(70, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hash20 = hashN.get(70);
			
			if(hash20.get(year)==null)
			{
				hash20.put(year, new ArrayList<Double>());
			}
			result = hash20;
		}else if("80".equals(diameterRange))
		{
			if(hashN.get(80)==null)
			{
				hashN.put(80, new HashMap<Integer, ArrayList<Double>>());
			}
			HashMap<Integer, ArrayList<Double>> hashYearly = hashN.get(80);
			
			if(hashYearly.get(year)==null)
			{
				hashYearly.put(year, new ArrayList<Double>());
			}
			result = hashYearly;
		}
		
		return result.get(year);
	}
	
	

	public ArrayList<Double> getPlotaN(String clusterKey, Integer year, String diameterRange) {
		ArrayList<Double> result = null;
		if ("20".equals(diameterRange)) {
			result = getHashPlotaN(clusterKey, year, "20");
		} else if ("30".equals(diameterRange)) {
			result = getHashPlotaN(clusterKey, year, "30");
		} else if ("40".equals(diameterRange)) {
			result = getHashPlotaN(clusterKey, year, "40");
		} else if ("50".equals(diameterRange)) {
			result = getHashPlotaN(clusterKey, year, "50");
		} else if ("60".equals(diameterRange)) {
			result = getHashPlotaN(clusterKey, year, "60");
		} else if ("70".equals(diameterRange)) {
			result = getHashPlotaN(clusterKey, year, "70");
		} else if ("80".equals(diameterRange)) {
			result = getHashPlotaN(clusterKey, year, "80");
		}
		return result;
	}
	
	public ArrayList<Double> getTract5N(String clusterKey, Integer year, String diameterRange) {
		ArrayList<Double> result = null;
		if ("20".equals(diameterRange)) {
			result = getHashTract5N(clusterKey, year, "20");
		} else if ("30".equals(diameterRange)) {
			result = getHashTract5N(clusterKey, year, "30");
		} else if ("40".equals(diameterRange)) {
			result = getHashTract5N(clusterKey, year, "40");
		} else if ("50".equals(diameterRange)) {
			result = getHashTract5N(clusterKey, year, "50");
		} else if ("60".equals(diameterRange)) {
			result = getHashTract5N(clusterKey, year, "60");
		} else if ("70".equals(diameterRange)) {
			result = getHashTract5N(clusterKey, year, "70");
		} else if ("80".equals(diameterRange)) {
			result = getHashTract5N(clusterKey, year, "80");
		}
		return result;
	}

	public HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<Double>>>> getHashClusterPlotaV() {
		return hashClusterPlotaV;
	}
	
	public HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<Double>>>> getHashClusterPlotaN() {
		return hashClusterPlotaN;
	}
	
	public HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<Double>>>> getHashClusterTract5V() {
		return hashClusterTract5V;
	}
	
	public HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<Double>>>> getHashClusterTract5N() {
		return hashClusterTract5N;
	}

	// SD per diameter tersedia jika diperlukan
	public VolumeStatistic getTract5Volume(String clusterKey, Integer year, String diameter) {
		
		DescriptiveStatistics stats = DescriptiveStatistics.newInstance();
		float totalV = 0;
		for(double v : getHashTract5V(clusterKey, year, diameter))
		{
			stats.addValue(v);
			totalV +=v;
		}
		return new VolumeStatistic(totalV, stats.getStandardDeviation());
	}

	public VolumeStatistic getTract5StandarDeviation(String clusterKey, Integer year) {
		
		DescriptiveStatistics stats = DescriptiveStatistics.newInstance();
		float totalV = 0;
		for(double v : getHashTract5V(clusterKey, year, "20"))
		{
			stats.addValue(v);
			totalV +=v;
		}
		for(double v : getHashTract5V(clusterKey, year, "30"))
		{
			stats.addValue(v);
			totalV +=v;
		}
		for(double v : getHashTract5V(clusterKey, year, "40"))
		{
			stats.addValue(v);
			totalV +=v;
		}
		for(double v : getHashTract5V(clusterKey, year, "50"))
		{
			stats.addValue(v);
			totalV +=v;
		}
		for(double v : getHashTract5V(clusterKey, year, "60"))
		{
			stats.addValue(v);
			totalV +=v;
		}
		for(double v : getHashTract5V(clusterKey, year, "70"))
		{
			stats.addValue(v);
			totalV +=v;
		}
		for(double v : getHashTract5V(clusterKey, year, "80"))
		{
			stats.addValue(v);
			totalV +=v;
		}		
		return new VolumeStatistic(totalV, stats.getStandardDeviation()); 
	}
	
public VolumeStatistic getPlotaVolume(String clusterKey, Integer year, String diameter) {
		
		DescriptiveStatistics stats = DescriptiveStatistics.newInstance();
		float totalV = 0;
		for(double v : getHashPlotaV(clusterKey, year, diameter))
		{
			stats.addValue(v);
			totalV +=v;
		}
		return new VolumeStatistic(totalV, stats.getStandardDeviation());
	}

public VolumeStatistic getPlotaStandarDeviation(String clusterKey, Integer year) {
		
		DescriptiveStatistics stats = DescriptiveStatistics.newInstance();
		float totalV = 0;
		for(double v : getHashPlotaV(clusterKey, year, "20"))
		{
			stats.addValue(v);
			totalV +=v;
		}
		for(double v : getHashPlotaV(clusterKey, year, "30"))
		{
			stats.addValue(v);
			totalV +=v;
		}
		for(double v : getHashPlotaV(clusterKey, year, "40"))
		{
			stats.addValue(v);
			totalV +=v;
		}
		for(double v : getHashPlotaV(clusterKey, year, "50"))
		{
			stats.addValue(v);
			totalV +=v;
		}
		for(double v : getHashPlotaV(clusterKey, year, "60"))
		{
			stats.addValue(v);
			totalV +=v;
		}
		for(double v : getHashPlotaV(clusterKey, year, "70"))
		{
			stats.addValue(v);
			totalV +=v;
		}
		for(double v : getHashPlotaV(clusterKey, year, "80"))
		{
			stats.addValue(v);
			totalV +=v;
		}		
		return new VolumeStatistic(totalV, stats.getStandardDeviation()); 
	}

}

class VolumeStatistic {

	private float totalV;
	public float getTotalV() {
		return totalV;
	}

	public void setTotalV(float totalV) {
		this.totalV = totalV;
	}

	public double getStandardDeviation() {
		return standardDeviation;
	}

	public void setStandardDeviation(double standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

	private double standardDeviation;

	public VolumeStatistic(float totalV, double standardDeviation) {
		this.totalV = totalV;
		this.standardDeviation = standardDeviation;
	}

}
