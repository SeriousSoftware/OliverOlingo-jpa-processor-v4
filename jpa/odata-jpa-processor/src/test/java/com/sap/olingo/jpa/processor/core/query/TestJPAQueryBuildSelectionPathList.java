package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.UriResourceValue;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPADefaultEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataContextAccessDouble;
import com.sap.olingo.jpa.processor.core.exception.JPAIllicalAccessException;
import com.sap.olingo.jpa.processor.core.processor.JPAODataRequestContextImpl;
import com.sap.olingo.jpa.processor.core.util.SelectOptionDouble;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;
import com.sap.olingo.jpa.processor.core.util.UriInfoDouble;

public class TestJPAQueryBuildSelectionPathList extends TestBase {

  private JPAAbstractJoinQuery cut;
  private JPAODataCRUDContextAccess sessionContext;
  private UriInfo uriInfo;
  private JPAODataRequestContextImpl requestContext;

  @BeforeEach
  public void setup() throws ODataException, JPAIllicalAccessException {
    buildUriInfo("BusinessPartners", "BusinessPartner");

    helper = new TestHelper(emf, PUNIT_NAME);
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
    createHeaders();
    sessionContext = new JPAODataContextAccessDouble(new JPAEdmProvider(PUNIT_NAME, emf, null, TestBase.enumPackages),
        ds,
        null);
    requestContext = new JPAODataRequestContextImpl();
    requestContext.setEntityManager(emf.createEntityManager());
    requestContext.setUriInfo(uriInfo);
    cut = new JPAJoinQuery(null, sessionContext, headers, requestContext);

  }

  private List<UriResource> buildUriInfo(final String esName, final String etName) {
    uriInfo = mock(UriInfo.class);
    final EdmEntitySet odataEs = mock(EdmEntitySet.class);
    final EdmType odataType = mock(EdmEntityType.class);
    final List<UriResource> resources = new ArrayList<>();
    final UriResourceEntitySet esResource = mock(UriResourceEntitySet.class);
    when(uriInfo.getUriResourceParts()).thenReturn(resources);
    when(esResource.getKeyPredicates()).thenReturn(new ArrayList<>(0));
    when(esResource.getEntitySet()).thenReturn(odataEs);
    when(esResource.getKind()).thenReturn(UriResourceKind.entitySet);
    when(esResource.getType()).thenReturn(odataType);
    when(odataEs.getName()).thenReturn(esName);
    when(odataType.getNamespace()).thenReturn(PUNIT_NAME);
    when(odataType.getName()).thenReturn(etName);
    resources.add(esResource);
    return resources;
  }

  @Test
  public void checkSelectAllAsNoSelectionGiven() throws ODataApplicationException {
    final Collection<JPAPath> act = cut.buildSelectionPathList(uriInfo);
    assertEquals(23, act.size());
  }

  @Test
  public void checkSelectAllAsStarGiven() throws ODataApplicationException {

    final Collection<JPAPath> act = cut.buildSelectionPathList(new UriInfoDouble(new SelectOptionDouble("*")));
    assertEquals(23, act.size());
  }

  @Test
  public void checkSelectPrimitiveWithKey() throws ODataApplicationException {
    final Collection<JPAPath> act = cut.buildSelectionPathList(new UriInfoDouble(new SelectOptionDouble("Country")));
    assertEquals(3, act.size());
  }

  @Test
  public void checkSelectAllFromComplexWithKey() throws ODataApplicationException {
    final Collection<JPAPath> act = cut.buildSelectionPathList(new UriInfoDouble(new SelectOptionDouble("Address")));
    assertEquals(11, act.size());
  }

  @Test
  public void checkSelectKeyNoDuplicates() throws ODataApplicationException {
    final Collection<JPAPath> act = cut.buildSelectionPathList(new UriInfoDouble(new SelectOptionDouble("ID")));
    assertEquals(2, act.size());
  }

  @Test
  public void checkSelectAllFromNavigationComplexPrimitiveWithKey() throws ODataApplicationException {
    final Collection<JPAPath> act = cut.buildSelectionPathList(new UriInfoDouble(new SelectOptionDouble(
        "Address/CountryName")));
    assertEquals(3, act.size());
  }

  @Test
  public void checkSelectTwoPrimitiveWithKey() throws ODataApplicationException {
    final Collection<JPAPath> act = cut.buildSelectionPathList(new UriInfoDouble(new SelectOptionDouble(
        "Country,ETag")));
    assertEquals(3, act.size());
  }

  @Test
  public void checkSelectAllFromComplexAndOnePrimitiveWithKey() throws ODataApplicationException {
    final Collection<JPAPath> act = cut.buildSelectionPathList(new UriInfoDouble(new SelectOptionDouble(
        "Address,ETag")));
    assertEquals(11, act.size());
  }

  @Test
  public void checkSelectAllFromNavgateComplexPrimitiveAndOnePrimitiveWithKey() throws ODataApplicationException {
    final Collection<JPAPath> act = cut.buildSelectionPathList(new UriInfoDouble(new SelectOptionDouble(
        "Address/CountryName,Country")));
    assertEquals(4, act.size());
  }

  @Test
  public void checkSelectNavigationCompex() throws ODataException {
    List<UriResource> resourcePath = buildUriInfo("BusinessPartners", "BusinessPartner");
    final UriResourceComplexProperty complexResource = mock(UriResourceComplexProperty.class);
    final EdmProperty property = mock(EdmProperty.class);
    when(complexResource.getProperty()).thenReturn(property);
    when(property.getName()).thenReturn("AdministrativeInformation");
    resourcePath.add(complexResource);

    final Collection<JPAPath> act = cut.buildSelectionPathList(uriInfo);
    assertEquals(6, act.size());
  }

  @Test
  public void checkSelectNavigationCompexComplex() throws ODataException {
    List<UriResource> resourcePath = buildUriInfo("BusinessPartners", "BusinessPartner");
    final UriResourceComplexProperty adminInfoResource = mock(UriResourceComplexProperty.class);
    final EdmProperty adminInfoProperty = mock(EdmProperty.class);
    when(adminInfoResource.getProperty()).thenReturn(adminInfoProperty);
    when(adminInfoProperty.getName()).thenReturn("AdministrativeInformation");
    resourcePath.add(adminInfoResource);

    final UriResourceComplexProperty createdResource = mock(UriResourceComplexProperty.class);
    final EdmProperty createdProperty = mock(EdmProperty.class);
    when(createdResource.getProperty()).thenReturn(createdProperty);
    when(createdProperty.getName()).thenReturn("Created");
    resourcePath.add(createdResource);

    final Collection<JPAPath> act = cut.buildSelectionPathList(uriInfo);
    assertEquals(4, act.size());
  }

  @Test
  public void checkSelectNavigationCompexComplexProperty() throws ODataException {
    List<UriResource> resourcePath = buildUriInfo("BusinessPartners", "BusinessPartner");
    final UriResourceComplexProperty adminInfoResource = mock(UriResourceComplexProperty.class);
    final EdmProperty adminInfoProperty = mock(EdmProperty.class);
    when(adminInfoResource.getProperty()).thenReturn(adminInfoProperty);
    when(adminInfoProperty.getName()).thenReturn("AdministrativeInformation");
    resourcePath.add(adminInfoResource);

    final UriResourceComplexProperty createdResource = mock(UriResourceComplexProperty.class);
    final EdmProperty createdProperty = mock(EdmProperty.class);
    when(createdResource.getProperty()).thenReturn(createdProperty);
    when(createdProperty.getName()).thenReturn("Created");
    resourcePath.add(createdResource);

    final UriResourcePrimitiveProperty byResource = mock(UriResourcePrimitiveProperty.class);
    final EdmProperty byProperty = mock(EdmProperty.class);
    when(byResource.getProperty()).thenReturn(byProperty);
    when(byProperty.getName()).thenReturn("By");
    resourcePath.add(byResource);

    final Collection<JPAPath> act = cut.buildSelectionPathList(uriInfo);
    assertEquals(3, act.size());
  }

  @Test
  public void checkSelectNavigationPropertyValue() throws ODataException {
    List<UriResource> resourcePath = buildUriInfo("BusinessPartners", "BusinessPartner");

    final UriResourcePrimitiveProperty byResource = mock(UriResourcePrimitiveProperty.class);
    final EdmProperty byProperty = mock(EdmProperty.class);
    when(byResource.getProperty()).thenReturn(byProperty);
    when(byProperty.getName()).thenReturn("Country");
    resourcePath.add(byResource);

    final UriResourceValue valueResource = mock(UriResourceValue.class);
    when(valueResource.getSegmentValue()).thenReturn(Util.VALUE_RESOURCE.toLowerCase());
    resourcePath.add(valueResource);

    final Collection<JPAPath> act = cut.buildSelectionPathList(uriInfo);
    assertEquals(3, act.size());
  }

  @Test
  public void checkSelectNavigationCompexWithSelectPrimitive() throws ODataException {
    List<UriResource> resourcePath = buildUriInfo("BusinessPartners", "BusinessPartner");
    final UriResourceComplexProperty addressResource = mock(UriResourceComplexProperty.class);
    final EdmProperty addressProperty = mock(EdmProperty.class);
    when(addressResource.getProperty()).thenReturn(addressProperty);
    when(addressProperty.getName()).thenReturn("Address");
    resourcePath.add(addressResource);

    SelectOption selOptions = new SelectOptionDouble("CountryName");

    when(uriInfo.getSelectOption()).thenReturn(selOptions);

    final Collection<JPAPath> act = cut.buildSelectionPathList(uriInfo);
    assertEquals(3, act.size());
  }

  @Test
  public void checkSelectContainesVersionEvenSoIgnored() throws ODataApplicationException, ODataJPAModelException {
    final List<UriResource> resourcePath = buildUriInfo("BusinessPartnerProtecteds", "BusinessPartnerProtected");
    final UriResourcePrimitiveProperty byResource = mock(UriResourcePrimitiveProperty.class);
    final EdmProperty byProperty = mock(EdmProperty.class);
    when(byResource.getProperty()).thenReturn(byProperty);
    when(byProperty.getName()).thenReturn("ID");
    resourcePath.add(byResource);

    final UriResourceValue valueResource = mock(UriResourceValue.class);
    when(valueResource.getSegmentValue()).thenReturn(Util.VALUE_RESOURCE.toLowerCase());
    resourcePath.add(valueResource);
    final Collection<JPAPath> act = cut.buildSelectionPathList(uriInfo);
    assertEquals(2, act.size());
    for (final JPAPath actElement : act) {
      if ("ETag".equals(actElement.getLeaf().getExternalName()))
        return;
    }
    fail("ETag not found");
  }
}
