package org.apache.olingo.jpa.processor.core.testmodel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

public class ImageLoader {
  private static final String SELECT_IMAGE =
      "SELECT * FROM \"OLINGO\".\"org.apache.olingo.jpa::BusinessPartnerImage\" WHERE ID = '$&1'";
  private static final String PATH = "images/";
  private static final String ENTITY_MANAGER_DATA_SOURCE = "javax.persistence.nonJtaDataSource";
  private static final String PUNIT_NAME = "org.apache.olingo.jpa";

  public static void main(String[] args) throws Exception {
    ImageLoader i = new ImageLoader();
    i.load("OlingoOrangeTM.png", "99");
  }

  public void load(String imageName, String businessPartnerID) {
    byte[] image = loadImage(imageName);
    storeImage(image, businessPartnerID);
  }

  private void storeImage(byte[] image, String businessPartnerID) {
    final Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(ENTITY_MANAGER_DATA_SOURCE, DataSourceHelper.createDataSource(DataSourceHelper.DB_H2));
    final EntityManagerFactory emf = Persistence.createEntityManagerFactory(PUNIT_NAME, properties);
    EntityManager em = emf.createEntityManager();
    String s = SELECT_IMAGE.replace("$&1", businessPartnerID);
    Query q = em.createNativeQuery(s, BusinessPartnerImage.class);
    List<BusinessPartnerImage> result = q.getResultList();
    result.get(0).setImage(image);
    em.getTransaction().begin();
    em.persist(result.get(0));
    em.getTransaction().commit();
  }

  private byte[] loadImage(String imageName) {
    String path = PATH + imageName;
    InputStream i = null;
    byte[] image = null;
    URL u = this.getClass().getClassLoader().getResource(path);
    try {
      i = u.openStream();
      image = new byte[i.available()];
      i.read(image);
    } catch (IOException e1) {
      e1.printStackTrace();
    } finally {
      try {
        i.close();
        return image;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }
}