package xml;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;


/**
 * <p>Java class for anonymous complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="price" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * <p>
 */
@SuppressWarnings("unused")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "item")
public class Item {

    @Nonnull
    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "price")
    @XmlSchemaType(name = "positiveInteger")
    private int price;

    Item() {
        name = "";
        price = 0;
        checkRep();
    }

    private void checkRep() {
        assert price >= 0;
    }

    /**
     * constructor for tests
     *
     * @param name  item's name
     * @param price item's price
     */
    public Item(@Nonnull String name, int price) {
        Objects.requireNonNull(name);

        this.name = name;
        this.price = price;
        checkRep();
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String}
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String}
     */
    public void setName(@Nonnull String value) {
        Objects.requireNonNull(value);
        this.name = value;
        checkRep();
    }

    /**
     * Gets the value of the price property.
     *
     * @return price
     */
    public int getPrice() {
        return price;
    }

    /**
     * Sets the value of the price property.
     *
     * @param value of price
     */
    public void setPrice(int value) {
        Objects.requireNonNull(value);
        this.price = value;
        checkRep();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Item item = (Item) o;
        return Objects.equals(name, item.name) &&
                Objects.equals(price, item.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price);
    }

    @Override
    public String toString() {
        return "Item{" +
                "name='" + name + '\'' +
                ", price=" + price +
                '}';
    }

}
