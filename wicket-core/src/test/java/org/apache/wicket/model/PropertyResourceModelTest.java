/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.collections.MiniMap;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for the {@link PropertyResourceModel}.
 * 
 * @author Chris Turner
 * @author svenmeier
 */
public class PropertyResourceModelTest extends WicketTestCase
{
	private WebPage page;

	private WeatherStation ws;

	private IModel<WeatherStation> wsModel;

	/**
	 * @throws Exception
	 */
	@Before
	public void before() throws Exception
	{
		page = new TestPage();
		ws = new WeatherStation();
		wsModel = new Model<WeatherStation>(ws);
	}


	/** */
	@Test
	public void getSimpleResource()
	{
		PropertyResourceModel model = new PropertyResourceModel("simple.text", page, null);
		assertEquals("Text should be as expected", "Simple text", model.getString());
		assertEquals("Text should be as expected", "Simple text", model.getObject());
	}

	/** */
	@Test
	public void getWrappedOnAssignmentResource()
	{
		Label label1 = new Label("resourceModelWithComponent", new PropertyResourceModel(
			"wrappedOnAssignment.text", page, null));
		page.add(label1);
		assertEquals("Text should be as expected", "Non-wrapped text",
			label1.getDefaultModelObject());

		Label label2 = new Label("resourceModelWithoutComponent", new PropertyResourceModel(
			"wrappedOnAssignment.text", (Component)null, null));
		page.add(label2);
		assertEquals("Text should be as expected", "Wrapped text",
			label2.getDefaultModelObject());
	}

	/** */
	@Test(expected = IllegalArgumentException.class)
	public void nullResourceKey()
	{
		new PropertyResourceModel(null, page, null);
	}

	/** */
	@Test
	public void getSimpleResourceWithKeySubstitution()
	{
		PropertyResourceModel model = new PropertyResourceModel("weather.${currentStatus}", page,
			wsModel);
		assertEquals("Text should be as expected", "It's sunny, wear sunscreen",
			model.getString());
		ws.setCurrentStatus("raining");
		assertEquals("Text should be as expected", "It's raining, take an umbrella",
			model.getString());
	}

	/** */
	@Test
	public void getSimpleResourceWithKeySubstitutionForNonString()
	{
		// German uses comma (,) as decimal separator
		Session.get().setLocale(Locale.GERMAN);

		PropertyResourceModel model = new PropertyResourceModel("weather.${currentTemperature}",
			page,
			wsModel);
		assertEquals("Text should be as expected", "Twenty-five dot seven",
			model.getString());
	}

	/** */
	@Test
	public void getPropertySubstitutedResource()
	{
		tester.getSession().setLocale(Locale.ENGLISH);
		PropertyResourceModel model = new PropertyResourceModel("weather.message", page, wsModel);
		assertEquals(
			"Text should be as expected",
			"Weather station \"Europe's main weather station\" reports that the temperature is 25.7 \u00B0C",
			model.getString());
		ws.setCurrentTemperature(11.5);
		assertEquals(
			"Text should be as expected",
			"Weather station \"Europe's main weather station\" reports that the temperature is 11.5 \u00B0C",
			model.getString());
	}

	/** */
	@Test
	public void substitutionParametersResource()
	{
		tester.getSession().setLocale(Locale.ENGLISH);

		Calendar cal = Calendar.getInstance();
		cal.set(2004, Calendar.OCTOBER, 15, 13, 21);

		PropertyResourceModel model = new PropertyResourceModel("weather.detail", page,
			MiniMap.<String, IModel<?>> of(
			"time", new PropertyModel<Date>(cal, "time"),
			"station", wsModel),
			null);

		assertEquals(
			"Text should be as expected",
			"The report for 10/15/04, shows the temperature as 25.7 °C and the weather to be sunny",
			model.getString());
		ws.setCurrentStatus("raining");
		ws.setCurrentTemperature(11.568);
		assertEquals(
			"Text should be as expected",
			"The report for 10/15/04, shows the temperature as 11.568 °C and the weather to be raining",
			model.getString());
	}

	/** */
	@Test(expected = UnsupportedOperationException.class)
	public void setObject()
	{
		PropertyResourceModel model = new PropertyResourceModel("simple.text", page, null);
		model.setObject("Some value");
	}

	/** */
	@Test
	public void detachAttachDetachableModel()
	{
		IModel<WeatherStation> wsDetachModel = new LoadableDetachableModel<WeatherStation>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected WeatherStation load()
			{
				return new WeatherStation();
			}


		};

		PropertyResourceModel model = new PropertyResourceModel("simple.text", page, wsDetachModel);
		model.getObject();
		assertNotNull(model.getLocalizer());
		model.detach();
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4323
	 */
	@Test
	public void detachSubstituteModelFromAssignmentWrapper()
	{
		IModel<WeatherStation> nullOnDetachModel = new Model<WeatherStation>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void detach()
			{
				setObject(null);
			}
		};

		nullOnDetachModel.setObject(ws);
		Label label1 = new Label("resourceModelWithComponent", new PropertyResourceModel(
			"wrappedOnAssignment.text", page, nullOnDetachModel));
		page.add(label1);
		label1.getDefaultModelObject();
		label1.detach();
		assertNull(nullOnDetachModel.getObject());

		nullOnDetachModel.setObject(ws);
		Label label2 = new Label("resourceModelWithoutComponent", new PropertyResourceModel(
			"wrappedOnAssignment.text", nullOnDetachModel));
		page.add(label2);
		label2.getDefaultModelObject();
		label2.detach();
		assertNull(nullOnDetachModel.getObject());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5176
	 */
	@Test
	public void detachEvenNotAttached() {
		Wicket5176Model wrappedModel = new Wicket5176Model();
		PropertyResourceModel stringResourceModel = new PropertyResourceModel("test",
			(Component)null, wrappedModel);
		assertFalse(stringResourceModel.isAttached());
		assertTrue(wrappedModel.isAttached());
		stringResourceModel.detach();
		assertFalse(wrappedModel.isAttached());
	}

	private static class Wicket5176Model implements IModel {
		private boolean attached = true;

		@Override
		public Object getObject() {
			return null;
		}

		@Override
		public void setObject(Object object) {
		}

		@Override
		public void detach() {
			attached = false;
		}

		private boolean isAttached() {
			return attached;
		}
	}

	/**
	 * Inner class used for testing.
	 */
	public static class WeatherStation implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final String name = "Europe's main weather station";

		private String currentStatus = "sunny";

		private double currentTemperature = 25.7;

		/**
		 * @return status
		 */
		public String getCurrentStatus()
		{
			return currentStatus;
		}

		/**
		 * @param currentStatus
		 */
		public void setCurrentStatus(String currentStatus)
		{
			this.currentStatus = currentStatus;
		}

		/**
		 * @return current temp
		 */
		public double getCurrentTemperature()
		{
			return currentTemperature;
		}

		/**
		 * @param currentTemperature
		 */
		public void setCurrentTemperature(double currentTemperature)
		{
			this.currentTemperature = currentTemperature;
		}

		/**
		 * @return units
		 */
		public String getUnits()
		{
			return "\u00B0C";
		}

		/**
		 * @return name
		 */
		public String getName()
		{
			return name;
		}
	}

	/**
	 * Test page.
	 */
	public static class TestPage extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
		public TestPage()
		{
		}
	}
}
