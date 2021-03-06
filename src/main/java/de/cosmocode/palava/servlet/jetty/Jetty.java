/**
 * Copyright 2010 CosmoCode GmbH
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

package de.cosmocode.palava.servlet.jetty;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.internal.Sets;
import com.google.inject.name.Named;
import com.google.inject.servlet.GuiceFilter;

import de.cosmocode.palava.core.lifecycle.Disposable;
import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;
import de.cosmocode.palava.servlet.Webapp;

/**
 * A service which configured and manages an embedded jetty server.
 * 
 * @author Tobias Sarnowski
 * @author Willi Schoenborn 
 */
final class Jetty implements Initializable, Disposable, Provider<Server> {
    
    private static final Logger LOG = LoggerFactory.getLogger(Jetty.class);

    private URL config;
    private Set<Webapp> webapps = Sets.newLinkedHashSet();
    private int port;

    private Server jetty;
    
    @Inject(optional = true)
    void setWebapps(Set<Webapp> webapps) {
        this.webapps = Preconditions.checkNotNull(webapps, "Webapps");
    }

    @Inject(optional = true)
    void setConfig(@Named(JettyConfig.CONFIG) URL config) {
        this.config = config;
    }

    @Inject(optional = true)
    void setPort(@Named(JettyConfig.PORT) int port) {
        this.port = port;
    }

    @Override
    public void initialize() throws LifecycleException {
        if (port < 0) {
            jetty = new Server();
        } else {
            jetty = new Server(port);
        }

        final ContextHandlerCollection contexts = new ContextHandlerCollection();
        jetty.setHandler(contexts);

        new ServletContextHandler(contexts, "/", ServletContextHandler.SESSIONS);

        final List<Handler> handlers = Lists.newArrayList();

        if (webapps.isEmpty()) {
            LOG.info("No programmatically added webapp to configure.");
        } else {
            for (Webapp webapp : webapps) {
                LOG.info("Adding webapp {}", webapp);
                
                final ServletContextHandler handler = new ServletContextHandler(
                    contexts, webapp.getContext(), ServletContextHandler.SESSIONS);
                
                // add guice servlet filter
                // http://code.google.com/p/google-guice/wiki/ServletModule
                handler.addFilter(GuiceFilter.class, "/*", 0);
                handler.setResourceBase(webapp.getLocation());
                handler.addServlet(DefaultServlet.class, "/");
                
                handlers.add(handler);
            }
        }

        contexts.setHandlers(handlers.toArray(new Handler[handlers.size()]));

        // configure with jetty.xml
        if (config == null) {
            LOG.info("No configuration file given to load.");
        } else {
            try {
                LOG.info("Loading configuration {}", config);
                new XmlConfiguration(config).configure(jetty);
            /* CHECKSTYLE:OFF */
            } catch (Exception e) {
            /* CHECKSTYLE:ON */
                throw new LifecycleException(e);
            }
        }

        try {
            jetty.start();
        /* CHECKSTYLE:OFF */
        } catch (Exception e) {
        /* CHECKSTYLE:ON */
            throw new LifecycleException(e);
        }
    }

    @Override
    public void dispose() throws LifecycleException {
        try {
            jetty.stop();
        /* CHECKSTYLE:OFF */
        } catch (Exception e) {
        /* CHECKSTYLE:ON */
            throw new LifecycleException(e);
        }
    }

    @Override
    public Server get() {
        return jetty;
    }
    
}

