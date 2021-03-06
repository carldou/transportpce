/*
 * Copyright (c) 2018 Orange and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fd.honeycomb.transportpce.device;

import com.google.inject.AbstractModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jmob.guice.conf.core.ConfigurationModule;

/**
 * @author Martial COULIBALY ( martial.coulibaly@gfi.com ) on behalf of Orange
 */
final class DeviceConfigurationModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceConfigurationModule.class);

    @Override
    protected void configure() {
        LOG.info("Initializing Device Readers Module");
        install(ConfigurationModule.create());
        requestInjection(DeviceConfiguration.class);
    }

}
