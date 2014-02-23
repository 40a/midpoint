/*
 * Copyright (c) 2010-2013 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.schema;

import static com.evolveum.midpoint.prism.util.PrismTestUtil.*;
import static org.testng.AssertJUnit.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.namespace.QName;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import com.evolveum.midpoint.prism.PrismPropertyValue;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.AndFilter;
import com.evolveum.midpoint.prism.query.EqualsFilter;
import com.evolveum.midpoint.prism.query.LogicalFilter;
import com.evolveum.midpoint.prism.query.NaryLogicalFilter;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.ObjectPaging;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.OrFilter;
import com.evolveum.midpoint.prism.query.RefFilter;
import com.evolveum.midpoint.schema.constants.MidPointConstants;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ConnectorType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.FailedOperationTypeType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.GenericObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ShadowType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.UserType;
import com.evolveum.prism.xml.ns._public.query_2.PagingType;
import com.evolveum.prism.xml.ns._public.query_2.QueryType;
import com.evolveum.prism.xml.ns._public.types_2.PolyStringType;

public class TestQueryConvertor {

	private static final Trace LOGGER = TraceManager.getTrace(TestQueryConvertor.class);

	private static final File TEST_DIR = new File("src/test/resources/queryconvertor");

	private static final String NS_EXTENSION = "http://midpoint.evolveum.com/xml/ns/test/extension";
	private static final String NS_ICFS = "http://midpoint.evolveum.com/xml/ns/public/connector/icf-1/resource-schema-2";
	private static final String NS_BLA = "http://midpoint.evolveum.com/blabla";

	private static final QName intExtensionDefinition = new QName(NS_EXTENSION, "intType");
	private static final QName stringExtensionDefinition = new QName(NS_EXTENSION, "stringType");
	private static final QName fooBlaDefinition = new QName(NS_BLA, "foo");
	private static final QName ICF_NAME = new QName(NS_ICFS, "name");

	private static final QName FAILED_OPERATION_TYPE_QNAME = new QName(SchemaConstantsGenerated.NS_COMMON,
			"FailedOperationTypeType");

	private static final File QUERY_AND_GENERIC = new File(TEST_DIR + "/query-and-generic.xml");
	private static final File QUERY_ACCOUNT = new File(TEST_DIR + "/query.xml");
	private static final File QUERY_ACCOUNT_ATTRIBUTES_RESOURCE_REF = new File(TEST_DIR
			+ "/query-account-by-attributes-and-resource-ref.xml");
	private static final File QUERY_OR_COMPOSITE = new File(TEST_DIR + "/query-or-composite.xml");

	@BeforeSuite
	public void setup() throws SchemaException, SAXException, IOException {
		PrettyPrinter.setDefaultNamespacePrefix(MidPointConstants.NS_MIDPOINT_PUBLIC_PREFIX);
		resetPrismContext(MidPointPrismContextFactory.FACTORY);
	}

	@Test
	public void testAccountQuery() throws Exception {
		displayTestTitle("testAccountQuery");

		QueryType queryType = unmarshalQuery(QUERY_ACCOUNT);
		ObjectQuery query = toObjectQuery(ShadowType.class, queryType);
		displayQuery(query);

		assertNotNull(query);

		ObjectFilter filter = query.getFilter();
		assertAndFilter(filter, 2);

		ObjectFilter first = getFilterCondition(filter, 0);
		assertEqualsFilter(first, ShadowType.F_FAILED_OPERATION_TYPE, FAILED_OPERATION_TYPE_QNAME,
				new ItemPath(ShadowType.F_FAILED_OPERATION_TYPE));
		assertEqualsFilterValue((EqualsFilter) first, FailedOperationTypeType.ADD);

		ObjectFilter second = getFilterCondition(filter, 1);
		assertEqualsFilter(second, ShadowType.F_NAME, PolyStringType.COMPLEX_TYPE, new ItemPath(
				ShadowType.F_NAME));
		assertEqualsFilterValue((EqualsFilter) second, createPolyString("someName"));

		QueryType convertedQueryType = toQueryType(query);
		LOGGER.info(DOMUtil.serializeDOMToString(convertedQueryType.getFilter()));

		// TODO: add some asserts
	}

	@Test
	public void testAccountQueryAttributesAndResource() throws Exception {
		displayTestTitle("testAccountQueryAttributesAndResource");

		QueryType queryType = unmarshalQuery(QUERY_ACCOUNT_ATTRIBUTES_RESOURCE_REF);
		ObjectQuery query = toObjectQuery(ShadowType.class, queryType);
		displayQuery(query);

		assertNotNull(query);

		ObjectFilter filter = query.getFilter();
		assertAndFilter(filter, 2);

		ObjectFilter first = getFilterCondition(filter, 0);
		assertRefFilter(first, ShadowType.F_RESOURCE_REF, ObjectReferenceType.COMPLEX_TYPE, new ItemPath(
				ShadowType.F_RESOURCE_REF));
		assertRefFilterValue((RefFilter) first, "aae7be60-df56-11df-8608-0002a5d5c51b");

		ObjectFilter second = getFilterCondition(filter, 1);
		assertEqualsFilter(second, ICF_NAME, DOMUtil.XSD_STRING, new ItemPath(ShadowType.F_ATTRIBUTES,
				ICF_NAME));
		assertEqualsFilterValue((EqualsFilter) second, "uid=jbond,ou=People,dc=example,dc=com");

		QueryType convertedQueryType = toQueryType(query);
		LOGGER.info(DOMUtil.serializeDOMToString(convertedQueryType.getFilter()));

		// TODO: add some asserts
	}

	@Test
	public void testAccountQueryCompositeOr() throws Exception {
		displayTestTitle("testAccountQueryCompositeOr");

		QueryType queryType = unmarshalQuery(QUERY_OR_COMPOSITE);
		ObjectQuery query = toObjectQuery(ShadowType.class, queryType);
		displayQuery(query);

		assertNotNull(query);

		ObjectFilter filter = query.getFilter();
		assertOrFilter(filter, 4);

		ObjectFilter first = getFilterCondition(filter, 0);
		assertEqualsFilter(first, ShadowType.F_INTENT, DOMUtil.XSD_STRING, new ItemPath(ShadowType.F_INTENT));
		assertEqualsFilterValue((EqualsFilter) first, "some account type");

		ObjectFilter second = getFilterCondition(filter, 1);
		assertEqualsFilter(second, fooBlaDefinition, DOMUtil.XSD_STRING, new ItemPath(
				ShadowType.F_ATTRIBUTES, fooBlaDefinition));
		assertEqualsFilterValue((EqualsFilter) second, "foo value");

		ObjectFilter third = getFilterCondition(filter, 2);
		assertEqualsFilter(third, stringExtensionDefinition, DOMUtil.XSD_STRING, new ItemPath(
				ShadowType.F_EXTENSION, stringExtensionDefinition));
		assertEqualsFilterValue((EqualsFilter) third, "uid=test,dc=example,dc=com");

		ObjectFilter forth = getFilterCondition(filter, 3);
		assertRefFilter(forth, ShadowType.F_RESOURCE_REF, ObjectReferenceType.COMPLEX_TYPE, new ItemPath(
				ShadowType.F_RESOURCE_REF));
		assertRefFilterValue((RefFilter) forth, "d0db5be9-cb93-401f-b6c1-86ffffe4cd5e");

		QueryType convertedQueryType = toQueryType(query);
		LOGGER.info(DOMUtil.serializeDOMToString(convertedQueryType.getFilter()));

		// TODO: add some asserts
	}

	@Test
	public void testConnectorQuery() throws Exception {
		displayTestTitle("testConnectorQuery");
		File connectorQueryToTest = new File(TEST_DIR + "/query-connector-by-type.xml");
		QueryType queryType = getJaxbUtil().unmarshalObject(connectorQueryToTest, QueryType.class);
		ObjectQuery query = null;
		try {
			query = QueryJaxbConvertor.createObjectQuery(ConnectorType.class, queryType, getPrismContext());
			displayQuery(query);

			assertNotNull(query);
			ObjectFilter filter = query.getFilter();
			assertEqualsFilter(query.getFilter(), ConnectorType.F_CONNECTOR_TYPE, DOMUtil.XSD_STRING,
					new ItemPath(ConnectorType.F_CONNECTOR_TYPE));
			assertEqualsFilterValue((EqualsFilter) filter, "org.identityconnectors.ldap.LdapConnector");

			QueryType convertedQueryType = toQueryType(query);
			displayQueryType(convertedQueryType);
		} catch (SchemaException ex) {
			LOGGER.error("Error while converting query: {}", ex.getMessage(), ex);
			throw ex;
		} catch (RuntimeException ex) {
			LOGGER.error("Error while converting query: {}", ex.getMessage(), ex);
			throw ex;
		} catch (Exception ex) {
			LOGGER.error("Error while converting query: {}", ex.getMessage(), ex);
			throw ex;
		}

	}

	private void assertOrFilter(ObjectFilter filter, int conditions) {
		assertEquals(OrFilter.class, filter.getClass());
		assertEquals(conditions, ((OrFilter) filter).getCondition().size());
	}

	private void assertAndFilter(ObjectFilter filter, int conditions) {
		assertEquals(AndFilter.class, filter.getClass());
		assertEquals(conditions, ((AndFilter) filter).getCondition().size());
	}

	private ObjectFilter getFilterCondition(ObjectFilter filter, int index) {
		if (!(filter instanceof NaryLogicalFilter)) {
			fail("Filter not an instance of n-ary logical filter.");
		}
		return ((LogicalFilter) filter).getCondition().get(index);
	}

	private void assertEqualsFilter(ObjectFilter objectFilter, QName expectedFilterDef,
			QName expectedTypeName, ItemPath path) {
		assertEquals(EqualsFilter.class, objectFilter.getClass());
		EqualsFilter filter = (EqualsFilter) objectFilter;
		assertEquals(expectedFilterDef, filter.getDefinition().getName());
		assertEquals(expectedTypeName, filter.getDefinition().getTypeName());
		assertEquals(path, filter.getFullPath());
	}

	private void assertRefFilter(ObjectFilter objectFilter, QName expectedFilterDef, QName expectedTypeName,
			ItemPath path) {
		assertEquals(RefFilter.class, objectFilter.getClass());
		RefFilter filter = (RefFilter) objectFilter;
		assertEquals(expectedFilterDef, filter.getDefinition().getName());
		assertEquals(expectedTypeName, filter.getDefinition().getTypeName());
		assertEquals(path, filter.getFullPath());
	}

	private void assertRefFilterValue(RefFilter filter, String oid) {
		List<? extends PrismValue> values = filter.getValues();
		assertEquals(1, values.size());
		assertEquals(PrismReferenceValue.class, values.get(0).getClass());
		PrismReferenceValue val = (PrismReferenceValue) values.get(0);

		assertEquals(oid, val.getOid());
		// TODO: maybe some more asserts for type, relation and other reference
		// values
	}

	private <T> void assertEqualsFilterValue(EqualsFilter filter, T value) {
		List<? extends PrismValue> values = filter.getValues();
		assertEquals(1, values.size());
		assertEquals(PrismPropertyValue.class, values.get(0).getClass());
		PrismPropertyValue val = (PrismPropertyValue) values.get(0);
		assertEquals(value, val.getValue());
	}

	private ObjectQuery toObjectQuery(Class type, QueryType queryType) throws Exception {
		ObjectQuery query = QueryJaxbConvertor.createObjectQuery(type, queryType,
				getPrismContext());
		return query;
	}

	private QueryType unmarshalQuery(File file) throws Exception {
		return getJaxbUtil().unmarshalObject(file, QueryType.class);
	}

	private QueryType toQueryType(ObjectQuery query) throws Exception {
		return QueryJaxbConvertor.createQueryType(query, getPrismContext());
	}

	private void displayQuery(ObjectQuery query) {
		LOGGER.trace("object query: ");
		LOGGER.trace("QUERY DUMP: {}", query.debugDump());
		LOGGER.info("QUERY Pretty print: {}", query.toString());
		System.out.println("QUERY Pretty print: " + query.toString());
	}

	private void displayQueryType(QueryType queryType) {
		LOGGER.info(DOMUtil.serializeDOMToString(queryType.getFilter()));
	}

	@Test
	public void testGenericQuery() throws Exception {
		displayTestTitle("testGenericQuery");
		LOGGER.info("===[ testGenericQuery ]===");

		QueryType queryType = unmarshalQuery(QUERY_AND_GENERIC);
		ObjectQuery query = toObjectQuery(GenericObjectType.class, queryType);

		displayQuery(query);

		// check parent filter
		assertNotNull(query);
		ObjectFilter filter = query.getFilter();
		assertAndFilter(filter, 2);
		// check first condition
		ObjectFilter first = getFilterCondition(filter, 0);
		assertEqualsFilter(first, GenericObjectType.F_NAME, PolyStringType.COMPLEX_TYPE, new ItemPath(
				GenericObjectType.F_NAME));
		assertEqualsFilterValue((EqualsFilter) first, createPolyString("generic object"));
		// check second condition
		ObjectFilter second = getFilterCondition(filter, 1);
		assertEqualsFilter(second, intExtensionDefinition, DOMUtil.XSD_INT, new ItemPath(
				ObjectType.F_EXTENSION, new QName(NS_EXTENSION, "intType")));
		assertEqualsFilterValue((EqualsFilter) second, 123);

		QueryType convertedQueryType = toQueryType(query);
		assertNotNull("Re-serialized query is null ", convertedQueryType);
		assertNotNull("Filter in re-serialized query must not be null.", convertedQueryType.getFilter());

		displayQueryType(convertedQueryType);
	}

	@Test
	public void testUserQuery() throws Exception {
		LOGGER.info("===[ testUserQuery ]===");
		File[] userQueriesToTest = new File[] { new File(TEST_DIR + "/query-user-by-fullName.xml"),
				new File(TEST_DIR + "/query-user-by-name.xml"),
				new File(TEST_DIR + "/query-user-substring-fullName.xml") };
		// prismContext.silentMarshalObject(queryTypeNew, LOGGER);
		for (File file : userQueriesToTest) {
			QueryType queryType = getJaxbUtil().unmarshalObject(file, QueryType.class);
			LOGGER.info("===[ query type parsed ]===");
			ObjectQuery query = null;
			try {
				query = QueryJaxbConvertor.createObjectQuery(UserType.class, queryType, getPrismContext());
				LOGGER.info("query converted: ");

				LOGGER.info("QUERY DUMP: {}", query.debugDump());
				LOGGER.info("QUERY Pretty print: {}", query.toString());
				System.out.println("QUERY Pretty print: " + query.toString());

				QueryType convertedQueryType = QueryJaxbConvertor.createQueryType(query, getPrismContext());
				LOGGER.info(DOMUtil.serializeDOMToString(convertedQueryType.getFilter()));
			} catch (SchemaException ex) {
				LOGGER.error("Error while converting query: {}", ex.getMessage(), ex);
				throw ex;
			} catch (RuntimeException ex) {
				LOGGER.error("Error while converting query: {}", ex.getMessage(), ex);
				throw ex;
			} catch (Exception ex) {
				LOGGER.error("Error while converting query: {}", ex.getMessage(), ex);
				throw ex;
			}
		}
	}

	@Test
	public void testConvertQueryNullFilter() throws Exception {
		ObjectQuery query = ObjectQuery.createObjectQuery(ObjectPaging.createPaging(0, 10));
		QueryType queryType = QueryJaxbConvertor.createQueryType(query, getPrismContext());

		assertNotNull(queryType);
		assertNull(queryType.getFilter());
		PagingType paging = queryType.getPaging();
		assertNotNull(paging);
		assertEquals(new Integer(0), paging.getOffset());
		assertEquals(new Integer(10), paging.getMaxSize());

	}
}
