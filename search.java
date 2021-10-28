import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class search {
    public static String xmlString = "";
    public static String path;
    public static void main(String[] args){
        ArrayList<String> foundlist = new ArrayList<String>();
        path = args[0];
        findPath(path);
        Document xml = StringtoXML(xmlString);
        Element elXML = xml.getDocumentElement();
        String filename = "save.xml";
        saveXML(xml,new File(filename));
        searchPath(elXML,args[1],foundlist);
        for ( int i = 0 ; i < foundlist.size() ; i+=2 ){  //print found file and their directory
            System.out.println(foundlist.get(i) + " in " + foundlist.get(i+1));
        }
        System.out.println("FINISH\n");
    }

    public static void findPath(String path){
        String[] listpath = path.split("/");
        String lastpath = listpath[listpath.length-1];
        xmlString += "<folder name=" + '"' + lastpath + '"' + ">";
        File f = new File(path);
        File[] files =  f. listFiles();
//        System.out.println(f);
        for ( int i = 0 ; i < files.length ; i++ ){
            File file = files[i];
//            System.out.println(file);
            String filename = file.getName();
            if ( file.isDirectory() ){
                String nextpath = files[i].toString();
                findPath(nextpath);
            }
            else{
                xmlString += "<file>" + filename + "</file>";
            }
        }
        xmlString += "</folder>";
    }
    public static Document StringtoXML(String xmlString){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;

        try{
            builder = factory.newDocumentBuilder();

            Document doc =  builder.parse(new InputSource(new StringReader(xmlString)));
            return doc;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static void saveXML(Document document, File file){
        try{
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            LSSerializer writer = impl.createLSSerializer();
            FileOutputStream fos = new FileOutputStream(file);
            LSOutput lso = impl.createLSOutput();
            lso.setByteStream(fos);
            writer.write(document, lso);
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void searchPath(Element sp, String searchname,ArrayList<String> foundlist) {
        try {
            boolean matchFound = false;
            boolean matchFound2 = false;
//            Element f = sp.getDocumentElement();
//            System.out.println(f.getAttribute("name"));
            NodeList files = sp.getChildNodes();

            for ( int i = 0 ; i < files.getLength() ; i++ ){
//                System.out.println(i);
                if( files.item(i).getNodeType() == Node.ELEMENT_NODE ){
                    Element element = (Element) files.item(i);
//                System.out.println(el.getElementsByTagName("file").item(i).getTextContent());
                    if( element.getNodeName().contains("folder") ){
                        String nameFolder = element.getAttributes().getNamedItem("name").getNodeValue();
                        try{
                            Pattern pattern = Pattern.compile(searchname.charAt(0) + ".{" + (searchname.length()-2) + "}" + searchname.charAt(searchname.length()-1),Pattern.CASE_INSENSITIVE);
                            Matcher matcher = pattern.matcher(nameFolder);
                            matchFound = matcher.find();
                            if( matchFound ){
                                searchname = matcher.group(0);
                            }
                        }
                        catch (Exception e){

                        }

                        Pattern pattern2 = Pattern.compile(searchname,Pattern.CASE_INSENSITIVE);
                        Matcher matcher2 = pattern2.matcher(nameFolder);
                        matchFound2 = matcher2.find();

                        if( matchFound2 ){
                            searchname = matcher2.group(0);
                        }

                        if( nameFolder.equals(searchname) && (matchFound || matchFound2)){
                            foundlist.add(nameFolder); // recently folder name
                            foundlist.add(sp.getAttribute("name"));  //Head Folder name
                        }
                        else if( nameFolder.contains(searchname) && (matchFound || matchFound2) ){
                            foundlist.add(nameFolder); // recently folder name
                            foundlist.add(sp.getAttribute("name"));  //Head Folder name
                        }
                        searchPath(element,searchname,foundlist);
                    }
                    if( element.getNodeName().contains("file") ){
                        String nameFile = element.getTextContent();
                        try{
                            Pattern pattern = Pattern.compile(searchname.charAt(0) + ".{" + (searchname.length()-2) + "}" + searchname.charAt(searchname.length()-1),Pattern.CASE_INSENSITIVE);
                            Matcher matcher = pattern.matcher(nameFile);
                            matchFound = matcher.find();
                            if( matchFound ){
                                searchname = matcher.group(0);
                            }
                        }
                        catch (Exception e){

                        }
                        Pattern pattern2 = Pattern.compile(searchname,Pattern.CASE_INSENSITIVE);
                        Matcher matcher2 = pattern2.matcher(nameFile);
                        matchFound2 = matcher2.find();

                        if( matchFound2 ){
                            searchname = matcher2.group(0);
                        }
//                        System.out.println(nameFile);
                        if( nameFile.equals(searchname) && (matchFound || matchFound2)){
                            foundlist.add(nameFile);
                            foundlist.add(sp.getAttribute("name"));
                        }
                        else if( nameFile.contains(searchname) && (matchFound || matchFound2)){
                            foundlist.add(nameFile);
                            foundlist.add(sp.getAttribute("name"));
                        }
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
