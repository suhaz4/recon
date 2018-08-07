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
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import com.oic.hcs.recon.service.ReconServiceBeanLocal;
import com.oic.hcs.recon.util.Util;

/**
 *
 * @author noaman000
 */
@ManagedBean(name = "loginBean")
@SessionScoped
public class LoginBean implements Serializable{
    
    @EJB
    ReconServiceBeanLocal reconEJB;
    
    private String password;
    private String message, uname;    
 
    public String getMessage() {
        return message;
    }
 
    public void setMessage(String message) {
        this.message = message;
    }
 
    public String getPassword() {
        return password;
    }
 
    public void setPassword(String password) {
        this.password = password;
    }
 
    public String getUname() {
        return uname;
    }
 
    public void setUname(String uname) {
        this.uname = uname;
    }
    
    public String validateLogin(){
        double limit = reconEJB.authenticate(uname,password);
        if(limit>=0){
            HttpSession session = Util.getSession();
            session.setAttribute("username", uname);
            session.setAttribute("limit", limit);
 
            return "home";
        }
        else{
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Incorrect Username and/or Passowrd.",
                            "<br/>Please enter authorised username and Password"));
            return null;
        }
    }
        
    public String logout() {
      HttpSession session = Util.getSession();
      session.invalidate(); 
      FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "You have been logged out.",""));
      FacesContext.
          getCurrentInstance().
          getExternalContext().getFlash().setKeepMessages(true);
      return "login";
   }
}
