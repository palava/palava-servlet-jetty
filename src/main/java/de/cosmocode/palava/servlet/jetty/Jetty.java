/**
 * palava - a java-php-bridge
 * Copyright (C) 2007-2010  CosmoCode GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package de.cosmocode.palava.servlet.jetty;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.servlet.GuiceFilter;
import de.cosmocode.palava.core.lifecycle.Disposable;
import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;
import de.cosmocode.palava.servlet.Webapp;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Set;


/**
 * @author Tobias Sarnowski
 */
final class Jetty implements Initializable, Disposable, Provider<Server> {
    private static final Logger LOG = LoggerFactory.getLogger(Jetty.class);

    private Server jetty;

    private URL config;
    private Set<Webapp> webapps;
    private int port;

    @Inject(optional = true)
    public void setWebapps(Set<Webapp> webapps) {
        this.webapps = webapps;
    }

    @Inject(optional = true)
    public void setConfig(@Named(JettyConfig.CONFIG) URL config) {
        this.config = config;
    }

    @Inject(optional = true)
    public void setPort(@Named(JettyConfig.PORT) int port) {
        this.port = port;
    }

    @Override
    public void initialize() throws LifecycleException {
        // initialize jetty
        if (port >= 0) {
            jetty = new Server(port);
        } else {
            jetty = new Server();
        }

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        jetty.setHandler(contexts);

        ServletContextHandler root = new ServletContextHandler(contexts, "/", ServletContextHandler.SESSIONS);

        ArrayList<Handler> handlers = Lists.newArrayList();

        if (webapps != null) {
            for (Webapp webapp: webapps) {
                LOG.info("Adding webapp {}", webapp);

                ServletContextHandler appctx= new ServletContextHandler(contexts, webapp.getContext(), ServletContextHandler.SESSIONS);

                // add guice servlet filter
                // http://code.google.com/p/google-guice/wiki/ServletModule
                appctx.addFilter(GuiceFilter.class, "/*", 0);

                appctx.setResourceBase(webapp.getLocation());

                appctx.addServlet(DefaultServlet.class, "/");

                //ServletHolder staticServlet= new ServletHolder();
                //staticServlet.setInitParameter("dirAllowed", "false");
                //staticServlet.setServlet(new DefaultServlet());
                //appctx.addServlet(staticServlet, "/*");

                handlers.add(appctx);
            }
        } else {
            LOG.info("No programmatically added webapp to configure.");
        }

        contexts.setHandlers(handlers.toArray(new Handler[handlers.size()]));

        // configure with jetty.xml
        if (config != null) {
            try {
                LOG.info("Loading configuration {}", config);
                new XmlConfiguration(config).configure(jetty);
            } catch (Exception e) {
                throw new LifecycleException(e);
            }
        } else {
            LOG.info("No configuration file given to load.");
        }

        try {
            jetty.start();
        } catch (Exception e) {
            throw new LifecycleException(e);
        }
    }

    @Override
    public void dispose() throws LifecycleException {
        try {
            jetty.stop();
        } catch (Exception e) {
            throw new LifecycleException(e);
        }
    }

    @Override
    public Server get() {
        return jetty;
    }
}

