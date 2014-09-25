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

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Localizer;
import org.apache.wicket.Session;
import org.apache.wicket.core.util.string.interpolator.PropertyVariableInterpolator;
import org.apache.wicket.resource.loader.ComponentStringResourceLoader;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.lang.Args;


/**
 * This model class encapsulates the full power of localization support within the Wicket framework.
 * It combines the flexible Wicket resource loading mechanism with property expressions. This
 * combination should be able to solve any dynamic localization requirement that a project has.
 * <p>
 * The model should be created with four parameters, which are described in detail below:
 * <ul>
 * <li><b>resourceKey </b>- This is the most important parameter as it contains the key that should
 * be used to obtain resources from any string resource loaders. This parameter is mandatory: a null
 * value will throw an exception. Typically it will contain an ordinary string such as
 * &quot;label.username&quot;. To add extra power to the key functionality the key may also contain
 * a property expression which will be evaluated if the model parameter (see below) is not null.
 * This allows keys to be changed dynamically as the application is running. For example, the key
 * could be &quot;product.${product.id}&quot; which prior to rendering will call
 * model.getObject().getProduct().getId() and substitute this value into the resource key before is
 * is passed to the loader.
 * <li><b>component </b>- This parameter should be a component that the string resource is relative
 * to. In a simple application this will usually be the Page on which the component resides. For
 * reusable components/containers that are packaged with their own string resource bundles it should
 * be the actual component/container rather than the page. For more information on this please see
 * {@link org.apache.wicket.resource.loader.ComponentStringResourceLoader}. The relative component
 * may actually be {@code null} if this model is wrapped on assignment (
 * {@link IComponentAssignedModel}) or when all resource loading is to be done from a global
 * resource loader. However, we recommend that a relative component is still supplied even in the
 * latter case in order to 'future proof' your application with regards to changing resource loading
 * strategies.
 * <li><b>model</b>- This parameter is mandatory if either the resourceKey or the found string
 * resource (see below) contain property expressions. Where property expressions are present they
 * will all be evaluated relative to this model object. If there are no property expressions present
 * then this model parameter may be <code>null</code><br>
 * Alternatively you can pass a map of models, each identified by a String which is used as a prefix
 * for all property expressions.
 * <li><b>defaultValue</b>- a default to be used if the string resource is not defined.
 * </ul>
 * <p>
 * <b>Example 1 </b>
 * <p>
 * In its simplest form, the model can be used as follows:
 * 
 * <pre>
 * public class MyPage extends WebPage&lt;Void&gt;
 * {
 * 	public MyPage(final PageParameters parameters)
 * 	{
 * 		add(new Label(&quot;username&quot;, new PropertyResourceModel(&quot;label.username&quot;, this, null)));
 * 	}
 * }
 * </pre>
 * 
 * Where the resource bundle for the page contains the entry <code>label.username=Username</code>
 * <p>
 * <b>Example 2 </b>
 * <p>
 * In this example, the resource key is selected based on the evaluation of a property expression:
 * 
 * <pre>
 * public class MyPage extends WebPage&lt;Void&gt;
 * {
 * 	public MyPage(final PageParameters parameters)
 *     {
 *         WeatherStation ws = new WeatherStation();
 *         add(new Label(&quot;weatherMessage&quot;,
 *             new PropertyResourceModel(&quot;weather.${currentStatus}&quot;, this, new Model&lt;WeatherStation&gt;(ws)));
 *     }
 * }
 * </pre>
 * 
 * Which will call the WeatherStation.getCurrentStatus() method each time the string resource model
 * is used and where the resource bundle for the page contains the entries:
 * 
 * <pre>
 * weather.sunny=Don't forget sunscreen!
 * weather.raining=You might need an umbrella
 * weather.snowing=Got your skis?
 * weather.overcast=Best take a coat to be safe
 * </pre>
 * 
 * <p>
 * <b>Example 3 </b>
 * <p>
 * In this example the found resource string contains a property expression that is substituted via
 * the model:
 * 
 * <pre>
 * public class MyPage extends WebPage&lt;Void&gt;
 * {
 * 	public MyPage(final PageParameters parameters)
 *     {
 *         WeatherStation ws = new WeatherStation();
 *         add(new Label(&quot;weatherMessage&quot;,
 *             new PropertyResourceModel(&quot;weather.message&quot;, this, new Model&lt;WeatherStation&gt;(ws)));
 *     }
 * }
 * </pre>
 * 
 * Where the resource bundle contains the entry <code>weather.message=Weather station reports that
 * the temperature is ${currentTemperature} ${units}</code>
 * <p>
 * <b>Example 4 </b>
 * <p>
 * This is an example of the most complex and powerful use of the string resource model with
 * multiple nested models:
 * 
 * <pre>
 * public class MyPage extends WebPage&lt;Void&gt;
 * {
 * 	public MyPage(final PageParameters parameters)
 * 	{
 * 		WeatherStation ws = new WeatherStation();
 * 
 * 		Map&lt;String, IModel&lt;?&gt;&gt; models = new HashMap&lt;&gt;();
 * 		models.put(&quot;date&quot;, Model.of(new Date()));
 * 		models.put(&quot;currentStatus&quot;, new PropertyModel&lt;?&gt;(ws, &quot;currentStatus&quot;));
 * 		models.put(&quot;ws&quot;, Model.of(ws));
 * 		add(new Label(&quot;weatherMessage&quot;, new PropertyResourceModel(&quot;weather.detail&quot;, this, models)));
 * 	}
 * }
 * </pre>
 * 
 * In the resource bundle all property expressions are prefixed with the identifier of the
 * respective model:
 * 
 * <pre>
 * weather.detail=The report for ${date}, shows the temperature as ${ws.currentStatus} ${ws.unit} \
 *     and the weather to be ${currentStatus}
 * </pre>
 * 
 * @see ComponentStringResourceLoader for additional information especially on the component search
 *      order
 * 
 * @author Chris Turner
 * @author svenmeier
 */
public class PropertyResourceModel extends LoadableDetachableModel<String>
	implements
		IComponentAssignedModel<String>
{
	private static final long serialVersionUID = 1L;

	/**
	 * The models for property resolving.
	 */
	private final Map<String, IModel<?>> models;

	/** The relative component used for lookups. */
	private final Component component;

	/** The key of message to get. */
	private final String resourceKey;

	/** The default value of the message. */
	private final IModel<String> defaultValue;

	@Override
	public IWrapModel<String> wrapOnAssignment(Component component)
	{
		return new AssignmentWrapper(component);
	}

	private class AssignmentWrapper extends LoadableDetachableModel<String>
		implements
			IWrapModel<String>
	{
		private static final long serialVersionUID = 1L;

		private final Component component;

		/**
		 * Construct.
		 * 
		 * @param component
		 */
		public AssignmentWrapper(Component component)
		{
			this.component = component;
		}

		@Override
		public void detach()
		{
			super.detach();

			PropertyResourceModel.this.detach();
		}

		@Override
		protected void onDetach()
		{
			if (PropertyResourceModel.this.component == null)
			{
				PropertyResourceModel.this.onDetach();
			}
		}

		@Override
		protected String load()
		{
			if (PropertyResourceModel.this.component != null)
			{
				// ignore assignment if component was specified explicitly
				return PropertyResourceModel.this.getObject();
			}
			else
			{
				return getString(component);
			}
		}

		@Override
		public void setObject(String object)
		{
			PropertyResourceModel.this.setObject(object);
		}

		@Override
		public IModel<String> getWrappedModel()
		{
			return PropertyResourceModel.this;
		}
	}

	/**
	 * Creates a new string resource model using the supplied parameters.
	 * 
	 * @param resourceKey
	 *            The resource key for this string resource
	 * @param model
	 *            An optional model to use for property substitutions
	 */
	public PropertyResourceModel(final String resourceKey, final IModel<?> model)
	{
		this(resourceKey, null, model, null);
	}


	/**
	 * Creates a new string resource model using the supplied parameters.
	 * <p>
	 * The relative component parameter should generally be supplied, as without it resources can
	 * not be obtained from resource bundles that are held relative to a particular component or
	 * page. However, for application that use only global resources then this parameter may be
	 * null.
	 * 
	 * @param resourceKey
	 *            The resource key for this string resource
	 * @param component
	 *            The component that the resource is relative to
	 * @param model
	 *            An optional model to use for property substitutions
	 */
	public PropertyResourceModel(final String resourceKey, final Component component,
		final IModel<?> model)
	{
		this(resourceKey, component, model, null);
	}

	/**
	 * Creates a new string resource model using the supplied parameters.
	 * 
	 * @param resourceKey
	 *            The resource key for this string resource
	 * @param model
	 *            An optional model to use for property substitutions
	 * @param defaultValue
	 *            The default value if the resource key is not found.
	 */
	public PropertyResourceModel(final String resourceKey, final IModel<?> model,
		final IModel<String> defaultValue)
	{
		this(resourceKey, null, model, defaultValue);
	}

	/**
	 * Creates a new string resource model using the supplied parameters.
	 * <p>
	 * The relative component parameter should generally be supplied, as without it resources can
	 * not be obtained from resource bundles that are held relative to a particular component or
	 * page. However, for application that use only global resources then this parameter may be
	 * null.
	 * 
	 * @param resourceKey
	 *            The resource key for this string resource
	 * @param component
	 *            The component that the resource is relative to
	 * @param model
	 *            An optional model to use for property substitutions
	 * @param defaultValue
	 *            The default value if the resource key is not found.
	 */
	public PropertyResourceModel(final String resourceKey, final Component component,
		final IModel<?> model, final IModel<String> defaultValue)
	{
		this(resourceKey, component, new MicroMap<String, IModel<?>>("", model), defaultValue);
	}

	/**
	 * Creates a new string resource model using the supplied parameters.
	 * <p>
	 * The relative component parameter should generally be supplied, as without it resources can
	 * not be obtained from resource bundles that are held relative to a particular component or
	 * page. However, for application that use only global resources then this parameter may be
	 * null.
	 * 
	 * @param resourceKey
	 *            The resource key for this string resource
	 * @param component
	 *            The component that the resource is relative to
	 * @param models
	 *            A map of models to use for property substitutions
	 * @param defaultValue
	 *            The default value if the resource key is not found.
	 */
	public PropertyResourceModel(final String resourceKey, final Component component,
		final Map<String, IModel<?>> models, final IModel<String> defaultValue)
	{
		Args.notNull(resourceKey, "resourceKey");
		Args.notNull(models, "models");

		this.resourceKey = resourceKey;
		this.component = component;
		this.models = models;
		this.defaultValue = defaultValue;
	}


	/**
	 * Gets the localizer that is being used by this string resource model.
	 * 
	 * @return The localizer
	 */
	public Localizer getLocalizer()
	{
		return Application.get().getResourceSettings().getLocalizer();
	}

	/**
	 * Gets the string currently represented by this model. The string that is returned may vary for
	 * each call to this method depending on the values contained in the model and an the parameters
	 * that were passed when this string resource model was created.
	 * 
	 * @return The string
	 */
	public final String getString()
	{
		return getString(component);
	}

	private String getString(final Component component)
	{

		final Localizer localizer = getLocalizer();
		IModel<?> target = getTargetModel();
		String defaultVal = defaultValue != null ? defaultValue.getObject() : null;

		return localizer.getString(getResourceKey(), component, target, defaultVal);
	}

	/**
	 * @return The locale to use when formatting the resource value
	 */
	protected Locale getLocale()
	{
		final Locale locale;
		if (component != null)
		{
			locale = component.getLocale();
		}
		else
		{
			locale = Session.exists() ? Session.get().getLocale() : Locale.getDefault();
		}
		return locale;
	}

	/**
	 * This method just returns debug information, so it won't return the localized string. Please
	 * use getString() for that.
	 * 
	 * @return The string for this model object
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("PropertyResourceModel[");
		sb.append("key:");
		sb.append(resourceKey);
		sb.append(",default:");
		sb.append(defaultValue);
		return sb.toString();
	}

	/**
	 * Get the model to use as target for all property expressions.
	 */
	private IModel<?> getTargetModel()
	{
		if (models == null)
		{
			return null;
		}
		else if (models instanceof MicroMap)
		{
			// a single model only, use it as is
			return models.values().iterator().next();
		}
		else
		{
			// a wrapper which returns the object contained in each model
			return new AbstractReadOnlyModel<Map<String, Object>>()
			{
				@Override
				public Map<String, Object> getObject()
				{
					return new Map<String, Object>()
					{
						@Override
						public int size()
						{
							return models.size();
						}

						@Override
						public boolean isEmpty()
						{
							return models.isEmpty();
						}

						@Override
						public boolean containsKey(Object key)
						{
							return models.containsKey(key);
						}

						@Override
						public boolean containsValue(Object value)
						{
							return models.containsValue(value);
						}

						@Override
						public Object get(Object key)
						{
							IModel<?> value = models.get(key);
							if (value == null)
							{
								return value;
							}
							return value.getObject();
						}

						@Override
						public Object put(String key, Object value)
						{
							throw new UnsupportedOperationException();
						}

						@Override
						public Object remove(Object key)
						{
							throw new UnsupportedOperationException();
						}

						@Override
						public void putAll(Map<? extends String, ? extends Object> m)
						{
							throw new UnsupportedOperationException();
						}

						@Override
						public void clear()
						{
							throw new UnsupportedOperationException();
						}

						@Override
						public Set<String> keySet()
						{
							return models.keySet();
						}

						@Override
						public Collection<Object> values()
						{
							throw new UnsupportedOperationException();
						}

						@Override
						public Set<java.util.Map.Entry<String, Object>> entrySet()
						{
							throw new UnsupportedOperationException();
						}
					};
				}
			};
		}
	}

	/**
	 * Gets the resource key for this string resource. If the resource key contains property
	 * expressions and the model is not null then the returned value is the actual resource key with
	 * all substitutions undertaken.
	 * 
	 * @return The (possibly substituted) resource key
	 */
	protected final String getResourceKey()
	{
		IModel<?> target = getTargetModel();

		if (target == null)
		{
			return resourceKey;
		}
		else
		{
			return new PropertyVariableInterpolator(resourceKey, target.getObject()).toString();
		}
	}

	/**
	 * Gets the string that this string resource model currently represents.
	 * <p>
	 * Note: This method is used only if this model is used directly without assignment to a
	 * component, it is not called by the assignment wrapper returned from
	 * {@link #wrapOnAssignment(Component)}.
	 */
	@Override
	protected final String load()
	{
		return getString();
	}

	@Override
	public final void detach()
	{
		super.detach();

		for (IModel<?> model : models.values())
		{
			if (model != null)
			{
				model.detach();
			}
		}

		if (defaultValue != null)
		{
			defaultValue.detach();
		}
	}

	@Override
	public void setObject(String object)
	{
		throw new UnsupportedOperationException();
	}
}
