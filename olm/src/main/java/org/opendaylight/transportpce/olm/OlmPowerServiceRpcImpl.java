/*
 * Copyright © 2017 AT&T and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.transportpce.olm;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.transportpce.olm.service.OlmPowerService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.olm.rev170418.CalculateSpanlossBaseInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.olm.rev170418.CalculateSpanlossBaseOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.olm.rev170418.CalculateSpanlossCurrentInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.olm.rev170418.CalculateSpanlossCurrentOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.olm.rev170418.GetPmInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.olm.rev170418.GetPmOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.olm.rev170418.OlmService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.olm.rev170418.ServicePowerResetInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.olm.rev170418.ServicePowerResetOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.olm.rev170418.ServicePowerSetupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.olm.rev170418.ServicePowerSetupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.olm.rev170418.ServicePowerTurndownInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.olm.rev170418.ServicePowerTurndownOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * The Class OlmPowerServiceRpcImpl.
 */
public class OlmPowerServiceRpcImpl implements OlmService {
    private final OlmPowerService olmPowerService;

    public OlmPowerServiceRpcImpl(OlmPowerService olmPowerService) {
        this.olmPowerService = olmPowerService;
    }

    /**
     * This method is the implementation of the 'get-pm' RESTCONF service, which
     * is one of the external APIs into the olm application.
     *
     * <p>
     * 1. get-pm This operation traverse through current PM list and gets PM for
     * given NodeId and Resource name
     *
     * <p>
     * The signature for this method was generated by yang tools from the
     * olm API model.
     *
     * @param input
     *            Input parameter from the olm yang model
     *
     * @return Result of the request
     */
    @Override
    public ListenableFuture<RpcResult<GetPmOutput>> getPm(GetPmInput input) {
        return RpcResultBuilder.success(this.olmPowerService.getPm(input)).buildFuture();
    }

    /**
     * This method is the implementation of the 'service-power-setup' RESTCONF service, which
     * is one of the external APIs into the olm application.
     *
     * <p>
     * 1. service-power-setup: This operation performs following steps:
     *    Step1: Calculate Spanloss on all links which are part of service.
     *    TODO Step2: Calculate power levels for each Tp-Id
     *    TODO Step3: Post power values on roadm connections
     *
     * <p>
     * The signature for this method was generated by yang tools from the
     * olm API model.
     *
     * @param input
     *            Input parameter from the olm yang model
     *            Input will contain nodeId and termination point
     *
     * @return Result of the request
     */
    @Override
    public ListenableFuture<RpcResult<ServicePowerSetupOutput>> servicePowerSetup(
        ServicePowerSetupInput input) {
        return RpcResultBuilder.success(this.olmPowerService.servicePowerSetup(input)).buildFuture();
    }

    /**
     * This method is the implementation of the 'service-power-trundown' RESTCONF service, which
     * is one of the external APIs into the olm application.
     *
     * <p>
     * 1. service-power-turndown: This operation performs following steps:
     *    Step1: For each TP within Node sets interface outofservice .
     *    Step2: For each roam-connection sets power to -60dbm
     *    Step3: Turns power mode off
     *
     * <p>
     * The signature for this method was generated by yang tools from the
     * olm API model.
     *
     * @param input
     *            Input parameter from the olm yang model
     *            Input will contain nodeId and termination point
     *
     * @return Result of the request
     */
    @Override
    public  ListenableFuture<RpcResult<ServicePowerTurndownOutput>> servicePowerTurndown(ServicePowerTurndownInput input) {
        return RpcResultBuilder.success(this.olmPowerService.servicePowerTurndown(input)).buildFuture();
    }

    /**
     * This method calculates Spanloss for all Roadm to Roadm links,
     * part of active inventory in Network Model or for newly added links
     * based on input src-type.
     *
     * <p>
     * 1. Calculate-Spanloss-Base: This operation performs following steps:
     *    Step1: Read all Roadm-to-Roadm links from network model or get data for given linkID.
     *    Step2: Retrieve PMs for each end point for OTS interface
     *    Step3: Calculates Spanloss
     *    Step4: Posts calculated spanloss in Device and in network model
     *
     * <p>
     * The signature for this method was generated by yang tools from the
     * renderer API model.
     *
     * @param input
     *            Input parameter from the olm yang model
     *            Input will contain SourceType and linkId if srcType is Link
     *
     * @return Result of the request
     */
    @Override
    public ListenableFuture<RpcResult<CalculateSpanlossBaseOutput>> calculateSpanlossBase(CalculateSpanlossBaseInput input) {
        return RpcResultBuilder.success(this.olmPowerService.calculateSpanlossBase(input)).buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<CalculateSpanlossCurrentOutput>> calculateSpanlossCurrent(
            CalculateSpanlossCurrentInput input) {
        return RpcResultBuilder.success(this.olmPowerService.calculateSpanlossCurrent(input)).buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<ServicePowerResetOutput>> servicePowerReset(ServicePowerResetInput input) {
        return RpcResultBuilder.success(this.olmPowerService.servicePowerReset(input)).buildFuture();
    }
}