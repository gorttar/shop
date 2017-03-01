/*
 * Copyright (c) 2017 Andrey Antipov. All Rights Reserved.
 */
package helpers.xml;

import org.xml.sax.SAXException;
import xml.Item;
import xml.Items;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2017-02-27)
 */
public final class ItemsReader {
    private static final String SCHEMA_NAME = "items.xsd";

    private static final ClassLoader CLASS_LOADER = ItemsReader.class.getClassLoader();

    private static final Schema SCHEMA;

    private static final Unmarshaller UNMARSHALLER;

    static {
        try {
            SCHEMA = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    .newSchema(
                            new File(
                                    Optional
                                            .ofNullable(CLASS_LOADER.getResource(SCHEMA_NAME))
                                            .map(URL::getFile)
                                            .orElseThrow(() -> new IllegalStateException("Can't get resource " + SCHEMA_NAME))));
        } catch (SAXException e) {
            throw new IllegalStateException("Can't parse schema from resource " + SCHEMA_NAME, e);
        }

        try {
            UNMARSHALLER = JAXBContext.newInstance(Items.class).createUnmarshaller();
            UNMARSHALLER.setSchema(SCHEMA);
            UNMARSHALLER.setEventHandler(
                    event -> {
                        throw new IllegalStateException("Failed to read items from items.xml", event.getLinkedException());
                    });
        } catch (JAXBException e) {
            throw new IllegalStateException("Can't initialise unmarshaller", e);
        }
    }

    private ItemsReader() {
    }

    private static Items unmarshal(InputStream inputStream) throws JAXBException {
        return (Items) UNMARSHALLER.unmarshal(inputStream);
    }

    public static List<Item> readItems(String resourceName) {
        try {
            return ((Items) UNMARSHALLER.unmarshal(CLASS_LOADER.getResourceAsStream(resourceName))).getItem();
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't parse xml resource " + resourceName, e);
        }
    }

}