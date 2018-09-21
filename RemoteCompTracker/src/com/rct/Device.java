/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rct;

/**
 *
 * @author JWizard
 */
public class Device {
    private String ipAddress;
	private String location;
	private String description;

	public String getIpAddress(){return this.ipAddress;}
	public String getLocation(){return this.location;}
	public String getDescription(){return this.description;}
	public void setIpAddress(String ipAddress){this.ipAddress=ipAddress;}
	public void setLocation(String location){this.location=location;}
	public void setDescription(String description){this.description=description;}
    
}
