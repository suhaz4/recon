/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oic.hcs.recon.managed;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import com.oic.hcs.recon.service.ReconServiceBeanLocal;
import com.oic.hcs.recon.util.Util;
import com.oic.hcs.recon.utility.AlreadyExistsException;

/**
 *
 * @author noaman000
 */
public class NewReconBean implements Serializable{
    @EJB
    ReconServiceBeanLocal reconEJB;
    
    private String reconType="select";
    private boolean disableFlag = true;    
    private String reconId;            
    private boolean nextFlag;

    public String getReconType() {
        return reconType;
    }

    public void setReconType(String reconType) {
        this.reconType = reconType;
    }  

    public boolean isDisableFlag() {
        return disableFlag;
    }

    public void setDisableFlag(boolean disableFlag) {
        this.disableFlag = disableFlag;
    }

    public String getReconId() {
        return reconId;
    }

    public void setReconId(String reconId) {
        this.reconId = reconId;
    }
    
    public boolean isNextFlag() {
        return nextFlag;
    }

    public void setNextFlag(boolean nextFlag) {
        this.nextFlag = nextFlag;
    }
    
    public void uploadCandidateClaims(FileUploadEvent event) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (event.getFile() == null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please choose a valid file.", null));
        }
        ArrayList<ArrayList> candidatesList = new ArrayList<ArrayList>();
        ArrayList candidate = null;
        UploadedFile tfile = event.getFile();
        String str = tfile.getFileName();
        String ext = str.substring(str.lastIndexOf('.')+1, str.length());
        Workbook workbook = null;
        InputStream file;
        try {
            file = event.getFile().getInputstream();
            if (ext.equalsIgnoreCase("xls")) {
                workbook = new HSSFWorkbook(file);
            } else {
                workbook = new XSSFWorkbook(file);
            }
        } catch (IOException e) {
            facesContext
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error reading file" + e, null));
        }

        Sheet sheet = workbook.getSheetAt(0);

        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if(row.getRowNum()==0){
                continue;
            }
            candidate = new ArrayList();
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();

                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_NUMERIC:

                        if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                            candidate.add(cell.getDateCellValue());
                        } else {
                            candidate.add(cell.getNumericCellValue());
                        }
                        break;
                    case Cell.CELL_TYPE_STRING:
                        candidate.add(cell.getStringCellValue());
                        break;
                }

            }
            candidatesList.add(candidate);
        }
        int rowCount = 1;
        HttpSession session = Util.getSession();
        String user = (String)session.getAttribute("username");
        for(ArrayList candidateClaim: candidatesList){
            try{
            rowCount++;
            reconEJB.insertCandidateClaim(BigInteger.valueOf(Double.valueOf(String.valueOf(candidateClaim.get(0))).intValue()),
                    (String)candidateClaim.get(1), reconId,user ,reconType);            
            }catch(AlreadyExistsException e){ 
            	e.printStackTrace();
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error in uploading row number: "+rowCount
                        ,"<br/>"+e.getMessage()));
                break;
            }catch(Exception e){    
            	e.printStackTrace();
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error in uploading row number: "+rowCount
                        , "<br/>Incorrect data. "+e.getMessage()));
                break;
            }
        }
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Upload processed for "+(rowCount-1)+" claim(s).","Please check errors (if any) and proceed with validations."));        
        setDisableFlag(false);
    }                          
    
}
