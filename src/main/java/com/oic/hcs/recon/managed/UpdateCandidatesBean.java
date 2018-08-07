/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oic.hcs.recon.managed;

import com.oic.hcs.recon.entity.ReconClaimsData;
import com.oic.hcs.recon.entity.ReconClaimsDataPK;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
//import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
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
import com.oic.hcs.recon.utility.DataAndSummary;

/**
 *
 * @author noaman000
 */
@ManagedBean
@ViewScoped
public class UpdateCandidatesBean implements Serializable{

    @EJB
    ReconServiceBeanLocal reconEJB;

    private String reconId;
    DataAndSummary payload = new DataAndSummary();
    private ArrayList<ReconClaimsData> filteredReconData;

    public String getReconId() {
        return reconId;
    }

    public void setReconId(String reconId) {
        this.reconId = reconId;
    }

    public DataAndSummary getPayload() {
        return payload;
    }

    public void setPayload(DataAndSummary payload) {
        this.payload = payload;
    }

    public ArrayList<ReconClaimsData> getFilteredReconData() {
        return filteredReconData;
    }

    public void setFilteredReconData(ArrayList<ReconClaimsData> filteredReconData) {
        this.filteredReconData = filteredReconData;
    }

    public void initialise() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            payload = reconEJB.getReconClaimsDataByReconId(reconId);
        } catch (Exception e) {
            facesContext.addMessage("errorDeletions", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Unable to load data for reconId: " + reconId + " " + e.getMessage()));
        }
    }

    public void updateReconClaimsData(ReconClaimsDataPK key, Double newAmount, String notes) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = Util.getSession();
        String user = (String) session.getAttribute("username");
        try {
            reconEJB.updateReconClaimsData(key, newAmount, notes, user);
            payload = reconEJB.getUpdatedSummary(payload, key.getReconId(), "underProcess");
        } catch (AlreadyExistsException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        } catch (Exception ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to update the record. " + ex.getMessage()));
        }
    }

    public void uploadReconData(FileUploadEvent event) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (event.getFile() == null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please choose a valid file.", null));
        } else {
            ArrayList<ReconClaimsData> reconList = new ArrayList<ReconClaimsData>();
            ReconClaimsData claim = null;
            UploadedFile tfile = event.getFile();
            String str = tfile.getFileName();
            String ext = str.substring(str.lastIndexOf('.') + 1, str.length());
            Workbook workbook = null;
            InputStream file;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            int rowCount = 2;
            int cellCount = 0;
            try {
                file = event.getFile().getInputstream();
                if (ext.equalsIgnoreCase("xls")) {
                    workbook = new HSSFWorkbook(file);
                } else {
                    workbook = new XSSFWorkbook(file);
                }

                Sheet sheet = workbook.getSheetAt(0);                
                Iterator<Row> rowIterator = sheet.iterator();
                Cell cell;
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    if (row.getRowNum() == 0) {
                        continue;
                    }
                    //Iterator<Cell> cellIterator = row.cellIterator();                    
                    cellCount = 0;                    
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    claim = new ReconClaimsData();
                    ReconClaimsDataPK key = new ReconClaimsDataPK();
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                key.setReconId(String.valueOf(cell.getDateCellValue()));
                            } else {
                                key.setReconId(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                key.setReconId(null);
                            else 
                            key.setReconId(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            key.setReconId(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                key.setClaimId(BigInteger.valueOf(Double.valueOf(String.valueOf(cell.getNumericCellValue())).intValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:                            
                            if(cell.getStringCellValue().isEmpty())
                                key.setClaimId(null);
                            else 
                            key.setClaimId(new BigInteger(cell.getStringCellValue()));
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            key.setClaimId(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                key.setCclaimItem(BigInteger.valueOf(Double.valueOf(String.valueOf(cell.getNumericCellValue())).intValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                key.setCclaimItem(null);
                            else 
                            key.setCclaimItem(new BigInteger(cell.getStringCellValue()));
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            key.setCclaimItem(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }

                    claim.setReconClaimsDataPK(key);

                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setInvoiceNum(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setInvoiceNum(null);
                            else 
                            claim.setInvoiceNum(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setInvoiceNum(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setMemberId(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setMemberId(null);
                            else
                            claim.setMemberId(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setMemberId(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setResubmissiontype(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setResubmissiontype(null);
                            else
                            claim.setResubmissiontype(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setResubmissiontype(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                claim.setServiceDt(cell.getDateCellValue());
                            } else {
                                claim.setServiceDt(new Date((long) cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setServiceDt(null);
                            else
                            claim.setServiceDt(sdf.parse(cell.getStringCellValue()));
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setServiceDt(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setActivityId(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setActivityId(null);
                            else
                            claim.setActivityId(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setActivityId(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setDiagDescPrimary(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setDiagDescPrimary(null);
                            else
                            claim.setDiagDescPrimary(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setDiagDescPrimary(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setDiagDescSecondary(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setDiagDescSecondary(null);
                            else
                            claim.setDiagDescSecondary(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setDiagDescSecondary(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setBenefitType(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setBenefitType(null);
                            else
                            claim.setBenefitType(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setBenefitType(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setBenefitCode(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setBenefitCode(null);
                            else
                            claim.setBenefitCode(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setBenefitCode(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setBenefitDesc(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setBenefitDesc(null);
                            else
                            claim.setBenefitDesc(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setBenefitDesc(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setQuantity(cell.getNumericCellValue());
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setQuantity(null);
                            else
                            claim.setQuantity(new Double(cell.getStringCellValue()));
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setQuantity(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setClinicianId(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setClinicianId(null);
                            else
                            claim.setClinicianId(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setClinicianId(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setClinicianSpec(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setClinicianSpec(null);
                            else
                            claim.setClinicianSpec(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setClinicianSpec(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setClaimedNet(new Double(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setClaimedNet(null);
                            else
                            claim.setClaimedNet(new Double(cell.getStringCellValue()));
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setClaimedNet(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setDeductible(new Double(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setDeductible(null);
                            else
                            claim.setDeductible(new Double(cell.getStringCellValue()));
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setDeductible(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setPaidAmt(new Double(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setPaidAmt(null);
                            else
                            claim.setPaidAmt(new Double(cell.getStringCellValue()));
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setPaidAmt(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setRejEligibleAmt(new Double(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setRejEligibleAmt(null);
                            else
                            claim.setRejEligibleAmt(new Double(cell.getStringCellValue()));
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setRejEligibleAmt(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setTotalRejAmt(new Double(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setTotalRejAmt(null);
                            else
                            claim.setTotalRejAmt(new Double(cell.getStringCellValue()));
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setTotalRejAmt(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setNewPayableAmt(new Double(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setNewPayableAmt(null);
                            else                                        
                            claim.setNewPayableAmt(new Double(cell.getStringCellValue()));
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setNewPayableAmt(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    reconEJB.validateNewPayableAmount(claim);
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setDenialCd(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setDenialCd(null);
                            else
                            claim.setDenialCd(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setDenialCd(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setNotes(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setNotes(null);
                            else
                            claim.setNotes(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setNotes(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setPreauthId(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setPreauthId(null);
                            else
                            claim.setPreauthId(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setPreauthId(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setUsername(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setUsername(null);
                            else
                            claim.setUsername(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setUsername(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setCreatedBy(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setCreatedBy(null);
                            else
                            claim.setCreatedBy(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setCreatedBy(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                claim.setCreatedOn(cell.getDateCellValue());
                            } else {
                                claim.setCreatedOn(new Date((long) cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setCreatedOn(null);
                            else
                            claim.setCreatedOn(sdf.parse(cell.getStringCellValue()));
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setCreatedOn(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setLastUpdatedBy(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setLastUpdatedBy(null);
                            else
                            claim.setLastUpdatedBy(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setLastUpdatedBy(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                claim.setLastUpdatedOn(cell.getDateCellValue());
                            } else {
                                claim.setLastUpdatedOn(new Date((long) cell.getNumericCellValue()));
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setLastUpdatedOn(null);
                            else
                            claim.setLastUpdatedOn(sdf.parse(cell.getStringCellValue()));
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setLastUpdatedOn(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }

                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    String recType = cell.getStringCellValue();
                    switch (recType) {
                        case "Accepted Claims - Already paid manually":
                            claim.setReconType("AS");
                            break;
                        case "Accepted Claims - To be transferred for Payment":
                            claim.setReconType("AN");
                            break;
                        case "Pending Claims.":
                            claim.setReconType("PN");
                            break;
                        default:
                            throw new AlreadyExistsException("Invalid recon type. row number: " + rowCount + " column number: " + cellCount);
                    }
                    cellCount++;
                    cell = row.getCell(cellCount);
                    if(cell==null){
                        cell = row.createCell(cellCount);
                    }
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            if (HSSFDateUtil.isCellDateFormatted(cell) || HSSFDateUtil.isCellInternalDateFormatted(cell)) {
                                throw new AlreadyExistsException("Column should not be formatted as Date. row number: " + rowCount + " column number: " + cellCount);
                            } else {
                                claim.setFrozen((int) cell.getNumericCellValue());
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            if(cell.getStringCellValue().isEmpty())
                                claim.setFrozen(null);
                            else
                            claim.setFrozen(Integer.valueOf(cell.getStringCellValue()));
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            claim.setFrozen(null);
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            throw new AlreadyExistsException("Cell contains formula. row number: " + rowCount + " column number: " + cellCount);
                    }                    
                    reconList.add(claim);
                    rowCount++;
                }
                boolean isValid = validateReconUpload(reconList);
                if (isValid) {
                    HttpSession session = Util.getSession();
                    String user = (String) session.getAttribute("username");
                    try {
                        reconEJB.clearAndInsertReconClaimsData(reconList, user);
                    } catch (Exception e) {
                        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unable to upload. ", e.getMessage()));
                    }
                    initialise();
                    facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Upload processed. ", reconList.size() + " records uploaded."));
                    // setDisableFlag(false);
                }
            } catch (IOException e) {
                facesContext
                        .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error reading file" + e.getMessage(), null));
            } catch (AlreadyExistsException e) {
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unable to upload. ", e.getMessage()));
            } catch (Exception e) {
            	e.printStackTrace();
                facesContext
                        .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unable to upload. ", "Error in uploading row number: " + rowCount + " column number: " + cellCount +" "+ e.getMessage()));
            }
        }
    }

    public boolean validateReconUpload(List<ReconClaimsData> ls) throws Exception {
        DataAndSummary data;
        data = reconEJB.getReconClaimsDataByReconId(reconId);
        List<ReconClaimsData> dataList = data.getDataList();
        if (ls.size() != dataList.size()) {
            throw new AlreadyExistsException("Number of old records do not match with number of new records.");
        }
        Map<ReconClaimsDataPK, ReconClaimsData> map = new HashMap<ReconClaimsDataPK, ReconClaimsData>();
        for (ReconClaimsData claim : dataList) {
            map.put(claim.getReconClaimsDataPK(), claim);
        }

        for (ReconClaimsData newData : ls) {
            ReconClaimsData oldData = map.get(newData.getReconClaimsDataPK());
            if (oldData == null) {
                throw new AlreadyExistsException("No existing record found for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            }
            if (!Objects.equals(newData.getInvoiceNum(),oldData.getInvoiceNum())) {
                throw new AlreadyExistsException("Invoice number mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getMemberId(),oldData.getMemberId())) {
                throw new AlreadyExistsException("Member Id mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getResubmissiontype(),oldData.getResubmissiontype())) {
                throw new AlreadyExistsException("Resubmission Type mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getServiceDt(),oldData.getServiceDt())) {
                throw new AlreadyExistsException("Service date mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getActivityId(),oldData.getActivityId())) {
                throw new AlreadyExistsException("Activity Id mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getDiagDescPrimary(),oldData.getDiagDescPrimary())) {
                throw new AlreadyExistsException("Primary diagnosis description mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getDiagDescSecondary(),oldData.getDiagDescSecondary())) {
                throw new AlreadyExistsException("Secondary diagnosis description mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getBenefitType(),oldData.getBenefitType())) {
                throw new AlreadyExistsException("Benefit Type mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getBenefitCode(),oldData.getBenefitCode())) {
                throw new AlreadyExistsException("Benefit Code mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getBenefitDesc(),oldData.getBenefitDesc())) {
                throw new AlreadyExistsException("Benefit Description mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getQuantity(),oldData.getQuantity())) {
                throw new AlreadyExistsException("Quantity mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getClinicianId(),oldData.getClinicianId())) {
                throw new AlreadyExistsException("Clinician Id mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getClinicianSpec(),oldData.getClinicianSpec())) {
                throw new AlreadyExistsException("Clinician Spec mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getClaimedNet(),oldData.getClaimedNet())) {
                throw new AlreadyExistsException("Claimed Net mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getDeductible(),oldData.getDeductible())) {
                throw new AlreadyExistsException("Deductible mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getPaidAmt(),oldData.getPaidAmt())) {
                throw new AlreadyExistsException("Paid Amount mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getRejEligibleAmt(),oldData.getRejEligibleAmt())) {
                throw new AlreadyExistsException("Eligible Rejected Amount mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getTotalRejAmt(),oldData.getTotalRejAmt())) {
                throw new AlreadyExistsException("Total Rejected Amount mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getDenialCd(),oldData.getDenialCd())) {
                throw new AlreadyExistsException("Denial Code mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getPreauthId(),oldData.getPreauthId())) {
                throw new AlreadyExistsException("PreAuth Id mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getReconType(),oldData.getReconType())) {
                throw new AlreadyExistsException("Recon Type mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            } else if (!Objects.equals(newData.getFrozen(),oldData.getFrozen())) {
                throw new AlreadyExistsException("Frozen status mismatch for claim Id: " + newData.getReconClaimsDataPK().getClaimId()
                        + " claim item: " + newData.getReconClaimsDataPK().getCclaimItem() + " and recon Id: "
                        + newData.getReconClaimsDataPK().getReconId());
            }
        }
        return true;
    }
       
}
