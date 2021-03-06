/*
 * Copyright © 2016 Orange and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.transportpce.networkmodel;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.transportpce.common.StringConstants;
import org.opendaylight.transportpce.common.Timeouts;
import org.opendaylight.transportpce.common.device.DeviceTransactionManager;
import org.opendaylight.transportpce.networkmodel.dto.NodeRegistration;
import org.opendaylight.transportpce.networkmodel.listeners.AlarmNotificationListener;
import org.opendaylight.transportpce.networkmodel.listeners.DeOperationsListener;
import org.opendaylight.transportpce.networkmodel.listeners.DeviceListener;
import org.opendaylight.transportpce.networkmodel.listeners.LldpListener;
import org.opendaylight.transportpce.networkmodel.listeners.TcaListener;
import org.opendaylight.transportpce.networkmodel.service.NetworkModelService;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev161014.OrgOpenroadmAlarmListener;
import org.opendaylight.yang.gen.v1.http.org.openroadm.de.operations.rev161014.OrgOpenroadmDeOperationsListener;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev170206.OrgOpenroadmDeviceListener;
import org.opendaylight.yang.gen.v1.http.org.openroadm.lldp.rev161014.OrgOpenroadmLldpListener;
import org.opendaylight.yang.gen.v1.http.org.openroadm.tca.rev161014.OrgOpenroadmTcaListener;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.CreateSubscriptionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.NotificationsService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.StreamNameType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.Netconf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.Streams;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetConfTopologyListener implements DataTreeChangeListener<Node> {

    private static final Logger LOG = LoggerFactory.getLogger(NetConfTopologyListener.class);

    private final NetworkModelService networkModelService;
    private final R2RLinkDiscovery linkDiscovery;
    private final DataBroker dataBroker;
    private final DeviceTransactionManager deviceTransactionManager;
    private final Map<String, NodeRegistration> registrations;

    public NetConfTopologyListener(final NetworkModelService networkModelService, final DataBroker dataBroker,
            final R2RLinkDiscovery linkDiscovery, DeviceTransactionManager deviceTransactionManager) {
        this.networkModelService = networkModelService;
        this.linkDiscovery = linkDiscovery;
        this.dataBroker = dataBroker;
        this.deviceTransactionManager = deviceTransactionManager;
        this.registrations = new ConcurrentHashMap<>();
    }

    private void onDeviceConnected(final String nodeId) {
        LOG.info("onDeviceConnected: {}", nodeId);
        Optional<MountPoint> mountPointOpt = this.deviceTransactionManager.getDeviceMountPoint(nodeId);
        MountPoint mountPoint;
        if (mountPointOpt.isPresent()) {
            mountPoint = mountPointOpt.get();
        } else {
            LOG.error("Failed to get mount point for node {}", nodeId);
            return;
        }

        final Optional<NotificationService> notificationService =
                mountPoint.getService(NotificationService.class).toJavaUtil();
        if (!notificationService.isPresent()) {
            LOG.error("Failed to get RpcService for node {}", nodeId);
            return;
        }

        final OrgOpenroadmAlarmListener alarmListener = new AlarmNotificationListener(this.dataBroker);
        LOG.info("Registering notification listener on OrgOpenroadmAlarmListener for node: {}", nodeId);
        final ListenerRegistration<OrgOpenroadmAlarmListener> accessAlarmNotificationListenerRegistration =
                notificationService.get().registerNotificationListener(alarmListener);

        final OrgOpenroadmDeOperationsListener deOperationsListener = new DeOperationsListener();
        LOG.info("Registering notification listener on OrgOpenroadmDeOperationsListener for node: {}", nodeId);
        final ListenerRegistration<OrgOpenroadmDeOperationsListener>
            accessDeOperationasNotificationListenerRegistration =
                notificationService.get().registerNotificationListener(deOperationsListener);

        final OrgOpenroadmDeviceListener deviceListener = new DeviceListener();
        LOG.info("Registering notification listener on OrgOpenroadmDeviceListener for node: {}", nodeId);
        final ListenerRegistration<OrgOpenroadmDeviceListener> accessDeviceNotificationListenerRegistration =
                notificationService.get().registerNotificationListener(deviceListener);

        final OrgOpenroadmLldpListener lldpListener = new LldpListener(this.linkDiscovery, nodeId);
        LOG.info("Registering notification listener on OrgOpenroadmLldpListener for node: {}", nodeId);
        final ListenerRegistration<OrgOpenroadmLldpListener> accessLldpNotificationListenerRegistration =
                notificationService.get().registerNotificationListener(lldpListener);

        final OrgOpenroadmTcaListener tcaListener = new TcaListener();
        LOG.info("Registering notification listener on OrgOpenroadmTcaListener for node: {}", nodeId);
        final ListenerRegistration<OrgOpenroadmTcaListener> accessTcaNotificationListenerRegistration =
                notificationService.get().registerNotificationListener(tcaListener);

    }

    private void onDeviceDisConnected(final String nodeId) {
        LOG.info("onDeviceDisConnected: {}", nodeId);
        NodeRegistration nodeRegistration = this.registrations.remove(nodeId);
        if (nodeRegistration != null) {
            nodeRegistration.getAccessAlarmNotificationListenerRegistration().close();
            nodeRegistration.getAccessDeOperationasNotificationListenerRegistration().close();
            nodeRegistration.getAccessDeviceNotificationListenerRegistration().close();
            nodeRegistration.getAccessLldpNotificationListenerRegistration().close();
            nodeRegistration.getAccessTcaNotificationListenerRegistration().close();
        }
    }

    @Override
    @SuppressWarnings("checkstyle:FallThrough")
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Node>> changes) {
        LOG.info("onDataTreeChanged");
        for (DataTreeModification<Node> change : changes) {
            DataObjectModification<Node> rootNode = change.getRootNode();
            if ((rootNode.getDataAfter() == null) && (rootNode.getModificationType() != ModificationType.DELETE)) {
                LOG.error("rootNode.getDataAfter is null : Node not connected via Netconf protocol");
                continue;
            }
            if (rootNode.getModificationType() == ModificationType.DELETE) {
                if (rootNode.getDataBefore() != null) {
                    String nodeId = rootNode.getDataBefore().key().getNodeId().getValue();
                    LOG.info("Node {} deleted", nodeId);
                    this.networkModelService.deleteOpenROADMnode(nodeId);
                    onDeviceDisConnected(nodeId);
                } else {
                    LOG.error("rootNode.getDataBefore is null !");
                }
                continue;
            }
            String nodeId = rootNode.getDataAfter().key().getNodeId().getValue();
            NetconfNode netconfNode = rootNode.getDataAfter().augmentation(NetconfNode.class);

            if ((netconfNode != null) && !StringConstants.DEFAULT_NETCONF_NODEID.equals(nodeId)) {
                switch (rootNode.getModificationType()) {
                    case WRITE:
                        LOG.info("Node added: {}", nodeId);
                    case SUBTREE_MODIFIED:
                        NetconfNodeConnectionStatus.ConnectionStatus connectionStatus =
                                netconfNode.getConnectionStatus();
                        try {
                            long count = netconfNode.getAvailableCapabilities().getAvailableCapability().stream()
                                .filter(cp -> cp.getCapability().contains(StringConstants.OPENROADM_DEVICE_MODEL_NAME))
                                .count();
                            if (count > 0) {
                                LOG.info("OpenROADM node detected: {} {}", nodeId, connectionStatus.name());
                                switch (connectionStatus) {
                                    case Connected:
                                        this.networkModelService.createOpenROADMnode(nodeId);
                                        onDeviceConnected(nodeId);
                                        break;
                                    case Connecting:
                                    case UnableToConnect:
                                        this.networkModelService.setOpenROADMnodeStatus(nodeId, connectionStatus);
                                        onDeviceDisConnected(nodeId);
                                        break;
                                    default:
                                        LOG.warn("Unsupported device state {}", connectionStatus.getName());
                                        break;
                                }
                            }
                        } catch (NullPointerException e) {
                            LOG.error("Cannot get available Capabilities");
                        }
                        break;
                    default:
                        LOG.warn("Unexpected connection status : {}", rootNode.getModificationType());
                        break;
                }
            }
        }
    }


    private String getSupportedStream(String nodeId) {
        InstanceIdentifier<Streams> streamsIID = InstanceIdentifier.create(Netconf.class).child(Streams.class);
        try {
            Optional<Streams> ordmInfoObject =
                    this.deviceTransactionManager.getDataFromDevice(nodeId, LogicalDatastoreType.OPERATIONAL,
                            streamsIID, Timeouts.DEVICE_READ_TIMEOUT, Timeouts.DEVICE_READ_TIMEOUT_UNIT);
            if (!ordmInfoObject.isPresent()) {
                LOG.error("Info subtree is not present");
                return null;
            }
            for (org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf
                        .streams.Stream strm : ordmInfoObject.get().getStream()) {

                if ("OPENROADM".equalsIgnoreCase(strm.getName().getValue())) {
                    return strm.getName().getValue().toUpperCase();
                }
            }
            return "NETCONF";
        } catch (NullPointerException ex) {
            LOG.error("NullPointerException thrown while getting Info from a non Open ROADM device {}", nodeId);
            return null;
        }
    }
}
