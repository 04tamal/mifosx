package org.mifosng.platform.client.service;

import java.util.Collection;

import org.mifosng.data.ClientData;
import org.mifosng.data.ClientLoanAccountSummaryCollectionData;
import org.mifosng.data.NoteData;

public interface ClientReadPlatformService {

	Collection<ClientData> retrieveAllIndividualClients();

	ClientData retrieveIndividualClient(Long clientId);
	
	ClientData retrieveNewClientDetails();
	
	ClientLoanAccountSummaryCollectionData retrieveClientAccountDetails(Long clientId);
	
	Collection<NoteData> retrieveAllClientNotes(Long clientId);

	NoteData retrieveClientNote(Long clientId, Long noteId);
}