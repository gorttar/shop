package helpers.xml;

import static java.util.Arrays.asList;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import xml.Item;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2017-02-27)
 */
public class ItemsReaderTest {

    @Test
    public void testReadItems_correct() throws Exception {
        Assert.assertEquals(
                ItemsReader.readItems("helpers/correct_items.xml"),
                asList(new Item("item1", 1), new Item("item2", 2)));
    }

    @DataProvider(name = "testReadItems_incorrect")
    private Object[][] data4testReadItems_incorrect() {
        return new Object[][]{
                {"absent.xml"},
                {"helpers/duplicate_items.xml"},
                {"helpers/absent_attr_items.xml"},
                {"helpers/wrong_attr_items.xml"},
                {"helpers/wrong_tag_items.xml"},
        };
    }

    @Test(dataProvider = "testReadItems_incorrect", expectedExceptions = IllegalArgumentException.class)
    public void testReadItems_incorrect(String resourceName) throws Exception {
        ItemsReader.readItems(resourceName);
    }
}