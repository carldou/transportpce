/*
 * Copyright © 2017 AT&T and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.transportpce.renderer.provisiondevice.tasks;

import java.util.concurrent.Future;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.olm.rev170418.OlmService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.olm.rev170418.ServicePowerSetupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.olm.rev170418.ServicePowerTurndownInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.olm.rev170418.ServicePowerTurndownInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.olm.rev170418.ServicePowerTurndownOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OlmPowerSetupRollbackTask extends RollbackTask {

    private static final Logger LOG = LoggerFactory.getLogger(OlmPowerSetupRollbackTask.class);
    private static final String FAILED = "Failed";
    private final boolean isRollbackNecessary;
    private final OlmService olmService;
    private final ServicePowerSetupInput powerSetupInput;

    public OlmPowerSetupRollbackTask(String id, boolean isRollbackNecessary, OlmService olmService,
            ServicePowerSetupInput powerSetupInput) {
        super(id);
        this.isRollbackNecessary = isRollbackNecessary;
        this.olmService = olmService;
        this.powerSetupInput = powerSetupInput;
    }

    @Override
    public boolean isRollbackNecessary() {
        return isRollbackNecessary;
    }

    @Override
    public Void call() throws Exception {
        ServicePowerTurndownInput powerTurndownInput = new ServicePowerTurndownInputBuilder()
                .setNodes(this.powerSetupInput.getNodes())
                .setServiceName(this.powerSetupInput.getServiceName())
                .setWaveNumber(this.powerSetupInput.getWaveNumber())
                .build();

        Future<RpcResult<ServicePowerTurndownOutput>> powerTurndownResultFuture =
                this.olmService.servicePowerTurndown(powerTurndownInput);
        RpcResult<ServicePowerTurndownOutput> powerTurndownResult = powerTurndownResultFuture.get();
        if (FAILED.equals(powerTurndownResult.getResult().getResult())) {
            LOG.warn("Olmp power setup rollback for {} was not successful!", this.getId());
        } else {
            LOG.info("Olm power setup rollback for {} was successful.");
        }
        return null;
    }
}
