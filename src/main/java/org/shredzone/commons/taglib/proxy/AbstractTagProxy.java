/*
 * Shredzone Commons
 *
 * Copyright (C) 2012 Richard "Shred" Körber
 *   http://commons.shredzone.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.shredzone.commons.taglib.proxy;

import java.util.Enumeration;

import javax.annotation.Nonnull;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspTag;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.servlet.FrameworkServlet;

/**
 * An abstract TagProxy implementation that offers all basic methods.
 *
 * @param <T>
 *            Type of the {@link JspTag} this proxy delegates to
 * @author Richard "Shred" Körber
 */
public abstract class AbstractTagProxy<T extends JspTag> implements JspTag, ProxiedTag<T> {

    public static final String TAGPROXY_BEANFACTORY_CACHE = AbstractTagProxy.class + ".beanFactory";

    private T tagImpl;

    protected abstract String getBeanName();

    /**
     * Creates a new instance of the implementing target bean.
     *
     * @param jspContext
     *            {@link JspContext}
     */
    @SuppressWarnings("unchecked")
    protected void initTargetBean(@Nonnull JspContext jspContext) {
        BeanFactory bf = getBeanFactory(jspContext);

        String beanName = getBeanName();
        if (!bf.isPrototype(beanName)) {
            throw new IllegalStateException("Bean " + beanName + " must be prototype scoped!");
        }

        tagImpl = (T) bf.getBean(beanName);
    }

    /**
     * Gets the {@link BeanFactory} from the given {@link JspContext}. The default
     * implementation automagically finds a {@link BeanFactory} that was previously set by
     * a {@link FrameworkServlet}. The result is cached.
     *
     * @param jspContext
     *            {@link JspContext} to be used
     * @return {@link BeanFactory} found
     */
    @SuppressWarnings("unchecked")
    protected @Nonnull BeanFactory getBeanFactory(@Nonnull JspContext jspContext) {
        Object bfCache = jspContext.getAttribute(TAGPROXY_BEANFACTORY_CACHE, PageContext.APPLICATION_SCOPE);
        if (bfCache != null && bfCache instanceof BeanFactory) {
            return (BeanFactory) bfCache;
        }

        Enumeration<String> en = jspContext.getAttributeNamesInScope(PageContext.APPLICATION_SCOPE);
        while (en.hasMoreElements()) {
            String attribute = en.nextElement();
            if (attribute.startsWith(FrameworkServlet.SERVLET_CONTEXT_PREFIX)) {
                Object bf = jspContext.getAttribute(attribute, PageContext.APPLICATION_SCOPE);
                if (bf != null && bf instanceof BeanFactory) {
                    BeanFactory bfBean = (BeanFactory) bf;
                    jspContext.setAttribute(TAGPROXY_BEANFACTORY_CACHE, bfBean, PageContext.APPLICATION_SCOPE);
                    return bfBean;
                }
            }
        }

        throw new IllegalStateException("Could not find a BeanFactory. Use a FrameworkServlet or @BeanFactoryReference.");
    }

    @Override
    public T getTargetBean() {
        return tagImpl;
    }

}
