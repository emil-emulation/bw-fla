/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.emucomp.components.emulators;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
 
public class VirtualBoxEnvironment{
	private String fileReference = null;
	private String VboxUuid = null;
	private String StorageControllerName = null;
	private String AttachedDeviceType = null;
	private String AttachedDevicePort = null;
	private String AttachedDeviceDevice = null;
	private String DVDStorageControllerName = null;
	private String DVDAttachedDeviceType = null;
	private String DVDAttachedDevicePort = null;
	private String DVDAttachedDeviceDevice = null;
	private String HddImage = null;
	
	public VirtualBoxEnvironment(String configFile){
		File fXmlFile = new File(configFile); 
		parseXMLfile(fXmlFile);
	}
	
	public VirtualBoxEnvironment(File configFile){
		parseXMLfile(configFile);
	}
	
	private void parseXMLfile(File configFile){	
		try{
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(configFile);
			doc.getDocumentElement().normalize();
			
			
			// get 'ovf:id="fileX"' from <References><File ... ovf:id=".."/>
			// get hddFilename from <References><File ovf:href="...." ... />
			// <References>
			NodeList refList = doc.getElementsByTagName("References");
			for(int i=0; i < refList.getLength(); i++){
				Node refNode = refList.item(i);
				if(refNode.getNodeType() == Node.ELEMENT_NODE){
					Element eElement = (Element) refNode;
					// <File ovf:href="..." ovf:id="..."/>
					if(eElement.hasChildNodes()){
						NodeList fileList = refNode.getChildNodes();
						Node fileNode = fileList.item(1);
						Element fileElement = (Element) fileNode;
						this.setHddImage(fileElement.getAttribute("ovf:href"));
						this.setFileReference(fileElement.getAttribute("ovf:id"));
					}
				}
			}
			
			// get vbox:uuid on basis of ovf:id
			// <DiskSection>
			NodeList DiskSectionList = doc.getElementsByTagName("DiskSection");
			for(int i=0; i < DiskSectionList.getLength(); i++){
				Node DiskSectionNode = DiskSectionList.item(i);
				NodeList dsList = DiskSectionNode.getChildNodes();
				// <Info> blub </Info>
				// <Disk ... ovf:fileRef="..." ... vbox:uuid="..."/>
				for(int j=0; j < dsList.getLength() ; j++){
					Node dsNode = dsList.item(j);
					if(dsNode.getNodeType() == Node.ELEMENT_NODE){
						if(dsNode.getNodeName() == "Disk"){
							Element dsElement = (Element) dsNode;
							if(dsElement.getAttribute("ovf:fileRef").equals(this.getFileReference())){
								this.setVboxUuid(dsElement.getAttribute("vbox:uuid"));
							}
						}
					}
				}
			}
			
			// get StorageController Name
			// get AttachedDevice type, port-number and device-number
			//
			// <StorageControllers>
			NodeList SCsList = doc.getElementsByTagName("StorageControllers");
			for(int i=0; i < SCsList.getLength(); i++){
				Node SCsNode = SCsList.item(i);
				// <StorageController name="..." ...>
				NodeList SCList = SCsNode.getChildNodes();
				for(int j=0; j < SCList.getLength(); j++){
					Node SCNode = SCList.item(j);
					if(SCNode.getNodeType() == Node.ELEMENT_NODE){
						Element SCElement = (Element) SCNode;
						// <AttachedDevice type="HardDisk" ...>
						NodeList ADList = SCElement.getChildNodes();
						for(int k=0; k < ADList.getLength(); k++){
							Node ADNode = ADList.item(k);
							if(ADNode.getNodeType() == Node.ELEMENT_NODE){
								Element ADElement = (Element) ADNode;
								String adElementType = ADElement.getAttribute("type");
								if(adElementType.equals("DVD")){
									System.out.println("Name: " + SCElement.getAttribute("name"));
									this.setDVDStorageControllerName(SCElement.getAttribute("name"));
									System.out.println("DeviceType: " + adElementType);
									this.setDVDAttachedDeviceType(adElementType);
									System.out.println("Port: " + ADElement.getAttribute("port"));
									this.setDVDAttachedDevicePort(ADElement.getAttribute("port"));
									System.out.println("Device: " + ADElement.getAttribute("device"));
									this.setDVDAttachedDeviceDevice(ADElement.getAttribute("device"));
									continue;
								}
								
								if(adElementType.equals("HardDisk") && ADNode.hasChildNodes()){
									// <Image uuid="{....}"/>
									NodeList IList = ADNode.getChildNodes();
									for(int l=0; l < IList.getLength(); l++){
										Node INode = IList.item(l);
										if(INode.getNodeType() == Node.ELEMENT_NODE){
											Element IElement = (Element) INode;
											// check if the uuid matches + get rid of the brackets { }
											if(this.getVboxUuid().equals(IElement.getAttribute("uuid").replace("{", "").replace("}", ""))){
												System.out.println("Name: " + SCElement.getAttribute("name"));
												this.setStorageControllerName(SCElement.getAttribute("name"));
												System.out.println("DeviceType: " + adElementType);
												this.setAttachedDeviceType(adElementType);
												System.out.println("Port: " + ADElement.getAttribute("port"));
												this.setAttachedDevicePort(ADElement.getAttribute("port"));
												System.out.println("Device: " + ADElement.getAttribute("device"));
												this.setAttachedDeviceDevice(ADElement.getAttribute("device"));
												break;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public String getFileReference(){
		return fileReference;
	}
	
	public void setFileReference(String fileRef){
		this.fileReference = fileRef;
	}
	
	public String getVboxUuid(){
		return VboxUuid;
	}
	
	public void setVboxUuid(String uuid){
		this.VboxUuid = uuid;
	}
	
	public String getStorageControllerName(){
		return StorageControllerName;
	}
	
	public void setStorageControllerName(String name){
		this.StorageControllerName = name;
	}
	
	public String getAttachedDeviceType(){
		return AttachedDeviceType;
	}
	
	public void setAttachedDeviceType(String type){
		this.AttachedDeviceType = type;
	}
	
	public String getAttachedDevicePort(){
		return AttachedDevicePort;
	}
	
	public void setAttachedDevicePort(String port){
		this.AttachedDevicePort = port;
	}
	
	public String getAttachedDeviceDevice(){
		return AttachedDeviceDevice;
	}
	
	public void setAttachedDeviceDevice(String device){
		this.AttachedDeviceDevice = device;
	}
	
	public String getDVDStorageControllerName(){
		return DVDStorageControllerName;
	}
	
	public void setDVDStorageControllerName(String name){
		this.DVDStorageControllerName = name;
	}
	
	public String getDVDAttachedDeviceType(){
		return DVDAttachedDeviceType;
	}
	
	public void setDVDAttachedDeviceType(String type){
		this.DVDAttachedDeviceType = type;
	}
	
	public String getDVDAttachedDevicePort(){
		return DVDAttachedDevicePort;
	}
	
	public void setDVDAttachedDevicePort(String port){
		this.DVDAttachedDevicePort = port;
	}
	
	public String getDVDAttachedDeviceDevice(){
		return DVDAttachedDeviceDevice;
	}
	
	public void setDVDAttachedDeviceDevice(String device){
		this.DVDAttachedDeviceDevice = device;
	}

	
	
	public String getHddImage(){
		return HddImage;
	}
	
	public void setHddImage(String image){
		this.HddImage = image;
	}
}