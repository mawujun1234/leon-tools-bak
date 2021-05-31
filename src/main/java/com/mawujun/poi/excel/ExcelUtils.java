package com.mawujun.poi.excel;

/**
 * 下面是一个例子
 * @author mawujun qq:16064988 mawujun1234@163.com
 *
 */
public class ExcelUtils {
//	@RequestMapping("/sampleDesign/exportSample.do")
//	@ResponseBody
//	public void exportSample(MapParams params,HttpServletResponse response) throws Exception {
//		//samplePlanService.lockOrunlock(plspno, plspst);
//		//System.out.println(params);
//		XSSFWorkbook wb = new XSSFWorkbook();    
//		Sheet sheet1 = wb.createSheet("资料");
//		
//		LinkedHashMap<String,String> titles=new LinkedHashMap<String,String>();
//		titles.put("PLSPNM", "企划样衣编号");
//		titles.put("BRADNM", "品牌");
//		titles.put("SPYEAR", "年份");
//		titles.put("SPSEAN", "季节");
//		titles.put("SPBSENM", "大系列");
//		titles.put("SPRSENM", "品牌系列");
//		titles.put("SPCLNM", "大类");
//		titles.put("SPTYNM", "小类");
//		titles.put("SPSENM", "系列");
//		titles.put("SPLCNM", "定位");
//		titles.put("SPBANM", "上市批次");
//		titles.put("PLGRNM", "商品等级");
//		titles.put("SPFTPR", "出厂价");
//		titles.put("SPRTPR", "零售价");
//		titles.put("SPPLRD", "企划倍率");
//		titles.put("PLCTPR", "企划成本价");
//
//
//		
//		crreateTitle_exportSample(wb,sheet1,titles);
//		
//		crreateData_exportSample(wb,sheet1,titles,params);
//
//		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");    
//        response.setHeader("Content-disposition", "attachment;filename="+new String("企划样衣资料".getBytes(),"ISO8859-1")+".xlsx");    
//        OutputStream ouputStream = response.getOutputStream();    
//        wb.write(ouputStream);    
//        ouputStream.flush();    
//        ouputStream.close();    
//	}
//
//	private void crreateTitle_export(XSSFWorkbook wb,Sheet sheet1,LinkedHashMap<String,String> titles){
//		CellStyle cellStyle = wb.createCellStyle();
//		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
//	    cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
//	    cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
//		cellStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
//		
//		Font font = wb.createFont();
//	    //font.setFontHeightInPoints(18);
//	    font.setFontName("Courier New");
//	    cellStyle.setFont(font);
//		 
//		Row title = sheet1.createRow(0);
//		
//		int i=0;
//		for(Entry<String,String> entry:titles.entrySet()){
//			Cell cell = title.createCell(i);
//			cell.setCellValue(entry.getValue());
//			cell.setCellStyle(cellStyle);
//			i++;
//		}
//		
//	}
//	private void crreateData_export(XSSFWorkbook wb,Sheet sheet1,LinkedHashMap<String,String> titles,List<OrderNumTotal> list) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//		
//		
//		
//		if(list==null || list.size()==0){
//			return;
//		}
//		for(int i=0;i<list.size();i++){
//			OrderNumTotal orderNumTotal=list.get(i);
//			Row row = sheet1.createRow(i+1);
//			int j=0;
//			for(Entry<String,String> entry:titles.entrySet()){
//				Cell cell = row.createCell(j);
//				j++;
//				String get_name="get"+StringUtils.capitalize(entry.getKey());
//				Object value=ReflectionUtils.findMethod(OrderNumTotal.class, get_name).invoke(orderNumTotal);
//				if(value!=null){
//					cell.setCellValue(value.toString());
//				}
//			}
//		}
//	}
//	private void crreateData_exportSample(XSSFWorkbook wb,Sheet sheet1,LinkedHashMap<String,String> titles,MapParams params) {
//		List<Map<String,Object>> list=sampleDesignService.query_exportSample(params);
//		if(list==null || list.size()==0){
//			return;
//		}
//		for(int i=0;i<list.size();i++){
//			Map<String,Object> map=list.get(i);
//			Row row = sheet1.createRow(i+1);
//			int j=0;
//			for(Entry<String,String> entry:titles.entrySet()){
//				Cell cell = row.createCell(j);
//				j++;
//				if(map.get(entry.getKey())!=null){
//					cell.setCellValue(map.get(entry.getKey()).toString());
//				}
//			}
//		}
//	}
}
