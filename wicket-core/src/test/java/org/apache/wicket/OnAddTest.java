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
package org.apache.wicket;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTesterScope;
import org.junit.Rule;
import org.junit.Test;

public class OnAddTest
{
	@Rule
	public WicketTesterScope scope = new WicketTesterScope();

	private boolean onAddCalled = false;

	private Component createProbe()
	{
		return new Label("foo")
		{
			@Override
			protected void onAddToPage()
			{
				super.onAddToPage();
				onAddCalled = true;
			}
		};
	}

	@Test
	public void onAddIsCalledIfParentIsInitialized()
	{
		Page page = createPage();
		page.internalInitialize();
		page.add(createProbe());
		assertTrue(onAddCalled);
	}

	private WebPage createPage()
	{
		return new WebPage()
		{
		};
	}

	@Test
	public void onAddIsNotCalledIfParentIsNotInitialized()
	{
		Page page = createPage();
		page.add(createProbe());
		assertFalse(onAddCalled);
	}

	@Test
	public void onAddIsCalledWhenParentIsInitialized()
	{
		Page page = createPage();
		page.add(createProbe());
		page.internalInitialize();
		assertTrue(onAddCalled);
	}

	@Test
	public void onAddIsNotCalledWhenParentIsNotConnectedToPage()
	{
		MarkupContainer container = createContainer();
		container.internalInitialize();
		container.add(createProbe());
		assertFalse(onAddCalled);
	}

	@Test
	public void onAddIsCalledWhenParentIsAddedToPage()
	{
		MarkupContainer container = createContainer();
		container.internalInitialize();
		container.add(createProbe());
		assertFalse(onAddCalled);
		WebPage page = createPage();
		page.internalInitialize();
		page.add(container);
		assertTrue(onAddCalled);
	}

	@Test
	public void onAddIsCalledAfterRemoveAndAdd()
	{
		Page page = createPage();
		page.internalInitialize();
		Component probe = createProbe();
		page.add(probe);
		assertTrue(onAddCalled);
		onAddCalled = false;
		page.remove(probe);
		assertFalse(onAddCalled);
		page.add(probe);
		assertTrue(onAddCalled);
	}

	@Test
	public void onAddRecursesToChildren()
	{
		Page page = createPage();
		page.internalInitialize();
		page.add(createNestedProbe());
		assertTrue(onAddCalled);
	}

	@Test
	public void onAddEnforcesSuperCall()
	{
		Page page = createPage();
		page.internalInitialize();
		try
		{
			page.add(new Label("foo")
			{
				@Override
				protected void onAddToPage()
				{
					; // I should call super, but since I don't, this should throw an exception
				}
			});
			fail("should have thrown exception");
		} catch (IllegalStateException e)
		{
			assertTrue(e.getMessage().contains("super.onAddToPage"));
		}
	}

	private Component createNestedProbe()
	{
		return createContainer().add(createProbe());
	}

	private MarkupContainer createContainer()
	{
		return new WebMarkupContainer("bar");
	}
}
