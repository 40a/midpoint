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

package com.evolveum.midpoint.web.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.markup.html.tabs.*;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import java.util.List;

/**
 * Ajaxified version of the tabbed panel. Uses AjaxFallbackLink instead of regular wicket links so
 * it can update itself inplace.
 *
 * @author Igor Vaynberg (ivaynberg)
 * @author shood
 *
 * @param <T>
 *            The type of panel to be used for this component's tabs. Just use {@link org.apache.wicket.extensions.markup.html.tabs.ITab} if you
 *            have no special needs here.
 */
public class AjaxTabbedPanel<T extends ITab> extends TabbedPanel<T>
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     *
     * @param id
     * @param tabs
     */
    public AjaxTabbedPanel(final String id, final List<T> tabs)
    {
        this(id, tabs, null);
    }

    /**
     * Constructor
     *
     * @param id
     * @param tabs
     * @param model
     *            model holding the index of the selected tab
     */
	public AjaxTabbedPanel(final String id, final List<T> tabs, IModel<Integer> model) {
		this(id, tabs, model, null);
	}

	public AjaxTabbedPanel(final String id, final List<T> tabs, IModel<Integer> model, RightSideItemProvider rightSideItemProvider) {
        super(id, tabs, model, rightSideItemProvider);
        setOutputMarkupId(true);

        setVersioned(false);
    }

    @Override
    protected WebMarkupContainer newLink(final String linkId, final int index)
    {
        return new AjaxFallbackLink<Void>(linkId)
        {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(final AjaxRequestTarget target)
            {
                setSelectedTab(index);
                onTabChange(index);
                if (target != null)
                {
                    target.add(AjaxTabbedPanel.this);
                }
                onAjaxUpdate(target);
            }

        };
    }

    /**
     * A template method that lets users add additional behavior when ajax update occurs. This
     * method is called after the current tab has been set so access to it can be obtained via
     * {@link #getSelectedTab()}.
     * <p>
     * <strong>Note</strong> Since an {@link AjaxFallbackLink} is used to back the ajax update the
     * <code>target</code> argument can be null when the client browser does not support ajax and
     * the fallback mode is used. See {@link AjaxFallbackLink} for details.
     *
     * @param target
     *            ajax target used to update this component
     */
    protected void onAjaxUpdate(final AjaxRequestTarget target){}
}

