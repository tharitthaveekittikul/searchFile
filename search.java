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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.text.SimpleDateFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class search {
    public static String xmlString = "";
    public static String path;
    public static void main(String[] args){
        
        if(args.length <= 1 || args.length > 2){
            System.out.println("++++++++++++++++++++++++++WARNING!!++++++++++++++++++++++++++++++++++++");
            System.out.println("     please insert 2 arguments"); 
            System.out.println("     java search [path or -l] [what are you looking for?]");
            System.out.println("     Ex.1 java search /home/Document/Creature dog");
            System.out.println("     Ex.2 java search -l dog");
            System.out.println("     P.S. -l is load data from xml file");
        }
        else if(args[0].equals("-l") && args[1].length() >= 1){
            try{
                ArrayList<String> foundlist = new ArrayList<String>();
                File xmlFile = new File("save.xml");
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(xmlFile);
                Element elxml = doc.getDocumentElement();
                searchPath(elxml,args[1],foundlist);
                if(foundlist.size() != 0){
                    System.out.println("-------------->load from file save.xml");
                    for ( int i = 0 ; i < foundlist.size() ; i+=2 ){  //print found file and their directory
                        System.out.println(foundlist.get(i) + " in " + foundlist.get(i+1));
                    }
                }
                else{
                    System.out.println("-------NOT FOUND-------");
                }
                    System.out.println("FINISH\n");
            }
            catch(Exception e){
                System.out.println("++++++++++++++++++++++++++WARNING!!++++++++++++++++++++++++++++++++++++");
                System.out.println("     you don't have save.xml file please insert path");
                System.out.println("     please insert 2 arguments"); 
                System.out.println("     java search [path] [what are you looking for?]");
                System.out.println("     Ex.1 java search /home/Document/Creature dog");
                System.out.println("     P.S. -l is load data from xml file");
            }
        }
        else if(args[0].length() >= 1 && args[1].length() >= 1){
            ArrayList<String> foundlist = new ArrayList<String>();
            path = args[0];
            findPath(path);
            Document xml = StringtoXML(xmlString);
            Element elXML = xml.getDocumentElement();
            String filename = "save.xml";
            saveXML(xml,new File(filename));
            searchPath(elXML,args[1],foundlist);
            if(foundlist.size() != 0){
                System.out.println("-------------->new save.xml file");
                for ( int i = 0 ; i < foundlist.size() ; i+=2 ){  //print found file and their directory
                    System.out.println(foundlist.get(i) + " in " + foundlist.get(i+1));
                }
            }
            else{
                System.out.println("-------NOT FOUND-------");
            }
            System.out.println("FINISH\n");
        }
    }


    public static void findPath(String path){
        String[] listpath = path.split("/");
        String lastpath = listpath[listpath.length-1];
        xmlString += "<folder name=" + '"' + lastpath + '"' + ">";
        File f = new File(path);
        File[] files =  f. listFiles();
//        System.out.println(path);
        for ( int i = 0 ; i < files.length ; i++ ){
            File file = files[i];
//            System.out.println(file);
            String filename = file.getName();
//            System.out.println(filename);
            if ( file.isDirectory() ){
                String nextpath = files[i].toString();
//                System.out.println(nextpath);
                findPath(nextpath);
            }
            else {
                File data = new File(path+"/"+filename);
                SimpleDateFormat style = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                String  date = style.format(data.lastModified()); //date time of file
                Path pathfile = Paths.get(path+"/"+filename);
                
//                System.out.println(data);
                try {
                    long bytes = Files.size(pathfile); // size of file (bytes)
                    // System.out.println(String.format("%,d bytes",bytes));
                    MessageDigest md5Digest = MessageDigest.getInstance("MD5");
                    String checksum = getFileChecksum(md5Digest,data);
//                    System.out.println(checksum);
                    xmlString += "<file md5=" + '"' + checksum + '"' +" "+"date=" +'"'+ date +'"'+" "+"size ="+'"'+String.format("%,d bytes",bytes)+'"'+">" + filename + "</file>";
                } catch (NoSuchAlgorithmException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
        xmlString += "</folder>";
    }

    private static String getFileChecksum(MessageDigest digest, File file) throws IOException
    {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
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