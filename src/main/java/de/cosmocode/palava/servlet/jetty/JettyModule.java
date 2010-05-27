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

import org.eclipse.jetty.server.Server;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import de.cosmocode.palava.servlet.Webapp;

/**
 * Installs {@link Jetty} as eager singleton and as a provider for {@link Server}.
 * 
 * @author Tobias Sarnowski
 */
public class JettyModule implements Module {

    @Override
    public void configure(Binder binder) {
        Multibinder.newSetBinder(binder, Webapp.class);
        binder.bind(Jetty.class).asEagerSingleton();
        binder.bind(Server.class).toProvider(Jetty.class).in(Singleton.class);
    }
}
