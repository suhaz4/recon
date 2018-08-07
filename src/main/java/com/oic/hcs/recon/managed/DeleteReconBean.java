/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oic.hcs.recon.managed;

import java.io.Serializable;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import com.oic.hcs.recon.service.ReconServiceBeanLocal;
import com.oic.hcs.recon.utility.AlreadyExistsException;

/**
 *
 * @author noaman000
 */
@ManagedBean
@ViewScoped
public class DeleteReconBean implements Serializable{
    @EJB
    ReconServiceBeanLocal reconEJB;

    private String reconId;

    public String getReconId() {
        return reconId;
    }

    public void setReconId(String reconId) {
        this.reconId = reconId;
    }
    
    public void deleteRecon(){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try{
            reconEJB.deleteRecon(reconId);
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Deletion complete. " ,"Recon "+reconId+" has been deleted from the system."));        
        }catch(AlreadyExistsException e){
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error","Can't delete recon because accepted recon claims exist in production for recond ID: "+reconId)); 
        }catch(Exception ex){
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error","Unable to delete recon. "+ex.getMessage()));
        }
    }
}
