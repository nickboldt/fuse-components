/**
 * Copyright 2014 Red Hat, Inc.
 * 
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 */
package org.fusesource.camel.component.sap;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.fusesource.camel.component.sap.model.idoc.DocumentList;
import org.fusesource.camel.component.sap.util.IDocUtil;
import org.fusesource.camel.component.sap.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An SAP producer sending a IDoc (Intermediate Document) list to an SAP system using
 * the transactional remote function call (tRFC) protocol.
 * 
 * @author William Collins <punkhornsw@gmail.com>
 * 
 */
public class SapTransactionalIDocListProducer extends DefaultProducer {

	private static final transient Logger LOG = LoggerFactory.getLogger(SapTransactionalIDocListProducer.class);

	public SapTransactionalIDocListProducer(SapTransactionalIDocListDestinationEndpoint endpoint) {
		super(endpoint);
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		
		// Populate SAP exchange properties
		SapExchangePropertiesUtil.addDestinationPropertiesToExchange(getEndpoint(), exchange);
		
		Message message = exchange.getIn();

		// Populate message headers
		SapMessageHeadersUtil.addSapHeadersToMessage(getEndpoint(), message);

		DocumentList documentList = message.getBody(DocumentList.class);
		if (documentList == null) {
			LOG.warn("Exchange input message body does not contain a valid IDoc document list");
			return;
		}
		if (LOG.isDebugEnabled()) {
			try {
				LOG.debug("Sending IDoc document list to ''{}''", getEndpoint().getEndpointUri());
				LOG.debug("Document: " + (documentList == null ? documentList : Util.marshal(documentList)));
			} catch (Exception e) {
				LOG.warn("Failed to log request", e);
			}
		}
		String tid = DestinationRfcTransactionHandler.getTID(exchange, getEndpoint().getDestination());
		IDocUtil.sendDocumentList(getEndpoint().getDestination(), documentList, tid);
	}

	@Override
	public SapTransactionalIDocListDestinationEndpoint getEndpoint() {
		return (SapTransactionalIDocListDestinationEndpoint) super.getEndpoint();
	}

}
