package com.vero.coreprocessor.destinations.service;

import com.vero.coreprocessor.destinations.model.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.*;
import java.nio.file.*;

@Service
@Slf4j
public class ChannelServiceImpl implements ChannelService {
    @Override
    public boolean createChannelMuxConfig(Destination destination) {
        try {
            createChannel(destination);
            createMUX(destination);
            createLogonManager(destination);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean updateChannelMuxConfig(Destination destination) {
        try {
            createChannel(destination);
            createMUX(destination);
            createLogonManager(destination);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createChannel(Destination destination) throws IOException {
        String channelFilePath = System.getProperty("user.dir")+ File.separator+"deploy"+File.separator+"cfg"+File.separator+"10_"+ destination.getDestinationName()+"_channel.xml";
        Path path = Paths.get(channelFilePath);
        if (Files.exists(path)){
            Files.delete(path);
        }
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element channelAdapter = document.createElement("channel-adaptor");
            document.appendChild(channelAdapter);

            Attr name = document.createAttribute("name");
            name.setValue(destination.getDestinationName()+"-channel");
            channelAdapter.setAttributeNode(name);

            Attr realm = document.createAttribute("realm");
            realm.setValue(destination.getDestinationName()+"-channel");
            channelAdapter.setAttributeNode(realm);


            Attr logger = document.createAttribute("logger");
            logger.setValue("Q2");
            channelAdapter.setAttributeNode(logger);

            Element channel = document.createElement("channel");
            channelAdapter.appendChild(channel);

            Attr channelclass = document.createAttribute("class");
            channelclass.setValue("org.jpos.iso.channel.XMLChannel");
            channel.setAttributeNode(channelclass);

            Attr channellogger = document.createAttribute("logger");
            channellogger.setValue("Q2");
            channel.setAttributeNode(channellogger);

            Attr channelrealm = document.createAttribute("realm");
            channelrealm.setValue(destination.getDestinationName()+"-channel");
            channel.setAttributeNode(channelrealm);

            Attr channelpackager = document.createAttribute("packager");
            channelpackager.setValue("org.jpos.iso.packager.XML2003Packager");
            channel.setAttributeNode(channelpackager);

            Element property0 = document.createElement("property");
            Attr property1name0 = document.createAttribute("name");
            property1name0.setValue("packager-realm");
            property0.setAttributeNode(property1name0);

            Attr property0value = document.createAttribute("value");
            property0value.setValue(destination.getDestinationName()+"-channel-packager");
            property0.setAttributeNode(property0value);

            channel.appendChild(property0);

            Element packagerlogger = document.createElement("property");
            Attr packagerloggername = document.createAttribute("name");
            packagerloggername.setValue("packager-logger");
            packagerlogger.setAttributeNode(packagerloggername);

            Attr packagerloggervalue = document.createAttribute("value");
            packagerloggervalue.setValue("Q2");
            packagerlogger.setAttributeNode(packagerloggervalue);


            Element property1 = document.createElement("property");
            Attr property1name = document.createAttribute("name");
            property1name.setValue("host");
            property1.setAttributeNode(property1name);

            Attr property1value = document.createAttribute("value");
            property1value.setValue(destination.getIp());
            property1.setAttributeNode(property1value);

            channel.appendChild(property1);

            Element property2 = document.createElement("property");
            Attr property2name = document.createAttribute("name");
            property2name.setValue("port");
            property2.setAttributeNode(property2name);

            Attr property2value = document.createAttribute("value");
            property2value.setValue(destination.getPort());
            property2.setAttributeNode(property2value);

            channel.appendChild(property2);

            Element property3 = document.createElement("property");
            Attr property3name = document.createAttribute("name");
            property3name.setValue("timeout");
            property3.setAttributeNode(property3name);

            Attr property3value = document.createAttribute("value");
            property3value.setValue("300000");
            property3.setAttributeNode(property3value);

            channel.appendChild(property3);

            Element space = document.createElement("space");
            space.appendChild(document.createTextNode(destination.getDestinationName()));
            channelAdapter.appendChild(space);

            Element in = document.createElement("in");
            in.appendChild(document.createTextNode(destination.getDestinationName()+".in"));
            channelAdapter.appendChild(in);

            Element out = document.createElement("out");
            out.appendChild(document.createTextNode(destination.getDestinationName()+".out"));
            channelAdapter.appendChild(out);

            Element reconnectdelay = document.createElement("timeout");
            reconnectdelay.appendChild(document.createTextNode("90000"));
            channelAdapter.appendChild(reconnectdelay);

            FileOutputStream output = new FileOutputStream(channelFilePath);

            writeXml(document,output);
            output.flush();

//            TransformerFactory transformerFactory = TransformerFactory.newInstance();
//            Transformer transformer = transformerFactory.newTransformer();
//            DOMSource domSource = new DOMSource(document);
//            StreamResult streamResult = new StreamResult(new File(channelFilePath));
//            streamResult.getOutputStream().flush();
//
//            transformer.transform(domSource, streamResult);
//            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//
//            log.info("Done creating channel")
        }
        catch (ParserConfigurationException | TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    private void createMUX(Destination destination) throws IOException {
        String channelFilePath = System.getProperty("user.dir")+File.separator+"deploy"+File.separator+"cfg"+File.separator+"20_"+ destination.getDestinationName()+"_mux.xml";
        Path path = Paths.get(channelFilePath);
        if (Files.exists(path)){
            Files.delete(path);
        }
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element mux = document.createElement("mux");
            document.appendChild(mux);
            Attr muxName = document.createAttribute("name");
            muxName.setValue(destination.getDestinationName());
            mux.setAttributeNode(muxName);

            Attr muxClass = document.createAttribute("class");
            muxClass.setValue("org.jpos.q2.iso.QMUX");
            mux.setAttributeNode(muxClass);

            Attr realm = document.createAttribute("realm");
            realm.setValue(destination.getDestinationName()+"-mux");
            mux.setAttributeNode(realm);

            Attr muxLogger = document.createAttribute("logger");
            muxLogger.setValue("Q2");
            mux.setAttributeNode(muxLogger);

            Element space = document.createElement("space");
            space.appendChild(document.createTextNode(destination.getDestinationName()));
            mux.appendChild(space);

            Element muxIn = document.createElement("in");
            muxIn.appendChild(document.createTextNode(destination.getDestinationName()+".out"));
            mux.appendChild(muxIn);

            Element muxOut = document.createElement("out");
            muxOut.appendChild(document.createTextNode(destination.getDestinationName()+".in"));
            mux.appendChild(muxOut);

            Element muxReady = document.createElement("ready");
            muxReady.appendChild(document.createTextNode(destination.getDestinationName()+"-channel.ready"));
            mux.appendChild(muxReady);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(channelFilePath));

            transformer.transform(domSource, streamResult);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            log.info("Done creating mux");

        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    private static void createLogonManager(Destination destination) throws IOException {
        String channelFilePath = System.getProperty("user.dir")+ File.separator+"deploy"+File.separator+"cfg"+File.separator+"50_"+ destination.getDestinationName()+"_logon_manager.xml";
        Path path = Paths.get(channelFilePath);
        if (Files.exists(path)){
            Files.delete(path);
        }
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element channelAdapter = document.createElement(destination.getDestinationName()+"-logon-manager");
            document.appendChild(channelAdapter);

            Attr name = document.createAttribute("name");
            name.setValue(destination.getDestinationName()+"-logon-manager");
            channelAdapter.setAttributeNode(name);


            Attr logger = document.createAttribute("logger");
            logger.setValue("Q2");
            channelAdapter.setAttributeNode(logger);

            Attr channelclass = document.createAttribute("class");
            channelclass.setValue("logonmanagers.com.vero.coreprocessor.LogonManager");
            channelAdapter.setAttributeNode(channelclass);

            Attr channellogger = document.createAttribute("logger");
            channellogger.setValue("Q2");
            channelAdapter.setAttributeNode(channellogger);

            Attr channelrealm = document.createAttribute("realm");
            channelrealm.setValue(destination.getDestinationName()+"-logon-manager");
            channelAdapter.setAttributeNode(channelrealm);



            Element property1 = document.createElement("property");
            Attr property1name = document.createAttribute("name");
            property1name.setValue("space");
            property1.setAttributeNode(property1name);

            Attr property1value = document.createAttribute("value");
            property1value.setValue(destination.getDestinationName());
            property1.setAttributeNode(property1value);

            channelAdapter.appendChild(property1);

            Element property2 = document.createElement("property");
            Attr property2name = document.createAttribute("name");
            property2name.setValue("mux");
            property2.setAttributeNode(property2name);

            Attr property2value = document.createAttribute("value");
            property2value.setValue(destination.getDestinationName());
            property2.setAttributeNode(property2value);

            channelAdapter.appendChild(property2);

            Element property3 = document.createElement("property");
            Attr property3name = document.createAttribute("name");
            property3name.setValue("timeout");
            property3.setAttributeNode(property3name);

            Attr property3value = document.createAttribute("value");
            property3value.setValue("30000");
            property3.setAttributeNode(property3value);

            channelAdapter.appendChild(property3);

            Element property4 = document.createElement("property");
            Attr property4name = document.createAttribute("name");
            property4name.setValue("echo-interval");
            property4.setAttributeNode(property4name);

            Attr property4value = document.createAttribute("value");
            property4value.setValue("120000");
            property4.setAttributeNode(property4value);

            channelAdapter.appendChild(property4);

            Element property5 = document.createElement("property");
            Attr property5name = document.createAttribute("name");
            property5name.setValue("logon-interval");
            property5.setAttributeNode(property5name);

            Attr property5value = document.createAttribute("value");
            property5value.setValue("3600000");
            property5.setAttributeNode(property5value);

            channelAdapter.appendChild(property5);

            Element property6 = document.createElement("property");
            Attr property6name = document.createAttribute("name");
            property6name.setValue("channel-ready");
            property6.setAttributeNode(property6name);

            Attr property6value = document.createAttribute("value");
            property6value.setValue(destination.getDestinationName()+"-channel.ready");
            property6.setAttributeNode(property6value);

            channelAdapter.appendChild(property6);


            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(channelFilePath));

            transformer.transform(domSource, streamResult);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            log.info("Done");


        }
        catch (ParserConfigurationException | TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeXml(Document doc,
                                 OutputStream output)
            throws TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);

        transformer.transform(source, result);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        log.info("Done");

    }


}
