import sys
import os
import xml.etree.ElementTree as ET
import re
import hashlib

def setup(): # sys.argv[1],sys.argv[2]  path,find
    path = sys.argv[1]
    find = sys.argv[2]
    xmlString = ""
    foundlist = []
    xmlString = findPath(path,xmlString)
    # print(xmlString)
    # xml = ET.ElementTree(ET.fromstring(xmlString))
    xml = ET.XML(xmlString)
    with open("save.xml","wb") as f:
        f.write(ET.tostring(xml))

    element = ET.parse("save.xml")
    mainpath =  element.getroot()
    searchPath(mainpath,find,foundlist)
    for i in range(0,len(foundlist),2):
        print(foundlist[i] + " in " + foundlist[i+1])
    
    print("FINISH\n")
    

def findPath(pathsearch,xmlString):
    listpath = pathsearch.split("/")
    lastpath = listpath[len(listpath)-1]
    xmlString += "<folder name=" + '"' + lastpath + '"' + ">"
    f = os.path.abspath(pathsearch)
    files = os.listdir(f)
    for i in range(len(files)):
        file = pathsearch + "/" + files[i]
        # print(file)
        filename = files[i]
        #print(filename)
        if (os.path.isdir(file)):
            nextpath = file
            # print(nextpath)
            xmlString = findPath(nextpath,xmlString)
        else:
            openfile = open(file,"r",encoding='utf-8')
            for data in openfile:
                data = data.strip()
            xmlString += "<file md5=" + '"' +hashlib.md5(data.encode()).hexdigest() + '">' + filename + "</file>"
    xmlString += "</folder>";   
    return xmlString 

def searchPath(mainpath,searchname,foundlist):
    patterns = [searchname[0]+".{"+ str((len(searchname)-2)) + "}" + searchname[len(searchname)-1],searchname]

    for i in range(len(list(mainpath))):

        if(mainpath[i].tag == "folder"):
            nameFolder = mainpath[i].attrib["name"]
            # print("nameFolder = "+ nameFolder)
            matcher = re.search(patterns[0],nameFolder,re.IGNORECASE)
            matcher2 = re.search(patterns[1],nameFolder,re.IGNORECASE)
            if matcher:
                # print("searchname = " + searchname)  
                searchname = matcher.group(0)
                # print("searchname = " + searchname)  
      
            if matcher2:
                searchname = matcher2.group(0)      

            if(nameFolder == searchname and (matcher or matcher2)):
                foundlist.append(searchname)
                foundlist.append(mainpath.attrib["name"])
            elif (searchname in nameFolder and (matcher or matcher2)):
                foundlist.append(nameFolder)
                foundlist.append(mainpath.attrib["name"])

            searchPath(mainpath[i],searchname,foundlist)

        elif(mainpath[i].tag == "file"):
            nameFile = mainpath[i].text
            # print("nameFile = " + nameFile)
            matcher = re.search(patterns[0],nameFile,re.IGNORECASE)
            matcher2 = re.search(patterns[1],nameFile,re.IGNORECASE)
            if matcher:
                # print("searchname = " + searchname)  
                searchname = matcher.group(0)
                # print("searchname = " + searchname)  
      
            if matcher2:
                searchname = matcher2.group(0)   

            if(nameFile == searchname and (matcher or matcher2)):
                foundlist.append(searchname)
                foundlist.append(mainpath.attrib["name"])
            elif(searchname in nameFile and (matcher or matcher2)):
                foundlist.append(nameFile)
                foundlist.append(mainpath.attrib["name"])

if __name__ == "__main__":
    setup()