package de.bwl.bwfla.workflows.beans.common;

import de.bwl.bwfla.common.datatypes.EaasState;

public interface EaasStateObserver
{
	public void onStateChanged(EaasState oldstate, EaasState newstate) throws Exception;
}
